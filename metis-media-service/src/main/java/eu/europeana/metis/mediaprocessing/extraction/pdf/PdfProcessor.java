package eu.europeana.metis.mediaprocessing.extraction.pdf;

import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.extraction.pdf.PdfMetadataReader.PdfMetadata;
import eu.europeana.metis.mediaprocessing.extraction.thumbnail.ThumbnailGenerator;
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
public class PdfProcessor {

  private static final int PREVIEW_DPI = 72;

  private final ThumbnailGenerator thumbnailGenerator;

  /**
   * Creates a PDF processor using the supplied thumbnail generator.
   *
   * @param thumbnailGenerator The generator used to encode first-page previews.
   */
  public PdfProcessor(ThumbnailGenerator thumbnailGenerator) {
    this.thumbnailGenerator = thumbnailGenerator;
  }

  /**
   * Extracts PDF text and resolution metadata without rendering any pages.
   *
   * @param content The path to the PDF document.
   * @return The extracted metadata with a null thumbnail list.
   * @throws MediaExtractionException If the document cannot be opened, read, or closed.
   */
  public PdfResult processMetadata(Path content) throws MediaExtractionException {
    if (content == null) {
      throw new MediaExtractionException("File content is null or file does not exist.");
    }
    try (PDDocument pdDocument = openDocument(content)) {
      final PdfMetadata pdfMetadata = new PdfMetadataReader().read(pdDocument);
      return new PdfResult(pdfMetadata.containsText(), pdfMetadata.resolution(), null);
    } catch (IOException | RuntimeException e) {
      throw new MediaExtractionException("Could not process PDF in Java", e);
    }
  }

  /**
   * Extracts PDF metadata and generates thumbnails from the first page.
   *
   * @param content The path to the PDF document.
   * @param resourceUrl The resource URL used to name the thumbnails.
   * @return The extracted metadata and thumbnails, which the caller must close.
   * @throws MediaExtractionException If the document cannot be processed or thumbnails cannot be generated.
   */
  public PdfResult processWithThumbnails(Path content, String resourceUrl) throws MediaExtractionException {
    if (content == null) {
      throw new MediaExtractionException("File content is null or file does not exist.");
    }
    List<Thumbnail> thumbnails = null;
    try (PDDocument pdDocument = openDocument(content)) {
      final BufferedImage pdfBufferedImage = renderFirstPage(pdDocument);
      try {
        thumbnails = thumbnailGenerator.generatePdfThumbnails(resourceUrl, pdfBufferedImage);
      } finally {
        pdfBufferedImage.flush();
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

  PDDocument openDocument(Path path) throws IOException {
    return Loader.loadPDF(path.toFile(), IOUtils.createTempFileOnlyStreamCache());
  }

  BufferedImage renderFirstPage(PDDocument pdDocument) throws IOException {
    if (pdDocument.getNumberOfPages() == 0) {
      throw new IOException("PDF has no pages");
    }
    final PDFRenderer pdfRenderer = new PDFRenderer(pdDocument);
    pdfRenderer.setSubsamplingAllowed(true);
    return pdfRenderer.renderImageWithDPI(0, PREVIEW_DPI, ImageType.RGB);
  }

  /**
   * Holds extracted PDF metadata and any generated thumbnails.
   *
   * @param containsText Whether the document contains non-empty text.
   * @param resolution The first detected positive image resolution in DPI, or null if none is found.
   * @param thumbnails The generated thumbnails, which the caller must close, or null for metadata-only processing.
   */
  public record PdfResult(boolean containsText, Integer resolution, List<Thumbnail> thumbnails) {

  }
}
