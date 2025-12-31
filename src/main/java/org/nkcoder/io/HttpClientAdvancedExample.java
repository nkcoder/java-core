package org.nkcoder.io;

import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

/**
 * HTTP Client Advanced Features: HTTP/2, authentication, cookies, and more.
 *
 * <p><strong>Java 25 Status:</strong> All features shown are stable. WebSocket support is also available via
 * HttpClient.
 *
 * <p>Key concepts:
 *
 * <ul>
 *   <li>HTTP/2 support and server push
 *   <li>Authentication (Basic, custom)
 *   <li>Cookie handling
 *   <li>File upload/download
 *   <li>Error handling patterns
 * </ul>
 */
public class HttpClientAdvancedExample {

    private static final String TEST_URL = "https://httpbin.org";

    static void main(String[] args) throws Exception {
        http2Support();
        authenticationBasic();
        cookieHandling();
        fileDownload();
        fileUpload();
        errorHandling();
        timeoutPatterns();
        bestPractices();
    }

    // ===== HTTP/2 Support =====

    static void http2Support() throws IOException, InterruptedException {
        System.out.println("=== HTTP/2 Support ===");

        // Client configured for HTTP/2
        HttpClient client =
                HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();

        HttpRequest request =
                HttpRequest.newBuilder().uri(URI.create(TEST_URL + "/get")).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("  Requested version: HTTP/2");
        System.out.println("  Actual version: " + response.version());
        System.out.println("  Status: " + response.statusCode());

        // Note: Server may respond with HTTP/1.1 if it doesn't support HTTP/2
        // The client will automatically fall back

        System.out.println("""

        HTTP/2 features:
        - Multiplexing: Multiple requests over single connection
        - Header compression: Reduced overhead
        - Server push: Server can send resources proactively
        - Binary protocol: More efficient than HTTP/1.1 text
        - Automatic fallback to HTTP/1.1 if needed
        """);
    }

    // ===== Authentication =====

    static void authenticationBasic() throws IOException, InterruptedException {
        System.out.println("=== Basic Authentication ===");

        // Client with authenticator
        HttpClient client = HttpClient.newBuilder()
                .authenticator(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("testuser", "testpass".toCharArray());
                    }
                })
                .build();

        // httpbin.org basic auth endpoint
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TEST_URL + "/basic-auth/testuser/testpass"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("  Basic auth request:");
        System.out.println("    Status: " + response.statusCode());
        System.out.println("    Body: " + response.body());

        // Manual Authorization header (Bearer token example)
        System.out.println("\n  Manual Authorization header:");
        HttpRequest tokenRequest = HttpRequest.newBuilder()
                .uri(URI.create(TEST_URL + "/bearer"))
                .header("Authorization", "Bearer my-secret-token")
                .build();

        HttpResponse<String> tokenResponse = client.send(tokenRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("    Status: " + tokenResponse.statusCode());

        System.out.println("""

        Authentication approaches:
        - Authenticator: For Basic/Digest auth (automatic)
        - Manual header: For Bearer tokens, API keys
        - Custom: Implement your own auth flow
        """);
    }

    // ===== Cookie Handling =====

    static void cookieHandling() throws IOException, InterruptedException {
        System.out.println("=== Cookie Handling ===");

        // Client with cookie manager
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        HttpClient client = HttpClient.newBuilder().cookieHandler(cookieManager).build();

        // First request - sets cookies
        HttpRequest setCookieRequest = HttpRequest.newBuilder()
                .uri(URI.create(TEST_URL + "/cookies/set/session/abc123"))
                .build();

        HttpResponse<String> response1 = client.send(setCookieRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("  Set cookie request status: " + response1.statusCode());

        // Show cookies in manager
        System.out.println("  Cookies stored:");
        cookieManager
                .getCookieStore()
                .getCookies()
                .forEach(cookie -> System.out.println("    " + cookie.getName() + " = " + cookie.getValue()));

        // Second request - cookies sent automatically
        HttpRequest getCookieRequest =
                HttpRequest.newBuilder().uri(URI.create(TEST_URL + "/cookies")).build();

        HttpResponse<String> response2 = client.send(getCookieRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("  Get cookies response: " + response2.body().trim());

        System.out.println("""

        Cookie policies:
        - ACCEPT_ALL: Accept all cookies
        - ACCEPT_ORIGINAL_SERVER: Only from original server
        - ACCEPT_NONE: Reject all cookies
        """);
    }

    // ===== File Download =====

    static void fileDownload() throws IOException, InterruptedException {
        System.out.println("=== File Download ===");

        HttpClient client = HttpClient.newHttpClient();

        // Download to file
        Path downloadPath = Files.createTempFile("download-", ".json");

        HttpRequest request =
                HttpRequest.newBuilder().uri(URI.create(TEST_URL + "/json")).build();

        HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(downloadPath));

        System.out.println("  Downloaded to: " + response.body());
        System.out.println("  File size: " + Files.size(downloadPath) + " bytes");
        System.out.println("  Content preview: " + truncate(Files.readString(downloadPath), 80));

        // Download with progress (using InputStream)
        System.out.println("\n  Download with InputStream (for progress):");
        HttpResponse<java.io.InputStream> streamResponse =
                client.send(request, HttpResponse.BodyHandlers.ofInputStream());

        try (var input = streamResponse.body()) {
            byte[] data = input.readAllBytes();
            System.out.println("    Read " + data.length + " bytes");
        }

        // Clean up
        Files.delete(downloadPath);

        System.out.println();
    }

    // ===== File Upload =====

    static void fileUpload() throws IOException, InterruptedException {
        System.out.println("=== File Upload ===");

        HttpClient client = HttpClient.newHttpClient();

        // Create temp file to upload
        Path uploadFile = Files.createTempFile("upload-", ".txt");
        Files.writeString(uploadFile, "This is the file content to upload.");

        // Upload file
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TEST_URL + "/post"))
                .header("Content-Type", "text/plain")
                .POST(HttpRequest.BodyPublishers.ofFile(uploadFile))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("  File upload status: " + response.statusCode());
        System.out.println("  Response (truncated): " + truncate(response.body(), 100));

        // Multipart form data (manual construction)
        System.out.println("\n  Multipart form data (simplified example):");
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        String multipartBody = """
        ------WebKitFormBoundary%s
        Content-Disposition: form-data; name="field1"

        value1
        ------WebKitFormBoundary%s
        Content-Disposition: form-data; name="field2"

        value2
        ------WebKitFormBoundary%s--
        """.formatted(
                        String.valueOf(System.currentTimeMillis()),
                        String.valueOf(System.currentTimeMillis()),
                        String.valueOf(System.currentTimeMillis()));

        // Note: For real multipart uploads, consider using a library like Apache HttpComponents

        // Clean up
        Files.delete(uploadFile);

        System.out.println("""

        BodyPublishers for uploads:
        - ofFile(Path): Upload file content
        - ofString(String): Upload text
        - ofByteArray(byte[]): Upload binary data
        - ofInputStream(Supplier): Streaming upload
        - noBody(): Empty body
        """);
    }

    // ===== Error Handling =====

    static void errorHandling() {
        System.out.println("=== Error Handling ===");

        HttpClient client =
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

        // 1. Handle HTTP error status codes
        System.out.println("  HTTP 404 handling:");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TEST_URL + "/status/404"))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // HTTP errors don't throw exceptions - check status code
            if (response.statusCode() >= 400) {
                System.out.println("    HTTP error: " + response.statusCode());
            }
        } catch (Exception e) {
            System.out.println("    Exception: " + e.getMessage());
        }

        // 2. Connection errors
        System.out.println("\n  Connection error handling:");
        try {
            HttpRequest badRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://nonexistent.invalid/"))
                    .timeout(Duration.ofSeconds(2))
                    .build();

            client.send(badRequest, HttpResponse.BodyHandlers.ofString());
        } catch (java.net.http.HttpConnectTimeoutException e) {
            System.out.println("    Connect timeout: " + e.getMessage());
        } catch (java.net.http.HttpTimeoutException e) {
            System.out.println("    Request timeout: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("    IO error (expected): " + e.getClass().getSimpleName());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("    Interrupted");
        }

        // 3. Async error handling
        System.out.println("\n  Async error handling:");
        HttpRequest asyncRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://nonexistent.invalid/"))
                .timeout(Duration.ofSeconds(2))
                .build();

        String result = client.sendAsync(asyncRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(r -> "Success: " + r.statusCode())
                .exceptionally(ex -> "Error: " + ex.getCause().getClass().getSimpleName())
                .join();

        System.out.println("    " + result);

        System.out.println("""

        Error handling summary:
        - HTTP 4xx/5xx: Not exceptions, check statusCode()
        - Connection failures: IOException
        - Timeouts: HttpTimeoutException subclasses
        - Async: Use exceptionally() or handle()
        """);
    }

    // ===== Timeout Patterns =====

    static void timeoutPatterns() throws IOException, InterruptedException {
        System.out.println("=== Timeout Patterns ===");

        // Connection timeout (on client)
        HttpClient client =
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

        // Request timeout (per request)
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TEST_URL + "/get"))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("  Request completed: " + response.statusCode());

        // Async with timeout
        System.out.println("\n  Async with CompletableFuture timeout:");
        CompletableFuture<HttpResponse<String>> future =
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        try {
            HttpResponse<String> asyncResponse =
                    future.orTimeout(5, TimeUnit.SECONDS).join();
            System.out.println("    Completed: " + asyncResponse.statusCode());
        } catch (CompletionException e) {
            System.out.println("    Timeout or error: " + e.getCause().getMessage());
        }

        System.out.println("""

        Timeout types:
        - connectTimeout: Time to establish connection
        - request timeout: Total time for request/response
        - CompletableFuture.orTimeout(): Async deadline
        """);
    }

    // ===== Best Practices =====

    static void bestPractices() {
        System.out.println("=== Best Practices ===");

        System.out.println("""
        HttpClient Best Practices:

        1. REUSE THE CLIENT
           - Create once, use for all requests
           - Thread-safe, handles connection pooling
           - Don't create per request

        2. HANDLE ERRORS PROPERLY
           - Check response.statusCode() for HTTP errors
           - Catch IOException for network errors
           - Use exceptionally() for async

        3. SET TIMEOUTS
           - Always set connectTimeout on client
           - Set per-request timeout for slow endpoints
           - Use orTimeout() for async deadlines

        4. CLOSE RESOURCES
           - InputStream body handlers: close the stream
           - Stream<String> from ofLines(): close the stream
           - File downloads: files are auto-closed

        5. USE APPROPRIATE BODY HANDLERS
           - ofString(): Small text responses
           - ofFile(): Large downloads
           - ofInputStream(): When you need streaming
           - discarding(): When you only need status

        6. HTTP/2 CONSIDERATIONS
           - Client automatically negotiates version
           - Multiplexing benefits concurrent requests
           - Falls back to HTTP/1.1 gracefully

        7. AUTHENTICATION
           - Use Authenticator for Basic auth
           - Manual headers for Bearer/API keys
           - Store credentials securely

        8. TESTING
           - Mock the HttpClient for unit tests
           - Use WireMock or similar for integration tests
        """);
    }

    // ===== Helper =====

    private static String truncate(String s, int maxLen) {
        if (s == null) return "null";
        s = s.replace("\n", " ").replace("\r", "");
        return s.length() <= maxLen ? s : s.substring(0, maxLen) + "...";
    }
}
