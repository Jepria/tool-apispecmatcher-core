package org.jepria.tools.apispecmatcher.core;

import com.google.gson.GsonBuilder;
import java.util.List;
import java.util.Map;

public class MethodMatcherImpl implements MethodMatcher {

  @Override
  public boolean match(SpecMethod specMethod, JaxrsMethod jaxrsMethod) {

    List<Method.Parameter> jaxrsParams = jaxrsMethod.params();
    List<Method.Parameter> specParams = specMethod.params();
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


    if (!matchRequestBodies(jaxrsMethod.requestBodySchema(), specMethod.requestBodySchema())) {
      return false;
    }

    if (specMethod.responseBodySchema() == null) {
      switch (jaxrsMethod.responseBodySchemaExtractionStatus()) {
        case METHOD_RETURN_TYPE_DECLARED: case STATIC_VARIABLE_DECLARED: {
          // the response body schema must be declared in the spec
          return false;
        }
        default: {
          // do not match schemas because the response *probably* has no body
          // TODO warn anyway?
          break;
        }
      }
    } else {
      switch (jaxrsMethod.responseBodySchemaExtractionStatus()) {
        case METHOD_RETURN_TYPE_DECLARED: case STATIC_VARIABLE_DECLARED: {
          // match response body schemas
          if (!matchResponseBodies(jaxrsMethod.responseBodySchema(), specMethod.responseBodySchema())) {
            return false;
          }
        }
        default: {
          // do not match schemas because these cases MUST be processed (and logged) by the invoker
          // TODO warn anyway?
          break;
        }
      }
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
    if (schema1.equals(schema2)) { // try to match by simple equality
      return true;
    } else { // simple equality match failed, apply smart match
      System.out.println();
      System.out.println("///two schemas are not simply equal (but must be), apply smart match:");
      System.out.println("///schema1:" + new GsonBuilder().setPrettyPrinting().create().toJson(schema1));
      System.out.println("///schema2:" + new GsonBuilder().setPrettyPrinting().create().toJson(schema2));
      System.out.println();
      // TODO apply smart match
      return false;
    }
  }
}
