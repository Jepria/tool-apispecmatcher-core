package org.jepria.tools.apispecmatcher.core;

import com.google.gson.GsonBuilder;

import java.util.*;

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
        case METHOD_RETURN_TYPE_DECLARED:
        case STATIC_VARIABLE_DECLARED: {
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
        case METHOD_RETURN_TYPE_DECLARED:
        case STATIC_VARIABLE_DECLARED: {
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
//    if (schema1.equals(schema2)) {
//      return true;
//    }

    if (schema1 == null && schema2 == null) {
      return true;
    } else if (schema1 == null || schema2 == null) {
      return false;
    }

    if (schema1.get("type") != null && schema2.get("type") != null) {
      if ("object".equalsIgnoreCase((String) schema1.get("type"))) {
        if (!"object".equalsIgnoreCase((String) schema2.get("type"))) {
          printDifferentSchemas(schema1, schema2);
          return false;
        } else {
          Map<String, Object> properties1;
          Map<String, Object> properties2;
          try {
            properties1 = (Map<String, Object>) schema1.get("properties");
            properties2 = (Map<String, Object>) schema2.get("properties");
          } catch (ClassCastException ex) {
            System.out.println(ex.getMessage());
            return false;
          }

          if (properties1 == null && properties2 == null) {
            return true;
          } else if (properties1 == null || properties2 == null) {
            printDifferentSchemas(schema1, schema2);
            return false;
          }

          if (!properties1.keySet().equals(properties2.keySet())) {
            printDifferentSchemas(schema1, schema2);
            return false;
          } else {
            for (Map.Entry<String, Object> entry : properties1.entrySet()) {
              String key = entry.getKey();
              Map<String, Object> value1;
              Map<String, Object> value2;
              try {
                value1 = (Map<String, Object>) entry.getValue();
                value2 = (Map<String, Object>) properties2.get(key);
                if (!matchSchemas(value1, value2)) {
                  return false;
                }
              } catch (ClassCastException ex) {
                System.out.println(ex.getMessage());
                return false;
              }
            }
            return true;
          }
        }
      }
      if ("array".equalsIgnoreCase((String) schema1.get("type"))) {
        if (!"array".equalsIgnoreCase((String) schema2.get("type"))) {
          printDifferentSchemas(schema1, schema2);
          return false;
        } else {
          Map<String, Object> items1;
          Map<String, Object> items2;
          try {
            items1 = (Map<String, Object>) schema1.get("items");
            items2 = (Map<String, Object>) schema2.get("items");
          } catch (ClassCastException ex) {
            System.out.println(ex.getMessage());
            return false;
          }

          if (items1 == null && items2 == null) {
            return true;
          } else if (items1 == null || items2 == null) {
            printDifferentSchemas(schema1, schema2);
            return false;
          }

          if (!items1.keySet().equals(items2.keySet())) {
            printDifferentSchemas(schema1, schema2);
            return false;
          } else {
            if (!matchSchemas(items1, items2)) {
              printDifferentSchemas(schema1, schema2);
              return false;
            }
            return true;
//            for (Map.Entry<String, Object> entry : items1.entrySet()) {
//              String key = entry.getKey();
//              if ((entry.getValue() instanceof Map) && (items2.get(key) instanceof Map)) {
//                Map<String, Object> value1;
//                Map<String, Object> value2;
//                try {
//                  value1 = (Map<String, Object>) entry.getValue();
//                  value2 = (Map<String, Object>) items2.get(key);
//                  if (!matchSchemas(value1, value2)) {
//                    return false;
//                  }
//                } catch (ClassCastException ex) {
//                  System.out.println(ex.getMessage());
//                  return false;
//                }
//              }
//              if((entry.getValue() instanceof String) && (items2.get(key) instanceof String)){
//                if (!matchSchemas(items1, items2)) {
//                  return false;
//                }
//              }
//            }
          }
        }
      }
      if (matchPrimitiveTypes(schema1, schema2, "integer")) {
        return true;
      }

      if (matchPrimitiveTypes(schema1, schema2, "string")) {
        return true;
      }

      if (matchPrimitiveTypes(schema1, schema2, "boolean")) {
        return true;
      }
      printDifferentSchemas(schema1, schema2);
      return false;
    } else {
      printDifferentSchemas(schema1, schema2);
      return false;
    }
  }

  private boolean matchPrimitiveTypes(Map<String, Object> schema1, Map<String, Object> schema2, String primitiveType) {
    String type1;
    String type2;
    try {
      type1 = (String) schema1.get("type");
      type2 = (String) schema2.get("type");
    } catch (ClassCastException ex) {
      System.out.println(ex.getMessage());
      return false;
    }
    if (type1 == null && type2 == null) {
      return true;
    } else if (type1 == null || type2 == null) {
      return false;
    }
    if ((primitiveType.equalsIgnoreCase(type1) && primitiveType.equalsIgnoreCase(type2))
            && type1.equalsIgnoreCase(type2)) {
      return true;
    } else {
      return false;
    }
  }

  private void printDifferentSchemas(Map<String, Object> schema1, Map<String, Object> schema2) {
    System.out.println();
    System.out.println("///two schemas are not simply equal (but must be), apply smart match:");
    System.out.println("///schema1:" + new GsonBuilder().setPrettyPrinting().create().toJson(schema1));
    System.out.println("///schema2:" + new GsonBuilder().setPrettyPrinting().create().toJson(schema2));
    System.out.println();
  }

}
