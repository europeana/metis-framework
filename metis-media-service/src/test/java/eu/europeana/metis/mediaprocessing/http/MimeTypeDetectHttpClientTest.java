package eu.europeana.metis.mediaprocessing.http;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.http.ContentDisposition;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MimeTypeDetectHttpClientTest {
    private static final String EXPECTED_MIME_TYPE = "audio/mpeg";
    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().dynamicHttpsPort())
            .build();

    private MimeTypeDetectHttpClient mimeTypeDetectHttpClient
            = new MimeTypeDetectHttpClient(5000, 5000, 5000);

    @Test
    void getResourceUrl() throws MalformedURLException {
        // when
        String resultUrl = mimeTypeDetectHttpClient.getResourceUrl(new URL("http://localhost"));

        // then
        assertEquals("http://localhost", resultUrl);
    }

    @Test
    void download_withContentDisposition_expectSuccess() throws IOException {
        // given
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("__files/audio_test.mp3")) {
            byte[] audioBytes = inputStream.readAllBytes();
            wireMockExtension.stubFor(get("/imagen_id.do?idImagen=10610909").willReturn(aResponse()
                    .withStatus(200)
                    .withBody(audioBytes)
                    .withHeader("Content-Disposition", "inline; filename=\"F34-0003_0001.mp3\"")
                    .withHeader("Content-Type", "audio/mpeg")));
        }
        final String url = String.format("http://localhost:%d/imagen_id.do?idImagen=10610909", wireMockExtension.getPort());
        // when
        String detectedMimeType = mimeTypeDetectHttpClient.download(new URL(url));

        // then
        assertEquals(EXPECTED_MIME_TYPE, detectedMimeType);
    }

}
