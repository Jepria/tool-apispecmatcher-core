package org.jepria.tools.apispecmatcher;

import java.io.Reader;
import java.util.List;

/**
 * Extracts jaxrs methods from a resource
 */
public interface JaxrsMethodExtractor {

  List<JaxrsMethod> extract(Reader jaxrsAdapter);

  interface JaxrsMethod extends Method {
    /**
     * GET, POST, etc
     * @return
     */
    String httpMethod();

    // TODO better Path?
    String path();

  }
}
