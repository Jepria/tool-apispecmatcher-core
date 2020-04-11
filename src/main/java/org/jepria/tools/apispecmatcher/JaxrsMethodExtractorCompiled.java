package org.jepria.tools.apispecmatcher;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Extracts jaxrs methods from the set of compiled java classes
 */
public class JaxrsMethodExtractorCompiled {

  protected final ClassLoader classLoader;

  /**
   * @param classHrcRoots list of directories, each is a class file hierarchy root
   * @param jarFiles list of jar files with classes to load
   */
  public JaxrsMethodExtractorCompiled(List<File> classHrcRoots, List<File> jarFiles) {
    this(classPathToClassLoader(classHrcRoots, jarFiles));
  }

  /**
   * Creates a ClassLoader from a classpath represented by a set of .jar and .class files
   * @param classHrcRoots list of directories, each is a class file hierarchy root
   * @param jarFiles list of jar files with classes to load
   */
  protected static ClassLoader classPathToClassLoader(List<File> classHrcRoots, List<File> jarFiles) {
    List<File> files = new ArrayList<>();
    if (classHrcRoots != null) {
      files.addAll(classHrcRoots);
    }
    if (jarFiles != null) {
      files.addAll(jarFiles);
    }

    final URL[] urls = new URL[files.size()];
    int i = 0;
    for (File file: files) {
      URL url;
      try {
        url = file.toURI().toURL();
      } catch (MalformedURLException e) {
        // impossible
        throw new RuntimeException(e);
      }
      urls[i++] = url;
    }

    return new URLClassLoader(urls);
  }

  public JaxrsMethodExtractorCompiled(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  public List<Method> extract(String jaxrsAdapterClassname) {

    final List<Method> result = new ArrayList<>();

    final Class<?> clazz;
    try {
      clazz = classLoader.loadClass(jaxrsAdapterClassname);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

    String classPathAnnotationValue = extractJaxrsPath(clazz);

    for (java.lang.reflect.Method refMethod: clazz.getDeclaredMethods()) {

      // nullablle
      String methodPathAnnotationValue = extractJaxrsPath(refMethod);
      final String pathAnnotationValue = (classPathAnnotationValue != null ? classPathAnnotationValue : "") + (methodPathAnnotationValue != null ? methodPathAnnotationValue : "");

      final String httpMethod;
      {
        String httpMethod0 = null;
        Annotation[] annotations = refMethod.getDeclaredAnnotations();
        if (annotations != null) {
          for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            String annotationStr = annotationType.getCanonicalName();

            if ("javax.ws.rs.DELETE".equals(annotationStr)
                    || "javax.ws.rs.GET".equals(annotationStr)
                    || "javax.ws.rs.HEAD".equals(annotationStr)
                    || "javax.ws.rs.OPTIONS".equals(annotationStr)
                    || "javax.ws.rs.POST".equals(annotationStr)
                    || "javax.ws.rs.PUT".equals(annotationStr)) {
              // retain simple name only
              httpMethod0 = annotationStr.substring("javax.ws.rs.".length());

              break; //found the first
              // TODO check the only jaxrs annotation found on the method... or do not check it here?
            }
          }
        }
        httpMethod = httpMethod0;
      }


      if (httpMethod != null) { // check httpMethod annotation only, path annotation might be null or empty

        final Map<String, Object> requestBodySchema;

        // extract params
        final List<Method.Parameter> params = new ArrayList<>();
        {
          Map<String, Object> requestBodySchema0 = null;

          Parameter[] refParams = refMethod.getParameters();
          Type[] refParamGenTypes = refMethod.getGenericParameterTypes();
          // assert refParams and refParamGenTypes are of the same length

          for (int i = 0; i < refParams.length; i++) {
            Parameter refParam = refParams[i];
            Type refParamGenType = refParamGenTypes[i];

            // extract param annotations

            final String in;
            final String name;
            {
              String in0 = null;
              String name0 = null;
              Annotation[] annotations = refParam.getAnnotations();
              if (annotations != null) {
                for (Annotation annotation : annotations) {
                  Class<? extends Annotation> annotationType = annotation.annotationType();
                  String annotationStr = annotationType.getCanonicalName();

                  if ("javax.ws.rs.BeanParam".equals(annotationStr)) {
                    throw new UnsupportedOperationException("javax.ws.rs.BeanParam is unsupported");// TODO specify param and method location in the exception
                  }
                  if ("javax.ws.rs.FormParam".equals(annotationStr)) {
                    throw new UnsupportedOperationException("javax.ws.rs.FormParam is unsupported");// TODO specify param and method location in the exception
                  }
                  if ("javax.ws.rs.MatrixParam".equals(annotationStr)) {
                    throw new UnsupportedOperationException("javax.ws.rs.MatrixParam is unsupported by OpenAPI 3.0");// TODO specify param and method location in the exception
                  }

                  if ("javax.ws.rs.QueryParam".equals(annotationStr)
                          || "javax.ws.rs.PathParam".equals(annotationStr)
                          || "javax.ws.rs.HeaderParam".equals(annotationStr)
                          || "javax.ws.rs.CookieParam".equals(annotationStr)) {
                    // retain simple name only
                    in0 = annotationStr.substring("javax.ws.rs.".length(), annotationStr.length() - "Param".length());

                    try {
                      // invoke method value() on the annotation
                      Object res = annotationType.getDeclaredMethod("value").invoke(annotation);
                      name0 = (String) res;
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                      // impossible
                      throw new RuntimeException(e);
                    }

                    break; //found the first
                    // TODO check the only param annotation found on the parameter... or do not check it here?
                  }
                }
              }

              // for the body param both name and in variables are null
              if (in0 == null && name0 == null) {
                requestBodySchema0 = buildSchema(refParamGenType);
                // TODO check the only method param found as a body... or do not check it here?
              }

              name = name0;
              in = in0;
            }

            if (name != null && in != null) { // otherwise this is a request body, not a method param
              params.add(new Method.Parameter() {
                @Override
                public String name() {
                  return name;
                }

                @Override
                public String in() {
                  return in;
                }

                @Override
                public Map<String, Object> schema() {
                  return buildSchema(refParam.getType());
                }
              });
            }
          }

          requestBodySchema = requestBodySchema0;
        }

        Method method = new Method() {
          @Override
          public String httpMethod() {
            return httpMethod;
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
                return jaxrsAdapterClassname + "(?:?)";
              }
            };
          }
          @Override
          public List<Parameter> params() {
            return params;
          }
          @Override
          public Map<String, Object> requestBodySchema() {
            return requestBodySchema;
          }
        };

        result.add(method);
      }
    }

    return result;
  }

  protected String extractJaxrsPath(AnnotatedElement element) {
    Annotation[] annotations = element.getAnnotations();
    if (annotations != null) {
      for (Annotation annotation : annotations) {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        if ("javax.ws.rs.Path".equals(annotationType.getCanonicalName())) {
          String pathValue;
          try {
            // invoke method value() on the annotation
            Object res = annotationType.getDeclaredMethod("value").invoke(annotation);
            pathValue = (String) res;
          } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // impossible
            throw new RuntimeException(e);
          }
          return pathValue;
        }
      }
    }
    return null;
  }

  protected Map<String, Object> buildSchema(Type type) {
    return OpenApiSchemaBuilder.buildSchema(type);
  }
}
