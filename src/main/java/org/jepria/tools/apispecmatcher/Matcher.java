package org.jepria.tools.apispecmatcher;

import java.util.List;

public interface Matcher {

  /**
   * A single set of output values of matching
   */
  class MatchResult {
    /**
     * Tuples of methods which matched completely
     */
    public List<MethodTuple> matchedMethods;
    /**
     * List of specification methods which do not have corresponding implementation
     */
    public List<ApiSpecMethod> nonImplementedMethods;
    /**
     * List of jaxrs methods which do not have corresponding specification (documentation)
     */
    public List<JaxrsMethod> nonDocumentedMethods;

    static class MethodTuple {
      public ApiSpecMethod apiSpecMethod;
      public JaxrsMethod jaxrsMethod;
    }
  }

  /**
   * A single set of input params for matching
   */
  class MatchParams {
    public List<Resource> apiSpecsJson;
    public List<Resource> jaxrsAdaptersJava;

    public MatchParams(List<Resource> apiSpecsJson, List<Resource> jaxrsAdaptersJava) {
      this.apiSpecsJson = apiSpecsJson;
      this.jaxrsAdaptersJava = jaxrsAdaptersJava;
    }
  }

  MatchResult match(MatchParams params);
}
