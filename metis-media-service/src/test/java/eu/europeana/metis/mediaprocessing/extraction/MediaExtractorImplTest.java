package eu.europeana.metis.mediaprocessing.extraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.extraction.MediaExtractorImpl.ProcessingMode;
import eu.europeana.metis.mediaprocessing.http.MimeTypeDetectHttpClient;
import eu.europeana.metis.mediaprocessing.http.ResourceDownloadClient;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.RdfResourceKind;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResultImpl;
import eu.europeana.metis.mediaprocessing.model.ResourceImpl;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import eu.europeana.metis.mediaprocessing.model.VideoResourceMetadata;
import eu.europeana.metis.mediaprocessing.wrappers.TikaWrapper;
import eu.europeana.metis.schema.model.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.tika.metadata.Metadata;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class MediaExtractorImplTest {

  private static ResourceDownloadClient resourceDownloadClient;
  private static MimeTypeDetectHttpClient mimeTypeDetectHttpClient;
  private static CommandExecutor commandExecutor;
  private static TikaWrapper tika;

  private static ImageProcessor imageProcessor;
  private static AudioVideoProcessor audioVideoProcessor;
  private static TextProcessor textProcessor;
  private static Media3dProcessor media3dProcessor;
  private static OEmbedProcessor oEmbedProcessor;
  private static IIIFProcessor iiifProcessor;
  private static MediaExtractorImpl mediaExtractor;

  private void testGetMode(ProcessingMode expected, Set<UrlType> urlTypes) {
    final RdfResourceEntry entry = new RdfResourceEntry("url string", new ArrayList<>(urlTypes), RdfResourceKind.STANDARD);
    assertEquals(expected, mediaExtractor.getMode(entry));
  }

  @BeforeAll
  static void prepare() {
    resourceDownloadClient = mock(ResourceDownloadClient.class);
    mimeTypeDetectHttpClient = mock(MimeTypeDetectHttpClient.class);
    commandExecutor = mock(CommandExecutor.class);
    tika = mock(TikaWrapper.class);
    imageProcessor = mock(ImageProcessor.class);
    audioVideoProcessor = mock(AudioVideoProcessor.class);
    textProcessor = mock(TextProcessor.class);
    media3dProcessor = mock(Media3dProcessor.class);
    oEmbedProcessor = mock(OEmbedProcessor.class);
    mediaExtractor = spy(new MediaExtractorImpl(resourceDownloadClient, mimeTypeDetectHttpClient,
        tika, List.of(imageProcessor, audioVideoProcessor, textProcessor, media3dProcessor, oEmbedProcessor)));
  }

  @BeforeEach
  void resetMocks() {
    reset(resourceDownloadClient, mimeTypeDetectHttpClient, commandExecutor, tika, imageProcessor,
        audioVideoProcessor, textProcessor, mediaExtractor, oEmbedProcessor);
  }

  @Test
  void testDetectAndVerifyMimeType() throws IOException, MediaExtractionException, URISyntaxException {

    // Create resource
    final URI actualLocation = new URI("http://resource.actual.location.test.com");
    final Path contentPath = Paths.get("content path");
    final Resource resource = mock(Resource.class);
    doReturn("resource url").when(resource).getResourceUrl();
    doReturn(actualLocation).when(resource).getActualLocation();
    final String providedMimeType = "mime type";
    doReturn(providedMimeType).when(resource).getProvidedMimeType();
    doReturn(contentPath).when(resource).getContentPath();

    // Register mime types
    final String detectedMimeTypeNoContent = "detected mime type no content";
    doReturn(false).when(mediaExtractor).shouldDownloadForFullProcessing(detectedMimeTypeNoContent, RdfResourceKind.STANDARD);
    final String detectedMimeTypeWithContent = "detected mime type with content";
    doReturn(true).when(mediaExtractor).shouldDownloadForFullProcessing(detectedMimeTypeWithContent, RdfResourceKind.STANDARD);

    // Test case where there is no content
    doReturn(false).when(resource).hasContent();
    doReturn(detectedMimeTypeNoContent).when(mimeTypeDetectHttpClient).download(actualLocation.toURL());
    assertEquals(detectedMimeTypeNoContent, mediaExtractor.detectAndVerifyMimeType(resource, ProcessingMode.FULL));
    assertEquals(detectedMimeTypeNoContent, mediaExtractor.detectAndVerifyMimeType(resource, ProcessingMode.REDUCED));

    // Test case where there is content
    doReturn(true).when(resource).hasContent();
    doReturn(detectedMimeTypeWithContent).when(mediaExtractor).detectType(contentPath, providedMimeType);
    assertEquals(detectedMimeTypeWithContent, mediaExtractor.detectAndVerifyMimeType(resource, ProcessingMode.FULL));
    assertEquals(detectedMimeTypeWithContent, mediaExtractor.detectAndVerifyMimeType(resource, ProcessingMode.REDUCED));

    // Test when tika throws exception
    doThrow(IOException.class).when(mediaExtractor).detectType(contentPath, providedMimeType);
    assertThrows(MediaExtractionException.class, () -> mediaExtractor.detectAndVerifyMimeType(resource, ProcessingMode.FULL));
    doReturn(detectedMimeTypeWithContent).when(mediaExtractor).detectType(contentPath, providedMimeType);

    // Check what happens if resource.hasContent() throws an exception.
    doThrow(IOException.class).when(resource).hasContent();
    assertThrows(MediaExtractionException.class, () -> mediaExtractor.detectAndVerifyMimeType(resource, ProcessingMode.FULL));
    doReturn(true).when(resource).hasContent();

    // Check what happens if we are not supposed to process
    assertThrows(IllegalStateException.class, () -> mediaExtractor.detectAndVerifyMimeType(resource, ProcessingMode.NONE));
  }

  @Test
  void testVerifyAndCorrectContentAvailability() throws MediaExtractionException, IOException {

    // Set up the resource
    final String location = "resource url";
    final Resource resource = mock(Resource.class);
    doReturn(location).when(resource).getResourceUrl();
    final Resource resourceWithContent = mock(Resource.class);
    final InputStream content = mock(InputStream.class);
    doReturn(content).when(resourceWithContent).getContentStream();

    // Register mime types
    final String detectedMimeTypeNoContent = "detected mime type no content";
    doReturn(false).when(mediaExtractor).shouldDownloadForFullProcessing(detectedMimeTypeNoContent, RdfResourceKind.STANDARD);
    final String detectedMimeTypeWithContent = "detected mime type with content";
    doReturn(true).when(mediaExtractor).shouldDownloadForFullProcessing(detectedMimeTypeWithContent, RdfResourceKind.STANDARD);

    // Test case where there is content regardless of whether there should be, or the processing
    // mode doesn't require content or the detected mime type doesn't require content.
    doReturn(true).when(resource).hasContent();
    doReturn(detectedMimeTypeWithContent).when(resource).getProvidedMimeType();
    mediaExtractor.verifyAndCorrectContentAvailability(resource, ProcessingMode.FULL, detectedMimeTypeWithContent, RdfResourceKind.STANDARD);
    mediaExtractor.verifyAndCorrectContentAvailability(resource, ProcessingMode.FULL, detectedMimeTypeNoContent, RdfResourceKind.STANDARD);
    mediaExtractor.verifyAndCorrectContentAvailability(resource, ProcessingMode.REDUCED, detectedMimeTypeWithContent, RdfResourceKind.STANDARD);
    mediaExtractor.verifyAndCorrectContentAvailability(resource, ProcessingMode.REDUCED, detectedMimeTypeNoContent, RdfResourceKind.STANDARD);
    doReturn(false).when(resource).hasContent();
    doReturn(detectedMimeTypeNoContent).when(resource).getProvidedMimeType();
    mediaExtractor.verifyAndCorrectContentAvailability(resource, ProcessingMode.FULL, detectedMimeTypeNoContent, RdfResourceKind.STANDARD);
    mediaExtractor.verifyAndCorrectContentAvailability(resource, ProcessingMode.REDUCED, detectedMimeTypeWithContent, RdfResourceKind.STANDARD);
    mediaExtractor.verifyAndCorrectContentAvailability(resource, ProcessingMode.REDUCED, detectedMimeTypeNoContent, RdfResourceKind.STANDARD);
    doReturn(false).when(resource).hasContent();
    doReturn(detectedMimeTypeWithContent).when(resource).getProvidedMimeType();
    mediaExtractor.verifyAndCorrectContentAvailability(resource, ProcessingMode.FULL, detectedMimeTypeNoContent, RdfResourceKind.STANDARD);
    mediaExtractor.verifyAndCorrectContentAvailability(resource, ProcessingMode.REDUCED, detectedMimeTypeWithContent, RdfResourceKind.STANDARD);
    mediaExtractor.verifyAndCorrectContentAvailability(resource, ProcessingMode.REDUCED, detectedMimeTypeNoContent, RdfResourceKind.STANDARD);

    // Test case where there should be content but there isn't and it is flagged as an exception.
    doReturn(false).when(resource).hasContent();
    doReturn(detectedMimeTypeWithContent).when(resource).getProvidedMimeType();
    assertThrows(MediaExtractionException.class,
        () -> mediaExtractor.verifyAndCorrectContentAvailability(resource, ProcessingMode.FULL,
            detectedMimeTypeWithContent, RdfResourceKind.STANDARD));

    // Test case where there is no content, but there should be and a correction is attempted.
    // Step 1: set the mocking to use a boolean that changes when content is set.
    final AtomicBoolean hasContent = new AtomicBoolean(false);
    doAnswer(invocation -> hasContent.get()).when(resource).hasContent();
    doAnswer(invocation -> {
      hasContent.set(true);
      return null;
    }).when(resource).markAsWithContent(any());
    doReturn(detectedMimeTypeNoContent).when(resource).getProvidedMimeType();
    doReturn(resourceWithContent).when(resourceDownloadClient).downloadWithContent(any());
    doReturn(true).when(resourceWithContent).hasContent();

    // Step 2: make the call and check that the download has occurred.
    verify(resourceDownloadClient, never()).downloadWithContent(any());
    mediaExtractor.verifyAndCorrectContentAvailability(resource, ProcessingMode.FULL, detectedMimeTypeWithContent, RdfResourceKind.STANDARD);
    final ArgumentCaptor<RdfResourceEntry> entryCaptor =
        ArgumentCaptor.forClass(RdfResourceEntry.class);
    verify(resourceDownloadClient, times(1)).downloadWithContent(entryCaptor.capture());
    verifyNoMoreInteractions(resourceDownloadClient);
    final RdfResourceEntry entry = entryCaptor.getValue();
    assertEquals(location, entry.getResourceUrl());
    verify(resource, times(1)).markAsWithContent(content);

    // Step 3: check what happens when the download does not include content either.
    hasContent.set(false);
    doReturn(false).when(resourceWithContent).hasContent();
    assertThrows(MediaExtractionException.class,
        () -> mediaExtractor.verifyAndCorrectContentAvailability(resource, ProcessingMode.FULL,
            detectedMimeTypeWithContent, RdfResourceKind.STANDARD));
  }

  @Test
  void testChooseMediaProcessor() {
    assertSame(imageProcessor, mediaExtractor.chooseMediaProcessor(MediaType.IMAGE,"image/subtype", RdfResourceKind.STANDARD).get(0));
    assertSame(audioVideoProcessor, mediaExtractor.chooseMediaProcessor(MediaType.AUDIO,"audio/subtype", RdfResourceKind.STANDARD).get(0));
    assertSame(audioVideoProcessor, mediaExtractor.chooseMediaProcessor(MediaType.VIDEO,"video/subtype", RdfResourceKind.STANDARD).get(0));
    assertSame(textProcessor, mediaExtractor.chooseMediaProcessor(MediaType.TEXT, "text/subtype", RdfResourceKind.STANDARD).get(0));
    assertSame(media3dProcessor, mediaExtractor.chooseMediaProcessor(MediaType.THREE_D,"model/subtype", RdfResourceKind.STANDARD).get(0));
    assertSame(oEmbedProcessor, mediaExtractor.chooseMediaProcessor(MediaType.OTHER,"application/json", RdfResourceKind.OEMBEDDED).get(0));
    assertSame(textProcessor, mediaExtractor.chooseMediaProcessor(MediaType.OTHER,"application/json", RdfResourceKind.OEMBEDDED).get(1));
    assertSame(oEmbedProcessor, mediaExtractor.chooseMediaProcessor(MediaType.OTHER,"application/xml", RdfResourceKind.OEMBEDDED).get(0));
    assertSame(textProcessor, mediaExtractor.chooseMediaProcessor(MediaType.OTHER,"application/xml", RdfResourceKind.OEMBEDDED).get(1));
    assertSame(oEmbedProcessor, mediaExtractor.chooseMediaProcessor(MediaType.OTHER,"text/xml", RdfResourceKind.OEMBEDDED).get(0));
    assertSame(textProcessor, mediaExtractor.chooseMediaProcessor(MediaType.OTHER,"text/xml", RdfResourceKind.OEMBEDDED).get(1));
    assertTrue(mediaExtractor.chooseMediaProcessor(MediaType.OTHER,"application/json", RdfResourceKind.STANDARD).isEmpty());
    assertTrue(mediaExtractor.chooseMediaProcessor(MediaType.OTHER,"application/xml", RdfResourceKind.STANDARD).isEmpty());
    assertTrue(mediaExtractor.chooseMediaProcessor(MediaType.OTHER,"text/xml", RdfResourceKind.STANDARD).isEmpty());
  }

  @Test
  void testProcessResource() throws MediaExtractionException, IOException {

    // Create resource
    final Resource resource = mock(Resource.class);
    final boolean hasMainThumbnail = true;

    // Set detected mime type
    final String detectedMimeType = "detected mime type";
    doReturn(detectedMimeType).when(mediaExtractor).detectAndVerifyMimeType(eq(resource), any());

    // Set processor.
    doReturn(List.of(audioVideoProcessor))
        .when(mediaExtractor).chooseMediaProcessor(MediaType.getMediaType(detectedMimeType), detectedMimeType, RdfResourceKind.STANDARD);
    final ResourceExtractionResultImpl result1 = new ResourceExtractionResultImpl(null, null);
    doReturn(result1).when(audioVideoProcessor).extractMetadata(resource, detectedMimeType, hasMainThumbnail);
    final ResourceExtractionResultImpl result2 = new ResourceExtractionResultImpl(null, null);
    doReturn(result2).when(audioVideoProcessor).copyMetadata(resource, detectedMimeType);

    // Make the call.
    assertSame(result1, mediaExtractor.performProcessing(resource, ProcessingMode.FULL, hasMainThumbnail, RdfResourceKind.STANDARD));
    verify(mediaExtractor, times(1)).detectAndVerifyMimeType(resource, ProcessingMode.FULL);
    verify(mediaExtractor, times(1)).verifyAndCorrectContentAvailability(resource,
        ProcessingMode.FULL, detectedMimeType, RdfResourceKind.STANDARD);
    assertSame(result2, mediaExtractor.performProcessing(resource, ProcessingMode.REDUCED, hasMainThumbnail, RdfResourceKind.STANDARD));
    verify(mediaExtractor, times(1)).detectAndVerifyMimeType(resource, ProcessingMode.REDUCED);
    verify(mediaExtractor, times(1)).verifyAndCorrectContentAvailability(resource,
        ProcessingMode.REDUCED, detectedMimeType, RdfResourceKind.STANDARD);

    // Check what happens if we are not supposed to process
    assertThrows(IllegalStateException.class,
        () -> mediaExtractor.performProcessing(resource, ProcessingMode.NONE, hasMainThumbnail, RdfResourceKind.STANDARD));

    // Check what happens if there is no processor
    doReturn(Collections.emptyList()).when(mediaExtractor).chooseMediaProcessor(MediaType.getMediaType(detectedMimeType), detectedMimeType, RdfResourceKind.STANDARD);
    assertNull(mediaExtractor.performProcessing(resource, ProcessingMode.FULL, hasMainThumbnail, RdfResourceKind.STANDARD));
    assertNull(mediaExtractor.performProcessing(resource, ProcessingMode.REDUCED, hasMainThumbnail, RdfResourceKind.STANDARD));
  }

  @Test
  void testPerformMediaExtraction() throws IOException, MediaExtractionException {

    // Create objects and mock for full processing.
    final RdfResourceEntry entry1 = new RdfResourceEntry("resource url 1", Collections.emptyList(), RdfResourceKind.OEMBEDDED);
    final Resource resource1 = mock(Resource.class);
    final boolean hasMainThumbnail = false;
    doReturn(ProcessingMode.FULL).when(mediaExtractor).getMode(entry1);
    doReturn(resource1).when(resourceDownloadClient).downloadBasedOnMimeType(entry1);
    final ResourceExtractionResultImpl result1 = new ResourceExtractionResultImpl(null, null);
    doReturn(result1).when(mediaExtractor).performProcessing(resource1, ProcessingMode.FULL, hasMainThumbnail, RdfResourceKind.OEMBEDDED);

    // Make the call and verify that the resource is closed.
    assertSame(result1, mediaExtractor.performMediaExtraction(entry1, hasMainThumbnail));
    verify(resource1).close();

    // mock for reduced processing
    final RdfResourceEntry entry2 = new RdfResourceEntry("resource url 2", Collections.emptyList(), RdfResourceKind.OEMBEDDED);
    final Resource resource2 = mock(Resource.class);
    doReturn(ProcessingMode.REDUCED).when(mediaExtractor).getMode(entry2);
    doReturn(resource2).when(resourceDownloadClient).downloadWithoutContent(entry2);
    final ResourceExtractionResultImpl result2 = new ResourceExtractionResultImpl(null, null);
    doReturn(result2).when(mediaExtractor).performProcessing(resource2, ProcessingMode.REDUCED, hasMainThumbnail, RdfResourceKind.OEMBEDDED);

    // Make the call and verify that the resource is closed.
    assertSame(result2, mediaExtractor.performMediaExtraction(entry2, hasMainThumbnail));
    verify(resource2).close();

    // Check exception from downloading.
    final RdfResourceEntry entry3 = new RdfResourceEntry("resource url 3", Collections.emptyList(), RdfResourceKind.OEMBEDDED);
    doReturn(ProcessingMode.FULL).when(mediaExtractor).getMode(entry3);
    doThrow(IOException.class).when(resourceDownloadClient).downloadBasedOnMimeType(entry3);
    assertThrows(MediaExtractionException.class,
        () -> mediaExtractor.performMediaExtraction(entry3, hasMainThumbnail));
    doThrow(RuntimeException.class).when(resourceDownloadClient).downloadBasedOnMimeType(entry3);
    assertThrows(MediaExtractionException.class,
        () -> mediaExtractor.performMediaExtraction(entry3, hasMainThumbnail));

    // Verify sanity check
    doReturn(ProcessingMode.NONE).when(mediaExtractor).getMode(entry3);
    assertNull(mediaExtractor.performMediaExtraction(entry3, hasMainThumbnail));
  }

  @Test
  void testClose() throws IOException {
    mediaExtractor.close();
    verify(resourceDownloadClient, times(3)).close();
  }

  @Test
  void testshouldDownloadForFullProcessing() {
    doReturn(true).when(imageProcessor).downloadResourceForFullProcessing();
    doReturn(true).when(textProcessor).downloadResourceForFullProcessing();
    doReturn(false).when(audioVideoProcessor).downloadResourceForFullProcessing();
    doReturn(false).when(media3dProcessor).downloadResourceForFullProcessing();
    doReturn(true).when(oEmbedProcessor).downloadResourceForFullProcessing();
    assertTrue(mediaExtractor.shouldDownloadForFullProcessing("image/unknown_type", RdfResourceKind.STANDARD));
    assertTrue(mediaExtractor.shouldDownloadForFullProcessing("text/unknown_type", RdfResourceKind.STANDARD));
    assertFalse(mediaExtractor.shouldDownloadForFullProcessing("audio/unknown_type", RdfResourceKind.STANDARD));
    assertFalse(mediaExtractor.shouldDownloadForFullProcessing("video/unknown_type", RdfResourceKind.STANDARD));
    assertFalse(mediaExtractor.shouldDownloadForFullProcessing("model/unknown_type", RdfResourceKind.STANDARD));
    assertFalse(mediaExtractor.shouldDownloadForFullProcessing("unknown_type", RdfResourceKind.STANDARD));
    assertTrue(mediaExtractor.shouldDownloadForFullProcessing("application/xml", RdfResourceKind.OEMBEDDED));
    assertTrue(mediaExtractor.shouldDownloadForFullProcessing("application/json", RdfResourceKind.OEMBEDDED));
    assertTrue(mediaExtractor.shouldDownloadForFullProcessing("application/xml", RdfResourceKind.STANDARD));
    assertFalse(mediaExtractor.shouldDownloadForFullProcessing("application/json", RdfResourceKind.STANDARD));
  }

  @Test
  void testGetMode() {
    testGetMode(ProcessingMode.NONE, EnumSet.noneOf(UrlType.class));
    testGetMode(ProcessingMode.FULL, EnumSet.of(UrlType.HAS_VIEW));
    testGetMode(ProcessingMode.REDUCED, EnumSet.of(UrlType.IS_SHOWN_AT));
    testGetMode(ProcessingMode.FULL, EnumSet.of(UrlType.IS_SHOWN_BY));
    testGetMode(ProcessingMode.FULL, EnumSet.of(UrlType.OBJECT));
    testGetMode(ProcessingMode.FULL, EnumSet.of(UrlType.HAS_VIEW, UrlType.IS_SHOWN_AT));
    testGetMode(ProcessingMode.FULL, EnumSet.of(UrlType.HAS_VIEW, UrlType.IS_SHOWN_BY));
    testGetMode(ProcessingMode.FULL, EnumSet.of(UrlType.HAS_VIEW, UrlType.OBJECT));
    testGetMode(ProcessingMode.FULL, EnumSet.of(UrlType.IS_SHOWN_AT, UrlType.IS_SHOWN_BY));
    testGetMode(ProcessingMode.FULL, EnumSet.of(UrlType.IS_SHOWN_AT, UrlType.OBJECT));
    testGetMode(ProcessingMode.FULL, EnumSet.of(UrlType.HAS_VIEW, UrlType.OBJECT));
    testGetMode(ProcessingMode.FULL, EnumSet.of(UrlType.HAS_VIEW, UrlType.IS_SHOWN_AT, UrlType.IS_SHOWN_BY));
    testGetMode(ProcessingMode.FULL, EnumSet.of(UrlType.HAS_VIEW, UrlType.IS_SHOWN_AT, UrlType.OBJECT));
    testGetMode(ProcessingMode.FULL, EnumSet.of(UrlType.HAS_VIEW, UrlType.IS_SHOWN_BY, UrlType.OBJECT));
    testGetMode(ProcessingMode.FULL, EnumSet.of(UrlType.IS_SHOWN_AT, UrlType.IS_SHOWN_BY, UrlType.OBJECT));
    testGetMode(ProcessingMode.FULL, EnumSet.allOf(UrlType.class));
  }

  @Test
  void getOEmbedJson() throws MediaExtractionException, IOException {
    final String resourceUrl = "https://vimeo.com/api/oembed.json?url=https%3A%2F%2Fvimeo.com%2F24416915";

    final String detectedMimeType = "application/json+oembed";
    final RdfResourceEntry rdfResourceEntry = new RdfResourceEntry(resourceUrl, Collections.singletonList(UrlType.IS_SHOWN_BY), RdfResourceKind.OEMBEDDED);
    final Resource resource = spy(
        new ResourceImpl(rdfResourceEntry, null, null, URI.create(resourceUrl)));
    doReturn(true)
        .when(resource).hasContent();
    doReturn(detectedMimeType)
        .when(tika).detect(any(InputStream.class), any(Metadata.class));
    doReturn(Paths.get(getClass().getClassLoader().getResource("__files/oembed.json").getPath()))
        .when(resource).getContentPath();
    doReturn(resource).when(resourceDownloadClient).downloadBasedOnMimeType(rdfResourceEntry);
    ResourceExtractionResult extractionResult = new ResourceExtractionResultImpl(
        new VideoResourceMetadata(detectedMimeType, resourceUrl, 0L));
    doReturn(extractionResult).when(oEmbedProcessor).extractMetadata(any(Resource.class), anyString(), anyBoolean());

    ResourceExtractionResult resourceExtractionResult = mediaExtractor.performMediaExtraction(rdfResourceEntry, false);
    assertEquals(resourceUrl, resourceExtractionResult.getMetadata().getResourceUrl());
  }

  @Test
  void getOEmbedXml() throws MediaExtractionException, IOException {
    final String resourceUrl = "https://vimeo.com/api/oembed.xml?url=https%3A%2F%2Fvimeo.com%2F24416915";

    final String detectedMimeType = "application/xml+oembed";
    final RdfResourceEntry rdfResourceEntry = new RdfResourceEntry(resourceUrl, Collections.singletonList(UrlType.IS_SHOWN_BY), RdfResourceKind.OEMBEDDED);
    final ResourceImpl resource = spy(
        new ResourceImpl(rdfResourceEntry, detectedMimeType, null, URI.create(resourceUrl)));
    doReturn(true)
        .when(resource).hasContent();
    doReturn(detectedMimeType)
        .when(tika).detect(any(InputStream.class), any(Metadata.class));
    doReturn(Paths.get(getClass().getClassLoader().getResource("__files/oembed.xml").getPath()))
        .when(resource).getContentPath();
    doReturn(resource).when(resourceDownloadClient).downloadBasedOnMimeType(rdfResourceEntry);
    ResourceExtractionResult extractionResult = new ResourceExtractionResultImpl(
        new VideoResourceMetadata(detectedMimeType, resourceUrl, 0L));
    doReturn(extractionResult).when(oEmbedProcessor).extractMetadata(any(Resource.class), anyString(), anyBoolean());

    ResourceExtractionResult resourceExtractionResult = mediaExtractor.performMediaExtraction(rdfResourceEntry, false);
    assertEquals(resourceUrl, resourceExtractionResult.getMetadata().getResourceUrl());
  }

  @Test
  void getIIIF() throws MediaExtractionException, IOException {
    final String resourceUrl = "https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/full/0/default.jpg";

    final String detectedMimeType = "application/json+oembed";
    final RdfResourceEntry rdfResourceEntry = new RdfResourceEntry(resourceUrl, Collections.singletonList(UrlType.IS_SHOWN_BY), RdfResourceKind.IIIF);
    final Resource resource = spy(
        new ResourceImpl(rdfResourceEntry, null, null, URI.create(resourceUrl)));
    doReturn(true)
        .when(resource).hasContent();
    doReturn(detectedMimeType)
        .when(tika).detect(any(InputStream.class), any(Metadata.class));
    doReturn(Paths.get(getClass().getClassLoader().getResource("__files/iiif_info.json").getPath()))
        .when(resource).getContentPath();
    doReturn(resource).when(resourceDownloadClient).downloadBasedOnMimeType(rdfResourceEntry);
    ResourceExtractionResult extractionResult = new ResourceExtractionResultImpl(
        new VideoResourceMetadata(detectedMimeType, resourceUrl, 0L));
    doReturn(extractionResult).when(oEmbedProcessor).extractMetadata(any(Resource.class), anyString(), anyBoolean());

    ResourceExtractionResult resourceExtractionResult = mediaExtractor.performMediaExtraction(rdfResourceEntry, false);
    assertEquals(resourceUrl, resourceExtractionResult.getMetadata().getResourceUrl());
  }
}
