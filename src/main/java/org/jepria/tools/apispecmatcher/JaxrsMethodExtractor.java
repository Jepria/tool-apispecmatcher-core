package org.jepria.tools.apispecmatcher;

import java.io.Reader;
import java.util.List;

/**
 * Extracts jaxrs methods from a resource
 */
public interface JaxrsMethodExtractor {
  List<JaxrsMethod> extract(Resource jaxrsAdapter);
}
