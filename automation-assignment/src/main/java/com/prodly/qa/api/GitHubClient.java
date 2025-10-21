package com.prodly.qa.api;

import com.prodly.qa.config.Config;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * GitHub REST API client for file and branch operations.
 * Handles file create/update/delete and branch management with detailed logging.
 */
public class GitHubClient {
    private static final Logger log = LoggerFactory.getLogger(GitHubClient.class);
    private final String apiBase = "https://api.github.com";

    private io.restassured.specification.RequestSpecification base() {
        return RestAssured.given()
                .config(RestAssuredConfig.config().httpClient(
                        HttpClientConfig.httpClientConfig()
                                .setParam("http.connection.timeout", Config.HTTP_TIMEOUT_MS)
                                .setParam("http.socket.timeout", Config.HTTP_TIMEOUT_MS)
                ))
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + Config.GITHUB_TOKEN)
                .header("User-Agent", "Prodly-QA-Automation")
                .header("X-GitHub-Api-Version", "2022-11-28");
    }

    private String repoBase() {
        return apiBase + "/repos/" + Config.GITHUB_OWNER + "/" + Config.GITHUB_REPO;
    }

    /** Get SHA for a specific branch (used when creating new branches). */
    public String getDefaultBranchSha(String branch) {
        log.info("GitHub: get ref for branch {}", branch);
        Response r = base().get(repoBase() + "/git/ref/heads/" + branch);
        if (r.statusCode() != 200) {
            log.error("Failed to get ref: {}", r.asString());
            throw new RuntimeException("Get ref failed: " + r.asString());
        }
        return r.jsonPath().getString("object.sha");
    }

    /** Create a new branch from an existing one (if not exists). */
    public void createBranch(String newBranch, String fromBranch) {
        String sha = getDefaultBranchSha(fromBranch);
        Map<String, Object> body = new HashMap<>();
        body.put("ref", "refs/heads/" + newBranch);
        body.put("sha", sha);
        log.info("GitHub: create branch {} from {}", newBranch, fromBranch);
        Response r = base().body(body).post(repoBase() + "/git/refs");
        if (r.statusCode() != 201 && r.statusCode() != 422) {
            log.error("Create branch failed: {}", r.asString());
            throw new RuntimeException("Create branch failed: " + r.asString());
        }
    }

    /** Create or update a file in the given branch. */
    public void putFile(String branch, String path, byte[] content, String message) {
        String url = repoBase() + "/contents/" + path;
        Map<String, Object> body = new HashMap<>();
        body.put("message", message);
        body.put("content", Base64.getEncoder().encodeToString(content));
        body.put("branch", branch);

        // If file exists — include sha to update
        Response get = base().queryParam("ref", branch).get(url);
        if (get.statusCode() == 200) {
            String sha = get.jsonPath().getString("sha");
            body.put("sha", sha);
            log.info("GitHub: updating existing file {} on {}", path, branch);
        } else {
            log.info("GitHub: creating new file {} on {}", path, branch);
        }

        Response r = base().body(body).put(url);
        if (r.statusCode() != 201 && r.statusCode() != 200) {
            log.error("Put file failed: {}", r.asString());
            throw new RuntimeException("Put file failed: " + r.asString());
        }
    }

    /** Get (download) file contents from a branch. */
    public byte[] getFile(String branch, String path) {
        String url = repoBase() + "/contents/" + path;
        log.info("GitHub: download {} from {}", path, branch);
        Response r = base().queryParam("ref", branch).get(url);
        if (r.statusCode() != 200) {
            log.error("Get file failed: {}", r.asString());
            throw new RuntimeException("Get file failed: " + r.asString());
        }
        String b64 = r.jsonPath().getString("content");
        return Base64.getMimeDecoder().decode(b64.getBytes(StandardCharsets.UTF_8));
    }

    /** Delete file from a branch (used for cleanup). */
    public void deleteFile(String branch, String path, String message) {
        try {
            String url = repoBase() + "/contents/" + path;
            Response get = base().queryParam("ref", branch).get(url);
            if (get.statusCode() == 200) {
                String sha = get.jsonPath().getString("sha");
                Map<String, Object> body = new HashMap<>();
                body.put("message", message);
                body.put("sha", sha);
                body.put("branch", branch);
                Response del = base().body(body).delete(url);
                if (del.statusCode() == 200) {
                    log.info("GitHub: deleted file {} from {}", path, branch);
                } else {
                    log.warn("GitHub deleteFile failed: {}", del.asPrettyString());
                }
            } else {
                log.info("GitHub: file {} not found on {}, skip delete", path, branch);
            }
        } catch (Exception e) {
            log.warn("GitHub: deleteFile exception for {}: {}", path, e.getMessage());
        }
    }

    /** Delete a branch if it exists (used for cleanup). */
    public void deleteBranchIfExists(String branch) {
        try {
            String url = repoBase() + "/git/refs/heads/" + branch;
            Response get = base().get(url);
            if (get.statusCode() == 200) {
                Response del = base().delete(url);
                if (del.statusCode() == 204) {
                    log.info("GitHub: deleted branch {}", branch);
                } else {
                    log.warn("GitHub deleteBranch failed: {}", del.asPrettyString());
                }
            } else {
                log.info("GitHub: branch {} not found, skip delete", branch);
            }
        } catch (Exception e) {
            log.warn("GitHub: deleteBranch exception for {}: {}", branch, e.getMessage());
        }
    }
}