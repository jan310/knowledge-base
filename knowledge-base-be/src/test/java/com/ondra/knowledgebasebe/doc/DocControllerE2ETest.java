package com.ondra.knowledgebasebe.doc;

import io.restassured.RestAssured;
import org.bson.types.Binary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
class DocControllerE2ETest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    static class GotenbergContainer extends GenericContainer<DocControllerE2ETest.GotenbergContainer> {
        public GotenbergContainer(String image) {
            super(image);
        }
    }
    @Container
    static DocControllerE2ETest.GotenbergContainer gotenbergContainer = new DocControllerE2ETest.GotenbergContainer("gotenberg/gotenberg:7.10")
        .withExposedPorts(3000);

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("gotenberg.port", gotenbergContainer::getFirstMappedPort);
    }

    //In order for this variable to be initialized correctly, the environment variables "CLIENT_ID_1" and
    // "CLIENT_SECRET_1" must be set for the test run configuration.
    private static final String bearerTokenUser1 = "Bearer " + getAccessToken(
        System.getenv("CLIENT_ID_1"), System.getenv("CLIENT_SECRET_1")
    );
    private static final String userId1 = "32xSgSNZHBlvmDMyK8BUzg9ioA6BAgE0@clients";

    //In order for this variable to be initialized correctly, the environment variables "CLIENT_ID_1" and
    // "CLIENT_SECRET_1" must be set for the test run configuration.
    private static final String bearerTokenUser2 = "Bearer " + getAccessToken(
        System.getenv("CLIENT_ID_2"), System.getenv("CLIENT_SECRET_2")
    );
    private static final String userId2 = "3Vp2rfLvoBiLg1WFNuEoiDqdcIPoMggq@clients";

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

    @Autowired
    private DocRepository docRepository;

    @BeforeEach
    void setup(@LocalServerPort int port) {
        RestAssured.port = port;
        docRepository.deleteAll();
    }

    @Nested
    class Security {

        @Test
        void shouldRejectRequestIfUnauthorized() {
            get("/test").then().statusCode(401);
        }

    }

    @Nested
    class AddDoc {

        @Test
        void shouldCreateDoc() throws IOException {
            byte[] docxFileBytes = Files.readAllBytes(Paths.get("src/test/resources/test1.docx"));

            String id = given()
                .header("Authorization", bearerTokenUser1)
                .multiPart("docxFile", "file.docx", docxFileBytes, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                .formParam("name", "Java")
                .when()
                .post("/api/v1/docs")
                .then()
                .assertThat()
                .statusCode(201)
                .body("$", hasKey("id"))
                .body("userId", equalTo(userId1))
                .body("name", equalTo("Java"))
                .body("size()", equalTo(3))
                .extract().body().jsonPath().getString("id");

            List<Doc> docs = docRepository.findAll();
            assertEquals(1, docs.size());
            assertEquals(id, docs.getFirst().getId());
            assertEquals(userId1, docs.getFirst().getUserId());
            assertEquals("Java", docs.getFirst().getName());
            assertEquals(new Binary(docxFileBytes), docs.getFirst().getDocxFile());
            assertNotEquals(null, docs.getFirst().getPdfFile());
        }

        @Test
        void shouldNotCreateDocIfNameAlreadyExists() throws IOException {
            byte[] docxFileBytes = Files.readAllBytes(Paths.get("src/test/resources/test1.docx"));
            byte[] pdfFileBytes = Files.readAllBytes(Paths.get("src/test/resources/test1.pdf"));
            docRepository.save(new Doc(null, userId1, "Java", new Binary(docxFileBytes), new Binary(pdfFileBytes)));

            given()
                .header("Authorization", bearerTokenUser1)
                .multiPart("docxFile", "file.docx", docxFileBytes, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                .formParam("name", "Java")
                .when()
                .post("/api/v1/docs")
                .then()
                .assertThat()
                .statusCode(409)
                .body(emptyString());

            List<Doc> docs = docRepository.findAll();
            assertEquals(1, docs.size());
        }

    }

    @Nested
    class GetAllDocs {

        @Test
        void shouldOnlyReturnDocsOfRequestingUser() throws IOException {
            byte[] docxFileBytes1 = Files.readAllBytes(Paths.get("src/test/resources/test1.docx"));
            byte[] pdfFileBytes1 = Files.readAllBytes(Paths.get("src/test/resources/test1.pdf"));
            docRepository.save(new Doc(null, userId1, "Java", new Binary(docxFileBytes1), new Binary(pdfFileBytes1)));
            byte[] docxFileBytes2 = Files.readAllBytes(Paths.get("src/test/resources/test2.docx"));
            byte[] pdfFileBytes2 = Files.readAllBytes(Paths.get("src/test/resources/test2.pdf"));
            docRepository.save(new Doc(null, userId1, "Kotlin", new Binary(docxFileBytes2), new Binary(pdfFileBytes2)));
            byte[] docxFileBytes3 = Files.readAllBytes(Paths.get("src/test/resources/test3.docx"));
            byte[] pdfFileBytes3 = Files.readAllBytes(Paths.get("src/test/resources/test3.pdf"));
            docRepository.save(new Doc(null, userId2, "Python", new Binary(docxFileBytes3), new Binary(pdfFileBytes3)));

            given()
                .header("Authorization", bearerTokenUser1)
                .when()
                .get("/api/v1/docs")
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("[0].userId", equalTo(userId1))
                .body("[0].name", equalTo("Java"))
                .body("[1].userId", equalTo(userId1))
                .body("[1].name", equalTo("Kotlin"));
        }

    }

    @Nested
    class GetPdf {

        @Test
        void shouldReturnPdf() throws IOException {
            byte[] docxFileBytes = Files.readAllBytes(Paths.get("src/test/resources/test1.docx"));
            byte[] pdfFileBytes = Files.readAllBytes(Paths.get("src/test/resources/test1.pdf"));
            String id = docRepository.save(new Doc(null, userId1, "Java", new Binary(docxFileBytes), new Binary(pdfFileBytes))).getId();

            byte[] pdf = given()
                .header("Authorization", bearerTokenUser1)
                .when()
                .get("/api/v1/docs/" + id + "/pdf")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType("application/pdf")
                .extract().asByteArray();

            assertArrayEquals(pdfFileBytes, pdf);
        }

        @Test
        void shouldNotReturnPdfIfPdfDoesNotExist() {
            given()
                .header("Authorization", bearerTokenUser1)
                .when()
                .get("/api/v1/docs/non-existent-id/pdf")
                .then()
                .assertThat()
                .statusCode(404)
                .body(emptyString());
        }

        @Test
        void shouldNotReturnPdfIfPdfDoesNotBelongToRequestingUser() throws IOException {
            byte[] docxFileBytes = Files.readAllBytes(Paths.get("src/test/resources/test1.docx"));
            byte[] pdfFileBytes = Files.readAllBytes(Paths.get("src/test/resources/test1.pdf"));
            String id = docRepository.save(new Doc(null, userId1, "Java", new Binary(docxFileBytes), new Binary(pdfFileBytes))).getId();

            given()
                .header("Authorization", bearerTokenUser2)
                .when()
                .get("/api/v1/docs/" + id + "/pdf")
                .then()
                .assertThat()
                .statusCode(404)
                .body(emptyString());
        }

    }

    @Nested
    class GetDocx {

        @Test
        void shouldReturnDocx() throws IOException {
            byte[] docxFileBytes = Files.readAllBytes(Paths.get("src/test/resources/test1.docx"));
            byte[] pdfFileBytes = Files.readAllBytes(Paths.get("src/test/resources/test1.pdf"));
            String id = docRepository.save(new Doc(null, userId1, "Java", new Binary(docxFileBytes), new Binary(pdfFileBytes))).getId();

            byte[] docx = given()
                .header("Authorization", bearerTokenUser1)
                .when()
                .get("/api/v1/docs/" + id + "/docx")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                .extract().asByteArray();

            assertArrayEquals(docxFileBytes, docx);
        }

        @Test
        void shouldNotReturnDocxIfDocxDoesNotExist() {
            given()
                .header("Authorization", bearerTokenUser1)
                .when()
                .get("/api/v1/docs/non-existent-id/docx")
                .then()
                .assertThat()
                .statusCode(404)
                .body(emptyString());
        }

        @Test
        void shouldNotReturnDocxIfDocxDoesNotBelongToRequestingUser() throws IOException {
            byte[] docxFileBytes = Files.readAllBytes(Paths.get("src/test/resources/test1.docx"));
            byte[] pdfFileBytes = Files.readAllBytes(Paths.get("src/test/resources/test1.pdf"));
            String id = docRepository.save(new Doc(null, userId1, "Java", new Binary(docxFileBytes), new Binary(pdfFileBytes))).getId();

            given()
                .header("Authorization", bearerTokenUser2)
                .when()
                .get("/api/v1/docs/" + id + "/docx")
                .then()
                .assertThat()
                .statusCode(404)
                .body(emptyString());
        }

    }

    @Nested
    class RenameDoc {

        @Test
        void shouldRenameDoc() throws IOException {
            byte[] docxFileBytes = Files.readAllBytes(Paths.get("src/test/resources/test1.docx"));
            byte[] pdfFileBytes = Files.readAllBytes(Paths.get("src/test/resources/test1.pdf"));
            String id = docRepository.save(new Doc(null, userId1, "Java", new Binary(docxFileBytes), new Binary(pdfFileBytes))).getId();

            given()
                .header("Authorization", bearerTokenUser1)
                .formParam("name", "Kotlin")
                .when()
                .patch("/api/v1/docs/" + id + "/rename")
                .then()
                .assertThat()
                .statusCode(202)
                .body("$", hasKey("id"))
                .body("userId", equalTo(userId1))
                .body("name", equalTo("Kotlin"))
                .body("size()", equalTo(3));

            List<Doc> docs = docRepository.findAll();
            assertEquals(1, docs.size());
            assertEquals(id, docs.getFirst().getId());
            assertEquals(userId1, docs.getFirst().getUserId());
            assertEquals("Kotlin", docs.getFirst().getName());
            assertEquals(new Binary(docxFileBytes), docs.getFirst().getDocxFile());
            assertNotEquals(null, docs.getFirst().getPdfFile());
        }

        @Test
        void shouldNotRenameDocIfDocDoesNotExist() {
            given()
                .header("Authorization", bearerTokenUser1)
                .formParam("name", "Kotlin")
                .when()
                .patch("/api/v1/docs/non-existent-id/rename")
                .then()
                .assertThat()
                .statusCode(404)
                .body(emptyString());
        }

        @Test
        void shouldNotRenameDocIfDocWithSameNameAlreadyExists() throws IOException {
            byte[] docxFileBytes1 = Files.readAllBytes(Paths.get("src/test/resources/test1.docx"));
            byte[] pdfFileBytes1 = Files.readAllBytes(Paths.get("src/test/resources/test1.pdf"));
            String id = docRepository.save(new Doc(null, userId1, "Java", new Binary(docxFileBytes1), new Binary(pdfFileBytes1))).getId();

            byte[] docxFileBytes2 = Files.readAllBytes(Paths.get("src/test/resources/test2.docx"));
            byte[] pdfFileBytes2 = Files.readAllBytes(Paths.get("src/test/resources/test2.pdf"));
            docRepository.save(new Doc(null, userId1, "Kotlin", new Binary(docxFileBytes2), new Binary(pdfFileBytes2)));

            given()
                .header("Authorization", bearerTokenUser1)
                .formParam("name", "Kotlin")
                .when()
                .patch("/api/v1/docs/" + id + "/rename")
                .then()
                .assertThat()
                .statusCode(409)
                .body(emptyString());

            List<Doc> docs = docRepository.findAll();
            assertEquals(2, docs.size());
            assertEquals(id, docs.getFirst().getId());
            assertEquals(userId1, docs.getFirst().getUserId());
            assertEquals("Java", docs.getFirst().getName());
            assertEquals(new Binary(docxFileBytes1), docs.getFirst().getDocxFile());
            assertNotEquals(null, docs.getFirst().getPdfFile());
        }

        @Test
        void shouldNotRenameDocIfDocDoesNotBelongToRequestingUser() throws IOException {
            byte[] docxFileBytes = Files.readAllBytes(Paths.get("src/test/resources/test1.docx"));
            byte[] pdfFileBytes = Files.readAllBytes(Paths.get("src/test/resources/test1.pdf"));
            String id = docRepository.save(new Doc(null, userId1, "Java", new Binary(docxFileBytes), new Binary(pdfFileBytes))).getId();

            given()
                .header("Authorization", bearerTokenUser2)
                .formParam("name", "Kotlin")
                .when()
                .patch("/api/v1/docs/" + id + "/rename")
                .then()
                .assertThat()
                .statusCode(404)
                .body(emptyString());

            List<Doc> docs = docRepository.findAll();
            assertEquals(1, docs.size());
            assertEquals(id, docs.getFirst().getId());
            assertEquals(userId1, docs.getFirst().getUserId());
            assertEquals("Java", docs.getFirst().getName());
            assertEquals(new Binary(docxFileBytes), docs.getFirst().getDocxFile());
            assertNotEquals(null, docs.getFirst().getPdfFile());
        }

    }

    @Nested
    class ReplaceFile {

        @Test
        void shouldReplaceFile() throws IOException {
            byte[] docxFileBytes1 = Files.readAllBytes(Paths.get("src/test/resources/test1.docx"));
            byte[] pdfFileBytes1 = Files.readAllBytes(Paths.get("src/test/resources/test1.pdf"));
            String id = docRepository.save(new Doc(null, userId1, "Java", new Binary(docxFileBytes1), new Binary(pdfFileBytes1))).getId();

            byte[] docxFileBytes2 = Files.readAllBytes(Paths.get("src/test/resources/test2.docx"));

            given()
                .header("Authorization", bearerTokenUser1)
                .multiPart("docxFile", "file.docx", docxFileBytes2, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                .formParam("name", "Java")
                .when()
                .patch("/api/v1/docs/" + id + "/replace-file")
                .then()
                .assertThat()
                .statusCode(202)
                .body("$", hasKey("id"))
                .body("userId", equalTo(userId1))
                .body("name", equalTo("Java"))
                .body("size()", equalTo(3));

            List<Doc> docs = docRepository.findAll();
            assertEquals(1, docs.size());
            assertEquals(id, docs.getFirst().getId());
            assertEquals(userId1, docs.getFirst().getUserId());
            assertEquals("Java", docs.getFirst().getName());
            assertEquals(new Binary(docxFileBytes2), docs.getFirst().getDocxFile());
            assertNotEquals(null, docs.getFirst().getPdfFile());
        }

        @Test
        void shouldNotReplaceFileIfDocDoesNotExist() throws IOException {
            byte[] docxFileBytes = Files.readAllBytes(Paths.get("src/test/resources/test2.docx"));

            given()
                .header("Authorization", bearerTokenUser1)
                .multiPart("docxFile", "file.docx", docxFileBytes, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                .formParam("name", "Java")
                .when()
                .patch("/api/v1/docs/non-existent-id/replace-file")
                .then()
                .assertThat()
                .statusCode(404)
                .body(emptyString());

            List<Doc> docs = docRepository.findAll();
            assertEquals(0, docs.size());
        }

        @Test
        void shouldNotReplaceFileIfDocDoesNotBelongToRequestingUser() throws IOException {
            byte[] docxFileBytes1 = Files.readAllBytes(Paths.get("src/test/resources/test1.docx"));
            byte[] pdfFileBytes1 = Files.readAllBytes(Paths.get("src/test/resources/test1.pdf"));
            String id = docRepository.save(new Doc(null, userId1, "Java", new Binary(docxFileBytes1), new Binary(pdfFileBytes1))).getId();

            byte[] docxFileBytes2 = Files.readAllBytes(Paths.get("src/test/resources/test2.docx"));

            given()
                .header("Authorization", bearerTokenUser2)
                .multiPart("docxFile", "file.docx", docxFileBytes2, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                .formParam("name", "Java")
                .when()
                .patch("/api/v1/docs/" + id + "/replace-file")
                .then()
                .assertThat()
                .statusCode(404)
                .body(emptyString());

            List<Doc> docs = docRepository.findAll();
            assertEquals(1, docs.size());
            assertEquals(id, docs.getFirst().getId());
            assertEquals(userId1, docs.getFirst().getUserId());
            assertEquals("Java", docs.getFirst().getName());
            assertEquals(new Binary(docxFileBytes1), docs.getFirst().getDocxFile());
            assertNotEquals(null, docs.getFirst().getPdfFile());
        }

    }

    @Nested
    class DeleteDoc {

        @Test
        void shouldDeleteDoc() throws IOException {
            byte[] docxFileBytes = Files.readAllBytes(Paths.get("src/test/resources/test1.docx"));
            byte[] pdfFileBytes = Files.readAllBytes(Paths.get("src/test/resources/test1.pdf"));
            String id = docRepository.save(new Doc(null, userId1, "Java", new Binary(docxFileBytes), new Binary(pdfFileBytes))).getId();

            given()
                .header("Authorization", bearerTokenUser1)
                .when()
                .delete("/api/v1/docs/" + id)
                .then()
                .assertThat()
                .statusCode(204)
                .body(emptyString());

            List<Doc> docs = docRepository.findAll();
            assertEquals(0, docs.size());
        }

        @Test
        void shouldNotDeleteDocIfDocDoesNotExist() {
            given()
                .header("Authorization", bearerTokenUser1)
                .when()
                .delete("/api/v1/docs/non-existent-id")
                .then()
                .assertThat()
                .statusCode(404)
                .body(emptyString());
        }

        @Test
        void shouldNotDeleteDocIfDocDoesNotBelongToRequestingUser() throws IOException {
            byte[] docxFileBytes = Files.readAllBytes(Paths.get("src/test/resources/test1.docx"));
            byte[] pdfFileBytes = Files.readAllBytes(Paths.get("src/test/resources/test1.pdf"));
            String id = docRepository.save(new Doc(null, userId1, "Java", new Binary(docxFileBytes), new Binary(pdfFileBytes))).getId();

            given()
                .header("Authorization", bearerTokenUser2)
                .when()
                .delete("/api/v1/docs/" + id)
                .then()
                .assertThat()
                .statusCode(404)
                .body(emptyString());

            List<Doc> docs = docRepository.findAll();
            assertEquals(1, docs.size());
            assertEquals(id, docs.getFirst().getId());
            assertEquals(userId1, docs.getFirst().getUserId());
            assertEquals("Java", docs.getFirst().getName());
            assertEquals(new Binary(docxFileBytes), docs.getFirst().getDocxFile());
            assertNotEquals(null, docs.getFirst().getPdfFile());
        }

    }

}