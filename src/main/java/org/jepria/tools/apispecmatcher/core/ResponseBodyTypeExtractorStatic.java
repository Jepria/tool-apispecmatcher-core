package org.jepria.tools.apispecmatcher.core;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;

import java.io.Reader;
import java.util.*;

/**
 * Extracts the 'runtime' response body type from the source code
 */
public class ResponseBodyTypeExtractorStatic {

  public static class NoSuchMethod extends Exception {
    protected final java.lang.reflect.Method method;
    public NoSuchMethod(java.lang.reflect.Method method) {
      this.method = method;
    }
    public java.lang.reflect.Method getMethod() {
      return method;
    }
  }


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
   * Builds {@link ParameterizedType}s from the AST type nodes
   */
  protected static class ParameterizedTypeBuilder {

    protected final CompilationUnit cu;

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

    public ParameterizedTypeBuilder(CompilationUnit cu, CanonicalClassnameResolver resolver) {
      this.cu = cu;
      this.resolver = resolver;
    }

    /**
     *
     * @param type with simple type names
     * @return
     */
    public ParameterizedType buildSchema(Type type) {
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

  /**
   * Extracts type for the "responseBody" variable declared in the method body
   * @param reader
   * @param refMethod
   * @param resolver might be {@code null} if the no resolution is actually required
   *                 (if all canonical classnames statically extracted are unambiguous)
   * @return {@code null} if no "responseBody" variable declaration found in the method body
   */
  // TODO make the method extract qualified type name (semantic analysis may be required)
  public ParameterizedType extract(Reader reader, java.lang.reflect.Method refMethod,
                                   CanonicalClassnameResolver resolver) throws NoSuchMethod {
    // better to open and close the reader at the same place (at the method invoker, not here)
    CompilationUnit cu = JavaParser.parse(reader);
    NodeList<TypeDeclaration<?>> types = cu.getTypes();
    if (types.size() != 1) {
      throw new IllegalStateException("Only single TypeDeclaration per java file is supported, actual: " + types.size());
    }
    final TypeDeclaration<?> type = types.get(0);

    final MethodDeclaration srcMethod;
    {
      MethodDeclaration srcMethod0 = null;
      List<MethodDeclaration> srcMethods = type.getMethods();
      for (MethodDeclaration srcMethod1 : srcMethods) {
        if (methodEquals(srcMethod1, refMethod)) {
          srcMethod0 = srcMethod1;
          break; //found the first
          // TODO check the only method pair matched... or do not check it here?
        }
      }
      srcMethod = srcMethod0;
    }

    if (srcMethod == null) {
      throw new NoSuchMethod(refMethod);
    }

    Type responseBodyType = extract(srcMethod);
    if (responseBodyType == null) {
      return null;
    }

    ParameterizedType parameterizedType = new ParameterizedTypeBuilder(cu, resolver).buildSchema(responseBodyType);

    return parameterizedType;
  }

  protected boolean methodEquals(com.github.javaparser.ast.body.MethodDeclaration srcMethod, java.lang.reflect.Method refMethod) {
    if (!srcMethod.getName().asString().equals(refMethod.getName())) {
      return false;
    }

    List<com.github.javaparser.ast.body.Parameter> srcParams = srcMethod.getParameters();
    java.lang.reflect.Parameter[] refParams = refMethod.getParameters();
    if (refParams == null && srcParams == null) {
      return true;
    }
    if (srcParams != null && refParams == null || srcParams == null && refParams != null || srcParams.size() != refParams.length) {
      return false;
    }
    for (int i = 0; i < srcParams.size(); i++) {
      com.github.javaparser.ast.body.Parameter srcParam = srcParams.get(i);
      java.lang.reflect.Parameter refParam = refParams[i];
      if (!paramEquals(srcParam, refParam)) {
        return false;
      }
    }
    return true;
  }

  protected boolean paramEquals(com.github.javaparser.ast.body.Parameter srcParam, java.lang.reflect.Parameter refParam) {
    // type of srcParam is always ClassOrInterfaceType // TODO fragile assertion
    ClassOrInterfaceType srcParamType = (ClassOrInterfaceType) srcParam.getType();

    // if the parameter type is parameterized, check only top-level type because of the java type erasure
    // TODO support qualified parameter types. Now only simple names are checked, so the params will be equal: java.lang.Integer param and a.b.c.Integer param
    String srcParamTypeSimpleName = srcParamType.getNameAsString();
    if (!srcParamTypeSimpleName.equals(refParam.getType().getSimpleName())) {
      return false;
    }
    return true;
  }

  /**
   *
   * @param method
   * @return {@code null} if no "responseBody" variable declaration found in the method body
   */
  protected Type extract(MethodDeclaration method) {
    if (method == null) {
      return null;
    }
    BlockStmt body = method.getBody().orElse(null);
    if (body != null) {
      List<Statement> statements = body.getStatements();
      for (Statement s: statements) {
        if (s instanceof ExpressionStmt) {
          ExpressionStmt e = (ExpressionStmt)s;
          // is the first child // TODO fragile assertion
          if (e.getChildNodes().size() > 0) {
            Node child = e.getChildNodes().get(0);
            if (child instanceof VariableDeclarationExpr) {
              VariableDeclarationExpr var = (VariableDeclarationExpr) child;
              for (VariableDeclarator varDec: var.getVariables()) {
                if ("responseBody".equals(varDec.getName().asString())) {
                  Type type = var.getElementType();
                  return type;
                }
              }
            }
          }
        }
      }
    }
    return null;
  }


}
