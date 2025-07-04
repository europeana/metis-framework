package eu.europeana.metis.mediaprocessing.extraction.iiif;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.http.JvmProxyConfigurer;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.RdfResourceKind;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
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

  private static WireMockServer wireMockServer;
  private static IIIFValidation iiifValidation;

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
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/pct:10,10,10,10/0/default.jpg",
            false),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/pct:80/max/0/default.jpg", false),
        Arguments.of("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/pct:10,10,10,10/0/default.jpg",
            false)
    );
  }

  @BeforeAll
  static void createWireMock() {
    iiifValidation = new IIIFValidation();
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
  void fetchInfoJsonV2() throws IOException {
    InputStream inputStreamInfoJson = getClass().getClassLoader().getResourceAsStream("__files/iiif_info_v2.json");
    assertNotNull(inputStreamInfoJson);
    String infoJson = new String(inputStreamInfoJson.readAllBytes());

    wireMockServer.stubFor(get(urlEqualTo("/image/iiif/zw031pj2507/zw031pj2507_0001/info.json"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(infoJson)
            .withStatus(HttpStatus.OK.value())));

    RdfResourceEntry resourceEntry = new RdfResourceEntry(
        "http://localhost:" + wireMockServer.port() + "/image/iiif/zw031pj2507/zw031pj2507_0001/full/full/0/default.jpg",
        Set.of(),
        RdfResourceKind.IIIF);
    IIIFInfoJsonV2 model = (IIIFInfoJsonV2) iiifValidation.fetchInfoJson(resourceEntry);
    assertNotNull(model);
    assertEquals("http://iiif.io/api/image/2/context.json", model.getContext());
    assertEquals("https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001", model.getId());
    assertEquals("http://iiif.io/api/image", model.getProtocol());
    assertEquals(3088, model.getHeight());
    assertEquals(3710, model.getWidth());
    assertIterableEquals(List.of(new Tile(1024, 1024, List.of(1, 2, 4, 8, 16, 32))), model.getTiles());
    assertIterableEquals(List.of(new Size(116, 97), new Size(232, 193),
        new Size(464, 386), new Size(928, 772),
        new Size(1855, 1544), new Size(3710, 3088),
        new Size(3710, 3088)), model.getSizes());
    assertEquals("http://iiif.io/api/image/2/level2.json", model.getProfile().getUrl());
  }

  @Test
  void fetchInfoJsonV2WithProfile() throws IOException {
    InputStream inputStreamInfoJson = getClass().getClassLoader().getResourceAsStream("__files/iiif_info_v2_with_profile.json");
    assertNotNull(inputStreamInfoJson);
    String infoJson = new String(inputStreamInfoJson.readAllBytes());

    wireMockServer.stubFor(get(urlEqualTo("/image/b20432033_B0008608.JP2/info.json"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(infoJson)
            .withStatus(HttpStatus.OK.value())));

    RdfResourceEntry resourceEntry = new RdfResourceEntry(
        "http://localhost:" + wireMockServer.port() + "/image/b20432033_B0008608.JP2/full/full/0/default.jpg",
        Set.of(),
        RdfResourceKind.IIIF);
    IIIFInfoJsonV2 model = (IIIFInfoJsonV2) iiifValidation.fetchInfoJson(resourceEntry);

    assertNotNull(model);
    assertEquals("http://iiif.io/api/image/2/context.json", model.getContext());
    assertEquals("https://iiif.wellcomecollection.org/image/b20432033_B0008608.JP2", model.getId());
    assertEquals("http://iiif.io/api/image", model.getProtocol());
    assertEquals(2480, model.getHeight());
    assertEquals(3543, model.getWidth());
    assertIterableEquals(List.of(new Tile(1024, 1024, List.of(1, 2, 4, 8, 16, 32))), model.getTiles());
    assertIterableEquals(List.of(new Size(1024, 717), new Size(400, 280),
        new Size(200, 140), new Size(100, 70)), model.getSizes());
    assertEquals("http://iiif.io/api/image/2/level2.json", model.getProfile().getUrl());
    assertIterableEquals(List.of("jpg", "tif", "gif", "png"), model.getProfile().getDetail().getFormats());
    assertIterableEquals(List.of("bitonal", "default", "gray", "color"), model.getProfile().getDetail().getQualities());
    assertIterableEquals(List.of("regionByPx", "sizeByW", "sizeByWhListed", "cors", "regionSquare", "sizeByDistortedWh",
        "canonicalLinkHeader", "sizeByConfinedWh", "sizeByPct", "jsonldMediaType", "regionByPct",
        "rotationArbitrary", "sizeByH", "baseUriRedirect", "rotationBy90s", "profileLinkHeader",
        "sizeByForcedWh", "sizeByWh", "mirroring"), model.getProfile().getDetail().getSupports());
  }

  @Test
  void fetchInfoJsonV3() throws IOException {
    InputStream inputStreamInfoJson = getClass().getClassLoader().getResourceAsStream("__files/iiif_info_v3.json");
    assertNotNull(inputStreamInfoJson);
    String infoJson = new String(inputStreamInfoJson.readAllBytes());

    wireMockServer.stubFor(get(urlEqualTo("/iiif/image/cb1813c2-cbed-4f0a-8b5b-3b31fda5619a/info.json"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(infoJson)
            .withStatus(HttpStatus.OK.value())));

    RdfResourceEntry resourceEntry = new RdfResourceEntry(
        "http://localhost:" + wireMockServer.port() + "/iiif/image/cb1813c2-cbed-4f0a-8b5b-3b31fda5619a/full/full/0/default.jpg",
        Set.of(),
        RdfResourceKind.IIIF);
    IIIFInfoJsonV3 model = (IIIFInfoJsonV3) iiifValidation.fetchInfoJson(resourceEntry);

    assertNotNull(model);
    assertEquals(List.of("http://example.org/extension/context1.json", "http://iiif.io/api/image/3/context.json"),
        model.getContext());
    assertEquals("https://media.getty.edu/iiif/image/cb1813c2-cbed-4f0a-8b5b-3b31fda5619a", model.getId());
    assertEquals("ImageService3", model.getType());
    assertEquals("http://iiif.io/api/image", model.getProtocol());

    assertEquals(5788, model.getWidth());
    assertEquals(8208, model.getHeight());
    assertEquals(5788, model.getMaxWidth());
    assertEquals(8208, model.getMaxHeight());
    assertEquals(47507904, model.getMaxArea());
    assertIterableEquals(List.of(new Tile(256, 256, List.of(1, 2, 4, 8, 16, 32))), model.getTiles());
    assertIterableEquals(List.of(new Size(180, 256), new Size(361, 513),
        new Size(723, 1026), new Size(1447, 2052), new Size(2894, 4104)), model.getSizes());
    assertEquals("level2", model.getProfile());
    assertIterableEquals(List.of("png", "gif"), model.getPreferredFormats());
    assertEquals("http://rightsstatements.org/vocab/InC-EDU/1.0/", model.getRights());
    assertIterableEquals(List.of("png", "gif", "pdf"), model.getExtraFormats());
    assertIterableEquals(List.of("native", "color", "gray", "bitonal"), model.getExtraQualities());
    assertIterableEquals(List.of("canonicalLinkHeader", "rotationArbitrary", "profileLinkHeader"), model.getExtraFeatures());
    assertIterableEquals(List.of(new IIIFLink("https://example.org/image1.xml",
            "Dataset", Map.of("en", List.of("Technical image metadata")), "text/xml", "https://example.org/profiles/imagedata")),
        model.getSeeAlso());
    assertIterableEquals(List.of(new IIIFLink("https://example.org/manifest/1",
        "Manifest", Map.of("en", List.of("A Book")), null, null)), model.getPartOf());
    assertIterableEquals(List.of(new IIIFLink("https://example.org/service/example",
        "Service", null, null, "https://example.org/docs/example-service.html")), model.getService());
  }

  @Test
  void adjustResourceEntryToSmallIIIF() throws IOException {
    final InputStream inputStreamInfoJson = getClass().getClassLoader().getResourceAsStream("__files/iiif_info_v2.json");
    assertNotNull(inputStreamInfoJson);
    final String infoJson = new String(inputStreamInfoJson.readAllBytes());

    wireMockServer.stubFor(get(urlEqualTo("/image/iiif/zw031pj2507/zw031pj2507_0001/info.json"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(infoJson)
            .withStatus(HttpStatus.OK.value())));
    final String url =
        "http://localhost:" + wireMockServer.port() + "/image/iiif/zw031pj2507/zw031pj2507_0001/full/full/0/default.jpg";

    RdfResourceEntry rdfResourceEntry = new RdfResourceEntry(url, Set.of(UrlType.IS_SHOWN_BY), RdfResourceKind.IIIF);
    IIIFInfoJson iiifInfoJson = iiifValidation.fetchInfoJson(rdfResourceEntry);
    RdfResourceEntry resourceEntry = iiifValidation.adjustResourceEntryToSmallIIIF(rdfResourceEntry, iiifInfoJson);
    assertNotNull(resourceEntry);
    assertTrue(resourceEntry.getResourceUrl().contains("/full/!400,400/0/default.jpg"));
    assertNotNull(iiifInfoJson);
  }
}
