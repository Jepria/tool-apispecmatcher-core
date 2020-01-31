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
   * Represents match results within a single pair of method sets
   */
  protected static class MethodCollectionMatchResult {
    /**
     * modifications on the object reflect to its source
     */
    public Collection<? extends ApiSpecMethodExtractor.ApiSpecMethod> nonImplementedMethods;
    /**
     * modifications on the object reflect to its source
     */
    public Collection<? extends JaxrsMethodExtractor.JaxrsMethod> nonDocumentedMethods;
  }

  protected static class MethodCollectionMatchParams {
    public Collection<ApiSpecMethodExtractor.ApiSpecMethod> apiSpecMethods;
    public Collection<JaxrsMethodExtractor.JaxrsMethod> jaxrsMethods;
  }

  protected MethodCollectionMatchResult match(MethodCollectionMatchParams params) {

    final MethodCollectionMatchResult result = new MethodCollectionMatchResult();

    // match and retain unmatched
    result.nonImplementedMethods = new ArrayList<>(params.apiSpecMethods);
    result.nonDocumentedMethods = new ArrayList<>(params.jaxrsMethods);

    Iterator<? extends ApiSpecMethodExtractor.ApiSpecMethod> apiSpecMethodIterator = result.nonImplementedMethods.iterator();
    while (apiSpecMethodIterator.hasNext()) {
      ApiSpecMethodExtractor.ApiSpecMethod apiSpecMethod = apiSpecMethodIterator.next();

      Iterator<? extends JaxrsMethodExtractor.JaxrsMethod> jaxrsMethodIterator = result.nonDocumentedMethods.iterator();
      while (jaxrsMethodIterator.hasNext()) {
        JaxrsMethodExtractor.JaxrsMethod jaxrsMethod = jaxrsMethodIterator.next();

        if (matchMethods(jaxrsMethod, apiSpecMethod)) {
          apiSpecMethodIterator.remove();
          jaxrsMethodIterator.remove();
        }
      }
    }

    return result;
  }

  protected boolean matchMethods(JaxrsMethodExtractor.JaxrsMethod jaxrsMethod, ApiSpecMethodExtractor.ApiSpecMethod apiSpecMethod) {
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
  public ResourceCollectionMatchResult match(ResourceCollectionMatchParams params) {

    final ResourceCollectionMatchResult result = new ResourceCollectionMatchResult();
    result.nonImplementedMethods = new ArrayList<>();
    result.nonDocumentedMethods = new ArrayList<>();

    ApiSpecMethodExtractor ext1 = new ApiSpecMethodExtractorJsonImpl();
    List<ApiSpecMethodExtractor.ApiSpecMethod> apiSpecMethods = new ArrayList<>();
    for (Reader r: params.apiSpecsJson) {
      List<ApiSpecMethodExtractor.ApiSpecMethod> apiSpecMethodsForResource = ext1.extract(r);
      result.nonImplementedMethods.add(apiSpecMethodsForResource);
      apiSpecMethods.addAll(apiSpecMethodsForResource);
    }

    JaxrsMethodExtractor ext2 = new JaxrsMethodExtractorImpl();
    List<JaxrsMethodExtractor.JaxrsMethod> jaxrsMethods = new ArrayList<>();
    for (Reader r: params.jaxrsAdaptersJava) {
      List<JaxrsMethodExtractor.JaxrsMethod> jaxrsMethodsForResource = ext2.extract(r);
      result.nonDocumentedMethods.add(jaxrsMethodsForResource);
      jaxrsMethods.addAll(jaxrsMethodsForResource);
    }

    MethodCollectionMatchParams mcmParams = new MethodCollectionMatchParams();
    mcmParams.apiSpecMethods = apiSpecMethods;
    mcmParams.jaxrsMethods = jaxrsMethods;
    MethodCollectionMatchResult mcmResult = match(mcmParams);
    for (Collection<ApiSpecMethodExtractor.ApiSpecMethod> apiSpecMethodsForResource: result.nonImplementedMethods) {
      apiSpecMethodsForResource.retainAll(mcmResult.nonImplementedMethods);
    }
    for (Collection<JaxrsMethodExtractor.JaxrsMethod> jaxrsMethodsForResource: result.nonDocumentedMethods) {
      jaxrsMethodsForResource.retainAll(mcmResult.nonDocumentedMethods);
    }


    return result;
  }
}
