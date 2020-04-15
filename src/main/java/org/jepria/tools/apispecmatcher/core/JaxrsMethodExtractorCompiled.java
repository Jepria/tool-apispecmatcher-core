package org.jepria.tools.apispecmatcher.core;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.*;

/**
 * Extracts jaxrs methods from the set of compiled java classes
 */
public class JaxrsMethodExtractorCompiled {

  protected final ClassLoader classLoader;

  protected final List<File> sourceTreeRoots;

  /**
   * @param classHrcRoots list of directories, each is a class file hierarchy root
   * @param jarFiles list of jar files with classes to load
   * @param sourceTreeRoots list of directories, each is a java source root directory
   */
  public JaxrsMethodExtractorCompiled(List<File> classHrcRoots, List<File> jarFiles, List<File> sourceTreeRoots) {
    this(classPathToClassLoader(classHrcRoots, jarFiles), sourceTreeRoots);
  }

  public JaxrsMethodExtractorCompiled(ClassLoader classLoader, List<File> sourceTreeRoots) {
    this.classLoader = classLoader;
    this.sourceTreeRoots = sourceTreeRoots;
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

  public static class ExtractedMethod {

    public Method method;

    /**
     * Extraction features denoted by {@link Features} constants.
     * If extraction had no features, the collection is empty.
     */
    public final Set<Integer> features = new HashSet<>();

    public static class Features {
      public static final int DYNAMIC__TYPE_UNDECLARED = 1;
      public static final int STATIC__NO_SOURCE_TREE = 2;
      public static final int STATIC__NO_SOURCE_FILE = 3;
      public static final int STATIC__NO_SOURCE_METHOD = 4;
      public static final int STATIC__VARIABLE_UNDECLARED = 5;
    }
  }

  public List<ExtractedMethod> extract(String jaxrsAdapterClassname) {

    final List<ExtractedMethod> extractedMethods = new ArrayList<>();

    final Class<?> clazz;
    try {
      clazz = classLoader.loadClass(jaxrsAdapterClassname);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

    String classPathAnnotationValue = extractJaxrsPath(clazz);

    for (java.lang.reflect.Method refMethod: clazz.getDeclaredMethods()) {

      // nullable
      String methodPathAnnotationValue = extractJaxrsPath(refMethod);
      final String pathAnnotationValue;
      {
        pathAnnotationValue = (classPathAnnotationValue != null ? classPathAnnotationValue : "") + (methodPathAnnotationValue != null ? methodPathAnnotationValue : "");
      }

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

        final ExtractedMethod extractedMethod = new ExtractedMethod();

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

        final Map<String, Object> responseBodySchema;
        {
          Map<String, Object> responseBodySchema0 = null;
          Type returnGenType = refMethod.getGenericReturnType();

          if ("javax.ws.rs.core.Response".equals(returnGenType.getTypeName())) {
            // unable to determine the response body type from the compiled code, try static extraction
            extractedMethod.features.add(ExtractedMethod.Features.DYNAMIC__TYPE_UNDECLARED);

            if (sourceTreeRoots == null) {
              extractedMethod.features.add(ExtractedMethod.Features.STATIC__NO_SOURCE_TREE);

            } else {
              final File javaFile = getSourceFile(jaxrsAdapterClassname);

              if (javaFile == null) {
                extractedMethod.features.add(ExtractedMethod.Features.STATIC__NO_SOURCE_FILE);

              } else {

                ResponseBodyTypeExtractorStatic.CanonicalClassnameResolver resolver = new CanonicalClassnameResolver();

                ResponseBodyTypeExtractorStatic.ExtractedType extractedType;
                try (Reader reader = new FileReader(javaFile)) {
                  extractedType = new ResponseBodyTypeExtractorStatic().extract(reader, refMethod, resolver);
                } catch (FileNotFoundException e) {
                  // impossible
                  throw new RuntimeException(e);
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }

                if (extractedType.features.remove(ResponseBodyTypeExtractorStatic.ExtractedType.Feature.NO_SUCH_METHOD)) {
                  // TODO specify the non-found method (instead of suggesting to recompile the entire project)
                  extractedMethod.features.add(ExtractedMethod.Features.STATIC__NO_SOURCE_METHOD);
                }
                // check all features consumed
                if (extractedType.features.size() > 0) {
                  throw new IllegalStateException("All features must be consumed. Remained: " + extractedType.features);
                }

                if (extractedType.type != null) {
                  JavaType javaType = buildJavaType(extractedType.type);
                  responseBodySchema0 = OpenApiSchemaBuilder.buildSchema(javaType);

                } else {
                  extractedMethod.features.add(ExtractedMethod.Features.STATIC__VARIABLE_UNDECLARED);
                }
              }
            }

          } else {
            // the specified return type is a response body type
            responseBodySchema0 = buildSchema(returnGenType);

          }
          responseBodySchema = responseBodySchema0;
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

          @Override
          public Map<String, Object> responseBodySchema() {
            return responseBodySchema;
          }
        };

        extractedMethod.method = method;

        extractedMethods.add(extractedMethod);
      }
    }

    return extractedMethods;
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

  /**
   *
   * @param classname canonical classname
   * @return null if noo source found for the class
   */
  protected File getSourceFile(String classname) {
    File javaFile = null;
    if (sourceTreeRoots != null) {
      String childFile = classname.replaceAll("\\.", "/") + ".java";
      for (File sourceTreeRoot : sourceTreeRoots) {
        javaFile = new File(sourceTreeRoot, childFile);
        if (Files.isRegularFile(javaFile.toPath())) {
          break; //found the first
          // TODO check the only sourceFileRoot contains the corresponding java file... or do not check it here?
        }
      }
    }
    return javaFile;
  }

  // TODO non-production code. remove the method?
  protected static String methodToString(java.lang.reflect.Method method) {
    StringBuilder sb = new StringBuilder();
    sb.append(method.getName());
    sb.append('(');
    Parameter[] params = method.getParameters();
    if (params != null) {
      boolean first = true;
      for (Parameter param: params) {
        if (!first) {
          sb.append(',');
        } else {
          first = false;
        }
        sb.append(param.getType().getSimpleName());
      }
    }
    sb.append(')');
    return sb.toString();
  }

  protected class CanonicalClassnameResolver implements ResponseBodyTypeExtractorStatic.CanonicalClassnameResolver {
    @Override
    public String resolve(Set<String> possible) {
      String resolution = null;
      for (String canonicalClassname: possible) {
        try {
          // TODO check class existence without loading it
          Class<?> aClass = classLoader.loadClass(canonicalClassname);
          String resolution0 = aClass.getCanonicalName();
          if (resolution != null) {
            // a class from the same resolution set has already been found
            throw new IllegalStateException("The classpath expected to contain the only class from the set: "
                    + possible + ", but found at least two: " + resolution + " and " + resolution0);
          }
          resolution = resolution0;
        } catch (ClassNotFoundException e) {
          // swallow
        }
      }
      return resolution;
    }
  }

  // the method is here because it uses the local classloader
  protected JavaType buildJavaType(ParameterizedType type) {
    if (type == null) {
      return null;
    }

    // lookup root class
    final Class<?> rootClass;
    try {
      rootClass = classLoader.loadClass(type.typeName());
    } catch (ClassNotFoundException e) {
      // impossible
      throw new RuntimeException(e);
    }

    if (type.typeParams() == null) {
      // simple type
      return TypeFactory.defaultInstance().constructType(rootClass);

    } else {
      // parameterized type

      final JavaType[] paramJavaTypes = new JavaType[type.typeParams().size()];
      for (int i = 0; i < type.typeParams().size(); i++) {
        ParameterizedType paramType = type.typeParams().get(i);
        JavaType paramJavaType = buildJavaType(paramType);
        paramJavaTypes[i] = paramJavaType;
      }

      return TypeFactory.defaultInstance().constructParametricType(rootClass, paramJavaTypes);
    }
  }
}
