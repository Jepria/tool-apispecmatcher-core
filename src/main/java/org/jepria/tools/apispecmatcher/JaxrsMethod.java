package org.jepria.tools.apispecmatcher;

/**
 * A method represented in a Jaxrs implementation
 */
public interface JaxrsMethod {
  /**
   * GET, POST, etc
   *
   * @return
   */
  String httpMethod();

  // TODO better Path than String?
  String path();
  /**
   * Location of the method and the containing resource
   *
   * @return
   */
  Location location();

  interface Location {
    // a sample stub method // TODO remove
    String asString();
  }
}
