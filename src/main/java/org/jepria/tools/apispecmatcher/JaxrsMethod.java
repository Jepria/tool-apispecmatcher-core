package org.jepria.tools.apispecmatcher;

import com.github.javaparser.ast.body.MethodDeclaration;

/**
 * A method represented in a Jaxrs implementation
 */
public interface JaxrsMethod {
  // a sample stub method // TODO remove
  MethodDeclaration body();
  /**
   * GET, POST, etc
   *
   * @return
   */
  String httpMethod();

  // TODO better Path than String?
  String path();

  /**
   * Containing resource
   * @return
   */
  Resource resource();

  /**
   * Location of the method within the resource
   * @return
   */
  Location location();

  interface Location {
    // a sample stub method // TODO remove
    String asString();
  }
}
