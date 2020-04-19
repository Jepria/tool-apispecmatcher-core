package org.jepria.tools.apispecmatcher.core;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;

import java.util.*;

public class ParameterizedTypeBuilderStaticImpl implements ParameterizedTypeBuilderStatic {

  protected CompilationUnit cu;

  protected CanonicalClassnameResolver resolver;

  /**
   * Map for caching.
   * Key: simple classname, value: resolved canonical classname
   */
  protected final Map<String, String> canonicalClassnameCache = new HashMap<>();

  /**
   *
   * @param simpleClassname
   * @return canonical classname for the simple classname, null for null
   */
  protected String getCanonicalClassname(String simpleClassname) {
    if (simpleClassname == null) {
      return null;
    }
    String canonicalClassname = canonicalClassnameCache.get(simpleClassname);
    if (canonicalClassname == null) {
      Set<String> canonicalClassnameResolutions = buildCanonicalClassnameResolutions(simpleClassname);
      if (canonicalClassnameResolutions.size() == 1) {
        canonicalClassname = canonicalClassnameResolutions.iterator().next();
      } else {
        canonicalClassname = resolver.resolve(canonicalClassnameResolutions);
      }
      canonicalClassnameCache.put(simpleClassname, canonicalClassname);
    }
    return canonicalClassname;
  }

  /**
   * Builds a set of possible canonical names for the class specified by the simple name,
   * using the {@code package}, {@code import} and inner class declarations of the compilation unit.
   * @param simpleClassname
   * @return
   * let the input be "Classname", then the output consists of the following resolved canonical names:
   * 1) "a.b.c.Parent.Classname" if the compilation unit (itself declaring the "Parent" class) contains the "Classname" inner class declaration
   * 2) if none above matched, "a.b.c.Classname" if the compilation unit contains the direct import "import a.b.c.Classname"
   * 3) if none above matched, "d.e.f.Classname" for each asterisk import "import d.e.f.*",
   *    ("a.b.c.Classname" if the compilation unit contains package declaration "package a.b.c"
   *    or "Classname" if no package declaration found in the compilation unit)
   *    and "java.lang.Classname" for default classes.
   */
  // TODO implement paragraph 1 from the description above
  protected Set<String> buildCanonicalClassnameResolutions(String simpleClassname) {
    final Set<String> result = new HashSet<>();

    // direct imports
    for (ImportDeclaration impDec: cu.getImports()) {
      if (!impDec.isAsterisk()) {
        String name = impDec.getName().toString();
        if (name.endsWith("." + simpleClassname)) {
          // found a direct import
          result.add(name);
          break;
        }
      }
    }
    if (result.size() == 0) { // if none above matched

      // asterisk imports
      for (ImportDeclaration impDec : cu.getImports()) {
        if (impDec.isAsterisk()) {
          String name = impDec.getName().toString();
          result.add(name + "." + simpleClassname); // the class may be located in any of the asterisk packages
        }
      }

      PackageDeclaration packDec = cu.getPackageDeclaration().orElse(null);
      if (packDec != null) {
        String name = packDec.getName().toString();
        result.add(name + "." + simpleClassname); // the class may be located in the same package
      } else {
        result.add(simpleClassname); // the class may be located in the same (default) package
      }

      result.add("java.lang." + simpleClassname);
    }

    return result;
  }

  @Override
  public ParameterizedType build(CompilationUnit cu, Type type, CanonicalClassnameResolver resolver) {
    this.cu = cu;
    this.resolver = resolver;
    return buildSchemaInternal(type);
  }

  // method for recursive invocations
  private ParameterizedType buildSchemaInternal(Type type) {

    final String simpleClassname;
    {
      // inbox primitives
      if (type instanceof PrimitiveType) {
        type = ((PrimitiveType) type).toBoxedType();
      }
      // TODO at this point type must be instanceof ClassOrInterfaceType. assert this fragile assertion?

      String simpleClassname0 = null;
      // ClassOrInterfaceType must contain a SimpleName child // TODO fragile assertion
      List<Node> childs = type.getChildNodes();
      if (childs != null) {
        for (Node child : childs) {
          if (child instanceof SimpleName) {
            simpleClassname0 = ((SimpleName) child).asString();
            break; // found the first
          }
        }
      }
      simpleClassname = simpleClassname0;
    }

    final String canonicalClassname = getCanonicalClassname(simpleClassname);

    final List<ParameterizedType> typeParams;
    {
      List<ParameterizedType> typeParams0 = null;
      if (type instanceof ClassOrInterfaceType) {
        ClassOrInterfaceType classType = (ClassOrInterfaceType) type;
        List<Type> typeArguments = classType.getTypeArguments().orElse(null);
        if (typeArguments != null) {
          typeParams0 = new ArrayList<>();
          for (Type typeArgument : typeArguments) {
            ParameterizedType typeParam = buildSchemaInternal(typeArgument);
            typeParams0.add(typeParam);
          }
        }
      }
      typeParams = typeParams0;
    }

    return new ParameterizedType() {
      @Override
      public String typeName() {
        return canonicalClassname;
      }
      @Override
      public List<ParameterizedType> typeParams() {
        return typeParams;
      }
    };
  }
}