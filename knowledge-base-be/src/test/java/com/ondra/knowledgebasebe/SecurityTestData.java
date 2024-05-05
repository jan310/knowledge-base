package com.ondra.knowledgebasebe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestClient;

import java.util.Base64;

import static org.springframework.http.MediaType.APPLICATION_JSON;

public class SecurityTestData {

    public static final String BEARER_TOKEN_USER_1 = "Bearer " + getAccessToken(
        System.getenv("CLIENT_ID_1"), System.getenv("CLIENT_SECRET_1")
    );

    public static final String BEARER_TOKEN_USER_2 = "Bearer " + getAccessToken(
        System.getenv("CLIENT_ID_2"), System.getenv("CLIENT_SECRET_2")
    );

    public static final String USER_ID_1 = getUserIdFromBearerToken(BEARER_TOKEN_USER_1);

    public static final String USER_ID_2 = getUserIdFromBearerToken(BEARER_TOKEN_USER_2);

    private static String getAccessToken(String clientId, String clientSecret) {
        record GetAccessTokenRequestBody(String client_id, String client_secret, String audience, String grant_type) {}
        record GetAccessTokenResponseBody(String access_token, String expires_in, String token_type) {}

        var requestBody = new GetAccessTokenRequestBody(
            clientId,
            clientSecret,
            "https://knowledge-base-api/",
            "client_credentials"
        );

        RestClient restClient = RestClient.create();
        var responseBody = restClient
            .post()
            .uri("https://jan-ondra.eu.auth0.com/oauth/token")
            .contentType(APPLICATION_JSON)
            .body(requestBody)
            .retrieve()
            .body(GetAccessTokenResponseBody.class);

        return responseBody.access_token;
    }

    private static String getUserIdFromBearerToken(String bearerToken) {
        String jwt = bearerToken.replace("Bearer ", "");
        String jwtPayload = jwt.split("\\.")[1];
        String decodedJwtPayload = new String(Base64.getDecoder().decode(jwtPayload));
        JsonNode jsonNode;
        try {
            jsonNode = new ObjectMapper().readTree(decodedJwtPayload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return jsonNode.path("sub").asText();
    }

}
