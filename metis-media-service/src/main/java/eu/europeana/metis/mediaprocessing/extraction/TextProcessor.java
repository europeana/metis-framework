package eu.europeana.metis.mediaprocessing.extraction;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.Matrix;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import eu.europeana.metis.mediaprocessing.model.TextResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.Thumbnail;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link MediaProcessor} that is designed to handle resources of type
 * {@link ResourceType#TEXT}.
 */
class TextProcessor implements MediaProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(TextProcessor.class);
  
  private static final String PDF_MIME_TYPE = "application/pdf";

  private static final int DISPLAY_DPI = 72;

  private final ThumbnailGenerator thumbnailGenerator;

  /**
   * Constructor.
   * 
   * @param thumbnailGenerator An object that can generate thumbnails.
   */
  TextProcessor(ThumbnailGenerator thumbnailGenerator) {
    this.thumbnailGenerator = thumbnailGenerator;
  }

  @Override
  public ResourceExtractionResult process(String url, Set<UrlType> urlTypes, String mimeType,
      File content) throws MediaExtractionException {

    // Sanity checks
    if (!UrlType.shouldExtractMetadata(urlTypes)) {
      return null;
    }
    if (content == null) {
      throw new MediaExtractionException("File content is null");
    }

    // Create thumbnails in case of PDF file.
    final List<Thumbnail> thumbnails;
    if (PDF_MIME_TYPE.equals(mimeType)) {
      thumbnails = thumbnailGenerator.generateThumbnails(url, ResourceType.TEXT, content).getRight();
    } else {
      thumbnails = null;
    }

    // Set the resource properties relating to content.
    final boolean containsText;
    final Integer resolution;
    if (PDF_MIME_TYPE.equals(mimeType)) {
      final PdfPage characteristicPage = findCharacteristicPdfPage(content);
      containsText = characteristicPage.containsText;
      resolution = characteristicPage.resolution;
    } else {
      containsText = mimeType.startsWith("text/") || "application/xhtml+xml".equals(mimeType);
      resolution = null;
    }

    // Done
    final TextResourceMetadata metadata = new TextResourceMetadata(mimeType, url, content.length(),
        containsText, resolution, thumbnails);
    return new ResourceExtractionResult(metadata, thumbnails);
  }
  
  private static PdfPage findCharacteristicPdfPage(File content) throws MediaExtractionException {
    boolean containsText = false;
    Integer resolution = null;
    PdfReader reader = null;
    try {
      reader = new PdfReader(content.getAbsolutePath());
      PdfReaderContentParser parser = new PdfReaderContentParser(reader);
      PdfListener pdfListener = new PdfListener();
      for (int i = 1; i <= reader.getNumberOfPages(); i++) {
        parser.processContent(i, pdfListener);
        resolution = pdfListener.dpi;
        containsText = !StringUtils.isBlank(pdfListener.getResultantText());
        if (resolution != null && containsText) {
          break;
        }
      }
    } catch (IOException e) {
      throw new MediaExtractionException("Problem while reading PDF file.", e);
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
    return new PdfPage(containsText, resolution);
  }

  private static class PdfPage {
    
    final boolean containsText;
    final Integer resolution;

    public PdfPage(boolean containsText, Integer resolution) {
      this.containsText = containsText;
      this.resolution = resolution;
    }
  }

  private static class PdfListener extends SimpleTextExtractionStrategy {

    private Integer dpi;

    @Override
    public void renderImage(ImageRenderInfo iri) {

      // If we already have the DPI, we are done.
      if (dpi != null) {
        return;
      }

      try {

        // Get the image: if this is null, it means that the image is not there or the image is not
        // of a supported format.
        final BufferedImage image = iri.getImage().getBufferedImage();
        if (image == null) {
          return;
        }

        int widthInPixels = image.getWidth();
        int heightInPixels = image.getHeight();

        Matrix imageMatrix = iri.getImageCTM();
        double widthInInches = (double) imageMatrix.get(Matrix.I11) / DISPLAY_DPI;
        double heightInInches = (double) imageMatrix.get(Matrix.I22) / DISPLAY_DPI;

        long xDpi = Math.abs(Math.round(widthInPixels / widthInInches));
        long yDpi = Math.abs(Math.round(heightInPixels / heightInInches));
        dpi = (int) Math.min(xDpi, yDpi);
      } catch (IOException e) {
        LOGGER.info("Could not extract PDF image", e);
      }
    }
  }
}
