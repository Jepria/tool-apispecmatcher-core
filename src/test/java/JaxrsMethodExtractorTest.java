import org.jepria.tools.apispecmatcher.*;

import java.util.List;

public class JaxrsMethodExtractorTest {
  public static void main(String[] args) {
    try {


      final String jaxrsAdapterJavaPath = "C:\\work\\tool-apispec-matcher\\src\\test\\resources\\FeatureJaxrsAdapter.java";

      final Resource jaxrsAdapterJavaResource = new ResourceFileImpl(jaxrsAdapterJavaPath);

      JaxrsMethodExtractor ext = new JaxrsMethodExtractorImpl();
      List<JaxrsMethod> jaxrsMethods = ext.extract(jaxrsAdapterJavaResource);

      for (JaxrsMethod jaxrsMethod: jaxrsMethods) {
        System.out.println(jaxrsMethod.httpMethod() + ":" + jaxrsMethod.path());
      }

    } catch (Throwable e) { throw new RuntimeException(e); }
  }
}
