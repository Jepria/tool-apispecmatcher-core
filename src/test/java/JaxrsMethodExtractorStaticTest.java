import org.jepria.tools.apispecmatcher.JaxrsMethodExtractorStatic;
import org.jepria.tools.apispecmatcher.Method;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;

public class JaxrsMethodExtractorStaticTest {
  public static void main(String[] args) {
    try {

      File jaxrsAdapterJavaFile = new File("C:\\work\\jepria-showcase\\module\\JepRiaShowcase\\App\\service-rest\\src\\main\\java\\com\\technology\\jep\\jepriashowcase\\feature\\rest\\FeatureJaxrsAdapter.java");

      JaxrsMethodExtractorStatic ext = new JaxrsMethodExtractorStatic();
      List<Method> jaxrsMethods;
      try (Reader r = new FileReader(jaxrsAdapterJavaFile)) {
        jaxrsMethods = ext.extract(r);
      }

      for (Method jaxrsMethod: jaxrsMethods) {
        System.out.println(jaxrsMethod.asString());
      }

    } catch (Throwable e) { throw new RuntimeException(e); }
  }
}
