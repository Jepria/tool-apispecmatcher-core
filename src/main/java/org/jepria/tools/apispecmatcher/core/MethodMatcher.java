package org.jepria.tools.apispecmatcher.core;

public interface MethodMatcher {
  boolean match(Method jaxrsMethod, Method apiSpecMethod);
}
