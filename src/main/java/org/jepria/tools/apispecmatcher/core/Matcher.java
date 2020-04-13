package org.jepria.tools.apispecmatcher.core;

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
    public List<Method> nonImplementedMethods;
    /**
     * List of jaxrs methods which do not have corresponding specification (documentation)
     */
    public List<Method> nonDocumentedMethods;

    static class MethodTuple {
      public Method apiSpecMethod;
      public Method jaxrsMethod;
    }
  }

  /**
   * A single set of input params for matching
   */
  class MatchParams {
    public List<Method> apiSpecMethods;
    public List<Method> jaxrsMethods;

    public MatchParams(List<Method> apiSpecMethods, List<Method> jaxrsMethods) {
      this.apiSpecMethods = apiSpecMethods;
      this.jaxrsMethods = jaxrsMethods;
    }
  }

  MatchResult match(MatchParams params);
}
