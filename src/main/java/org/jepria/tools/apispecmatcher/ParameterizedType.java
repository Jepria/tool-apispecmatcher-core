package org.jepria.tools.apispecmatcher;

import java.util.List;

/**
 * Structure representing a parameterized type as a tree, e.g.
 * {@code ParentClass<ChildClass1, ChildClass2<GrandChildClass>> }
 */
public interface ParameterizedType {
  String typeName();

  /**
   * Either null if this type is a non-parameterized (simple) type
   * or non-empty list of type params if this type is parameterized
   * @return
   */
  List<ParameterizedType> typeParams();
}
