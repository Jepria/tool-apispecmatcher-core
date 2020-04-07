package org.jepria.tools.apispecmatcher;

import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MatcherImpl implements Matcher {

  protected boolean matchMethods(JaxrsMethod jaxrsMethod, ApiSpecMethod apiSpecMethod) {

    if (!jaxrsMethod.httpMethod().equalsIgnoreCase(apiSpecMethod.httpMethod())) {
      return false;
    }

    if (!matchPaths(jaxrsMethod.path(), apiSpecMethod.path())) {
      return false;
    }

    List<JaxrsMethod.Parameter> jaxrsParams = jaxrsMethod.params();
    List<ApiSpecMethod.Parameter> specParams = apiSpecMethod.params();
    if (jaxrsParams.size() != specParams.size()) {
      return false;
    }
    for (int i = 0; i < jaxrsParams.size(); i++) {
      JaxrsMethod.Parameter jaxrsParam = jaxrsParams.get(i);
      ApiSpecMethod.Parameter specParam = specParams.get(i);
      if (!matchParams(jaxrsParam, specParam)) {
        return false;
      }
    }


    if (!matchRequestBodies(jaxrsMethod.requestBodyType(), apiSpecMethod.requestBodySchema())) {
      return false;
    }

    return true;
  }

  protected boolean matchRequestBodies(Class<?> jaxrsRequestBodyType, Map<String, Object> specRequestBodySchema) {
    if (jaxrsRequestBodyType == null && specRequestBodySchema == null) {
      return true;
    } else if (jaxrsRequestBodyType == null || specRequestBodySchema == null) {
      return false;
    }

    Map<String, Object> jaxrsRequestBodySchema = OpenApiSchemaBuilder.buildSchema(jaxrsRequestBodyType);
    if (!matchSchemas(jaxrsRequestBodySchema, specRequestBodySchema)) {
      return false;
    }

    return true;
  }

  protected boolean matchParams(JaxrsMethod.Parameter jaxrsParam, ApiSpecMethod.Parameter specParam) {
    if (jaxrsParam == null && specParam == null) {
      return true;
    } else if (jaxrsParam == null || specParam == null) {
      return false;
    }

    if ("Query".equals(jaxrsParam.in()) && !"query".equals(specParam.in())
            || "Path".equals(jaxrsParam.in()) && !"path".equals(specParam.in())
            || "Header".equals(jaxrsParam.in()) && !"header".equals(specParam.in())
            || "Cookie".equals(jaxrsParam.in()) && !"cookie".equals(specParam.in())) {
      return false;
    }
    // TODO match any other param ins (Matrix, Bean, Form)?

    if (!jaxrsParam.name().equals(specParam.name())) {
      return false;
    }

    Map<String, Object> jaxrsParamSchema = OpenApiSchemaBuilder.buildSchema(jaxrsParam.type());
    Map<String, Object> specParamSchema = specParam.schema();
    if (!matchSchemas(jaxrsParamSchema, specParamSchema)) {
      return false;
    }

    return true;
  }

  protected boolean matchSchemas(Map<String, Object> schema1, Map<String, Object> schema2) {
    if (schema1.equals(schema2)) { // try to match by simple equality
      return true;
    } else { // simple equality match failed, apply smart match
      System.out.println();
      System.out.println("///two schemas are not simply equal (but must be), apply smart match:");
      System.out.println("///schema1:" + new GsonBuilder().setPrettyPrinting().create().toJson(schema1));
      System.out.println("///schema2:" + new GsonBuilder().setPrettyPrinting().create().toJson(schema2));
      System.out.println();
      // TODO apply smart match
      return false;
    }
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
