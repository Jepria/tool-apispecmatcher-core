package org.jepria.tools.apispecmatcher;

import java.io.Reader;
import java.util.Collection;
import java.util.List;

public interface Matcher {

  class ResourceCollectionMatchResult {
    public List<Collection<ApiSpecMethodExtractor.ApiSpecMethod>> nonImplementedMethods;
    public List<Collection<JaxrsMethodExtractor.JaxrsMethod>> nonDocumentedMethods;
  }

  class ResourceCollectionMatchParams {
    public List<Reader> apiSpecsJson;
    public List<Reader> jaxrsAdaptersJava;

    public ResourceCollectionMatchParams() { this(null, null); }

    public ResourceCollectionMatchParams(List<Reader> apiSpecsJson, List<Reader> jaxrsAdaptersJava) {
      this.apiSpecsJson = apiSpecsJson;
      this.jaxrsAdaptersJava = jaxrsAdaptersJava;
    }
  }

  ResourceCollectionMatchResult match(ResourceCollectionMatchParams params);
}
