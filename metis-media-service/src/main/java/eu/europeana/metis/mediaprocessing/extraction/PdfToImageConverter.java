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

/** Renders the first PDF page to an opaque image using Apache PDFBox. */
class PdfToImageConverter {

  // The caller owns the returned image and must flush it after generating thumbnails.
  BufferedImage convertPdfFirstPageToImage(Path content) throws MediaExtractionException {
    if (content == null) {
      throw new MediaExtractionException("File content is null or file does not exist.");
    }
    // File input and disk-backed stream caching avoid buffering the entire PDF on the heap.
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
