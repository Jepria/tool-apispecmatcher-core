package org.jepria.tools.apispecmatcher;

import java.util.List;
import java.util.Map;

/**
 * A method represented in an api spec resource
 */
public interface ApiSpecMethod {
  /**
   * One of get, post, put, delete, head, options
   *
   * @return
   */
  String httpMethod();

  /**
   * URL path
   * @return
   */
  // TODO better Path than String?
  String path();

  interface Location {
    // a sample stub method // TODO remove
    String asString();
  }

  /**
   * Location of the method and the containing resource
   *
   * @return
   */
  Location location();

  interface Parameter {
    /**
     * OpenAPI schema, as-is from json spec
     */
    Map<String, Object> schema();
    /**
     * NonNull
     * One of query, path, header, cookie
     */
    String in();

    /**
     * NonNull
     * Parameter functional name related to its 'in' type
     */
    String name();
  }

  /**
   * NotNull
   * @return at least empty list
   */
  List<Parameter> params();

  /**
   * {@code null} if the method has no request body
   * @return
   */
  Map<String, Object> requestBodySchema();

  default String asString() {
    StringBuilder sb = new StringBuilder();
    sb.append(httpMethod());
    sb.append(':');
    sb.append(path());
    sb.append('(');
    int params = 0;
    for (Parameter p: params()) {
      if (params++ > 0) {
        sb.append(", ");
      }
      sb.append(p.in()).append(':').append(p.name()).append(':').append("<...>");
    }
    if (requestBodySchema() != null) {
      if (params++ > 0) {
        sb.append(", ");
      }
      sb.append("Body:").append("<...>");
    }
    sb.append(')');

    return sb.toString();
  }
}
