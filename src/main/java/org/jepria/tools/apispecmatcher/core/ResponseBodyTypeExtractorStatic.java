package org.jepria.tools.apispecmatcher.core;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

import java.io.Reader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Extracts the 'runtime' response body type from the source code
 */
public class ResponseBodyTypeExtractorStatic {

  /**
   * Adapter interface for the {@link ParameterizedTypeBuilder.CanonicalClassnameResolver}
   */
  public interface CanonicalClassnameResolver {
    String resolve(Set<String> possible);
  }

  public static class ExtractedType {
    /**
     * {@code null} if no "responseBody" variable declaration found in the method body
     */
    public ParameterizedType type;

    /**
     * Extraction features denoted by {@link Feature} constants.
     * If extraction had no features, the collection is empty.
     */
    public final Set<Integer> features = new HashSet<>();

    public static class Feature {
      public static final int NO_SUCH_METHOD = 1;
    }
  }

  /**
   * Extracts type for the "responseBody" variable declared in the method body
   * @param reader
   * @param refMethod
   * @param resolver might be {@code null} if the no resolution is actually required
   *                 (if all canonical classnames statically extracted are unambiguous)
   * @return NotNull
   */
  // TODO make the method extract qualified type name (semantic analysis may be required)
  public ExtractedType extract(Reader reader, java.lang.reflect.Method refMethod,
                                   CanonicalClassnameResolver resolver) {

    final ExtractedType result = new ExtractedType();

    // better to open and close the reader at the same place (at the method invoker, not here)
    CompilationUnit cu = JavaParser.parse(reader);
    NodeList<TypeDeclaration<?>> srcTypes = cu.getTypes();
    if (srcTypes.size() != 1) {
      throw new IllegalStateException("Only single TypeDeclaration per java file is supported, actual: " + srcTypes.size());
    }
    final TypeDeclaration<?> srcType = srcTypes.get(0);

    final MethodDeclaration srcMethod;
    {
      MethodDeclaration srcMethod0 = null;
      List<MethodDeclaration> srcMethods = srcType.getMethods();
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
      result.features.add(ExtractedType.Feature.NO_SUCH_METHOD);

    } else {

      Type responseBodyType = extract(srcMethod);

      if (responseBodyType != null) {
        ParameterizedTypeBuilder.CanonicalClassnameResolver resolverAdopted
                = new ParameterizedTypeBuilder.CanonicalClassnameResolver() {
          @Override
          public String resolve(Set<String> possible) {
            return resolver.resolve(possible);
          }
        };

        ParameterizedType parameterizedType = new ParameterizedTypeBuilder(resolverAdopted).buildSchema(responseBodyType);
        result.type = parameterizedType;
      }
    }

    return result;
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
