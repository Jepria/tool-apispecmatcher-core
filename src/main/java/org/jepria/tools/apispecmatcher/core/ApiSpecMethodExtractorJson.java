package org.jepria.tools.apispecmatcher.core;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Extracts api spec methods from the json resource
 */
public class ApiSpecMethodExtractorJson {

  public List<SpecMethod> extract(Reader spec) {

    final List<SpecMethod> methods = new ArrayList<>();

    final Map<String, Object> map;

    map = new Gson().fromJson(spec, new TypeToken<Map<String, Object>>() {}.getType());
    JsonDereferencer.dereference(map);

    Map<String, Object> pathsMap = (Map<String, Object>)map.get("paths");
    if (pathsMap != null) {
      for (final String path : pathsMap.keySet()) {
        Map<String, Object> pathMap = (Map<String, Object>) pathsMap.get(path);
        if (pathMap != null) {
          for (final String httpMethod : pathMap.keySet()) {
            Map<String, Object> methodMap = (Map<String, Object>) pathMap.get(httpMethod);
            if (methodMap != null) {

              final Map<String, Object> requestBodySchema;
              {
                Map<String, Object> requestBodySchema0 = null;
                Map<String, Object> requestBodyMap = (Map<String, Object>) methodMap.get("requestBody");
                if (requestBodyMap != null) {
                  Map<String, Object> contentMap = (Map<String, Object>) requestBodyMap.get("content");
                  if (contentMap != null) {
                    Map<String, Object> theContentMap = null;
                    for (String contentType: contentMap.keySet()) {
                      if ("application/json".equals(contentType) || contentType.startsWith("application/json;")) {
                        theContentMap = (Map<String, Object>) contentMap.get(contentType);
                        break; // TODO support other content types?
                      }
                    }
                    if (theContentMap != null) {
                      requestBodySchema0 = (Map<String, Object>) theContentMap.get("schema");
                    }
                  }
                }
                requestBodySchema = requestBodySchema0;
              }

              // extract params
              final List<Method.Parameter> params = new ArrayList<>();
              {
                List<Map<String, Object>> parametersList = (List<Map<String, Object>>)methodMap.get("parameters");
                if (parametersList != null) {
                  for (Map<String, Object> parameterMap: parametersList) {

                    final Map<String, Object> schema = (Map<String, Object>)parameterMap.get("schema");
                    final String in = (String)parameterMap.get("in");
                    final String name = (String)parameterMap.get("name");

                    params.add(new Method.Parameter() {
                      @Override
                      public Map<String, Object> schema() {
                        return schema;
                      }
                      @Override
                      public String in() {
                        return in;
                      }
                      @Override
                      public String name() {
                        return name;
                      }
                    });
                  }
                }
              }

              final Map<String, Object> responseBodySchema;
              {
                Map<String, Object> responseBodySchema0 = null;
                Map<String, Object> responsesMap = (Map<String, Object>) methodMap.get("responses");
                if (responsesMap != null) {
                  Map<String, Object> response200Map = (Map<String, Object>) responsesMap.get("200");
                  if (response200Map != null) {
                    Map<String, Object> contentMap = (Map<String, Object>) response200Map.get("content");
                    if (contentMap != null) {
                      Map<String, Object> theContentMap = null;
                      for (String contentType: contentMap.keySet()) {
                        if ("application/json".equals(contentType) || contentType.startsWith("application/json;")) {
                          theContentMap = (Map<String, Object>) contentMap.get(contentType);
                          break; // TODO support other content types?
                        }
                      }
                      if (theContentMap != null) {
                        responseBodySchema0 = (Map<String, Object>) theContentMap.get("schema");
                      }
                    }
                  }
                }
                responseBodySchema = responseBodySchema0;
              }


              SpecMethod method = new SpecMethod() {
                @Override
                public String httpMethod() {
                  return httpMethod;
                }

                @Override
                public String path() {
                  return path;
                }

                @Override
                public List<Parameter> params() {
                  return params;
                }

                @Override
                public Map<String, Object> requestBodySchema() {
                  return requestBodySchema;
                }

                @Override
                public Map<String, Object> responseBodySchema() {
                  return responseBodySchema;
                }
              };

              methods.add(method);
            }
          }
        }
      }
    }

    return methods;
  }
}
