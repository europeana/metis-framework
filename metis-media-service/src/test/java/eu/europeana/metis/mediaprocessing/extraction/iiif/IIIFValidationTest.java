package eu.europeana.metis.mediaprocessing.extraction.iiif;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.http.JvmProxyConfigurer;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.extraction.iiif.IIIFInfoJson.Size;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.RdfResourceKind;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;

class IIIFValidationTest {

  private static Stream<Arguments> providedIIIFUrls() {
    return Stream.of(
        // valid
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/full/0/default.jpg", true),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/square/max/0/default.jpg", true),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/125,15,120,140/max/0/default.jpg", true),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/pct:41.6,7.5,40,70/max/0/default.jpg", true),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/125,15,200,200/max/0/default.jpg", true),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/pct:41.6,7.5,66.6,100/max/0/default.jpg", true),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/max/0/default.jpg", true),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/^max/0/default.jpg", true),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/150,/0/default.jpg", true),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/^360,/0/default.jpg", true),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/,150/0/default.jpg", true),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/,^240/0/default.jpg", true),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/pct:50/0/default.jpg", true),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/pct:120/0/default.jpg", true),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/225,100/0/default.jpg", true),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/^360,360/0/default.jpg", true),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/^!360,360/0/default.jpg", true),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/max/180/default.jpg", true),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/max/90/default.jpg", true),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/max/22.5/default.jpg", true),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/max/!0/default.jpg", true),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/max/!180/default.jpg", true),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/full/0/color.jpg", true),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/full/0/gray.jpg", true),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/full/0/bitonal.jpg", true),

        // invalid
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/full/0/bitonal.exe", false),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/full/^0/color.gif", false),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/^,100/0/color.gif", false),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/400,400/full/0/default.jpg", false),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/,400/full/0/default.jpg", false),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/250,/full/0/default.jpg", false),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/full/abc/default.jpg", false),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/!max/full/0/default.jpg", false),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/max/full/-20/default.jpg", false),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/max/90/20/gray.jpg", false),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/125,80,250/90,90/20/gray.jpg", false),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/3^60,360/0/default.jpg", false),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/pct:10,10,10,10/0/default.jpg", false),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/pct:80/max/0/default.jpg", false),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/pct:10,10,10,10/0/default.jpg", false)
    );
  }

  private static WireMockServer wireMockServer;

  @BeforeAll
  static void createWireMock() {
    wireMockServer = new WireMockServer(wireMockConfig()
        .dynamicPort()
        .enableBrowserProxying(true)
        .notifier(new ConsoleNotifier(true)));
    wireMockServer.start();
    JvmProxyConfigurer.configureFor(wireMockServer);
  }

  @AfterAll
  static void tearDownWireMock() {
    wireMockServer.stop();
  }

  @ParameterizedTest
  @MethodSource("providedIIIFUrls")
  void isIIIFUrl(String url, boolean expectedResult) {
        assertEquals(expectedResult, IIIFValidation.isIIIFUrl(url));
  }

  @Test
  void fetchInfoJson() throws MediaExtractionException, IOException {
    InputStream inputStreamInfoJson = getClass().getClassLoader().getResourceAsStream("__files/iiif_info_v2.json");
    String infoJson = new String(inputStreamInfoJson.readAllBytes());

    wireMockServer.stubFor(get(urlEqualTo("/image/iiif/zw031pj2507/zw031pj2507_0001/info.json"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(infoJson)
            .withStatus(HttpStatus.OK.value())));

    IIIFInfoJson model = IIIFValidation.fetchInfoJson(
        "http://localhost:" + wireMockServer.port() + "/image/iiif/zw031pj2507/zw031pj2507_0001/full/full/0/default.jpg");
    assertNotNull(model);
    assertEquals("http://iiif.io/api/image/2/context.json", model.getContext());
    assertEquals("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001", model.getId());
    assertEquals("http://iiif.io/api/image", model.getProtocol());
    assertNull( model.getType());
    assertEquals(3088, model.getHeight());
    assertEquals(3710, model.getWidth());
    assertIterableEquals(List.of(new IIIFInfoJson.Tile(1024,1024,List.of(1,2,4,8,16,32))),model.getTiles());
    assertIterableEquals(List.of(new Size(116,97), new Size(232,193),
        new Size(464,386), new Size(928,772),
        new Size(1855,1544), new Size(3710,3088),
        new Size(3710,3088)),model.getSizes());
    assertArrayEquals(new String[]{"http://iiif.io/api/image/2/level2.json"},model.getProfile().toArray());
  }

  @Test
  void fetchIIFSmallVersionOfResource() throws IOException, MediaExtractionException {
    final InputStream inputStreamInfoJson = getClass().getClassLoader().getResourceAsStream("__files/iiif_info_v2.json");
    final String infoJson = new String(inputStreamInfoJson.readAllBytes());

    wireMockServer.stubFor(get(urlEqualTo("/image/iiif/zw031pj2507/zw031pj2507_0001/info.json"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(infoJson)
            .withStatus(HttpStatus.OK.value())));
    final String url = "http://localhost:" + wireMockServer.port() + "/image/iiif/zw031pj2507/zw031pj2507_0001/full/full/0/default.jpg";
    RdfResourceEntry rdfResourceEntry = new RdfResourceEntry(url, Set.of(UrlType.IS_SHOWN_BY), RdfResourceKind.IIIF);
    RdfResourceEntry resourceEntry = IIIFValidation.fetchIIFSmallVersionOfResource(rdfResourceEntry);
    assertNotNull(resourceEntry);
  }
}
