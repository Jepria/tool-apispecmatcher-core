import org.jepria.tools.apispecmatcher.ApiSpecMethod;
import org.jepria.tools.apispecmatcher.ApiSpecMethodExtractorJson;

import java.io.FileReader;
import java.io.Reader;
import java.util.List;

public class ApiSpecMethodExtractorTest {

  public static void main(String[] args) {
    try {

      final String apiSpecJsonPath = "C:\\work\\tool-apispec-matcher\\src\\test\\resources\\swagger.json";

      ApiSpecMethodExtractorJson parser = new ApiSpecMethodExtractorJson();
      List<ApiSpecMethod> apiSpecMethods;
      try (Reader r = new FileReader(apiSpecJsonPath)) {
        apiSpecMethods = parser.extract(r);
      }

      for (ApiSpecMethod apiSpecMethod: apiSpecMethods) {
        System.out.println(apiSpecMethod.httpMethod() + ":" + apiSpecMethod.path());
      }

    } catch (Throwable e) { throw new RuntimeException(e); }
  }
}
