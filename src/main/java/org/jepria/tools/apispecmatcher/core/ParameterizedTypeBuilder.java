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

/**
 * Builds {@link ParameterizedType}s from the AST type nodes
 */
public class ParameterizedTypeBuilder {

  /**
   * Resolves a set of possible (ambiguous) canonical classnames of the same class into a single one,
   * against some external classpath.
   *
   * Canonical classname ambiguity origins from the static code analysis (AST) limitations, e.g.:
   * <pre>
   * import a.b.c.*;
   * import d.e.f.*;
   * public class A {
   *   X x;
   * }
   * </pre>
   * Here, the class X may have either {@code a.b.c.X} or {@code d.e.f.X} canonical classname.
   **/
  public interface CanonicalClassnameResolver {
    /**
     * @param possible set of ambiguous canonical classnames of the same class
     * @return one of the elements from the set
     */
    String resolve(Set<String> possible);
  }

  /**
   * Initialized by {@link #initCompilationUnit(Node)}
   */
  protected CompilationUnit cu;

  protected final CanonicalClassnameResolver resolver;
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

  public ParameterizedTypeBuilder(CanonicalClassnameResolver resolver) {
    this.resolver = resolver;
  }

  /**
   * Initializes {@link #cu} (if needed) by any node that belongs to it
   * MUST be invoked at each entry point of the class
   * @param node
   */
  protected void initCompilationUnit(Node node) {
    if (cu == null) {
      Node n = node;
      while (n != null && !(n instanceof CompilationUnit)) {
        n = n.getParentNode().orElse(null);
      }
      if (n == null) {
        throw new IllegalStateException("The node [" + node + "] has no CompilationUnit parent");
      }
      cu = (CompilationUnit) n;
    }
  }

  /**
   *
   * @param type with simple type names
   * @return
   */
  public ParameterizedType buildSchema(Type type) {
    initCompilationUnit(type);

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
            ParameterizedType typeParam = buildSchema(typeArgument);
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