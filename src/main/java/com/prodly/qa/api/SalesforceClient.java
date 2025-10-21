package com.prodly.qa.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prodly.qa.config.Config;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SalesforceClient {
    private static final Logger log = LoggerFactory.getLogger(SalesforceClient.class);
    private static final ObjectMapper om = new ObjectMapper();

    private String accessToken;
    private String baseUrl;

    private io.restassured.specification.RequestSpecification baseJson() {
        return RestAssured.given()
                .config(RestAssuredConfig.config().httpClient(
                        HttpClientConfig.httpClientConfig()
                                .setParam("http.connection.timeout", Config.HTTP_TIMEOUT_MS)
                                .setParam("http.socket.timeout", Config.HTTP_TIMEOUT_MS)
                ))
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken);
    }

    public void authenticate() {
        Map<String, String> form = new HashMap<>();
        form.put("grant_type", "client_credentials");
        form.put("client_id", Config.SF_CLIENT_ID);
        form.put("client_secret", Config.SF_CLIENT_SECRET);

        log.info("SF: authenticating to {}", Config.SF_TOKEN_URL);
        Response r = RestAssured.given()
                .config(RestAssuredConfig.config().httpClient(
                        HttpClientConfig.httpClientConfig()
                                .setParam("http.connection.timeout", Config.HTTP_TIMEOUT_MS)
                                .setParam("http.socket.timeout", Config.HTTP_TIMEOUT_MS)
                ))
                .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .baseUri(Config.SF_TOKEN_URL)
                .formParams(form)
                .post();

        if (r.statusCode() != 200) {
            log.error("SF auth failed: {}", r.asString());
            throw new RuntimeException("SF auth failed: " + r.statusCode());
        }
        JsonNode json;
        try { json = om.readTree(r.asString()); } catch (Exception e) { throw new RuntimeException(e); }

        this.accessToken = json.get("access_token").asText();
        this.baseUrl = json.get("instance_url").asText();
        log.info("SF: auth OK, instance {}", baseUrl);
    }

    private String apiV() { return baseUrl + "/services/data/v60.0"; }
    private String sObject(String type) { return apiV() + "/sobjects/" + type + "/"; }

    public String createAccount(Map<String, Object> fields) {
        log.info("SF: create Account {}", fields.get("Name"));
        Response r = baseJson().body(fields).post(sObject("Account"));
        if (r.statusCode() != 201) {
            log.error("SF create failed: {}", r.asString());
            throw new RuntimeException("Create failed: " + r.statusCode() + " " + r.asString());
        }
        return r.jsonPath().getString("id");
    }

    public void updateAccount(String id, Map<String, Object> fields) {
        log.info("SF: update Account {}", id);
        Response r = baseJson().body(fields).patch(sObject("Account") + id);
        if (r.statusCode() != 204) {
            log.error("SF update failed: {}", r.asString());
            throw new RuntimeException("Update failed for " + id + ": " + r.statusCode() + " " + r.asString());
        }
    }

    public void deleteAccount(String id) {
        log.info("SF: delete Account {}", id);
        Response r = baseJson().delete(sObject("Account") + id);
        if (r.statusCode() != 204 && r.statusCode() != 404) {
            log.error("SF delete failed: {}", r.asString());
            throw new RuntimeException("Delete failed for " + id + ": " + r.statusCode() + " " + r.asString());
        }
    }

    public List<Map<String,Object>> queryAccountsByNamePrefix(String prefix) {
        String soql = "SELECT Id, Name, BillingCountry, NumberOfEmployees, Phone FROM Account WHERE Name LIKE '" + prefix + "%'";
        Response r = RestAssured.given()
                .config(RestAssuredConfig.config().httpClient(
                        HttpClientConfig.httpClientConfig()
                                .setParam("http.connection.timeout", Config.HTTP_TIMEOUT_MS)
                                .setParam("http.socket.timeout", Config.HTTP_TIMEOUT_MS)
                ))
                .baseUri(apiV() + "/query")
                .header("Authorization","Bearer " + accessToken)
                .queryParam("q", soql)
                .get();
        if (r.statusCode() != 200) {
            log.error("SF query failed: {}", r.asString());
            throw new RuntimeException("Query failed: " + r.asString());
        }
        return r.jsonPath().getList("records");
    }
}
