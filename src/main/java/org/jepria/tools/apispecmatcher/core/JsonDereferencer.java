package org.jepria.tools.apispecmatcher.core;

import java.util.List;
import java.util.Map;

/**
 * Resolves (inlines) JSON references. See https://tools.ietf.org/html/draft-pbryan-zyp-json-ref-03
 */
public class JsonDereferencer {

  /**
   * @param root a json tree root (node corresponding to the '#' reference pth element), MUST be a modifiable map
   */
  public static void dereference(Map<String, Object> root) {
    new JsonDereferencer(root).dereferenceJsonObject(root);
  }

  private final Map<String, Object> root;

  private JsonDereferencer(Map<String, Object> root) {
    this.root = root;
  }

  /**
   *
   * @param object a json element of any type: array, object or a primitive
   */
  private void dereferenceJsonElement(Object object) {
    if (object instanceof Map) {
      Map<String, Object> map = (Map<String, Object>) object;
      dereferenceJsonObject(map);
    } else if (object instanceof List) {
      List<Object> array = (List<Object>) object;
      dereferenceJsonArray(array);
    }
    // no dereference of other types
  }

  /**
   * @param array a json array element
   */
  private void dereferenceJsonArray(List<Object> array) {
    if (array != null) {
      for (Object item: array) {
        dereferenceJsonElement(item);
      }
    }
  }

  /**
   * @param node a json object element
   */
  private void dereferenceJsonObject(Map<String, Object> node) {
    if (node != null) {
      final String refKey = "$ref";
      Object refValue;
      if (node.containsKey(refKey) && (refValue = node.get(refKey)) instanceof String) {
        String refValueStr = (String) refValue;
        node.remove(refKey);
        Map<String, Object> resolved = resolveReference(refValueStr);
        if (resolved != null) {
          node.putAll(resolved);
        }
        // Any members other than "$ref" in a JSON Reference object SHALL be ignored // see https://tools.ietf.org/html/draft-pbryan-zyp-json-ref-03
      } else {
        // If a JSON value does not have these characteristics, then it SHOULD NOT be interpreted as a JSON Reference // see https://tools.ietf.org/html/draft-pbryan-zyp-json-ref-03
        for (Map.Entry<String, Object> e : node.entrySet()) {
          Object value = e.getValue();
          dereferenceJsonElement(value);
        }
      }
    }
  }

  private Map<String, Object> resolveReference(String refValue) {
    if (refValue == null) {
      return null;
    }
    String[] pathParts = refValue.split("/");
    Map<String, Object> m = root;
    for (int i = 0; i < pathParts.length; i++) {
      String pathPart = pathParts[i];
      if (!"".equals(pathPart) && !"#".equals(pathPart)) { // skip "" and "#"
        m = (Map<String, Object>) m.get(pathParts[i]);
      }
    }
    return m;
  }
}
