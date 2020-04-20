//import org.jepria.tools.apispecmatcher.core.ApiSpecMethodExtractorJson;
//
//import java.io.FileReader;
//import java.io.Reader;
//import java.util.List;
//
//public class ApiSpecMethodExtractorTest {
//
//  public static void main(String[] args) {
//    try {
//
//      final String apiSpecJsonPath = "C:\\work\\tool-apispec-matcher\\src\\test\\resources\\swagger.json";
//
//      ApiSpecMethodExtractorJson parser = new ApiSpecMethodExtractorJson();
//      List<ApiSpecMethodExtractorJson.ExtractedMethod> apiSpecMethods;
//      try (Reader r = new FileReader(apiSpecJsonPath)) {
//        apiSpecMethods = parser.extract(r);
//      }
//
//      for (ApiSpecMethodExtractorJson.ExtractedMethod apiSpecMethod: apiSpecMethods) {
//        System.out.println(apiSpecMethod.method.httpMethod() + ":" + apiSpecMethod.method.path());
//      }
//
//    } catch (Throwable e) { throw new RuntimeException(e); }
//  }
//}
