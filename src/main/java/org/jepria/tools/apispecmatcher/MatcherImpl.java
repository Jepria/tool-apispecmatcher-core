package org.jepria.tools.apispecmatcher;

import java.util.ArrayList;
import java.util.Iterator;

public class MatcherImpl implements Matcher {

  protected boolean matchMethods(JaxrsMethod jaxrsMethod, ApiSpecMethod apiSpecMethod) {
    return jaxrsMethod.httpMethod().equalsIgnoreCase(apiSpecMethod.httpMethod()) && matchPaths(jaxrsMethod.path(), apiSpecMethod.path());
  }

  protected boolean matchPaths(String path1, String path2) {
    if (path1 == null && path2 == null) {
      return true;
    } else if (path1 == null || path2 == null) {
      return false;
    }

    // do not match path params
    String path1paramsIgnored = path1.replaceAll("\\{.+?\\}", "{}");
    String path2paramsIgnored = path2.replaceAll("\\{.+?\\}", "{}");

    return path1paramsIgnored.equals(path2paramsIgnored);
  }

  @Override
  public MatchResult match(MatchParams params) {

    final MatchResult result = new MatchResult();

    // match and retain unmatched
    result.nonImplementedMethods = new ArrayList<>(params.apiSpecMethods);
    result.nonDocumentedMethods = new ArrayList<>(params.jaxrsMethods);
    result.matchedMethods = new ArrayList<>();

    Iterator<? extends ApiSpecMethod> apiSpecMethodIterator = result.nonImplementedMethods.iterator();
    while (apiSpecMethodIterator.hasNext()) {
      ApiSpecMethod apiSpecMethod = apiSpecMethodIterator.next();

      Iterator<? extends JaxrsMethod> jaxrsMethodIterator = result.nonDocumentedMethods.iterator();
      while (jaxrsMethodIterator.hasNext()) {
        JaxrsMethod jaxrsMethod = jaxrsMethodIterator.next();

        if (matchMethods(jaxrsMethod, apiSpecMethod)) {
          apiSpecMethodIterator.remove();
          jaxrsMethodIterator.remove();

          MatchResult.MethodTuple tuple = new MatchResult.MethodTuple();
          tuple.apiSpecMethod = apiSpecMethod;
          tuple.jaxrsMethod = jaxrsMethod;
          result.matchedMethods.add(tuple);
        }
      }
    }

    return result;
  }
}
