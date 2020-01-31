import org.jepria.tools.apispecmatcher.JaxrsMethodExtractor;
import org.jepria.tools.apispecmatcher.JaxrsMethodExtractorImpl;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;

public class JaxrsMethodExtractorTest {
  public static void main(String[] args) {
    try {


      final String jaxrsAdapterJavaPath = "C:\\work\\tool-apispec-matcher\\src\\test\\FeatureJaxrsAdapter.java";

      JaxrsMethodExtractor ext = new JaxrsMethodExtractorImpl();
      List<JaxrsMethodExtractor.JaxrsMethod> jaxrsMethods;
      try (Reader reader = new FileReader(new File(jaxrsAdapterJavaPath))) {
        jaxrsMethods = ext.extract(reader);
      }
      for (JaxrsMethodExtractor.JaxrsMethod jaxrsMethod: jaxrsMethods) {
        System.out.println(jaxrsMethod.httpMethod() + ":" + jaxrsMethod.path());
      }

    } catch (Throwable e) { throw new RuntimeException(e); }
  }
}
