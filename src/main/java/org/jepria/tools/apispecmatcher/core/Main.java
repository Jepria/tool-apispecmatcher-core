package org.jepria.tools.apispecmatcher.core;


import java.io.*;
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
      final List<String> projectClasspathClassDirPaths = new ArrayList<>();
      final List<String> projectClasspathJarDirPaths = new ArrayList<>();
      final List<String> projectClasspathJarPaths = new ArrayList<>();
      final List<String> projectSourceRootDirPaths = new ArrayList<>();

      // read cmd args
      for (int i = 0; i < args.length; i++) {
        if (args[i].equals("--api-specs") && i < args.length - 1) {
          // coma separated list of absolute paths to the api spec (e.g. swagger.json) files that need to be matched
          i++;
          String apiSpecPathsArg = args[i];
          apiSpecPaths.addAll(Arrays.asList(apiSpecPathsArg.split("\\s*;\\s*")));
        } else if (args[i].equals("--jaxrs-adapters") && i < args.length - 1) {
          // coma separated list of qualified classnames of the jaxrs adapters that need to be matched
          i++;
          String jaxrsAdapterPathsArg = args[i];
          jaxrsAdapterPaths.addAll(Arrays.asList(jaxrsAdapterPathsArg.split("\\s*;\\s*")));
        } else if (args[i].equals("--project-classpath-class-dirs") && i < args.length - 1) {
          // coma separated list of absolute paths to the class file hierarchy roots (e.g. target/classes dir in maven), required for loading jaxrs-adapter classes
          i++;
          String projectClasspathClassDirsArg = args[i];
          projectClasspathClassDirPaths.addAll(Arrays.asList(projectClasspathClassDirsArg.split("\\s*;\\s*")));
        } else if (args[i].equals("--project-classpath-jars-dirs") && i < args.length - 1) {
          // coma separated list of absolute paths to the jar collection dirs (e.g. WEB-INF/lib dir in an exploded war), required for loading jaxrs-adapter classes
          i++;
          String projectClasspathJarDirsArg = args[i];
          projectClasspathJarDirPaths.addAll(Arrays.asList(projectClasspathJarDirsArg.split("\\s*;\\s*")));
        } else if (args[i].equals("--project-classpath-jars") && i < args.length - 1) {
          // coma separated list of absolute paths to the jar files, required for loading jaxrs-adapter classes
          i++;
          String projectClasspathJarsArg = args[i];
          projectClasspathJarPaths.addAll(Arrays.asList(projectClasspathJarsArg.split("\\s*;\\s*")));
        } else if (args[i].equals("--project-source-root-dirs") && i < args.length - 1) {
          // coma separated list of absolute paths to the source file hierarchy roots, corresponding to the class file hierarchy roots provided
          i++;
          String projectSourceRootDirsArg = args[i];
          projectSourceRootDirPaths.addAll(Arrays.asList(projectSourceRootDirsArg.split("\\s*;\\s*")));
        }
      }

      // validate and convert args
      boolean failed = false;

      final List<File> apiSpecs = new ArrayList<>();
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
          apiSpecs.add(path.toFile());
        }
      }

      final List<File> projectClasspathClassDirs = new ArrayList<>();
      for (String pathStr: projectClasspathClassDirPaths) {
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
        } else if (!Files.isDirectory(path)) {
          failed = true;
          out.println("Incorrect file path [" + pathStr + "]: not a directory");
        } else {
          projectClasspathClassDirs.add(path.toFile());
        }
      }

      final List<File> projectClasspathJarDirs = new ArrayList<>();
      for (String pathStr: projectClasspathJarDirPaths) {
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
        } else if (!Files.isDirectory(path)) {
          failed = true;
          out.println("Incorrect file path [" + pathStr + "]: not a directory");
        } else {
          projectClasspathJarDirs.add(path.toFile());
        }
      }

      final List<File> projectClasspathJars = new ArrayList<>();
      for (String pathStr: projectClasspathJarPaths) {
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
          projectClasspathJars.add(path.toFile());
        }
      }

      final List<File> projectSourceRootDirs = new ArrayList<>();
      for (String pathStr: projectSourceRootDirPaths) {
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
        } else if (!Files.isDirectory(path)) {
          failed = true;
          out.println("Incorrect file path [" + pathStr + "]: not a directory");
        } else {
          projectSourceRootDirs.add(path.toFile());
        }
      }

      if (failed) {
        return;
      }


      // get resources from files
      List<Reader> apiSpecResources = apiSpecs.stream().map(file -> {
        try {
          return new FileReader(file);
        } catch (FileNotFoundException e) {
          throw new RuntimeException(e);
        }
      }).collect(Collectors.toList());

      // extract methods from resources
      List<Method> apiSpecMethods;
      List<Method> jaxrsMethods;
      {
        apiSpecMethods = new ArrayList<>();
        ApiSpecMethodExtractorJson ext1 = new ApiSpecMethodExtractorJson();
        for (Reader r : apiSpecResources) {
          List<Method> apiSpecMethodsForResource;
          try (Reader r0 = r) {
            apiSpecMethodsForResource = ext1.extract(r0);
          }
          apiSpecMethods.addAll(apiSpecMethodsForResource);
        }
        jaxrsMethods = new ArrayList<>();

        JaxrsMethodExtractorCompiled ext2;
        {
          List<File> jars = new ArrayList<>();
          for (File dir: projectClasspathJarDirs) {
            File[] jars0 = dir.listFiles(file -> file.getName().endsWith(".jar"));
            if (jars0 != null) {
              jars.addAll(Arrays.asList(jars0));
            }
          }
          jars.addAll(projectClasspathJars);

          ext2 = new JaxrsMethodExtractorCompiled(projectClasspathClassDirs, jars, projectSourceRootDirs);
        }

        for (String r : jaxrsAdapterPaths) {
          List<Method> jaxrsMethodsForResource = ext2.extract(r);
          jaxrsMethods.addAll(jaxrsMethodsForResource);
        }
      }


      Matcher.MatchParams params = new Matcher.MatchParams(apiSpecMethods, jaxrsMethods);
      Matcher.MatchResult matchResult = new MatcherImpl().match(params);

      if (matchResult.nonDocumentedMethods != null && !matchResult.nonDocumentedMethods.isEmpty() ||
              matchResult.nonImplementedMethods != null && !matchResult.nonImplementedMethods.isEmpty()) {
        System.out.println("Match failed");

        if (matchResult.nonDocumentedMethods != null && !matchResult.nonDocumentedMethods.isEmpty()) {
          for (Method nonDocumentedMethod: matchResult.nonDocumentedMethods) {
            System.out.println("Non-documented method at " + nonDocumentedMethod.location().asString() + ": " + nonDocumentedMethod.asString());
          }
        }
        if (matchResult.nonImplementedMethods != null && !matchResult.nonImplementedMethods.isEmpty()) {
          for (Method nonImplementedMethod: matchResult.nonImplementedMethods) {
            System.out.println("Non-implemented method at " + nonImplementedMethod.location().asString() + ": " + nonImplementedMethod.asString());
          }
        }
      } else {
        System.out.println("Match succeeded");
      }

    } catch (Throwable e) { throw new RuntimeException(e); }
  }
}