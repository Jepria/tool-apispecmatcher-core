package org.jepria.tools.apispecmatcher;

import java.io.Reader;
import java.util.List;

/**
 * Extracts api-spec methods from a resource
 */
public interface ApiSpecMethodExtractor {
  List<ApiSpecMethod> extract(Reader apiSpec);

  interface ApiSpecMethod extends Method {
    /**
     * GET, POST, etc
     * @return
     */
    String httpMethod();

    // TODO better Path?
    String path();


  }
}
