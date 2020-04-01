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
  Map<String, Object> method();

  /**
   * GET, POST, etc
   *
   * @return
   */
  String httpMethod();

  // TODO better Path?
  String path();

  /**
   * Location of the method (e.g. within a file)
   *
   * @return
   */
  Location location();

  interface Location {
    // a sample stub method // TODO remove
    String asString();
  }
}
