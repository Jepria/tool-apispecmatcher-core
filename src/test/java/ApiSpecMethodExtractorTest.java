import org.jepria.tools.apispecmatcher.*;

import java.util.List;

public class ApiSpecMethodExtractorTest {

  public static void main(String[] args) {
    try {

      final String apiSpecJsonPath = "C:\\work\\tool-apispec-matcher\\src\\test\\resources\\swagger.json";

      final Resource apiSpecJsonResource = new ResourceFileImpl(apiSpecJsonPath);

      ApiSpecMethodExtractor parser = new ApiSpecMethodExtractorJsonImpl();
      List<ApiSpecMethod> apiSpecMethods = parser.extract(apiSpecJsonResource);

      for (ApiSpecMethod apiSpecMethod: apiSpecMethods) {
        System.out.println(apiSpecMethod.httpMethod() + ":" + apiSpecMethod.path());
      }

    } catch (Throwable e) { throw new RuntimeException(e); }
  }
}
