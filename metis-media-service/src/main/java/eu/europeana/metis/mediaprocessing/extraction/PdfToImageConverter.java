package eu.europeana.metis.mediaprocessing.extraction;

import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * Renders the first PDF page to an opaque image using Apache PDFBox.
 */
class PdfToImageConverter {

  /**
   * Converts the first page of a PDF file into an image. The image is rendered using Apache PDFBox and is returned as a
   * {@link BufferedImage}.
   * <p>
   * Read the PDF from its existing file without first copying its full contents into memory. Use disk-backed caching for any
   * streams PDFBox creates.
   * <p>
   * Note: The caller is responsible for managing and flushing the returned image after use.
   *
   * @param content the path to the PDF file to be processed; must not be null.
   * @return a {@link BufferedImage} representing the first page of the PDF.
   * @throws MediaExtractionException if the PDF cannot be processed, the file is null, does not exist, has no pages, or any other
   * error occurs during rendering.
   */
  BufferedImage convertPdfFirstPageToImage(Path content) throws MediaExtractionException {
    if (content == null) {
      throw new MediaExtractionException("File content is null or file does not exist.");
    }
    try (PDDocument pdDocument = Loader.loadPDF(content.toFile(), IOUtils.createTempFileOnlyStreamCache())) {
      if (pdDocument.getNumberOfPages() == 0) {
        throw new IOException("PDF has no pages");
      }
      final PDFRenderer renderer = new PDFRenderer(pdDocument);
      renderer.setSubsamplingAllowed(true);
      return renderer.renderImageWithDPI(0, TextProcessor.DISPLAY_DPI, ImageType.RGB);
    } catch (IOException | RuntimeException e) {
      throw new MediaExtractionException("Could not render the first PDF page in Java", e);
    }
  }
}
