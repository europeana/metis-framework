package eu.europeana.metis.mediaprocessing.extraction.pdf;

import static eu.europeana.metis.mediaprocessing.extraction.ImageTestUtils.assertThumbnail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.extraction.pdf.PdfProcessor.PdfResult;
import eu.europeana.metis.mediaprocessing.extraction.thumbnail.ThumbnailGenerator;
import eu.europeana.metis.mediaprocessing.model.Thumbnail;
import eu.europeana.metis.mediaprocessing.model.ThumbnailImpl;
import eu.europeana.metis.mediaprocessing.model.ThumbnailKind;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PdfProcessorTest {

  @TempDir
  Path directory;

  private final PdfProcessor processor = new PdfProcessor(new ThumbnailGenerator());

  @ParameterizedTest
  @CsvSource({"800,400", "100,50"})
  void combinesFirstPagePreviewsWithMetadataFromLaterPages(int width, int height) throws Exception {
    final Path path = createPdf(width, height);
    final PdfResult pdfResult = processor.processWithThumbnails(path, "pdf");
    try {
      assertTrue(pdfResult.containsText());
      assertEquals(300, pdfResult.resolution());
      assertEquals(2, pdfResult.thumbnails().size());
      assertFirstPageThumbnails(pdfResult.thumbnails());
    } finally {
      pdfResult.thumbnails().forEach(Thumbnail::close);
    }
  }

  @Test
  void rendersFirstPageAt72DpiAsOpaqueImage() throws Exception {
    try (PDDocument pdDocument = processor.openDocument(createPdf(300, 100))) {
      final BufferedImage bufferedImage = processor.renderFirstPage(pdDocument);
      try {
        assertEquals(300, bufferedImage.getWidth());
        assertEquals(100, bufferedImage.getHeight());
        assertFalse(bufferedImage.getColorModel().hasAlpha());
        assertEquals(Color.RED.getRGB(), bufferedImage.getRGB(75, 50));
        assertEquals(Color.WHITE.getRGB(), bufferedImage.getRGB(225, 50));
      } finally {
        bufferedImage.flush();
      }
    }
  }

  @Test
  void readsMetadataWithoutRenderingWhenThumbnailsAreNotRequested() throws Exception {
    final Path path = createPdf(800, 400);
    final ThumbnailGenerator thumbnailGenerator = spy(new ThumbnailGenerator());
    final PdfProcessor pdfProcessor = spy(new PdfProcessor(thumbnailGenerator));
    final PdfResult pdfResult = pdfProcessor.processMetadata(path);
    assertTrue(pdfResult.containsText());
    assertEquals(300, pdfResult.resolution());
    assertNull(pdfResult.thumbnails());
    verify(pdfProcessor, never()).renderFirstPage(any(PDDocument.class));
    verifyNoInteractions(thumbnailGenerator);
  }

  @Test
  void rejectsNullInput() {
    assertThrows(MediaExtractionException.class, () -> processor.processWithThumbnails(null, "pdf"));
    assertThrows(MediaExtractionException.class, () -> processor.processMetadata(null));
  }

  @Test
  void rejectsInvalidPdf() throws Exception {
    final Path path = directory.resolve("invalid.pdf");
    Files.writeString(path, "Not a PDF document");
    assertThrows(MediaExtractionException.class, () -> processor.processWithThumbnails(path, "pdf"));
    assertThrows(MediaExtractionException.class, () -> processor.processMetadata(path));
  }

  @Test
  void rejectsEmptyPdfWhenPreviewIsRequested() throws Exception {
    final Path input = directory.resolve("empty.pdf");
    try (PDDocument pdDocument = new PDDocument()) {
      pdDocument.save(input.toFile());
    }
    assertThrows(MediaExtractionException.class, () -> processor.processWithThumbnails(input, "pdf"));
  }

  @Test
  void rejectsPdfRequiringAPassword() throws Exception {
    final Path input = directory.resolve("encrypted.pdf");
    try (PDDocument pdDocument = new PDDocument()) {
      pdDocument.addPage(new PDPage());
      final StandardProtectionPolicy standardProtectionPolicy = new StandardProtectionPolicy("owner", "user",
          new AccessPermission());
      standardProtectionPolicy.setEncryptionKeyLength(128);
      pdDocument.protect(standardProtectionPolicy);
      pdDocument.save(input.toFile());
    }
    assertThrows(MediaExtractionException.class, () -> processor.processWithThumbnails(input, "pdf"));
    assertThrows(MediaExtractionException.class, () -> processor.processMetadata(input));
  }

  @Test
  void closesGeneratedThumbnailsWhenDocumentClosingFails() throws Exception {
    final Path path = createPdf(800, 400);
    final ThumbnailGenerator thumbnailGenerator = spy(new ThumbnailGenerator());
    final Map<ThumbnailKind, ThumbnailImpl> thumbnails = thumbnailGenerator.prepareThumbnails("pdf", "image/png");
    doReturn(thumbnails).when(thumbnailGenerator).prepareThumbnails("pdf", "image/png");
    final PdfProcessor failingProcessor = spy(new PdfProcessor(thumbnailGenerator));
    final PDDocument pdDocument = spy(failingProcessor.openDocument(path));
    doReturn(pdDocument).when(failingProcessor).openDocument(path);
    final IOException cause = new IOException("Document close failed");
    doAnswer(invocation -> {
      assertTrue(thumbnails.values().stream().allMatch(ThumbnailImpl::hasContent));
      invocation.callRealMethod();
      throw cause;
    }).when(pdDocument).close();
    final MediaExtractionException failure = assertThrows(MediaExtractionException.class,
        () -> failingProcessor.processWithThumbnails(path, "pdf"));
    assertSame(cause, failure.getCause());
    assertTrue(thumbnails.values().stream().noneMatch(ThumbnailImpl::hasContent));
  }

  @Test
  void closesGeneratedThumbnailsWhenMetadataReadingFails() throws Exception {
    final Path path = createPdf(800, 400);
    final ThumbnailGenerator thumbnailGenerator = spy(new ThumbnailGenerator());
    final Map<ThumbnailKind, ThumbnailImpl> thumbnails = thumbnailGenerator.prepareThumbnails("pdf", "image/png");
    doReturn(thumbnails).when(thumbnailGenerator).prepareThumbnails("pdf", "image/png");
    final PdfProcessor failingProcessor = spy(new PdfProcessor(thumbnailGenerator));
    final PDDocument pdDocument = spy(failingProcessor.openDocument(path));
    doReturn(pdDocument).when(failingProcessor).openDocument(path);
    final IllegalStateException cause = new IllegalStateException("Metadata failed");
    doAnswer(invocation -> {
      final Object result = invocation.callRealMethod();
      assertTrue(thumbnails.values().stream().allMatch(ThumbnailImpl::hasContent));
      doThrow(cause).when(pdDocument).getPages();
      return result;
    }).when(thumbnailGenerator).generatePdfThumbnails(eq("pdf"), any(BufferedImage.class));
    final MediaExtractionException failure = assertThrows(MediaExtractionException.class,
        () -> failingProcessor.processWithThumbnails(path, "pdf"));
    assertSame(cause, failure.getCause());
    assertTrue(thumbnails.values().stream().noneMatch(ThumbnailImpl::hasContent));
    verify(pdDocument).close();
  }

  @Test
  void processesDocumentsConcurrentlyUsingOneProcessor() throws Exception {
    final Path path = createPdf(800, 400);
    final List<Callable<Void>> jobs = new ArrayList<>();
    for (int i = 0; i < 8; i++) {
      final String url = "pdf-" + i;
      jobs.add(() -> {
        final PdfResult pdfResult = processor.processWithThumbnails(path, url);
        try {
          assertTrue(pdfResult.containsText());
          assertEquals(300, pdfResult.resolution());
          assertEquals(url, pdfResult.thumbnails().get(1).getResourceUrl());
          assertFirstPageThumbnails(pdfResult.thumbnails());
        } finally {
          pdfResult.thumbnails().forEach(Thumbnail::close);
        }
        return null;
      });
    }
    try (var pool = Executors.newFixedThreadPool(4)) {
      for (var future : pool.invokeAll(jobs)) {
        future.get();
      }
    }
  }

  private void assertFirstPageThumbnails(List<Thumbnail> thumbnails) throws IOException {
    for (int i = 0; i < 2; i++) {
      final Thumbnail thumbnail = thumbnails.get(i);
      assertEquals("image/png", thumbnail.getMimeType());
      final int width = (i == 0) ? 200 : 400;
      final BufferedImage bufferedImage = assertThumbnail(thumbnail, width, width / 2);
      assertFalse(bufferedImage.getColorModel().hasAlpha());
      assertEquals(Color.RED.getRGB(), bufferedImage.getRGB(bufferedImage.getWidth() / 4, bufferedImage.getHeight() / 2));
      assertEquals(Color.WHITE.getRGB(), bufferedImage.getRGB(bufferedImage.getWidth() * 3 / 4, bufferedImage.getHeight() / 2));
    }
  }

  private Path createPdf(int width, int height) throws IOException {
    final Path input = directory.resolve("document.pdf");
    try (PDDocument pdDocument = new PDDocument()) {
      final PDPage firstPage = new PDPage(new PDRectangle((float) width, (float) height));
      pdDocument.addPage(firstPage);
      try (PDPageContentStream pdPageContentStream = new PDPageContentStream(pdDocument, firstPage)) {
        pdPageContentStream.setNonStrokingColor(Color.RED);
        pdPageContentStream.addRect(0, 0, width / 2f, (float) height);
        pdPageContentStream.fill();
        pdPageContentStream.setNonStrokingColor(Color.BLACK);
        pdPageContentStream.beginText();
        pdPageContentStream.setFont(new PDType1Font(FontName.HELVETICA), 8);
        pdPageContentStream.newLineAtOffset(width * 0.6f, height * 0.85f);
        pdPageContentStream.showText("First page");
        pdPageContentStream.endText();
      }
      // Only the second page contains a raster image. It supplies the DPI, not the preview.
      final PDPage secondPage = new PDPage(new PDRectangle((float) width, (float) height));
      pdDocument.addPage(secondPage);
      try (PDPageContentStream content = new PDPageContentStream(pdDocument, secondPage)) {
        content.setNonStrokingColor(Color.BLUE);
        content.addRect(0, 0, (float) width, (float) height);
        content.fill();
        final BufferedImage image = new BufferedImage(600, 300, BufferedImage.TYPE_INT_RGB);
        content.drawImage(LosslessFactory.createFromImage(pdDocument, image), 0, 0, 144, 72);
      }
      pdDocument.save(input.toFile());
    }
    return input;
  }
}
