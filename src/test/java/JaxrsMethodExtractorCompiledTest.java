import org.jepria.tools.apispecmatcher.core.JaxrsMethodExtractorCompiled;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JaxrsMethodExtractorCompiledTest {
  public static void main(String[] args) {
    try {

      File jarsFolder = new File("C:\\work\\jepria-showcase\\module\\JepRiaShowcase\\App\\service-rest\\target\\jepriashowcase-service-rest\\WEB-INF\\lib");
      File servletJar = new File("C:\\work\\bin-ext\\build\\javax\\servlet\\servlet-api\\3.0.1\\servlet-api-3.0.1.jar");

      File[] jarFilesArray = jarsFolder.listFiles(file -> file.getName().endsWith(".jar"));
      String jaxrsAdapterClassname = "com.technology.jep.jepriashowcase.feature.rest.FeatureJaxrsAdapter";

      List<File> jarFiles = new ArrayList<>();
      if (jarFilesArray != null) {
        jarFiles.addAll(Arrays.asList(jarFilesArray));
      }
      jarFiles.add(servletJar);
      JaxrsMethodExtractorCompiled ext = new JaxrsMethodExtractorCompiled(null, jarFiles);
      List<JaxrsMethodExtractorCompiled.ExtractedMethod> jaxrsMethods = ext.extract(jaxrsAdapterClassname);

      for (JaxrsMethodExtractorCompiled.ExtractedMethod jaxrsMethod: jaxrsMethods) {
        System.out.println(jaxrsMethod.method.asString());
      }

    } catch (Throwable e) { throw new RuntimeException(e); }
  }
}
