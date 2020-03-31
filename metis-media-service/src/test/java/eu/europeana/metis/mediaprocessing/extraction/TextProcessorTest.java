package eu.europeana.metis.mediaprocessing.extraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.Matrix;
import com.itextpdf.text.pdf.parser.PdfImageObject;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.extraction.TextProcessor.OpenPdfFile;
import eu.europeana.metis.mediaprocessing.extraction.TextProcessor.PdfCharacteristics;
import eu.europeana.metis.mediaprocessing.extraction.TextProcessor.PdfListener;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResultImpl;
import eu.europeana.metis.mediaprocessing.model.ResourceImpl;
import eu.europeana.metis.mediaprocessing.model.TextResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.Thumbnail;
import eu.europeana.metis.mediaprocessing.model.ThumbnailImpl;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TextProcessorTest {

  private static ThumbnailGenerator thumbnailGenerator;
  private static PdfToImageConverter pdfToImageConverter;
  private static TextProcessor textProcessor;

  @BeforeAll
  static void createMocks() {
    thumbnailGenerator = mock(ThumbnailGenerator.class);
    pdfToImageConverter = mock(PdfToImageConverter.class);
    textProcessor = spy(new TextProcessor(thumbnailGenerator, pdfToImageConverter));
  }

  @BeforeEach
  void resetMocks() {
    reset(thumbnailGenerator, textProcessor);
  }

  @Test
  void testDownloadResourceForFullProcessing() {
    assertTrue(textProcessor.downloadResourceForFullProcessing());
  }

  @Test
  void testCopy() {

    // Create resource
    final Resource resource = mock(Resource.class);
    final Long fileSize = 12345L;
    final String url = "test url";
    doReturn(fileSize).when(resource).getProvidedFileSize();
    doReturn(url).when(resource).getResourceUrl();

    // Make call
    final String mediaType = "text type";
    final ResourceExtractionResultImpl result = textProcessor.copyMetadata(resource, mediaType);

    // Verify
    assertNotNull(result);
    assertNotNull(result.getOriginalMetadata());
    assertTrue(result.getOriginalMetadata() instanceof TextResourceMetadata);
    assertEquals(mediaType, result.getOriginalMetadata().getMimeType());
    assertEquals(fileSize, result.getOriginalMetadata().getContentSize());
    assertEquals(url, result.getOriginalMetadata().getResourceUrl());
    assertNull(result.getThumbnails());
    assertTrue(result.getOriginalMetadata().getThumbnailTargetNames().isEmpty());
    assertNull(((TextResourceMetadata)result.getOriginalMetadata()).getResolution());
    assertFalse(((TextResourceMetadata)result.getOriginalMetadata()).containsText());
  }

  @Test
  void testExtractForRegularText() throws IOException, MediaExtractionException {

    // Define input
    final RdfResourceEntry rdfResourceEntry = new RdfResourceEntry("testUrl",
        Collections.singletonList(UrlType.IS_SHOWN_BY));
    final ResourceImpl resource = spy(
        new ResourceImpl(rdfResourceEntry, null, null, URI.create("http://www.test.com")));
    final String detectedMimeType = "detected mime type";
    doReturn(true).when(resource).hasContent();
    doReturn(1234L).when(resource).getContentSize();

    // Call method
    final ResourceExtractionResultImpl result = textProcessor.extractMetadata(resource, detectedMimeType);

    // Verify result metadata general properties
    assertTrue(result.getOriginalMetadata() instanceof TextResourceMetadata);
    final TextResourceMetadata metadata = (TextResourceMetadata) result.getOriginalMetadata();
    assertEquals(rdfResourceEntry.getResourceUrl(), metadata.getResourceUrl());
    assertEquals(detectedMimeType, metadata.getMimeType());
    assertEquals(0, metadata.getThumbnailTargetNames().size());
    assertEquals(resource.getContentSize(), metadata.getContentSize());

    // Verify result metadata image specific properties
    assertFalse(metadata.containsText());
    assertNull(metadata.getResolution());

    // Verify result thumbnails
    assertNull(result.getThumbnails());

    // Check for resource with no content
    doReturn(false).when(resource).hasContent();
    assertThrows(MediaExtractionException.class,
        () -> textProcessor.extractMetadata(resource, detectedMimeType));
    doReturn(true).when(resource).hasContent();

    // Check for resource with IO exception
    doThrow(new IOException()).when(resource).hasContent();
    assertThrows(MediaExtractionException.class,
        () -> textProcessor.extractMetadata(resource, detectedMimeType));
    doReturn(true).when(resource).hasContent();
    doThrow(new IOException()).when(resource).getContentSize();
    assertThrows(MediaExtractionException.class,
        () -> textProcessor.extractMetadata(resource, detectedMimeType));
    doReturn(1234L).when(resource).getContentSize();

    // Check that all is well again.
    assertNotNull(textProcessor.extractMetadata(resource, detectedMimeType));
  }

  @Test
  void testExtractForPdf() throws IOException, MediaExtractionException {

    // Define input
    final String url = "testUrl";
    final Path contentPath = mock(Path.class);
    final File contentFile = new File("content");
    doReturn(contentFile).when(contentPath).toFile();
    final RdfResourceEntry rdfResourceEntry = new RdfResourceEntry("testUrl",
        Collections.singletonList(UrlType.IS_SHOWN_BY));
    final ResourceImpl resource = spy(
        new ResourceImpl(rdfResourceEntry, null, null, URI.create("http://www.test.com")));
    final String detectedMimeType = "application/pdf";
    doReturn(true).when(resource).hasContent();
    doReturn(1234L).when(resource).getContentSize();
    doReturn(contentPath).when(resource).getContentPath();

    // Define thumbnails
    final ThumbnailImpl thumbnail1 = mock(ThumbnailImpl.class);
    doReturn("thumbnail 1").when(thumbnail1).getTargetName();
    final ThumbnailImpl thumbnail2 = mock(ThumbnailImpl.class);
    doReturn("thumbnail 2").when(thumbnail1).getTargetName();

    // Define converted image and mock pdf to image converter.
    final Path pdfImagePath = mock(Path.class);
    final File pdfImageFile = new File("PDF image");
    doReturn(pdfImageFile).when(pdfImagePath).toFile();
    doReturn(pdfImagePath).when(pdfToImageConverter).convertToPdf(contentPath);
    doNothing().when(pdfToImageConverter).removePdfImageFileSilently(pdfImagePath);

    // Define output and mock thumbnail generator - resource type for which metadata is generated.
    final ImageMetadata imageMetadata = new ImageMetadata(123, 321, "sRGB",
        Arrays.asList("123456", "654321"));
    final Pair<ImageMetadata, List<Thumbnail>> thumbnailsAndMetadata = new ImmutablePair<>(
        imageMetadata, Arrays.asList(thumbnail1, thumbnail2));
    doReturn(thumbnailsAndMetadata).when(thumbnailGenerator)
        .generateThumbnails(url, "image/png", pdfImageFile, true);

    // define PDF analysis results and mock the method performing it.
    final PdfCharacteristics pdfCharacteristics = new PdfCharacteristics(true, 1);
    doReturn(pdfCharacteristics).when(textProcessor).findPdfCharacteristics(contentFile);

    // Call method
    final ResourceExtractionResultImpl result = textProcessor.extractMetadata(resource, detectedMimeType);

    // Verify result metadata general properties
    assertTrue(result.getOriginalMetadata() instanceof TextResourceMetadata);
    final TextResourceMetadata metadata = (TextResourceMetadata) result.getOriginalMetadata();
    assertEquals(rdfResourceEntry.getResourceUrl(), metadata.getResourceUrl());
    assertEquals(detectedMimeType, metadata.getMimeType());
    assertEquals(2, metadata.getThumbnailTargetNames().size());
    assertTrue(metadata.getThumbnailTargetNames().contains(thumbnail1.getTargetName()));
    assertTrue(metadata.getThumbnailTargetNames().contains(thumbnail2.getTargetName()));
    assertEquals(resource.getContentSize(), metadata.getContentSize());

    // Verify result metadata image specific properties
    assertEquals(pdfCharacteristics.containsText(), metadata.containsText());
    assertEquals(pdfCharacteristics.getResolution(), metadata.getResolution());

    // Verify result thumbnails
    assertEquals(thumbnailsAndMetadata.getRight(), result.getThumbnails());

    // Verify deletion of pdf image
    verify(pdfToImageConverter, times(1)).removePdfImageFileSilently(pdfImagePath);
    verify(pdfToImageConverter, times(1)).removePdfImageFileSilently(any());

    // Test exception occurring in thumbnail creation - revert and check all is well.
    doThrow(MediaExtractionException.class).when(thumbnailGenerator)
            .generateThumbnails(url, "image/png", pdfImageFile, true);
    assertThrows(MediaExtractionException.class,
            () -> textProcessor.extractMetadata(resource, detectedMimeType));
    verify(pdfToImageConverter, times(2)).removePdfImageFileSilently(pdfImagePath);
    verify(pdfToImageConverter, times(2)).removePdfImageFileSilently(any());
    doReturn(thumbnailsAndMetadata).when(thumbnailGenerator)
            .generateThumbnails(url, "image/png", pdfImageFile, true);
    textProcessor.extractMetadata(resource, detectedMimeType);

    // Test exception occurring in establishing content size
    doThrow(IOException.class).when(resource).getContentSize();
    assertThrows(MediaExtractionException.class,
            () -> textProcessor.extractMetadata(resource, detectedMimeType));
    verify(thumbnail1, times(1)).close();
    verify(thumbnail2, times(1)).close();
  }

  @Test
  void testFindPdfCharacteristics() throws IOException, MediaExtractionException {

    // Mock pdf reading utility classes: we have 4 pages.
    final File content = new File("content file");
    final OpenPdfFile openPdfFile = mock(OpenPdfFile.class);
    doReturn(4).when(openPdfFile).getNumberOfPages();
    doReturn(openPdfFile).when(textProcessor).openPdfFile(content);
    final PdfReaderContentParser pdfParser = mock(PdfReaderContentParser.class);
    doReturn(pdfParser).when(openPdfFile).getPdfParser();
    final PdfListener pdfListener = mock(PdfListener.class);
    doReturn(pdfListener).when(openPdfFile).getPdfListener();

    // First parse pdf that has image on page 3 and text content on page 2.
    doAnswer(invocation -> {
      final int page = invocation.getArgument(0);
      doReturn(page > 1).when(pdfListener).hasText();
      doReturn(page > 2 ? 1 : null).when(pdfListener).getDpi();
      return pdfListener;
    }).when(pdfParser).processContent(anyInt(), notNull());
    final PdfCharacteristics result1 = textProcessor.findPdfCharacteristics(content);
    assertEquals(Integer.valueOf(1), result1.getResolution());
    assertTrue(result1.containsText());
    verify(pdfParser, times(3)).processContent(anyInt(), eq(pdfListener));

    // Now parse pdf that has image on page 1 and text on page 2.
    doReturn(2).when(pdfListener).getDpi();
    doAnswer(invocation -> {
      final int page = invocation.getArgument(0);
      doReturn(page > 1).when(pdfListener).hasText();
      return pdfListener;
    }).when(pdfParser).processContent(anyInt(), notNull());
    final PdfCharacteristics result2 = textProcessor.findPdfCharacteristics(content);
    assertEquals(Integer.valueOf(2), result2.getResolution());
    assertTrue(result2.containsText());
    verify(pdfParser, times(5)).processContent(anyInt(), eq(pdfListener));

    // Now parse pdf without text, just image (on page 1)
    doReturn(3).when(pdfListener).getDpi();
    doReturn(false).when(pdfListener).hasText();
    doReturn(pdfListener).when(pdfParser).processContent(anyInt(), notNull());
    final PdfCharacteristics result3 = textProcessor.findPdfCharacteristics(content);
    assertEquals(Integer.valueOf(3), result3.getResolution());
    assertFalse(result3.containsText());
    verify(pdfParser, times(9)).processContent(anyInt(), eq(pdfListener));

    // Now parse pdf without image, just text (on page 1)
    doReturn(null).when(pdfListener).getDpi();
    doReturn(true).when(pdfListener).hasText();
    doReturn(pdfListener).when(pdfParser).processContent(anyInt(), notNull());
    final PdfCharacteristics result4 = textProcessor.findPdfCharacteristics(content);
    assertNull(result4.getResolution());
    assertTrue(result4.containsText());
    verify(pdfParser, times(13)).processContent(anyInt(), eq(pdfListener));

    // Test exception
    doThrow(IOException.class).when(textProcessor).openPdfFile(content);
    assertThrows(MediaExtractionException.class,
        () -> textProcessor.findPdfCharacteristics(content));
  }

  @Test
  void testPdfListenerText() {

    // Mock text pages
    final TextRenderInfo pageWithText = mock(TextRenderInfo.class);
    doReturn("Text").when(pageWithText).getText();
    final TextRenderInfo pageWithoutText = mock(TextRenderInfo.class);
    doReturn("").when(pageWithoutText).getText();

    // Create listener: should not have text.
    final PdfListener listener = new PdfListener();
    assertFalse(listener.hasText());

    // Send page without text: still no text.
    listener.renderText(pageWithoutText);
    assertFalse(listener.hasText());

    // Send page with text: fact that there is text should now be registered.
    listener.renderText(pageWithText);
    assertTrue(listener.hasText());

    // Send page with text: should remember that it has text.
    listener.renderText(pageWithoutText);
    assertTrue(listener.hasText());
  }

  @Test
  void testPdfListenerImage() throws IOException {

    // Define numbers
    final int imageWidth = 4;
    final int imageHeight = 5;
    final int horizontalScaling = 12;
    final int verticalScaling = 8;

    // Mock image pages
    final ImageRenderInfo renderInfo = mock(ImageRenderInfo.class);
    final PdfImageObject pdfImage = mock(PdfImageObject.class);
    doReturn(pdfImage).when(renderInfo).getImage();

    // Mock image properties
    final BufferedImage image = mock(BufferedImage.class);
    doReturn(horizontalScaling * imageWidth).when(image).getWidth();
    doReturn(verticalScaling * imageHeight).when(image).getHeight();
    final Matrix matrix = new Matrix(horizontalScaling * TextProcessor.DISPLAY_DPI, 0, 0,
        verticalScaling * TextProcessor.DISPLAY_DPI, 0, 0);
    doReturn(matrix).when(renderInfo).getImageCTM();

    // Create listener: should not have resolution.
    final PdfListener listener = new PdfListener();
    assertNull(listener.getDpi());

    // Send page without text: still no resolution.
    doReturn(null).when(pdfImage).getBufferedImage();
    listener.renderImage(renderInfo);
    assertNull(listener.getDpi());

    // Send page with text: fact that there is a resolution should now be registered.
    doReturn(image).when(pdfImage).getBufferedImage();
    listener.renderImage(renderInfo);
    assertEquals(Integer.valueOf(Math.min(imageWidth, imageHeight)),listener.getDpi());

    // Send page with text: should remember that it has a resolution.
    doReturn(image).when(pdfImage).getBufferedImage();
    listener.renderImage(renderInfo);
    assertEquals(Integer.valueOf(Math.min(imageWidth, imageHeight)),listener.getDpi());

    // In case of error: current value should not change. Try with new Image Listener.
    final PdfListener blankListener = new PdfListener();
    doThrow(IOException.class).when(pdfImage).getBufferedImage();
    blankListener.renderImage(renderInfo);
    assertNull(blankListener.getDpi());
  }
}
