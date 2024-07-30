package eu.europeana.metis.mediaprocessing.extraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import eu.europeana.metis.mediaprocessing.model.ResourceImpl;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LinkedProcessorTest {

  private LinkedProcessor linkedProcessor;
  private OEmbedProcessor oEmbedProcessor;
  private TextProcessor textProcessor;

  @BeforeEach
  void setUp() {
    oEmbedProcessor = spy(new OEmbedProcessor());
    ThumbnailGenerator thumbnailGenerator = mock(ThumbnailGenerator.class);
    PdfToImageConverter pdfToImageConverter = mock(PdfToImageConverter.class);
    textProcessor = spy(new TextProcessor(thumbnailGenerator, pdfToImageConverter));
    linkedProcessor = new LinkedProcessor(List.of(oEmbedProcessor, textProcessor));
  }

  @Disabled
  @Test
  void extractMetadataFromOEmbedProcessor() throws MediaExtractionException {
    String mimeType = "application/xml+oembed";
    String resourceUrl = "http://example.com";
    final RdfResourceEntry rdfResourceEntry = new RdfResourceEntry(resourceUrl, Collections.singletonList(UrlType.IS_SHOWN_BY));
    final ResourceImpl resource = spy(
        new ResourceImpl(rdfResourceEntry, mimeType, null, URI.create(resourceUrl)));
    doReturn(resourceUrl).when(resource).getResourceUrl();
    doReturn(Paths.get(getClass().getClassLoader().getResource("__files/oembed.xml").getPath()))
        .when(resource).getContentPath();

    ResourceExtractionResult result = linkedProcessor.extractMetadata(resource, mimeType, false);

    assertNotNull(result);
    assertEquals(resourceUrl, result.getMetadata().getResourceUrl());
    assertEquals(mimeType, result.getMetadata().getMimeType());
    verify(oEmbedProcessor, times(1)).extractMetadata(any(Resource.class), anyString(), anyBoolean());

    verify(textProcessor, times(0)).extractMetadata(any(Resource.class), anyString(), anyBoolean());
  }

  @Disabled
  @Test
  void extractMetadataFromTextProcessor() throws MediaExtractionException, IOException {
    Resource resource = mock(Resource.class);

    String mimeType = "mime type";
    String url = "http://example.com";
    doReturn(url).when(resource).getResourceUrl();
    doReturn(true).when(resource).hasContent();

    ResourceExtractionResult result = linkedProcessor.extractMetadata(resource, mimeType, false);
    assertNotNull(result);
    assertEquals(url, result.getMetadata().getResourceUrl());
    assertEquals(mimeType, result.getMetadata().getMimeType());
    verify(oEmbedProcessor, times(1)).extractMetadata(any(Resource.class), anyString(), anyBoolean());

    verify(textProcessor, times(1)).extractMetadata(any(Resource.class), anyString(), anyBoolean());
  }

  @Disabled
  @Test
  void copyMetadataFromTextProcessor() throws MediaExtractionException {
    Resource resource = mock(Resource.class);

    String mimeType = "mime type";
    String url = "http://example.com";
    doReturn(url).when(resource).getResourceUrl();

    ResourceExtractionResult result = linkedProcessor.copyMetadata(resource, mimeType);
    assertNotNull(result);
    assertEquals(url, result.getMetadata().getResourceUrl());
    assertEquals(mimeType, result.getMetadata().getMimeType());
    verify(oEmbedProcessor, times(1)).copyMetadata(any(Resource.class), anyString());

    verify(textProcessor, times(1)).copyMetadata(any(Resource.class), anyString());
  }

  @Disabled
  @Test
  void copyMetadataFromOEmbedProcessor() throws MediaExtractionException {
    Resource resource = mock(Resource.class);
    String mimeType = "application/json+oembed";

    ResourceExtractionResult result = linkedProcessor.copyMetadata(resource, mimeType);

    assertNull(result);
    verify(oEmbedProcessor, times(1)).copyMetadata(any(Resource.class), anyString());

    verify(textProcessor, times(0)).copyMetadata(any(Resource.class), anyString());
  }

  @Test
  void downloadResourceForFullProcessing() {
    assertTrue(linkedProcessor.downloadResourceForFullProcessing());
  }

}
