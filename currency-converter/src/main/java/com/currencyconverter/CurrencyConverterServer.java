package com.currencyconverter;

import com.currencyconverter.controller.ApiController;
import com.currencyconverter.service.AiAssistantService;
import com.currencyconverter.service.AlertEngine;
import com.currencyconverter.service.RateFetcherService;
import com.currencyconverter.store.AlertAndHistoryStore;
import com.currencyconverter.store.ExpenseStore;
import com.currencyconverter.store.UserStore;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

public class CurrencyConverterServer {

    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                // use default
            }
        }

        System.out.println("===============================================================");
        System.out.println("   GLOBAL CURRENCY CONVERTER WEB APPLICATION (JAVA 21)   ");
        System.out.println("===============================================================");

        // 1. Initialize Core Stores & Services
        AlertAndHistoryStore store = new AlertAndHistoryStore();
        UserStore userStore = new UserStore();
        ExpenseStore expenseStore = new ExpenseStore();
        RateFetcherService rateFetcherService = new RateFetcherService();
        AiAssistantService aiAssistantService = new AiAssistantService(rateFetcherService);

        AlertEngine alertEngine = new AlertEngine(rateFetcherService, store);
        alertEngine.start();

        // 2. Setup HTTP Server
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

            // API Route Handler
            ApiController apiController = new ApiController(
                rateFetcherService, store, userStore, expenseStore, aiAssistantService
            );
            server.createContext("/api", apiController);

            // Static Web Files Handler
            StaticFileHandler staticHandler = new StaticFileHandler();
            server.createContext("/", staticHandler);

            server.start();

            System.out.println("\n🚀 Server successfully started!");
            System.out.println("🌐 Access Application in Browser: http://localhost:" + port);
            System.out.println("📊 API Endpoint: http://localhost:" + port + "/api/rates");
            System.out.println("⚡ Press Ctrl+C to stop the server.\n");

        } catch (IOException e) {
            System.err.println("Fatal Error: Could not start HTTP server on port " + port + ": " + e.getMessage());
        }
    }

    private static class StaticFileHandler implements HttpHandler {

        private final Path staticDir;

        public StaticFileHandler() {
            Path[] candidates = new Path[]{
                Paths.get("src/main/resources/static").toAbsolutePath(),
                Paths.get("static").toAbsolutePath(),
                Paths.get("../src/main/resources/static").toAbsolutePath(),
                Paths.get("../static").toAbsolutePath()
            };

            Path resolved = null;
            for (Path candidate : candidates) {
                if (Files.exists(candidate) && Files.isDirectory(candidate)) {
                    resolved = candidate.normalize();
                    break;
                }
            }
            if (resolved == null) {
                resolved = Paths.get(".").toAbsolutePath().normalize();
            }
            this.staticDir = resolved;
            System.out.println("[StaticFileHandler] Serving static resources from: " + this.staticDir);
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();
            if (requestPath.equals("/")) {
                requestPath = "/index.html";
            }

            Path filePath = staticDir.resolve(requestPath.substring(1)).normalize();
            
            if (!filePath.startsWith(staticDir.normalize())) {
                sendError(exchange, 403, "Access Denied");
                return;
            }

            if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                String contentType = getMimeType(filePath.toString());
                byte[] content = Files.readAllBytes(filePath);

                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.getResponseHeaders().set("Cache-Control", "no-cache");
                exchange.sendResponseHeaders(200, content.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(content);
                }
            } else {
                Path indexPath = staticDir.resolve("index.html");
                if (Files.exists(indexPath)) {
                    byte[] content = Files.readAllBytes(indexPath);
                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
                    exchange.sendResponseHeaders(200, content.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(content);
                    }
                } else {
                    sendError(exchange, 404, "404 Not Found");
                }
            }
        }

        private String getMimeType(String fileName) {
            String lower = fileName.toLowerCase();
            if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html; charset=utf-8";
            if (lower.endsWith(".css")) return "text/css; charset=utf-8";
            if (lower.endsWith(".js")) return "application/javascript; charset=utf-8";
            if (lower.endsWith(".json")) return "application/json; charset=utf-8";
            if (lower.endsWith(".png")) return "image/png";
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
            if (lower.endsWith(".svg")) return "image/svg+xml";
            if (lower.endsWith(".ico")) return "image/x-icon";
            return "text/plain; charset=utf-8";
        }

        private void sendError(HttpExchange exchange, int status, String msg) throws IOException {
            byte[] bytes = msg.getBytes();
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}
