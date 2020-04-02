package org.jepria.tools.apispecmatcher;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

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

  public List<JaxrsMethod> extract(String jaxrsAdapterClassname) {

    final List<JaxrsMethod> result = new ArrayList<>();

    final Class<?> clazz;
    try {
      clazz = classLoader.loadClass(jaxrsAdapterClassname);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

    String classPathAnnotationValue = extractJaxrsPath(clazz);

    for (Method method: clazz.getDeclaredMethods()) {

      // nullablle
      String methodPathAnnotationValue = extractJaxrsPath(method);
      final String pathAnnotationValue = (classPathAnnotationValue != null ? classPathAnnotationValue : "") + (methodPathAnnotationValue != null ? methodPathAnnotationValue : "");

      String httpMethodAnnotationValue = null;
      {
        Annotation[] annotations = method.getDeclaredAnnotations();
        if (annotations != null) {
          for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            String annotationStr = annotationType.getSimpleName(); // TODO or check canonical names? or Classes?

            if ("DELETE".equals(annotationStr)
                    || "GET".equals(annotationStr)
                    || "HEAD".equals(annotationStr)
                    || "OPTIONS".equals(annotationStr)
                    || "POST".equals(annotationStr)
                    || "PUT".equals(annotationStr)) {

              httpMethodAnnotationValue = annotationStr;
              break; //found the first

              // TODO check the only jaxrs annotation found on the method... or do not check it here?
            }
          }
        }
      }

      if (httpMethodAnnotationValue != null) { // check httpMethod annotation only, path annotation might be null or empty

        final String httpMethodAnnotationValue0 = httpMethodAnnotationValue;

        JaxrsMethod jaxrsMethod = new JaxrsMethod() {
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
            return new Location() {
              @Override
              public String asString() {
                return jaxrsAdapterClassname + "(?:?)";
              }
            };
          }
        };

        result.add(jaxrsMethod);
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
            // invoke method value() on the javax.ws.rs.Path annotation
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

}
