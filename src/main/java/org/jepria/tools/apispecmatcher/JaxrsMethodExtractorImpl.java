package org.jepria.tools.apispecmatcher;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class JaxrsMethodExtractorImpl implements JaxrsMethodExtractor {
  @Override
  public List<JaxrsMethod> extract(Reader jaxrsAdapter) {

    final List<JaxrsMethod> result = new ArrayList<>();

    CompilationUnit cu = JavaParser.parse(jaxrsAdapter);
    NodeList<TypeDeclaration<?>> types = cu.getTypes();
    if (types.size() != 1) {
      throw new IllegalStateException("Only single TypeDeclaration per java file is supported, actual: " + types.size());
    }
    final TypeDeclaration<?> type = types.get(0);

    String classPathAnnotationValue = extractJaxrsPath(type);

    List<MethodDeclaration> methods = type.getMethods();
    for (MethodDeclaration method: methods) {
      NodeList<AnnotationExpr> annotationExprs = method.getAnnotations();

      // nullablle
      String methodPathAnnotationValue = extractJaxrsPath(method);
      final String pathAnnotationValue = (classPathAnnotationValue != null ? classPathAnnotationValue : "") + (methodPathAnnotationValue != null ? methodPathAnnotationValue : "");

      String httpMethodAnnotationValue = null;
      {
        for (AnnotationExpr annotationExpr : annotationExprs) {
          String annotationStr = annotationExpr.getNameAsString();

          if ("DELETE".equals(annotationStr)
                  || "GET".equals(annotationStr)
                  || "HEAD".equals(annotationStr)
                  || "OPTIONS".equals(annotationStr)
                  || "POST".equals(annotationStr)
                  || "PUT".equals(annotationStr)) {

            httpMethodAnnotationValue = annotationStr;
            break; //found
          }
        }
      }

      if (httpMethodAnnotationValue != null) { // check httpMethod annotation only, path annotation might be null or empty

        final String httpMethodAnnotationValue0 = httpMethodAnnotationValue;

        JaxrsMethod jaxrsMethod = new JaxrsMethod() {
          @Override
          public MethodDeclaration method() {
            return method;
          }
          @Override
          public String httpMethod() {
            return httpMethodAnnotationValue0;
          }
          @Override
          public String path() {
            return pathAnnotationValue;
          }
          @Override
          public Location location() {
            return null; // TODO provide location
          }
        };

        result.add(jaxrsMethod);
      }
    }


    return result;
  }

  protected String extractJaxrsPath(BodyDeclaration<?> element) {
    NodeList<AnnotationExpr> annotationExprs = element.getAnnotations();
    for (AnnotationExpr annotationExpr: annotationExprs) {
      if ("Path".equals(annotationExpr.getNameAsString()) || "javax.ws.rs.Path".equals(annotationExpr.getNameAsString())) {
        List<StringLiteralExpr> childs = annotationExpr.getChildNodesByType(StringLiteralExpr.class);
        if (childs.size() != 1) {
          throw new IllegalStateException("Expected 'Path' AnnotationExpr with exactly 1 child");
        }
        return childs.get(0).asString();
      }
    }
    return null;
  }

}
