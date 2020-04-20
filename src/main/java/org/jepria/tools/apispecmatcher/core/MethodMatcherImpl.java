package org.jepria.tools.apispecmatcher.core;

import java.util.List;
import java.util.Map;

public class MethodMatcherImpl implements MethodMatcher {

  @Override
  public boolean match(Method jaxrsMethod, Method apiSpecMethod) {

    // TODO remove this!
    if (!new MethodMapperImpl().map(jaxrsMethod, apiSpecMethod)) {
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

    if (!matchResponseBodies(jaxrsMethod.responseBodySchema(), apiSpecMethod.responseBodySchema())) {
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

  protected boolean matchResponseBodies(Map<String, Object> jaxrsResponseBodySchema, Map<String, Object> specResponseBodySchema) {
    if (jaxrsResponseBodySchema == null) {
      // TODO distinguish the two cases: either schema remained undetermined, or there is truly no response body (WARN already logged)
      return true;
    } else if (specResponseBodySchema == null) {
      return false;
    }

    if (!matchSchemas(jaxrsResponseBodySchema, specResponseBodySchema)) {
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
}
