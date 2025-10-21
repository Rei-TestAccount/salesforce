package com.prodly.qa.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Minimal .env loader with System.getenv() fallback.
 */
public class Env {
    private static final Map<String,String> PROPS = new HashMap<>();

    static {
        // load .env if present at project root
        File f = new File(".env");
        if (f.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    int idx = line.indexOf('=');
                    if (idx > 0) {
                        String k = line.substring(0, idx).trim();
                        String v = line.substring(idx + 1).trim();
                        PROPS.put(k, v);
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    public static String get(String key) {
        String v = System.getenv(key);
        if (v != null && !v.isBlank()) return v;
        return PROPS.get(key);
    }

    public static int getInt(String key, int def) {
        String v = get(key);
        if (v == null) return def;
        try { return Integer.parseInt(v); } catch (Exception e) { return def; }
    }
}
