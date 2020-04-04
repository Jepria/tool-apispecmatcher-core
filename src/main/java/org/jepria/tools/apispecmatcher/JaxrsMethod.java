package org.jepria.tools.apispecmatcher;

import java.util.List;

/**
 * A method represented in a Jaxrs implementation
 */
public interface JaxrsMethod {
  /**
   * One of GET, POST, PUT, DELETE, HEAD, OPTIONS
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
   * Location of the method within the resources
   *
   * @return
   */
  Location location();

  interface Parameter {
    /**
     * Qualified type name
     */
    String type();
    /**
     * NonNull
     * One of Query, Path, Header, Cookie
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
   * Request body type, {@code null} if the method has no request body
   */
  // TODO either return Class<?> or a tuple: String (canonicalClassName) and ClassLoader reference to load the class from. Which solution is worse?
  Class<?> requestBodyType();

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
      sb.append(p.in()).append(':').append(p.name()).append(':').append(p.type());
    }
    if (requestBodyType() != null) {
      if (params++ > 0) {
        sb.append(", ");
      }
      sb.append("Body:").append(requestBodyType());
    }
    sb.append(')');

    return sb.toString();
  }
}
