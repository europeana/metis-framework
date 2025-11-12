package eu.europeana.metis.mediaprocessing.http;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.of;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;


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
    String resultUrl = mimeTypeDetectHttpClient.getResourceUrl(new URI("http://localhost").toURL());
    assertEquals("http://localhost", resultUrl);
  }

  @ParameterizedTest(name = "{index} => file={0}, expected={1}")
  @MethodSource
  void download_detectMimeType(String resourcePath, String expectedMimeType, Map<String, String> extraHeaders)
      throws IOException, URISyntaxException {

    // Read file bytes
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
      assertNotNull(inputStream, "Test resource not found: " + resourcePath);
      byte[] fileBytes = inputStream.readAllBytes();

      // Build the base response
      ResponseDefinitionBuilder responseDefinitionBuilder = aResponse()
          .withStatus(200)
          .withBody(fileBytes)
          .withHeader("Content-Disposition", "inline; filename=\"" + extractFileName(resourcePath) + "\"");

      // Add optional headers if provided
      if (extraHeaders != null) {
        extraHeaders.forEach(responseDefinitionBuilder::withHeader);
      }

      wireMockExtension.stubFor(get("/path_to_file").willReturn(responseDefinitionBuilder));
    }

    String url = String.format("http://localhost:%d/path_to_file",
        wireMockExtension.getPort());
    String detectedMimeType = mimeTypeDetectHttpClient.download(new URI(url).toURL());

    assertEquals(expectedMimeType, detectedMimeType);
  }

  private static String extractFileName(String path) {
    return path.substring(path.lastIndexOf('/') + 1);
  }

  private static Stream<Arguments> download_detectMimeType() {
    return Stream.of(
        //MPEG
        of("__files/audio_test.mp3", EXPECTED_AUDIO_MIME_TYPE, Map.of("Content-Type", "audio/mpeg")),
        //STL
        of("__files/3d/block100.stl", "model/x.stl-ascii", null),
        of("__files/3d/cube.stl", "model/x.stl-binary", null),
        of("__files/3d/cube.stl", "model/x.stl-binary", Map.of("Content-Type", "model/stl")),
        //LAS
        of("__files/3d/cube_8_points.las", "application/vnd.las", null),
        //LAZ
        of("__files/3d/cube_8_points.laz", "application/vnd.laszip", null),
        //GLTF
        of("__files/3d/adamHead.gltf", "model/gltf+json", null),
        of("__files/3d/Duck.glb", "model/gltf-binary", null),
        //OEMBED
        of("__files/oembed.json", "application/json", null),
        of("__files/oembed.xml", "application/xml", null)
    );
  }
}
