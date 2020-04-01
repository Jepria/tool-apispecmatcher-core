package org.jepria.tools.apispecmatcher;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class MatcherImpl implements Matcher {

  /**
   * Local class (not a part of public API)
   *
   * Represents results of a single match between two method collections
   */
  protected static class MethodCollectionMatchResult {
    public Collection<? extends ApiSpecMethod> nonImplementedMethods;
    public Collection<? extends JaxrsMethod> nonDocumentedMethods;
    public Collection<Pair<ApiSpecMethod, JaxrsMethod>> matchedMethods;
  }

  protected static class MethodCollectionMatchParams {
    public Collection<ApiSpecMethod> apiSpecMethods;
    public Collection<JaxrsMethod> jaxrsMethods;
  }

  protected MethodCollectionMatchResult match(MethodCollectionMatchParams params) {

    final MethodCollectionMatchResult result = new MethodCollectionMatchResult();

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

          Pair<ApiSpecMethod, JaxrsMethod> pair = new Pair<>();
          pair.x = apiSpecMethod;
          pair.y = jaxrsMethod;
          result.matchedMethods.add(pair);
        }
      }
    }

    return result;
  }

  protected boolean matchMethods(JaxrsMethod jaxrsMethod, ApiSpecMethod apiSpecMethod) {
    return jaxrsMethod.httpMethod().equalsIgnoreCase(apiSpecMethod.httpMethod()) && matchPaths(jaxrsMethod.path(), apiSpecMethod.path());
  }

  protected boolean matchPaths(String path1, String path2) {
    if (path1 == null && path2 == null) {
      return true;
    }

    // do not match path params
    String path1paramsIgnored = path1.replaceAll("\\{.+?\\}", "{}");
    String path2paramsIgnored = path2.replaceAll("\\{.+?\\}", "{}");

    return path1paramsIgnored.equals(path2paramsIgnored);
  }

  @Override
  public MatchResult match(MatchParams params) {

    final MatchResult result = new MatchResult();
    result.nonImplementedMethods = new ArrayList<>();
    result.nonDocumentedMethods = new ArrayList<>();

    ApiSpecMethodExtractor ext1 = new ApiSpecMethodExtractorJsonImpl();
    List<ApiSpecMethod> apiSpecMethods = new ArrayList<>();
    for (Reader r: params.apiSpecsJson) {
      List<ApiSpecMethod> apiSpecMethodsForResource = ext1.extract(r);
      result.nonImplementedMethods.add(apiSpecMethodsForResource);
      apiSpecMethods.addAll(apiSpecMethodsForResource);
    }

    JaxrsMethodExtractor ext2 = new JaxrsMethodExtractorImpl();
    List<JaxrsMethod> jaxrsMethods = new ArrayList<>();
    for (Reader r: params.jaxrsAdaptersJava) {
      List<JaxrsMethod> jaxrsMethodsForResource = ext2.extract(r);
      result.nonDocumentedMethods.add(jaxrsMethodsForResource);
      jaxrsMethods.addAll(jaxrsMethodsForResource);
    }

    MethodCollectionMatchParams mcmParams = new MethodCollectionMatchParams();
    mcmParams.apiSpecMethods = apiSpecMethods;
    mcmParams.jaxrsMethods = jaxrsMethods;
    MethodCollectionMatchResult mcmResult = match(mcmParams);
    for (Collection<ApiSpecMethod> apiSpecMethodsForResource: result.nonImplementedMethods) {
      apiSpecMethodsForResource.retainAll(mcmResult.nonImplementedMethods);
    }
    for (Collection<JaxrsMethod> jaxrsMethodsForResource: result.nonDocumentedMethods) {
      jaxrsMethodsForResource.retainAll(mcmResult.nonDocumentedMethods);
    }


    return result;
  }
}
