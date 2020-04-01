package org.jepria.tools.apispecmatcher;

import com.github.javaparser.ast.body.MethodDeclaration;

/**
 * A method represented in a Jaxrs implementation
 */
public interface JaxrsMethod {
  // a sample stub method // TODO remove
  MethodDeclaration method();
  /**
   * GET, POST, etc
   *
   * @return
   */
  String httpMethod();

  // TODO better Path than String?
  String path();

  /**
   * Location of the method (e.g. within a file)
   * @return
   */
  Location location();

  interface Location {
    // a sample stub method // TODO remove
    String asString();
  }
}
