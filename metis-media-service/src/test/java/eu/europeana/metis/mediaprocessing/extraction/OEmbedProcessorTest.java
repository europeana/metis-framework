package eu.europeana.metis.mediaprocessing.extraction;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static eu.europeana.metis.mediaprocessing.extraction.oembed.OEmbedModel.isValidOEmbedPhotoOrVideo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import eu.europeana.metis.mediaprocessing.MediaProcessorFactory;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.extraction.oembed.OEmbedModel;
import eu.europeana.metis.mediaprocessing.http.ResourceDownloadClient;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class OEmbedProcessorTest {

  @RegisterExtension
  static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
                                                                .options(wireMockConfig()
                                                                    .dynamicPort()
                                                                    .dynamicHttpsPort())
                                                                .build();
  private OEmbedProcessor processor;
  private ResourceDownloadClient resourceDownloadClient;

  private OEmbedResourceTest getOEmbedResourceTest(String filename, String detectedMimeType) throws IOException {
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename)) {
      byte[] audioBytes = inputStream.readAllBytes();
      wireMockExtension.stubFor(get("/api/resource?url=https://vimeo.com/24416915").willReturn(aResponse()
          .withStatus(200)
          .withBody(audioBytes)
          .withHeader("Content-Disposition", "inline; filename=\"oembed.xml\"")));
    }
    final String resourceUrl = String.format("http://localhost:%d/api/resource?url=https://vimeo.com/24416915",
        wireMockExtension.getPort());

    final RdfResourceEntry rdfResourceEntry = new RdfResourceEntry(resourceUrl, Collections.singletonList(UrlType.IS_SHOWN_BY));
    final Resource resource = resourceDownloadClient.downloadBasedOnMimeType(rdfResourceEntry);
    return new OEmbedResourceTest(resourceUrl, detectedMimeType, resource);
  }

  private record OEmbedResourceTest(String resourceUrl, String detectedMimeType, Resource resource) {

  }

  @BeforeEach
  void setUp() {
    processor = new OEmbedProcessor();
    resourceDownloadClient = new ResourceDownloadClient(MediaProcessorFactory.DEFAULT_MAX_REDIRECT_COUNT, download -> true,
        MediaProcessorFactory.DEFAULT_RESOURCE_CONNECT_TIMEOUT,
        MediaProcessorFactory.DEFAULT_RESOURCE_RESPONSE_TIMEOUT,
        MediaProcessorFactory.DEFAULT_RESOURCE_DOWNLOAD_TIMEOUT);
  }

  @Test
  void extractMetadata() throws MediaExtractionException, IOException {
    // given
    OEmbedResourceTest oembedResource = getOEmbedResourceTest("__files/oembed.xml", "application/xml+oembed");
    // when
    ResourceExtractionResult resourceExtractionResult = processor.extractMetadata(oembedResource.resource(),
        oembedResource.detectedMimeType(), true);

    // then
    assertNotNull(resourceExtractionResult);
    assertEquals(oembedResource.resourceUrl(), resourceExtractionResult.getMetadata().getResourceUrl());
    assertEquals(oembedResource.detectedMimeType(), resourceExtractionResult.getMetadata().getMimeType());
  }

  @Test
  void copyMetadataWithOEmbed_expectNull() throws MediaExtractionException, IOException {
    // given
    OEmbedResourceTest oembedResource = getOEmbedResourceTest("__files/oembed.xml", "application/xml+oembed");
    // when
    ResourceExtractionResult resourceExtractionResult = processor.copyMetadata(oembedResource.resource,
        oembedResource.detectedMimeType);
    // then
    assertNull(resourceExtractionResult);
  }

  @Test
  void copyMetadataNotOEmbed_expectObject() throws MediaExtractionException, IOException {
    // given
    OEmbedResourceTest oembedResource = getOEmbedResourceTest("__files/not_oembed.xml", "application/xml");
    // when
    ResourceExtractionResult resourceExtractionResult = processor.copyMetadata(oembedResource.resource,
        oembedResource.detectedMimeType);
    // then
    assertNull(resourceExtractionResult);
  }

  @Test
  void downloadResourceForFullProcessing() {
    assertTrue(processor.downloadResourceForFullProcessing());
  }
}
