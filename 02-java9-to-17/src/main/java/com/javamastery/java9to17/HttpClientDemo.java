package com.javamastery.java9to17;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Demonstrates the new HttpClient API introduced in Java 11.
 *
 * Features: HTTP/2 support, async requests, WebSocket support.
 * Replaces the legacy HttpURLConnection API.
 */
public class HttpClientDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== HttpClient Demo (Java 11) ===\n");

        // --- 1. Create HttpClient (builder pattern, reusable) ---
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)       // prefer HTTP/2
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        // --- 2. Synchronous GET request ---
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://httpbin.org/get"))
                .GET()
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(15))
                .build();

        System.out.println("Sending synchronous GET to httpbin.org/get ...");
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Status: " + response.statusCode());
        System.out.println("Body (truncated): " + response.body().substring(0, Math.min(200, response.body().length())));

        // --- 3. Asynchronous GET request ---
        HttpRequest asyncRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://httpbin.org/delay/1"))
                .GET()
                .build();

        System.out.println("\nSending async GET...");
        client.sendAsync(asyncRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::statusCode)
                .thenAccept(status -> System.out.println("Async status: " + status))
                .join(); // wait for completion in main

        // --- 4. POST request ---
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://httpbin.org/post"))
                .POST(HttpRequest.BodyPublishers.ofString("{\"key\":\"value\"}"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("\nPOST status: " + postResponse.statusCode());

        System.out.println("\nNote: This demo requires network access. Run it as a standalone program.");
    }
}
