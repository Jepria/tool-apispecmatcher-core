package org.jepria.tools.apispecmatcher;

public interface Method {

  /**
   * Location of the method definition within its resource
   * @return
   */
  Location location();

  interface Location {
    // TODO must not be a string
    String asString();
  }
}
