package com.ondra.knowledgebasebe.doc;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.ondra.knowledgebasebe.exceptionhandling.exceptions.FileConversionException;
import org.bson.types.Binary;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.multipart.MultipartFile;

import static com.github.tomakehurst.wiremock.client.WireMock.aMultipart;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@WireMockTest(httpPort = 3001)
class FileConversionServiceWireMockTest {

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("gotenberg.port", () -> "3001");
    }

    @Autowired
    private FileConversionService fileConversionService;

    @Nested
    class ConvertDocxMultipartFileToPdfBinary {

        @Test
        void successfulConversion() {
            byte[] docxFile = new byte[]{0, 1, 2};
            byte[] pdfFile = new byte[]{3, 4, 5};
            stubFor(post("/forms/libreoffice/convert")
                .withMultipartRequestBody(
                    aMultipart().withBody(binaryEqualTo(docxFile))
                )
                .willReturn(
                    aResponse().withStatus(200).withBody(pdfFile)
                )
            );

            MultipartFile multipartFile = new MockMultipartFile("docxFile", docxFile);
            Binary result = fileConversionService.convertDocxMultipartFileToPdfBinary(multipartFile);

            assertThat(result.getData()).isEqualTo(pdfFile);
            verify(
                postRequestedFor(urlPathEqualTo("/forms/libreoffice/convert"))
                    .withRequestBodyPart(aMultipart().withBody(binaryEqualTo(docxFile)).build())
            );
        }

        @Test
        void failedConversion() {
            byte[] docxFile = new byte[]{0, 1, 2};
            stubFor(post("/forms/libreoffice/convert")
                .willReturn(
                    aResponse().withStatus(503)
                )
            );

            MultipartFile multipartFile = new MockMultipartFile("docxFile", docxFile);

            assertThatThrownBy(() -> fileConversionService.convertDocxMultipartFileToPdfBinary(multipartFile)).isInstanceOf(FileConversionException.class);
            verify(
                postRequestedFor(urlPathEqualTo("/forms/libreoffice/convert"))
                    .withRequestBodyPart(aMultipart().withBody(binaryEqualTo(docxFile)).build())
            );

        }

    }

}