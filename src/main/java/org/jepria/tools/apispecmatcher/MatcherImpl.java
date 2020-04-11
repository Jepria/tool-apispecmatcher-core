package org.jepria.tools.apispecmatcher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MatcherImpl implements Matcher {

  protected boolean matchMethods(Method jaxrsMethod, Method apiSpecMethod) {

    if (!jaxrsMethod.httpMethod().equalsIgnoreCase(apiSpecMethod.httpMethod())) {
      return false;
    }

    if (!matchPaths(jaxrsMethod.path(), apiSpecMethod.path())) {
      return false;
    }

    List<Method.Parameter> jaxrsParams = jaxrsMethod.params();
    List<Method.Parameter> specParams = apiSpecMethod.params();
    if (jaxrsParams.size() != specParams.size()) {
      return false;
    }
    for (int i = 0; i < jaxrsParams.size(); i++) {
      Method.Parameter jaxrsParam = jaxrsParams.get(i);
      Method.Parameter specParam = specParams.get(i);
      if (!matchParams(jaxrsParam, specParam)) {
        return false;
      }
    }


    if (!matchRequestBodies(jaxrsMethod.requestBodySchema(), apiSpecMethod.requestBodySchema())) {
      return false;
    }

    return true;
  }

  protected boolean matchRequestBodies(Map<String, Object> jaxrsRequestBodySchema, Map<String, Object> specRequestBodySchema) {
    if (jaxrsRequestBodySchema == null && specRequestBodySchema == null) {
      return true;
    } else if (jaxrsRequestBodySchema == null || specRequestBodySchema == null) {
      return false;
    }

    if (!matchSchemas(jaxrsRequestBodySchema, specRequestBodySchema)) {
      return false;
    }

    return true;
  }

  protected boolean matchParams(Method.Parameter jaxrsParam, Method.Parameter specParam) {
    if (jaxrsParam == null && specParam == null) {
      return true;
    } else if (jaxrsParam == null || specParam == null) {
      return false;
    }

    if ("Query".equals(jaxrsParam.in()) && !"query".equals(specParam.in())
            || "Path".equals(jaxrsParam.in()) && !"path".equals(specParam.in())
            || "Header".equals(jaxrsParam.in()) && !"header".equals(specParam.in())
            || "Cookie".equals(jaxrsParam.in()) && !"cookie".equals(specParam.in())) {
      return false;
    }
    // TODO match any other param ins (Matrix, Bean, Form)?

    if (!jaxrsParam.name().equals(specParam.name())) {
      return false;
    }

    Map<String, Object> jaxrsParamSchema = jaxrsParam.schema();
    Map<String, Object> specParamSchema = specParam.schema();
    if (!matchSchemas(jaxrsParamSchema, specParamSchema)) {
      return false;
    }

    return true;
  }

  protected boolean matchSchemas(Map<String, Object> schema1, Map<String, Object> schema2) {
    // TODO implement schema matching
    return true;
  }

  protected boolean matchPaths(String path1, String path2) {
    if (path1 == null && path2 == null) {
      return true;
    } else if (path1 == null || path2 == null) {
      return false;
    }

    // do not match path params
    String path1paramsIgnored = path1.replaceAll("\\{.+?\\}", "{}");
    String path2paramsIgnored = path2.replaceAll("\\{.+?\\}", "{}");

    return path1paramsIgnored.equals(path2paramsIgnored);
  }

  @Override
  public MatchResult match(MatchParams params) {

    final MatchResult result = new MatchResult();

    // match and retain unmatched
    result.nonImplementedMethods = new ArrayList<>(params.apiSpecMethods);
    result.nonDocumentedMethods = new ArrayList<>(params.jaxrsMethods);
    result.matchedMethods = new ArrayList<>();

    Iterator<? extends Method> apiSpecMethodIterator = result.nonImplementedMethods.iterator();
    while (apiSpecMethodIterator.hasNext()) {
      Method apiSpecMethod = apiSpecMethodIterator.next();

      Iterator<? extends Method> jaxrsMethodIterator = result.nonDocumentedMethods.iterator();
      while (jaxrsMethodIterator.hasNext()) {
        Method jaxrsMethod = jaxrsMethodIterator.next();

        if (matchMethods(jaxrsMethod, apiSpecMethod)) {
          apiSpecMethodIterator.remove();
          jaxrsMethodIterator.remove();

          MatchResult.MethodTuple tuple = new MatchResult.MethodTuple();
          tuple.apiSpecMethod = apiSpecMethod;
          tuple.jaxrsMethod = jaxrsMethod;
          result.matchedMethods.add(tuple);
        }
      }
    }

    return result;
  }
}
