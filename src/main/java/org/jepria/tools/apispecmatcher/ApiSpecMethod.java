package org.jepria.tools.apispecmatcher;

import java.util.Map;

/**
 * A method represented in an api spec resource
 */
public interface ApiSpecMethod {
  /**
   * In swagger.json this is the entire object under the (e.g.) "get" key
   *
   * @return
   */
  // a sample stub method // TODO remove
  Map<String, Object> body();

  /**
   * GET, POST, etc
   *
   * @return
   */
  String httpMethod();

  // TODO better Path?
  String path();

  /**
   * Containing resource
   * @return
   */
  Resource resource();

  /**
   * Location of the method within the resource
   *
   * @return
   */
  Location location();

  interface Location {
    // a sample stub method // TODO remove
    String asString();
  }
}
