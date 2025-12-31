package org.nkcoder.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

/**
 * Path and Files API (Java 7+, enhanced Java 11+): Modern file operations.
 *
 * <p><strong>Java 25 Status:</strong> This is THE recommended way for file I/O. java.io.File is legacy - always prefer
 * java.nio.file.Path and Files.
 *
 * <p>Key concepts:
 *
 * <ul>
 *   <li>Path: Modern replacement for java.io.File
 *   <li>Files: Static methods for all file operations
 *   <li>Files.readString/writeString (Java 11+): Simple text I/O
 *   <li>Cross-platform path handling
 * </ul>
 */
public class PathAndFilesExample {

    static void main(String[] args) throws IOException {
        pathBasics();
        pathOperations();
        readingFiles();
        writingFiles();
        fileAttributes();
        copyMoveDelete();
        tempFiles();
    }

    // ===== Path Basics =====

    static void pathBasics() {
        System.out.println("=== Path Basics ===");

        // Creating paths
        Path absolute = Path.of("/Users/alice/documents/file.txt");
        Path relative = Path.of("data", "config.json"); // Varargs for components
        Path fromString = Path.of("src/main/java");

        System.out.println("  Absolute: " + absolute);
        System.out.println("  Relative: " + relative);
        System.out.println("  From string: " + fromString);

        // Path components
        Path path = Path.of("/home/user/projects/myapp/src/Main.java");
        System.out.println("\n  Path components:");
        System.out.println("    getFileName(): " + path.getFileName()); // Main.java
        System.out.println("    getParent(): " + path.getParent()); // /home/user/projects/myapp/src
        System.out.println("    getRoot(): " + path.getRoot()); // /
        System.out.println("    getNameCount(): " + path.getNameCount()); // 6
        System.out.println("    getName(0): " + path.getName(0)); // home

        // Absolute vs relative
        Path rel = Path.of("config/app.properties");
        System.out.println("\n  isAbsolute():");
        System.out.println("    " + path + " -> " + path.isAbsolute());
        System.out.println("    " + rel + " -> " + rel.isAbsolute());

        // Convert to absolute
        System.out.println("    rel.toAbsolutePath(): " + rel.toAbsolutePath());

        System.out.println("""

        Path.of() vs Paths.get():
        - Path.of() added in Java 11 - preferred
        - Paths.get() older, still works
        - Both return the same Path
        """);
    }

    // ===== Path Operations =====

    static void pathOperations() {
        System.out.println("=== Path Operations ===");

        Path base = Path.of("/home/user/projects");
        Path relative = Path.of("myapp/src");

        // resolve() - append path
        Path resolved = base.resolve(relative);
        System.out.println("  resolve: " + base + " + " + relative + " = " + resolved);

        // resolve with string
        Path withFile = base.resolve("config.json");
        System.out.println("  resolve string: " + withFile);

        // resolveSibling() - replace filename
        Path original = Path.of("/home/user/file.txt");
        Path sibling = original.resolveSibling("backup.txt");
        System.out.println("  resolveSibling: " + original + " -> " + sibling);

        // relativize() - get relative path between two paths
        Path from = Path.of("/home/user/projects");
        Path to = Path.of("/home/user/documents/file.txt");
        Path relativePath = from.relativize(to);
        System.out.println("  relativize: from " + from + " to " + to + " = " + relativePath);

        // normalize() - remove redundant elements
        Path messy = Path.of("/home/user/../user/./projects//myapp");
        System.out.println("  normalize: " + messy + " -> " + messy.normalize());

        // startsWith() and endsWith()
        Path path = Path.of("/home/user/projects/myapp");
        System.out.println("\n  startsWith/endsWith:");
        System.out.println("    starts with /home/user: " + path.startsWith("/home/user"));
        System.out.println("    ends with myapp: " + path.endsWith("myapp"));

        System.out.println();
    }

    // ===== Reading Files =====

    static void readingFiles() throws IOException {
        System.out.println("=== Reading Files ===");

        // Create a temp file for demo
        Path tempFile = Files.createTempFile("demo", ".txt");
        Files.writeString(tempFile, "Line 1\nLine 2\nLine 3\n");

        // Java 11+: Files.readString() - simplest way for small files
        String content = Files.readString(tempFile);
        System.out.println("  readString():");
        System.out.println("    " + content.replace("\n", "\\n"));

        // With charset
        String utf8Content = Files.readString(tempFile, StandardCharsets.UTF_8);

        // Read all lines as List
        List<String> lines = Files.readAllLines(tempFile);
        System.out.println("\n  readAllLines(): " + lines);

        // Read as Stream (lazy, for large files)
        System.out.println("\n  lines() stream:");
        try (var lineStream = Files.lines(tempFile)) {
            lineStream.forEach(line -> System.out.println("    " + line));
        }

        // Read all bytes
        byte[] bytes = Files.readAllBytes(tempFile);
        System.out.println("\n  readAllBytes(): " + bytes.length + " bytes");

        // Clean up
        Files.delete(tempFile);

        System.out.println("""

        Reading methods:
        - readString(): Best for small text files (Java 11+)
        - readAllLines(): When you need List<String>
        - lines(): Stream for large files (use try-with-resources!)
        - readAllBytes(): For binary files
        """);
    }

    // ===== Writing Files =====

    static void writingFiles() throws IOException {
        System.out.println("=== Writing Files ===");

        Path tempDir = Files.createTempDirectory("demo");
        Path file = tempDir.resolve("output.txt");

        // Java 11+: Files.writeString() - simplest way
        Files.writeString(file, "Hello, World!\n");
        System.out.println("  writeString() created: " + file);
        System.out.println("    Content: " + Files.readString(file));

        // Append mode
        Files.writeString(file, "Appended line\n", StandardOpenOption.APPEND);
        System.out.println("\n  After append:");
        System.out.println("    " + Files.readString(file).replace("\n", "\\n"));

        // Write with options
        Files.writeString(file, "Overwritten\n", StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println("\n  After truncate:");
        System.out.println("    " + Files.readString(file));

        // Write lines
        Path linesFile = tempDir.resolve("lines.txt");
        Files.write(linesFile, List.of("Line 1", "Line 2", "Line 3"));
        System.out.println("\n  write(List<String>):");
        Files.readAllLines(linesFile).forEach(l -> System.out.println("    " + l));

        // Write bytes
        Path binaryFile = tempDir.resolve("data.bin");
        Files.write(binaryFile, new byte[] {0x48, 0x65, 0x6C, 0x6C, 0x6F});
        System.out.println("\n  write(byte[]): " + Files.readString(binaryFile));

        // Clean up
        Files.walk(tempDir).sorted((a, b) -> -a.compareTo(b)).forEach(p -> {
            try {
                Files.delete(p);
            } catch (IOException ignored) {
            }
        });

        System.out.println("""

        Writing methods:
        - writeString(): Best for text (Java 11+)
        - write(Iterable<String>): For list of lines
        - write(byte[]): For binary data

        Common options:
        - APPEND: Add to existing file
        - CREATE: Create if not exists (default)
        - TRUNCATE_EXISTING: Clear existing content
        - CREATE_NEW: Fail if file exists
        """);
    }

    // ===== File Attributes =====

    static void fileAttributes() throws IOException {
        System.out.println("=== File Attributes ===");

        Path path = Path.of("build.gradle.kts");

        if (Files.exists(path)) {
            // Basic checks
            System.out.println("  File: " + path);
            System.out.println("    exists: " + Files.exists(path));
            System.out.println("    isRegularFile: " + Files.isRegularFile(path));
            System.out.println("    isDirectory: " + Files.isDirectory(path));
            System.out.println("    isReadable: " + Files.isReadable(path));
            System.out.println("    isWritable: " + Files.isWritable(path));
            System.out.println("    isExecutable: " + Files.isExecutable(path));
            System.out.println("    isHidden: " + Files.isHidden(path));

            // Size
            System.out.println("    size: " + Files.size(path) + " bytes");

            // Timestamps
            System.out.println("    lastModified: " + Files.getLastModifiedTime(path));

            // All attributes at once (more efficient)
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            System.out.println("\n  BasicFileAttributes:");
            System.out.println("    creationTime: " + attrs.creationTime());
            System.out.println("    lastModifiedTime: " + attrs.lastModifiedTime());
            System.out.println("    lastAccessTime: " + attrs.lastAccessTime());
            System.out.println("    isSymbolicLink: " + attrs.isSymbolicLink());
        } else {
            System.out.println("  (build.gradle.kts not found, skipping attributes demo)");
        }

        System.out.println();
    }

    // ===== Copy, Move, Delete =====

    static void copyMoveDelete() throws IOException {
        System.out.println("=== Copy, Move, Delete ===");

        Path tempDir = Files.createTempDirectory("demo");

        // Create source file
        Path source = tempDir.resolve("source.txt");
        Files.writeString(source, "Original content");

        // Copy
        Path copy = tempDir.resolve("copy.txt");
        Files.copy(source, copy);
        System.out.println("  Copied " + source.getFileName() + " to " + copy.getFileName());

        // Copy with replace
        Files.writeString(source, "Updated content");
        Files.copy(source, copy, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("  Copied with REPLACE_EXISTING");

        // Move (rename)
        Path renamed = tempDir.resolve("renamed.txt");
        Files.move(copy, renamed);
        System.out.println("  Moved " + copy.getFileName() + " to " + renamed.getFileName());

        // Move with atomic option (if supported)
        Path atomic = tempDir.resolve("atomic.txt");
        Files.move(renamed, atomic, StandardCopyOption.ATOMIC_MOVE);
        System.out.println("  Atomic move completed");

        // Delete
        Files.delete(source);
        System.out.println("  Deleted " + source.getFileName());

        // deleteIfExists - no exception if missing
        boolean deleted = Files.deleteIfExists(source);
        System.out.println("  deleteIfExists (already gone): " + deleted);

        // Clean up
        Files.delete(atomic);
        Files.delete(tempDir);

        System.out.println("""

        Copy/Move options:
        - REPLACE_EXISTING: Overwrite target
        - COPY_ATTRIBUTES: Preserve timestamps etc.
        - ATOMIC_MOVE: Atomic rename (if supported)
        """);
    }

    // ===== Temporary Files =====

    static void tempFiles() throws IOException {
        System.out.println("=== Temporary Files ===");

        // Create temp file
        Path tempFile = Files.createTempFile("prefix-", "-suffix.tmp");
        System.out.println("  Temp file: " + tempFile);

        // Create temp directory
        Path tempDir = Files.createTempDirectory("myapp-");
        System.out.println("  Temp dir: " + tempDir);

        // Create temp file in specific directory
        Path tempInDir = Files.createTempFile(tempDir, "data-", ".json");
        System.out.println("  Temp in dir: " + tempInDir);

        // Best practice: clean up in finally or use deleteOnExit workaround
        // Note: Files doesn't have deleteOnExit, but you can:
        tempFile.toFile().deleteOnExit();

        // Manual cleanup
        Files.delete(tempInDir);
        Files.delete(tempDir);
        Files.delete(tempFile);

        System.out.println("""

        Temp file best practices:
        - Use try-with-resources pattern
        - Clean up explicitly when done
        - For auto-delete: path.toFile().deleteOnExit()
        - Temp directory: java.io.tmpdir system property
        """);
    }
}
