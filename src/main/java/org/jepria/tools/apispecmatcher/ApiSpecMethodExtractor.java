package org.jepria.tools.apispecmatcher;

import java.io.Reader;
import java.util.List;

/**
 * Extracts api-spec methods from a resource
 */
public interface ApiSpecMethodExtractor {
  List<ApiSpecMethod> extract(Resource apiSpec);
}
