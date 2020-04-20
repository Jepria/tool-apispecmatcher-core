package org.jepria.tools.apispecmatcher.core;

public interface MethodMatcher {
  boolean match(SpecMethod specMethod, JaxrsMethod jaxrsMethod);
}
