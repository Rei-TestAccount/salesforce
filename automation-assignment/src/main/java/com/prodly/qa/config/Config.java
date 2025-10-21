package com.prodly.qa.config;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Config {
    public static final String SF_INSTANCE_URL = Env.get("SF_INSTANCE_URL");
    public static final String SF_CLIENT_ID = Env.get("SF_CLIENT_ID");
    public static final String SF_CLIENT_SECRET = Env.get("SF_CLIENT_SECRET");
    public static final String SF_TOKEN_URL = SF_INSTANCE_URL + "/services/oauth2/token";

    public static final String GITHUB_OWNER = Env.get("GITHUB_OWNER");
    public static final String GITHUB_REPO = Env.get("GITHUB_REPO");
    public static final String GITHUB_TOKEN = Env.get("GITHUB_TOKEN");

    public static final int HTTP_TIMEOUT_MS = Env.getInt("HTTP_TIMEOUT_MS", 15000);

    public static final String RUN_ID = resolveRunId();

    private static String resolveRunId() {
        String env = Env.get("RUN_ID");
        if (env != null && !env.isBlank()) return env.trim();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "rei-" + today + "-autotest";
    }
}
