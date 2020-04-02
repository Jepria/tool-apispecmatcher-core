import org.jepria.tools.apispecmatcher.JaxrsMethod;
import org.jepria.tools.apispecmatcher.JaxrsMethodExtractorCompiled;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JaxrsMethodExtractorCompiledTest {
  public static void main(String[] args) {
    try {

      File classesDir = new File("C:\\work\\jepria-showcase\\module\\JepRiaShowcase\\App\\service-rest\\target\\jepriashowcase-service-rest\\WEB-INF\\classes");
      File jarsFolder = new File("C:\\work\\jepria-showcase\\module\\JepRiaShowcase\\App\\service-rest\\target\\jepriashowcase-service-rest\\WEB-INF\\lib");
      File servletJar = new File("C:\\work\\bin-ext\\build\\javax\\servlet\\servlet-api\\3.0.1\\servlet-api-3.0.1.jar");

      File[] jarFilesArray = jarsFolder.listFiles(file -> file.getName().endsWith(".jar"));
      String jaxrsAdapterClassname = "com.technology.jep.jepriashowcase.feature.rest.FeatureJaxrsAdapter";

      List<File> classDirs = new ArrayList<>();
      classDirs.add(classesDir);
      List<File> jarFiles = new ArrayList<>();
      if (jarFilesArray != null) {
        jarFiles.addAll(Arrays.asList(jarFilesArray));
      }
      jarFiles.add(servletJar);
      JaxrsMethodExtractorCompiled ext = new JaxrsMethodExtractorCompiled(classDirs, jarFiles);
      List<JaxrsMethod> jaxrsMethods = ext.extract(jaxrsAdapterClassname);

      for (JaxrsMethod jaxrsMethod: jaxrsMethods) {
        System.out.println(jaxrsMethod.httpMethod() + ":" + jaxrsMethod.path());
      }

    } catch (Throwable e) { throw new RuntimeException(e); }
  }
}
