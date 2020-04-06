package org.jepria.tools.apispecmatcher;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Extracts jaxrs methods from the set of java sources
 */
public class JaxrsMethodExtractorStatic {

  public List<JaxrsMethod> extract(Reader jaxrsAdapterJava) {

    final List<JaxrsMethod> result = new ArrayList<>();

    CompilationUnit cu = JavaParser.parse(jaxrsAdapterJava);
    NodeList<TypeDeclaration<?>> types = cu.getTypes();
    if (types.size() != 1) {
      throw new IllegalStateException("Only single TypeDeclaration per java file is supported, actual: " + types.size());
    }
    final TypeDeclaration<?> type = types.get(0);

    String classPathAnnotationValue = extractJaxrsPath(type);

    List<MethodDeclaration> methods = type.getMethods();
    for (MethodDeclaration method : methods) {
      NodeList<AnnotationExpr> annotationExprs = method.getAnnotations();

      // nullablle
      String methodPathAnnotationValue = extractJaxrsPath(method);
      final String pathAnnotationValue = (classPathAnnotationValue != null ? classPathAnnotationValue : "") + (methodPathAnnotationValue != null ? methodPathAnnotationValue : "");

      final String httpMethodAnnotationValue;
      {
        String httpMethodAnnotationValue0 = null;
        for (AnnotationExpr annotationExpr : annotationExprs) {
          String annotationStr = annotationExpr.getNameAsString();

          if ("DELETE".equals(annotationStr)
                  || "GET".equals(annotationStr)
                  || "HEAD".equals(annotationStr)
                  || "OPTIONS".equals(annotationStr)
                  || "POST".equals(annotationStr)
                  || "PUT".equals(annotationStr)) {
            httpMethodAnnotationValue0 = annotationStr;
          } else if ("javax.ws.rs.DELETE".equals(annotationStr)
                  || "javax.ws.rs.GET".equals(annotationStr)
                  || "javax.ws.rs.HEAD".equals(annotationStr)
                  || "javax.ws.rs.OPTIONS".equals(annotationStr)
                  || "javax.ws.rs.POST".equals(annotationStr)
                  || "javax.ws.rs.PUT".equals(annotationStr)) {
            httpMethodAnnotationValue0 = annotationStr.substring("javax.ws.rs.".length());
          }

          if (httpMethodAnnotationValue0 != null) {
            break; //found
          }
        }
        httpMethodAnnotationValue = httpMethodAnnotationValue0;
      }

      if (httpMethodAnnotationValue != null) { // check httpMethod annotation only, path annotation might be null or empty

        final String requestBodyType;
        final List<JaxrsMethod.Parameter> params = new ArrayList<>();
        {
          String requestBodyType0 = null;

          for (Parameter parameter : method.getParameters()) {
            // extract param annotations

            final String in;
            final String name;
            {
              String in0 = null;
              String name0 = null;
              for (AnnotationExpr annotationExpr : parameter.getAnnotations()) {
                String annotationStr = annotationExpr.getNameAsString();

                if ("BeanParam".equals(annotationStr) || "javax.ws.rs.BeanParam".equals(annotationStr)) {
                  throw new UnsupportedOperationException("javax.ws.rs.BeanParam is unsupported");// TODO specify param and method location in the exception
                }
                if ("FormParam".equals(annotationStr) || "javax.ws.rs.FormParam".equals(annotationStr)) {
                  throw new UnsupportedOperationException("javax.ws.rs.FormParam is unsupported");// TODO specify param and method location in the exception
                }
                if ("MatrixParam".equals(annotationStr) || "javax.ws.rs.MatrixParam".equals(annotationStr)) {
                  throw new UnsupportedOperationException("javax.ws.rs.MatrixParam is unsupported by OpenAPI 3.0");// TODO specify param and method location in the exception
                }

                if ("QueryParam".equals(annotationStr)
                        || "PathParam".equals(annotationStr)
                        || "HeaderParam".equals(annotationStr)
                        || "CookieParam".equals(annotationStr)) {
                  in0 = annotationStr;
                } else if ("javax.ws.rs.QueryParam".equals(annotationStr)
                        || "javax.ws.rs.PathParam".equals(annotationStr)
                        || "javax.ws.rs.HeaderParam".equals(annotationStr)
                        || "javax.ws.rs.CookieParam".equals(annotationStr)) {
                  // retain simple name only
                  in0 = annotationStr.substring("javax.ws.rs.".length());
                }

                if (in0 != null) {
                  // get value of the annotation
                  List<Node> childs = annotationExpr.getChildNodes();
                  if (childs.size() == 2) { // TODO weird assertion
                    Node value = childs.get(1);
                    if (value instanceof StringLiteralExpr) {
                      StringLiteralExpr sle = (StringLiteralExpr) value;
                      name0 = sle.asString();
                    }
                    // TODO support also referenced values, along with StringLiteralExpr
                  }

                  break; //found the first
                  // TODO check the only param annotation found on the parameter... or do not check it here?
                }
              }

              // for the body param both name and in variables are null
              if (in0 == null && name0 == null) {
                requestBodyType0 = parameter.getType().getClass().getCanonicalName();
                // TODO check the only method param found as a body... or do not check it here?
              }

              name = name0;
              in = in0;
            }

            if (name != null && in != null) { // otherwise this is a request body, not a method param
              params.add(new JaxrsMethod.Parameter() {
                @Override
                public String name() {
                  return name;
                }

                @Override
                public String in() {
                  return in;
                }

                @Override
                public String type() {
                  return parameter.getType().getClass().getCanonicalName();
                }
              });
            }
          }

          requestBodyType = requestBodyType0;
        }

        JaxrsMethod jaxrsMethod = new JaxrsMethod() {
          @Override
          public String httpMethod() {
            return httpMethodAnnotationValue;
          }

          @Override
          public String path() {
            return pathAnnotationValue;
          }

          @Override
          public Location location() {
            return new Location() {
              @Override
              public String asString() {
                return method.getBegin().isPresent() ?
                        ("(" + method.getBegin().get().line + ":" + method.getBegin().get().column + ")") :
                        "(?:?)";
              }
            };
          }

          @Override
          public List<Parameter> params() {
            return params;
          }

          @Override
          public Class<?> requestBodyType() {
            return null; // requestBodyType; // TODO convert from String to Class or declare JaxrsMethod.requestBodyType as returning String
          }
        };

        result.add(jaxrsMethod);
      }
    }

    return result;
  }

  protected String extractJaxrsPath(BodyDeclaration<?> element) {
    NodeList<AnnotationExpr> annotationExprs = element.getAnnotations();
    for (AnnotationExpr annotationExpr : annotationExprs) {
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
