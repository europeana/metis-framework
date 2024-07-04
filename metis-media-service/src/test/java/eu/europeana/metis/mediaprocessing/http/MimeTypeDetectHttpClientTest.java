package eu.europeana.metis.mediaprocessing.http;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.web.util.UriComponentsBuilder;


class MimeTypeDetectHttpClientTest {
    private static final String EXPECTED_AUDIO_MIME_TYPE = "audio/mpeg";
    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().dynamicHttpsPort())
            .build();

  private final MimeTypeDetectHttpClient mimeTypeDetectHttpClient
      = new MimeTypeDetectHttpClient(5000, 5000, 5000);

  @Test
  void getResourceUrl() throws MalformedURLException, URISyntaxException {
    // when
    String resultUrl = mimeTypeDetectHttpClient.getResourceUrl(new URI("http://localhost").toURL());

    // then
    assertEquals("http://localhost", resultUrl);
  }

  @Test
  void download_withContentDisposition_expectSuccess() throws IOException, URISyntaxException {
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
    String detectedMimeType = mimeTypeDetectHttpClient.download(new URI(url).toURL());

        // then
        assertEquals(EXPECTED_AUDIO_MIME_TYPE, detectedMimeType);
    }

    @Test
    void download_detectMimeTypeStlAscii_expectSuccess() throws IOException, URISyntaxException {
        // given
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("__files/block100.stl")) {
            byte[] fileBytes = inputStream.readAllBytes();
            wireMockExtension.stubFor(get("/imagen_id.do?idImagen=10610909").willReturn(aResponse()
                .withStatus(200)
                .withBody(fileBytes)
                .withHeader("Content-Disposition", "inline; filename=\"block100.stl\"")));
        }
        final String url = String.format("http://localhost:%d/imagen_id.do?idImagen=10610909", wireMockExtension.getPort());
        // when
        String detectedMimeType = mimeTypeDetectHttpClient.download(new URI(url).toURL());

        // then
        assertEquals("model/x.stl-ascii", detectedMimeType);
    }

    @Test
    void download_detectMimeTypeStlBinary_expectSuccess() throws IOException, URISyntaxException {
        // given
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("__files/Cube_3d_printing_sample.stl")) {
            byte[] fileBytes = inputStream.readAllBytes();
            wireMockExtension.stubFor(get("/imagen_id.do?idImagen=10610909").willReturn(aResponse()
                .withStatus(200)
                .withBody(fileBytes)
                .withHeader("Content-Disposition", "inline; filename=\"Cube_3d_printing_sample.stl\"")));
        }
        final String url = String.format("http://localhost:%d/imagen_id.do?idImagen=10610909", wireMockExtension.getPort());
        // when
        String detectedMimeType = mimeTypeDetectHttpClient.download(new URI(url).toURL());

        // then
        assertEquals("model/x.stl-binary", detectedMimeType);
    }

    @Test
    void download_returnProvidedStlMimeType_expectSuccess() throws IOException, URISyntaxException {
        // given
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("__files/Cube_3d_printing_sample.stl")) {
            byte[] fileBytes = inputStream.readAllBytes();
            wireMockExtension.stubFor(get("/imagen_id.do?idImagen=10610909").willReturn(aResponse()
                .withStatus(200)
                .withBody(fileBytes)
                .withHeader("Content-Disposition", "inline; filename=\"Cube_3d_printing_sample.stl\"")
                .withHeader("Content-Type", "model/stl")));
        }
        final String url = String.format("http://localhost:%d/imagen_id.do?idImagen=10610909", wireMockExtension.getPort());
        // when
        String detectedMimeType = mimeTypeDetectHttpClient.download(new URI(url).toURL());

        // then
        assertEquals("model/x.stl-binary", detectedMimeType);
    }

  @Test
  void download_detectMimeTypeGltf_expectSuccess() throws IOException, URISyntaxException {
    // given
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("__files/adamHead.gltf")) {
      byte[] audioBytes = inputStream.readAllBytes();
      wireMockExtension.stubFor(get("/imagen_id.do?idImagen=10610909").willReturn(aResponse()
          .withStatus(200)
          .withBody(audioBytes)
          .withHeader("Content-Disposition", "inline; filename=\"adamHead.gltf\"")));
    }
    final String url = String.format("http://localhost:%d/imagen_id.do?idImagen=10610909", wireMockExtension.getPort());
    // when
    String detectedMimeType = mimeTypeDetectHttpClient.download(new URI(url).toURL());

    // then
    assertEquals("model/gltf+json", detectedMimeType);
  }

  @Test
  void download_detectMimeTypeGlb_expectSuccess() throws IOException, URISyntaxException {
    // given
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("__files/Duck.glb")) {
      byte[] audioBytes = inputStream.readAllBytes();
      wireMockExtension.stubFor(get("/imagen_id.do?idImagen=10610909").willReturn(aResponse()
          .withStatus(200)
          .withBody(audioBytes)
          .withHeader("Content-Disposition", "inline; filename=\"Duck.glb\"")));
    }
    final String url = String.format("http://localhost:%d/imagen_id.do?idImagen=10610909", wireMockExtension.getPort());
    // when
    String detectedMimeType = mimeTypeDetectHttpClient.download(new URI(url).toURL());

    // then
    assertEquals("model/gltf-binary", detectedMimeType);
  }

  @Test
  void download_detectMimeTypeOembedJson_expectSuccess() throws IOException, URISyntaxException {
    // given
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("__files/oembed.json")) {
      byte[] audioBytes = inputStream.readAllBytes();
      wireMockExtension.stubFor(get("/api/oembed.json?url=https://vimeo.com/24416915").willReturn(aResponse()
          .withStatus(200)
          .withBody(audioBytes)
          .withHeader("Content-Disposition", "inline; filename=\"oembed.json\"")));
    }
    final String url = String.format("http://localhost:%d/api/oembed.json?url=https://vimeo.com/24416915", wireMockExtension.getPort());
    // when
    String detectedMimeType = mimeTypeDetectHttpClient.download(new URI(url).toURL());

    // then
    assertEquals("application/json", detectedMimeType);
  }

  @Test
  void download_detectMimeTypeOembedXml_expectSuccess() throws IOException, URISyntaxException {
    // given
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("__files/oembed.xml")) {
      byte[] audioBytes = inputStream.readAllBytes();
      wireMockExtension.stubFor(get("/api/oembed.xml?url=https://vimeo.com/24416915").willReturn(aResponse()
          .withStatus(200)
          .withBody(audioBytes)
          .withHeader("Content-Disposition", "inline; filename=\"oembed.xml\"")));
    }
    final String url = String.format("http://localhost:%d/api/oembed.xml?url=https://vimeo.com/24416915", wireMockExtension.getPort());
    // when
    String detectedMimeType = mimeTypeDetectHttpClient.download(new URI(url).toURL());

    // then
    assertEquals("application/xml", detectedMimeType);
  }
}
