package org.jepria.tools.apispecmatcher.core;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.type.Type;

import java.util.Set;

/**
 * Builds {@link ParameterizedType}s from the AST
 */
public interface ParameterizedTypeBuilderStatic {

  /**
   * Resolves a set of possible (ambiguous) canonical classnames of the same class into a single one,
   * against some external classpath.
   *
   * Canonical classname ambiguity origins from the static code analysis (AST) limitations, e.g.:
   * <pre>
   * import a.b.c.*;
   * import d.e.f.*;
   * public class A {
   *   X x;
   * }
   * </pre>
   * Here, the class X may have either {@code a.b.c.X} or {@code d.e.f.X} canonical classname.
   **/
  interface CanonicalClassnameResolver {
    /**
     * @param possible set of ambiguous canonical classnames of the same class
     * @return one of the elements from the set
     */
    String resolve(Set<String> possible);
  }

  /**
   *
   * @param compilationUnit
   * @param type must be a node of the compilationUnit
   * @param resolver may be {@code null} only if no resolution actually required
   *                 during type building (if all canonical classnames are unambiguous)
   * @return NotNull
   */
  ParameterizedType build(CompilationUnit compilationUnit, Type type, CanonicalClassnameResolver resolver);
}
