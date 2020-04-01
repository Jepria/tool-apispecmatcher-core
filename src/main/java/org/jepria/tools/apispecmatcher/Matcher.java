package org.jepria.tools.apispecmatcher;

import java.io.Reader;
import java.util.Collection;
import java.util.List;

public interface Matcher {

  /**
   * A single set of output values of matching
   */
  class MatchResult {
    public List<Collection<ApiSpecMethod>> nonImplementedMethods;
    public List<Collection<JaxrsMethod>> nonDocumentedMethods;
  }

  /**
   * A single set of input params for matching
   */
  class MatchParams {
    public List<Reader> apiSpecsJson;
    public List<Reader> jaxrsAdaptersJava;

    public MatchParams(List<Reader> apiSpecsJson, List<Reader> jaxrsAdaptersJava) {
      this.apiSpecsJson = apiSpecsJson;
      this.jaxrsAdaptersJava = jaxrsAdaptersJava;
    }
  }

  MatchResult match(MatchParams params);
}
