package org.jepria.tools.apispecmatcher;

import java.util.HashMap;
import java.util.Map;

public class OpenApiSchemaDeployer {
  /**
   * Deploys a complex Open API schema with referenced components ('$ref') into a plain schema
   * @param schema root of the schema to be deployed
   * @param anchor api spec root (corresponding to the '#' path element of the '$ref' values)
   * @return
   */
  public static Map<String, Object> deploySchema(Map<String, Object> schema, Map<String, Object> anchor) {
    if (schema == null || anchor == null) {
      // no deploy
      return schema;
    }

    String refValue = (String)schema.get("$ref");
    if (refValue != null) {
      if (schema.keySet().size() > 1) {
        // TODO specify location in the exception message
        throw new IllegalStateException("The schema must either contain the '$ref' element and nothing else or not contain '$ref' element at all");
      }
      String[] pathParts = refValue.split("/");
      Map<String, Object> m = anchor;
      for (int i = 0; i < pathParts.length; i++) {
        String pathPart = pathParts[i];
        if (!"".equals(pathPart) && !"#".equals(pathPart)) { // skip "" and "#"
          m = (Map<String, Object>) m.get(pathParts[i]);
        }
      }
      return deploySchema(m, anchor);

    } else {
      Map<String, Object> result = new HashMap<>();

      String typeVal = (String)schema.get("type");
      if ("string".equals(typeVal)
              || "number".equals(typeVal)
              || "integer".equals(typeVal)
              || "boolean".equals(typeVal)) {
        result.putAll(schema);
      } else if ("object".equals(typeVal)) {
        result.put("type", typeVal);
        // deploy properties
        Map<String, Object> propertiesMap = (Map<String, Object>)schema.get("properties");
        if (propertiesMap != null) {
          Map<String, Object> propertiesMapResult = new HashMap<>();
          {
            for (String property : propertiesMap.keySet()) {
              Map<String, Object> propertySchema = (Map<String, Object>) propertiesMap.get(property);
              propertiesMapResult.put(property, deploySchema(propertySchema, anchor));
            }
          }
          result.put("properties", propertiesMapResult);
        }
      } else if ("array".equals(typeVal)) {
        result.put("type", typeVal);
        // deploy items
        Map<String, Object> itemsSchema = (Map<String, Object>)schema.get("items");
        if (itemsSchema != null) {
          // TODO support mixed-type array items (oneOf, anyOf)
          result.put("items", deploySchema(itemsSchema, anchor));
        }
      }

      return result;
    }
  }
}
