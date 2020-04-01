package org.jepria.tools.apispecmatcher;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApiSpecMethodExtractorJsonImpl implements ApiSpecMethodExtractor {
  @Override
  public List<ApiSpecMethod> extract(Resource apiSpec) {

    final List<ApiSpecMethod> result = new ArrayList<>();

    try (Reader reader = apiSpec.newReader()) {
      Map<String, Object> map = new Gson().fromJson(reader, new TypeToken<Map<String, Object>>(){}.getType());

      Object pathsObject = map.get("paths");
      if (pathsObject instanceof Map) {
        Map<String, Object> pathsMap = (Map<String, Object>) pathsObject;

        for (final String path : pathsMap.keySet()) {
          Object pathObject = pathsMap.get(path);

          if (pathObject instanceof Map) {
            Map<String, Object> pathMap = (Map<String, Object>) pathObject;

            for (final String httpMethod : pathMap.keySet()) {

              Map<String, Object> method = (Map<String, Object>) pathMap.get(httpMethod);

              ApiSpecMethod apiSpecMethod = new ApiSpecMethod() {
                @Override
                public Map<String, Object> body() {
                  return method;
                }
                @Override
                public String httpMethod() {
                  return httpMethod;
                }
                @Override
                public String path() {
                  return path;
                }
                @Override
                public Resource resource() {
                  return apiSpec;
                }
                @Override
                public ApiSpecMethod.Location location() {
                  return null; // TODO provide location
                }
              };

              result.add(apiSpecMethod);
            }
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);//TODO
    }

    return result;
  }
}
