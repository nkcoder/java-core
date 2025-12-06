package org.nkcoder.io;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

/**
 * HTTP Client API (Java 11+): Modern, built-in HTTP client.
 *
 * <p><strong>Java 25 Status:</strong> The HttpClient API is stable and recommended. It replaces the
 * legacy HttpURLConnection for all new code.
 *
 * <p>Key concepts:
 *
 * <ul>
 *   <li>HttpClient - Reusable client instance
 *   <li>HttpRequest - Immutable request configuration
 *   <li>HttpResponse - Response with body handlers
 *   <li>Synchronous and asynchronous operations
 * </ul>
 */
public class HttpClientExample {

  // Use a public test API that returns JSON
  private static final String TEST_URL = "https://httpbin.org";

  static void main(String[] args) throws Exception {
    clientBasics();
    getRequest();
    postRequest();
    requestConfiguration();
    responseHandling();
    asyncRequests();
  }

  // ===== Client Basics =====

  static void clientBasics() {
    System.out.println("=== HttpClient Basics ===");

    // Default client
    HttpClient defaultClient = HttpClient.newHttpClient();
    System.out.println("  Default client created");

    // Configured client with builder
    HttpClient configuredClient =
        HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2) // Prefer HTTP/2
            .followRedirects(HttpClient.Redirect.NORMAL) // Follow redirects
            .connectTimeout(Duration.ofSeconds(10)) // Connection timeout
            .build();

    System.out.println("  Configured client:");
    System.out.println("    Version: " + configuredClient.version());
    System.out.println("    Follow redirects: " + configuredClient.followRedirects());
    System.out.println("    Connect timeout: " + configuredClient.connectTimeout());

    System.out.println(
        """

        HttpClient notes:
        - Immutable and thread-safe
        - Reuse for multiple requests (connection pooling)
        - Supports HTTP/1.1 and HTTP/2
        - Create once, use many times
        """);
  }

  // ===== GET Request =====

  static void getRequest() throws IOException, InterruptedException {
    System.out.println("=== GET Request ===");

    HttpClient client = HttpClient.newHttpClient();

    // Build GET request
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(TEST_URL + "/get"))
            .GET() // Default, can be omitted
            .build();

    // Send synchronously
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    System.out.println("  Request: GET " + request.uri());
    System.out.println("  Status: " + response.statusCode());
    System.out.println("  Body (truncated): " + truncate(response.body(), 100));

    // Response details
    System.out.println("\n  Response details:");
    System.out.println("    Protocol: " + response.version());
    System.out.println("    Headers count: " + response.headers().map().size());

    System.out.println();
  }

  // ===== POST Request =====

  static void postRequest() throws IOException, InterruptedException {
    System.out.println("=== POST Request ===");

    HttpClient client = HttpClient.newHttpClient();

    // JSON body
    String jsonBody =
        """
        {
          "name": "Alice",
          "email": "alice@example.com"
        }
        """;

    // Build POST request
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(TEST_URL + "/post"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    System.out.println("  Request: POST " + request.uri());
    System.out.println("  Status: " + response.statusCode());
    System.out.println("  Body (truncated): " + truncate(response.body(), 150));

    // Other HTTP methods
    System.out.println(
        """

        Other HTTP methods:
        - PUT: .PUT(BodyPublishers.ofString(body))
        - DELETE: .DELETE()
        - PATCH: .method("PATCH", BodyPublishers.ofString(body))
        """);
  }

  // ===== Request Configuration =====

  static void requestConfiguration() {
    System.out.println("=== Request Configuration ===");

    // Full request configuration
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(TEST_URL + "/headers"))
            // HTTP method
            .GET()
            // Headers
            .header("Accept", "application/json")
            .header("User-Agent", "Java HttpClient")
            .header("X-Custom-Header", "custom-value")
            // Headers as pairs
            .headers("Header1", "Value1", "Header2", "Value2")
            // Timeout for this request
            .timeout(Duration.ofSeconds(30))
            // HTTP version preference
            .version(HttpClient.Version.HTTP_2)
            .build();

    System.out.println("  Request configured:");
    System.out.println("    URI: " + request.uri());
    System.out.println("    Method: " + request.method());
    System.out.println("    Timeout: " + request.timeout().orElse(null));
    System.out.println("    Headers:");
    request
        .headers()
        .map()
        .forEach((name, values) -> System.out.println("      " + name + ": " + values));

    System.out.println();
  }

  // ===== Response Handling =====

  static void responseHandling() throws IOException, InterruptedException {
    System.out.println("=== Response Handling ===");

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(TEST_URL + "/get")).build();

    // Different body handlers
    System.out.println("  Body handlers:");

    // As String
    HttpResponse<String> stringResponse =
        client.send(request, HttpResponse.BodyHandlers.ofString());
    System.out.println("    ofString(): " + stringResponse.body().length() + " chars");

    // As byte array
    HttpResponse<byte[]> bytesResponse =
        client.send(request, HttpResponse.BodyHandlers.ofByteArray());
    System.out.println("    ofByteArray(): " + bytesResponse.body().length + " bytes");

    // As lines (Stream<String>)
    HttpResponse<Stream<String>> linesResponse =
        client.send(request, HttpResponse.BodyHandlers.ofLines());
    long lineCount = linesResponse.body().count();
    System.out.println("    ofLines(): " + lineCount + " lines");

    // Discard body
    HttpResponse<Void> discardResponse =
        client.send(request, HttpResponse.BodyHandlers.discarding());
    System.out.println(
        "    discarding(): body discarded, status = " + discardResponse.statusCode());

    // Response headers
    System.out.println("\n  Accessing headers:");
    var headers = stringResponse.headers();
    System.out.println("    Content-Type: " + headers.firstValue("content-type").orElse("N/A"));
    System.out.println("    All Content-Type values: " + headers.allValues("content-type"));

    System.out.println(
        """

        BodyHandlers summary:
        - ofString(): For text/JSON responses
        - ofByteArray(): For binary data
        - ofFile(Path): Download directly to file
        - ofLines(): Stream for line-by-line processing
        - ofInputStream(): When you need InputStream
        - discarding(): When you only need status/headers
        """);
  }

  // ===== Asynchronous Requests =====

  static void asyncRequests() throws ExecutionException, InterruptedException {
    System.out.println("=== Asynchronous Requests ===");

    HttpClient client = HttpClient.newHttpClient();

    // Single async request
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(TEST_URL + "/delay/1")) // 1 second delay
            .timeout(Duration.ofSeconds(5))
            .build();

    System.out.println("  Sending async request...");
    long start = System.currentTimeMillis();

    CompletableFuture<HttpResponse<String>> future =
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

    // Can do other work here
    System.out.println("  Request sent, doing other work...");

    // Wait for response
    HttpResponse<String> response = future.join();
    long elapsed = System.currentTimeMillis() - start;

    System.out.println("  Response received in " + elapsed + "ms");
    System.out.println("  Status: " + response.statusCode());

    // Chaining with CompletableFuture
    System.out.println("\n  Chaining operations:");
    HttpRequest quickRequest = HttpRequest.newBuilder().uri(URI.create(TEST_URL + "/get")).build();

    String result =
        client
            .sendAsync(quickRequest, HttpResponse.BodyHandlers.ofString())
            .thenApply(HttpResponse::body)
            .thenApply(body -> "Body length: " + body.length())
            .exceptionally(ex -> "Error: " + ex.getMessage())
            .join();

    System.out.println("    " + result);

    // Multiple concurrent requests
    System.out.println("\n  Multiple concurrent requests:");
    var futures =
        List.of(
            client.sendAsync(
                HttpRequest.newBuilder().uri(URI.create(TEST_URL + "/get")).build(),
                HttpResponse.BodyHandlers.ofString()),
            client.sendAsync(
                HttpRequest.newBuilder().uri(URI.create(TEST_URL + "/headers")).build(),
                HttpResponse.BodyHandlers.ofString()),
            client.sendAsync(
                HttpRequest.newBuilder().uri(URI.create(TEST_URL + "/ip")).build(),
                HttpResponse.BodyHandlers.ofString()));

    // Wait for all
    CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

    futures.forEach(
        f -> {
          try {
            HttpResponse<String> r = f.get();
            System.out.println("    " + r.uri().getPath() + " -> " + r.statusCode());
          } catch (Exception e) {
            System.out.println("    Error: " + e.getMessage());
          }
        });

    System.out.println(
        """

        Async patterns:
        - sendAsync() returns CompletableFuture
        - Use thenApply/thenAccept for chaining
        - Use join() or get() to wait for result
        - CompletableFuture.allOf() for multiple requests
        - Always handle exceptions with exceptionally()
        """);
  }

  // ===== Helper =====

  private static String truncate(String s, int maxLen) {
    if (s == null) return "null";
    s = s.replace("\n", " ").replace("\r", "");
    return s.length() <= maxLen ? s : s.substring(0, maxLen) + "...";
  }
}
