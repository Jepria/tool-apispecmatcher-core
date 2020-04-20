package org.jepria.tools.apispecmatcher.core;

public interface MethodMapper {
  boolean map(SpecMethod specMethod, JaxrsMethod jaxrsMethod);
}
