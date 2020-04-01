package org.jepria.tools.apispecmatcher;


import java.io.FileReader;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

      Matcher.MatchResult matchResult;

      {
        final List<Reader> apiSpecReaders = new ArrayList<>();
        final List<Reader> jaxrsAdapterReaders = new ArrayList<>();
        try {
          for (Path path: apiSpecs) {
            apiSpecReaders.add(new FileReader(path.toFile()));
          }
          for (Path path: jaxrsAdapters) {
            jaxrsAdapterReaders.add(new FileReader(path.toFile()));
          }


          Matcher.MatchParams params = new Matcher.MatchParams(apiSpecReaders, jaxrsAdapterReaders);
          matchResult = new MatcherImpl().match(params);


        } finally { // TODO this is not a correct try-with-resources replacement
          for (Reader r: apiSpecReaders) {
            r.close();
          }
          for (Reader r: jaxrsAdapterReaders) {
            r.close();
          }
        }
      }


      if (matchResult.nonDocumentedMethods.stream().anyMatch(collection -> !collection.isEmpty()) ||
              matchResult.nonImplementedMethods.stream().anyMatch(collection -> !collection.isEmpty())) {
        System.out.println("Match failed");

        if (!matchResult.nonDocumentedMethods.isEmpty()) {
          for (int i = 0; i < matchResult.nonDocumentedMethods.size(); i++) {
            for (JaxrsMethod nonDocumentedMethod: matchResult.nonDocumentedMethods.get(i)) {
              System.out.println("Non-documented method in file " + jaxrsAdapterPaths.get(i) + ": " + nonDocumentedMethod.httpMethod() + " " + nonDocumentedMethod.path());
            }
          }
        }
        if (!matchResult.nonImplementedMethods.isEmpty()) {
          for (int i = 0; i < matchResult.nonImplementedMethods.size(); i++) {
            for (ApiSpecMethod nonImplementedMethod: matchResult.nonImplementedMethods.get(i)) {
              System.out.println("Non-implemented method in file " + apiSpecPaths.get(i) + ": " + nonImplementedMethod.httpMethod() + " " + nonImplementedMethod.path());
            }
          }
        }
      } else {
        System.out.println("Match succeeded");
      }

    } catch (Throwable e) { throw new RuntimeException(e); }
  }
}
