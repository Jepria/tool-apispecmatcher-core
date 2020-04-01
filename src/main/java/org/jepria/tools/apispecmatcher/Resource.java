package org.jepria.tools.apispecmatcher;

import java.io.Reader;

public interface Resource {
  /**
   * Location of the resource (e.g. a file path)
   * @return
   */
  Location location();

  interface Location {
    // a sample stub method // TODO remove
    String asString();
  }

  Reader newReader();
}
