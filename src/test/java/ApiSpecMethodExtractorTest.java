import org.jepria.tools.apispecmatcher.ApiSpecMethodExtractor;
import org.jepria.tools.apispecmatcher.ApiSpecMethodExtractorJsonImpl;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;

public class ApiSpecMethodExtractorTest {
  public static void main(String[] args) {
    try {


      final String apiSpecJsonPath = "C:\\work\\tool-apispec-matcher\\src\\test\\swagger.json";

      ApiSpecMethodExtractor parser = new ApiSpecMethodExtractorJsonImpl();
      List<ApiSpecMethodExtractor.ApiSpecMethod> apiSpecMethods;
      try (Reader reader = new FileReader(new File(apiSpecJsonPath))) {
        apiSpecMethods = parser.extract(reader);
      }
      for (ApiSpecMethodExtractor.ApiSpecMethod apiSpecMethod: apiSpecMethods) {
        System.out.println(apiSpecMethod.httpMethod() + ":" + apiSpecMethod.path());
      }

    } catch (Throwable e) { throw new RuntimeException(e); }
  }
}
