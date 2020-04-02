package org.jepria.tools.apispecmatcher;


import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

  private static PrintStream out = System.out;

  public static void main(String[] args) {
    try {

      final List<String> apiSpecPaths = new ArrayList<>();
      final List<String> jaxrsAdapterPaths = new ArrayList<>();

      // read cmd args
      for (int i = 0; i < args.length; i++) {
        if ((args[i].equals("--api-spec") || args[i].equals("-a")) && i < args.length - 1) {
          i++;
          String apiSpecPathsArg = args[i];
          apiSpecPaths.addAll(Arrays.asList(apiSpecPathsArg.split("\\s*;\\s*")));
        } else if ((args[i].equals("--jaxrs-adapter") || args[i].equals("-j")) && i < args.length - 1) {
          i++;
          String jaxrsAdapterPathsArg = args[i];
          jaxrsAdapterPaths.addAll(Arrays.asList(jaxrsAdapterPathsArg.split("\\s*;\\s*")));
        }
      }

      // validate and convert args
      boolean failed = false;
      final List<Path> apiSpecs = new ArrayList<>();
      for (String pathStr: apiSpecPaths) {
        Path path = null;
        try {
          path = Paths.get(pathStr);
        } catch (Throwable e) {
          e.printStackTrace(); // TODO
          failed = true;
          out.println("Incorrect file path [" + pathStr + "]: resolve exception");
        }
        if (!Files.exists(path)) {
          failed = true;
          out.println("Incorrect file path [" + pathStr + "]: file does not exist");
        } else if (!Files.isRegularFile(path)) {
          failed = true;
          out.println("Incorrect file path [" + pathStr + "]: not a regular file");
        } else {
          apiSpecs.add(path);
        }
      }

      final List<Path> jaxrsAdapters = new ArrayList<>();
      for (String pathStr: jaxrsAdapterPaths) {
        Path path = null;
        try {
          path = Paths.get(pathStr);
        } catch (Throwable e) {
          e.printStackTrace(); // TODO
          failed = true;
          out.println("Incorrect file path [" + pathStr + "]: resolve exception");
        }
        if (!Files.exists(path)) {
          failed = true;
          out.println("Incorrect file path [" + pathStr + "]: file does not exist");
        } else if (!Files.isRegularFile(path)) {
          failed = true;
          out.println("Incorrect file path [" + pathStr + "]: not a regular file");
        } else {
          jaxrsAdapters.add(path);
        }
      }

      if (failed) {
        return;
      }


      // get resources from files
      List<Resource> apiSpecResources = apiSpecs.stream().map(path -> new ResourceFileImpl(path.toFile())).collect(Collectors.toList());
      List<Resource> jaxrsAdapterResources = jaxrsAdapters.stream().map(path -> new ResourceFileImpl(path.toFile())).collect(Collectors.toList());


      // extract methods from resources
      List<ApiSpecMethod> apiSpecMethods;
      List<JaxrsMethod> jaxrsMethods;
      {
        apiSpecMethods = new ArrayList<>();
        ApiSpecMethodExtractor ext1 = new ApiSpecMethodExtractorJsonImpl();
        for (Resource r : apiSpecResources) {
          List<ApiSpecMethod> apiSpecMethodsForResource = ext1.extract(r);
          apiSpecMethods.addAll(apiSpecMethodsForResource);
        }
        jaxrsMethods = new ArrayList<>();
        JaxrsMethodExtractor ext2 = new JaxrsMethodExtractorImpl();
        for (Resource r : jaxrsAdapterResources) {
          List<JaxrsMethod> jaxrsMethodsForResource = ext2.extract(r);
          jaxrsMethods.addAll(jaxrsMethodsForResource);
        }
      }


      Matcher.MatchParams params = new Matcher.MatchParams(apiSpecMethods, jaxrsMethods);
      Matcher.MatchResult matchResult = new MatcherImpl().match(params);

      if (matchResult.nonDocumentedMethods != null && !matchResult.nonDocumentedMethods.isEmpty() ||
              matchResult.nonImplementedMethods != null && !matchResult.nonImplementedMethods.isEmpty()) {
        System.out.println("Match failed");

        if (matchResult.nonDocumentedMethods != null && !matchResult.nonDocumentedMethods.isEmpty()) {
          for (JaxrsMethod nonDocumentedMethod: matchResult.nonDocumentedMethods) {
            System.out.println("Non-documented method in file " + nonDocumentedMethod.resource().location().asString() + ": " + nonDocumentedMethod.httpMethod() + " " + nonDocumentedMethod.path());
          }
        }
        if (matchResult.nonImplementedMethods != null && !matchResult.nonImplementedMethods.isEmpty()) {
          for (ApiSpecMethod nonImplementedMethod: matchResult.nonImplementedMethods) {
            System.out.println("Non-implemented method in file " + nonImplementedMethod.resource().location().asString() + ": " + nonImplementedMethod.httpMethod() + " " + nonImplementedMethod.path());
          }
        }
      } else {
        System.out.println("Match succeeded");
      }

    } catch (Throwable e) { throw new RuntimeException(e); }
  }
}
