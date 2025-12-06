package org.nkcoder.io;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Directory Operations: Walking, listing, and searching directories.
 *
 * <p><strong>Java 25 Status:</strong> All methods shown are stable and recommended. Files.walk()
 * and Files.list() with Streams are the modern approach.
 *
 * <p>Key concepts:
 *
 * <ul>
 *   <li>Files.list() - Direct children only
 *   <li>Files.walk() - Recursive traversal
 *   <li>Files.find() - Search with matcher
 *   <li>DirectoryStream - Memory-efficient listing
 * </ul>
 */
public class DirectoryOperationsExample {

  static void main(String[] args) throws IOException {
    createDirectory();
    listDirectory();
    walkDirectory();
    findFiles();
    directoryStream();
    fileVisitor();
    practicalExamples();
  }

  // ===== Creating Directories =====

  static void createDirectory() throws IOException {
    System.out.println("=== Creating Directories ===");

    Path tempBase = Files.createTempDirectory("demo");

    // Create single directory
    Path single = tempBase.resolve("single");
    Files.createDirectory(single);
    System.out.println("  Created: " + single);

    // Create nested directories (like mkdir -p)
    Path nested = tempBase.resolve("a/b/c");
    Files.createDirectories(nested);
    System.out.println("  Created nested: " + nested);

    // createDirectories is idempotent (no error if exists)
    Files.createDirectories(nested); // No exception
    System.out.println("  createDirectories is idempotent");

    // Clean up
    deleteRecursively(tempBase);

    System.out.println(
        """

        createDirectory vs createDirectories:
        - createDirectory(): Single level, parent must exist
        - createDirectories(): Creates all missing parents
        """);
  }

  // ===== Listing Directory Contents =====

  static void listDirectory() throws IOException {
    System.out.println("=== Listing Directory (Files.list) ===");

    Path srcDir = Path.of("src/main/java/org/nkcoder");

    if (Files.exists(srcDir)) {
      // Files.list() returns Stream - must close!
      System.out.println("  Contents of " + srcDir + ":");
      try (Stream<Path> stream = Files.list(srcDir)) {
        stream
            .sorted()
            .limit(10)
            .forEach(
                p -> {
                  String type = Files.isDirectory(p) ? "[DIR]" : "[FILE]";
                  System.out.println("    " + type + " " + p.getFileName());
                });
      }

      // Count items
      try (Stream<Path> stream = Files.list(srcDir)) {
        long count = stream.count();
        System.out.println("  Total items: " + count);
      }
    }

    System.out.println(
        """

        Files.list() notes:
        - Returns Stream<Path> - MUST close (try-with-resources)
        - Only direct children, not recursive
        - Lazy - efficient for large directories
        """);
  }

  // ===== Walking Directory Tree =====

  static void walkDirectory() throws IOException {
    System.out.println("=== Walking Directory Tree (Files.walk) ===");

    Path srcDir = Path.of("src/main/java/org/nkcoder");

    if (Files.exists(srcDir)) {
      // Walk all files recursively
      System.out.println("  Java files in " + srcDir + ":");
      try (Stream<Path> stream = Files.walk(srcDir)) {
        stream
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().endsWith(".java"))
            .limit(10)
            .forEach(p -> System.out.println("    " + srcDir.relativize(p)));
      }

      // Walk with depth limit
      System.out.println("\n  Walk with maxDepth=1:");
      try (Stream<Path> stream = Files.walk(srcDir, 1)) {
        stream.forEach(p -> System.out.println("    " + p.getFileName()));
      }

      // Count files by extension
      System.out.println("\n  File count by extension:");
      try (Stream<Path> stream = Files.walk(srcDir)) {
        var counts =
            stream
                .filter(Files::isRegularFile)
                .map(
                    p -> {
                      String name = p.getFileName().toString();
                      int dot = name.lastIndexOf('.');
                      return dot > 0 ? name.substring(dot) : "(none)";
                    })
                .collect(Collectors.groupingBy(ext -> ext, Collectors.counting()));
        counts.forEach((ext, count) -> System.out.println("    " + ext + ": " + count));
      }
    }

    System.out.println(
        """

        Files.walk() notes:
        - Recursive traversal
        - Second parameter: maxDepth (default: Integer.MAX_VALUE)
        - Always close the stream!
        - Directories are visited before their contents
        """);
  }

  // ===== Finding Files =====

  static void findFiles() throws IOException {
    System.out.println("=== Finding Files (Files.find) ===");

    Path srcDir = Path.of("src/main/java/org/nkcoder");

    if (Files.exists(srcDir)) {
      // Find with matcher - includes file attributes for efficiency
      System.out.println("  Large Java files (>5KB):");
      try (Stream<Path> stream =
          Files.find(
              srcDir,
              Integer.MAX_VALUE,
              (path, attrs) ->
                  attrs.isRegularFile()
                      && path.toString().endsWith(".java")
                      && attrs.size() > 5000)) {
        stream
            .limit(5)
            .forEach(
                p -> {
                  try {
                    System.out.println(
                        "    " + p.getFileName() + " (" + Files.size(p) / 1024 + "KB)");
                  } catch (IOException ignored) {
                  }
                });
      }

      // Find recently modified files
      System.out.println("\n  Recently modified files:");
      var yesterday = Instant.now().minusSeconds(86400);
      try (Stream<Path> stream =
          Files.find(
              srcDir,
              Integer.MAX_VALUE,
              (_, attrs) ->
                  attrs.isRegularFile()
                      && attrs.lastModifiedTime().toInstant().isAfter(yesterday))) {
        stream.limit(5).forEach(p -> System.out.println("    " + p.getFileName()));
      }
    }

    System.out.println(
        """

        Files.find() vs Files.walk():
        - find() takes BiPredicate(Path, BasicFileAttributes)
        - Attributes are read anyway, so more efficient
        - Use find() when filtering by attributes
        - Use walk() for simple path-based filtering
        """);
  }

  // ===== DirectoryStream =====

  static void directoryStream() throws IOException {
    System.out.println("=== DirectoryStream ===");

    Path srcDir = Path.of("src/main/java/org/nkcoder");

    if (Files.exists(srcDir)) {
      // Basic DirectoryStream
      System.out.println("  Directories only:");
      try (DirectoryStream<Path> stream = Files.newDirectoryStream(srcDir)) {
        for (Path path : stream) {
          if (Files.isDirectory(path)) {
            System.out.println("    " + path.getFileName());
          }
        }
      }

      // With glob pattern
      System.out.println("\n  Files matching *.java:");
      try (DirectoryStream<Path> stream = Files.newDirectoryStream(srcDir, "*.java")) {
        int count = 0;
        for (Path path : stream) {
          if (count++ < 5) {
            System.out.println("    " + path.getFileName());
          }
        }
        System.out.println("    ... and more");
      }

      // With filter
      System.out.println("\n  With custom filter (directories starting with 's'):");
      try (DirectoryStream<Path> stream =
          Files.newDirectoryStream(
              srcDir,
              entry ->
                  Files.isDirectory(entry) && entry.getFileName().toString().startsWith("s"))) {
        for (Path path : stream) {
          System.out.println("    " + path.getFileName());
        }
      }
    }

    System.out.println(
        """

        DirectoryStream:
        - Iterable, not Stream (use for-each)
        - Memory-efficient for large directories
        - Glob patterns: *, ?, [abc], {a,b,c}
        - Not recursive - direct children only
        """);
  }

  // ===== FileVisitor Pattern =====

  static void fileVisitor() throws IOException {
    System.out.println("=== FileVisitor Pattern ===");

    Path srcDir = Path.of("src/main/java/org/nkcoder");

    if (Files.exists(srcDir)) {
      // Count files and directories
      var counter = new int[2]; // [files, dirs]

      Files.walkFileTree(
          srcDir,
          new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
              counter[0]++;
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
              counter[1]++;
              return FileVisitResult.CONTINUE;
            }
          });

      System.out.println("  Files: " + counter[0]);
      System.out.println("  Directories: " + counter[1]);

      // Example: Skip certain directories
      System.out.println("\n  Walk skipping 'concurrency' directory:");
      Files.walkFileTree(
          srcDir,
          new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
              if (dir.getFileName().toString().equals("concurrency")) {
                System.out.println("    Skipping: " + dir.getFileName());
                return FileVisitResult.SKIP_SUBTREE;
              }
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
              // Just counting, not printing
              return FileVisitResult.CONTINUE;
            }
          });
    }

    System.out.println(
        """

        FileVisitor:
        - More control than Files.walk()
        - Can skip subtrees, terminate early
        - Handles errors gracefully
        - Use SimpleFileVisitor for convenience
        """);
  }

  // ===== Practical Examples =====

  static void practicalExamples() throws IOException {
    System.out.println("=== Practical Examples ===");

    // Calculate directory size
    Path srcDir = Path.of("src/main/java/org/nkcoder");

    if (Files.exists(srcDir)) {
      // Directory size
      try (Stream<Path> stream = Files.walk(srcDir)) {
        long totalSize =
            stream
                .filter(Files::isRegularFile)
                .mapToLong(
                    p -> {
                      try {
                        return Files.size(p);
                      } catch (IOException e) {
                        return 0;
                      }
                    })
                .sum();
        System.out.println("  Directory size: " + totalSize / 1024 + " KB");
      }

      // Find duplicate file names
      System.out.println("\n  Finding files with common names:");
      try (Stream<Path> stream = Files.walk(srcDir)) {
        var fileNames =
            stream
                .filter(Files::isRegularFile)
                .map(p -> p.getFileName().toString())
                .collect(
                    Collectors.groupingBy(
                        name -> name, Collectors.counting()));
        fileNames.entrySet().stream()
            .filter(e -> e.getValue() > 1)
            .limit(3)
            .forEach(
                e ->
                    System.out.println(
                        "    " + e.getKey() + " appears " + e.getValue() + " times"));
      }
    }

    System.out.println();
  }

  // ===== Helper: Delete Recursively =====

  private static void deleteRecursively(Path path) throws IOException {
    if (Files.isDirectory(path)) {
      try (Stream<Path> entries = Files.list(path)) {
        for (Path entry : entries.toList()) {
          deleteRecursively(entry);
        }
      }
    }
    Files.delete(path);
  }
}
