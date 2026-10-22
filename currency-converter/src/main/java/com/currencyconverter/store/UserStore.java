package com.currencyconverter.store;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserStore {

    private final Path dataDir;
    private final Path usersFile;
    private final Map<String, UserInfo> usersByEmail = new ConcurrentHashMap<>();
    private final Map<String, String> activeSessions = new ConcurrentHashMap<>(); // token -> email

    public static class UserInfo {
        public final String id;
        public final String name;
        public final String email;
        public final String passwordHash;
        public final String createdAt;

        public UserInfo(String id, String name, String email, String passwordHash, String createdAt) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.passwordHash = passwordHash;
            this.createdAt = createdAt;
        }

        public String toJson() {
            return String.format(
                "{\"id\":\"%s\",\"name\":\"%s\",\"email\":\"%s\",\"createdAt\":\"%s\"}",
                escapeJson(id), escapeJson(name), escapeJson(email), escapeJson(createdAt)
            );
        }

        private String escapeJson(String input) {
            if (input == null) return "";
            return input.replace("\\", "\\\\").replace("\"", "\\\"");
        }
    }

    public UserStore() {
        this.dataDir = Paths.get("data");
        try {
            if (!Files.exists(dataDir)) Files.createDirectories(dataDir);
        } catch (IOException e) {
            System.err.println("UserStore data directory error: " + e.getMessage());
        }
        this.usersFile = dataDir.resolve("users.json");
        loadUsers();
    }

    private void loadUsers() {
        if (Files.exists(usersFile)) {
            try {
                List<String> lines = Files.readAllLines(usersFile);
                usersByEmail.clear();
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (trimmed.endsWith(",")) {
                        trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
                    }
                    if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                        UserInfo u = parseUserJson(trimmed);
                        if (u != null) {
                            usersByEmail.put(u.email.toLowerCase(), u);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("UserStore load error: " + e.getMessage());
            }
        }
    }

    private synchronized void saveUsers() {
        try {
            StringBuilder sb = new StringBuilder("[\n");
            int count = 0;
            for (UserInfo u : usersByEmail.values()) {
                String json = String.format(
                    "{\"id\":\"%s\",\"name\":\"%s\",\"email\":\"%s\",\"passwordHash\":\"%s\",\"createdAt\":\"%s\"}",
                    escapeJson(u.id), escapeJson(u.name), escapeJson(u.email), escapeJson(u.passwordHash), escapeJson(u.createdAt)
                );
                sb.append("  ").append(json);
                if (++count < usersByEmail.size()) sb.append(",");
                sb.append("\n");
            }
            sb.append("]");
            Files.writeString(usersFile, sb.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("UserStore save error: " + e.getMessage());
        }
    }

    public synchronized String registerUser(String name, String email, String password) throws IllegalArgumentException {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name is required.");
        if (email == null || !email.contains("@") || !email.contains(".")) throw new IllegalArgumentException("Valid email address is required.");
        if (password == null || password.length() < 6) throw new IllegalArgumentException("Password must be at least 6 characters.");

        String lowerEmail = email.toLowerCase().trim();
        if (usersByEmail.containsKey(lowerEmail)) {
            throw new IllegalArgumentException("An account with this email address already exists.");
        }

        String id = UUID.randomUUID().toString().substring(0, 8);
        String hash = hashPassword(password);
        String createdAt = Instant.now().toString();

        UserInfo newUser = new UserInfo(id, name.trim(), lowerEmail, hash, createdAt);
        usersByEmail.put(lowerEmail, newUser);
        saveUsers();

        // Auto login & return session token
        String token = UUID.randomUUID().toString();
        activeSessions.put(token, lowerEmail);
        return token;
    }

    public synchronized String loginUser(String email, String password) throws IllegalArgumentException {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email is required.");
        if (password == null || password.isBlank()) throw new IllegalArgumentException("Password is required.");

        String lowerEmail = email.toLowerCase().trim();
        UserInfo user = usersByEmail.get(lowerEmail);

        if (user == null || !user.passwordHash.equals(hashPassword(password))) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        String token = UUID.randomUUID().toString();
        activeSessions.put(token, lowerEmail);
        return token;
    }

    public UserInfo getUserByToken(String token) {
        if (token == null) return null;
        String email = activeSessions.get(token);
        return email != null ? usersByEmail.get(email) : null;
    }

    public void logout(String token) {
        if (token != null) activeSessions.remove(token);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest((password + "CurrencyX_Salt_2026").getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return String.valueOf(password.hashCode());
        }
    }

    private UserInfo parseUserJson(String json) {
        try {
            String id = extractString(json, "id");
            String name = extractString(json, "name");
            String email = extractString(json, "email");
            String passwordHash = extractString(json, "passwordHash");
            String createdAt = extractString(json, "createdAt");
            if (id != null && email != null) {
                return new UserInfo(id, name != null ? name : "User", email, passwordHash != null ? passwordHash : "", createdAt != null ? createdAt : Instant.now().toString());
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private String extractString(String json, String key) {
        Pattern p = Pattern.compile("\"" + key + "\":\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : null;
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
