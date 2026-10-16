package eu.europeana.metis.mediaprocessing.extraction;

import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.extraction.PdfMetadataReader.PdfMetadata;
import eu.europeana.metis.mediaprocessing.model.Thumbnail;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * The PdfProcessor class provides functionality for processing PDF documents. It includes methods for extracting metadata,
 * rendering the first page, and generating thumbnails for a PDF document.
 */
class PdfProcessor {

  private static final int PREVIEW_DPI = 72;

  private final ThumbnailGenerator thumbnailGenerator;

  PdfProcessor(ThumbnailGenerator thumbnailGenerator) {
    this.thumbnailGenerator = thumbnailGenerator;
  }

  PdfResult process(Path content, String resourceUrl, boolean generateThumbnails) throws MediaExtractionException {
    if (content == null) {
      throw new MediaExtractionException("File content is null or file does not exist.");
    }
    List<Thumbnail> thumbnails = null;
    try (PDDocument pdDocument = openDocument(content)) {
      if (generateThumbnails) {
        final BufferedImage pdfBufferedImage = renderFirstPage(pdDocument);
        try {
          thumbnails = thumbnailGenerator.generatePdfThumbnails(resourceUrl, pdfBufferedImage);
        } finally {
          pdfBufferedImage.flush();
        }
      }
      final PdfMetadata pdfMetadata = new PdfMetadataReader().read(pdDocument);
      return new PdfResult(pdfMetadata.containsText(), pdfMetadata.resolution(), thumbnails);
    } catch (IOException | RuntimeException | MediaExtractionException e) {
      if (thumbnails != null) {
        thumbnails.forEach(Thumbnail::close);
      }
      throw new MediaExtractionException("Could not process PDF in Java", e);
    }
  }

  PDDocument openDocument(Path content) throws IOException {
    return Loader.loadPDF(content.toFile(), IOUtils.createTempFileOnlyStreamCache());
  }

  BufferedImage renderFirstPage(PDDocument pdDocument) throws IOException {
    if (pdDocument.getNumberOfPages() == 0) {
      throw new IOException("PDF has no pages");
    }
    final PDFRenderer renderer = new PDFRenderer(pdDocument);
    renderer.setSubsamplingAllowed(true);
    return renderer.renderImageWithDPI(0, PREVIEW_DPI, ImageType.RGB);
  }

  record PdfResult(boolean containsText, Integer resolution, List<Thumbnail> thumbnails) {

  }
}
