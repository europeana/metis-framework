package eu.europeana.metis.mediaprocessing.http;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.of;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;


class MimeTypeDetectHttpClientTest {

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
  void download_detectMimeType(String resourcePath, String expectedMimeType)
      throws IOException, URISyntaxException {

    // Read file bytes
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
      assertNotNull(inputStream, "Test resource not found: " + resourcePath);
      byte[] fileBytes = inputStream.readAllBytes();

      wireMockExtension.stubFor(get("/path_to_file").willReturn(
          aResponse()
              .withStatus(200)
              .withBody(fileBytes)
              .withHeader("Content-Disposition",
                  "inline; filename=\"" + extractFileName(resourcePath) + "\"")
      ));
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
        of("__files/audio_test.mp3", "audio/mpeg"),
        //LAS
        of("__files/3d/cube_8_points.las", "application/vnd.las"),
        //LAZ
        of("__files/3d/cube_8_points.laz", "application/vnd.laszip"),
        //E57
        of("__files/3d/cube_8_points.e57", "model/e57"),
        //DXF
        of("__files/3d/cube.dxf", "image/vnd.dxf"),
        //OBJ
        of("__files/3d/cube.obj", "model/obj"),
        //DAE
        of("__files/3d/cube.dae", "model/vnd.collada+xml"),
        //STL
        of("__files/3d/cube-ascii.stl", "model/x.stl-ascii"),
        of("__files/3d/cube-binary.stl", "model/x.stl-binary"),
        of("__files/3d/cube-binary.stl", "model/x.stl-binary"),
        //IGES
        of("__files/3d/cube.iges", "model/iges"),
        //STEP
        of("__files/3d/cube.stp", "model/step"),
        of("__files/3d/cube.stpz", "model/step+zip"),
        //VRML
        of("__files/3d/cube.vrml", "model/vrml"),
        //X3D
        of("__files/3d/cube.x3d", "model/x3d+xml"),
        of("__files/3d/cube.x3dv", "model/x3d-vrml"),
        //GLTF
        of("__files/3d/cube.gltf", "model/gltf+json"),
        of("__files/3d/cube.glb", "model/gltf-binary"),
        //OEMBED
        of("__files/oembed.json", "application/json"),
        of("__files/oembed.xml", "application/xml"),
        //USD
        of("__files/3d/cube.usda", "model/vnd.usda"),
        of("__files/3d/cube.usdz", "model/vnd.usdz+zip")
    );
  }
}
