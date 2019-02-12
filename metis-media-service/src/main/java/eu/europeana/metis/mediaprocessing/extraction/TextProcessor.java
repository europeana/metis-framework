package eu.europeana.metis.mediaprocessing.extraction;

import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.Matrix;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import eu.europeana.metis.mediaprocessing.model.TextResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.Thumbnail;
import eu.europeana.metis.mediaprocessing.model.UrlType;

/**
 * <p>
 * Implementation of {@link MediaProcessor} that is designed to handle resources of type {@link
 * ResourceType#TEXT}.
 * </p>
 * <p>
 * Note: if we don't have metadata, we don't return thumbnails either.
 * </p>
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
  public ResourceExtractionResult process(Resource resource) throws MediaExtractionException {

    // Sanity checks
    if (!shouldExtractMetadata(resource)) {
      return null;
    }
    try {
      if (!resource.hasContent()) {
        throw new MediaExtractionException("File does not exist or does not have content.");
      }
    } catch (IOException e) {
      throw new MediaExtractionException("Could not determine whether resource has content.", e);
    }

    // Create thumbnails in case of PDF file.
    final List<Thumbnail> thumbnails;
    if (PDF_MIME_TYPE.equals(resource.getMimeType())) {
      thumbnails = thumbnailGenerator.generateThumbnails(resource.getResourceUrl(),
          ResourceType.TEXT, resource.getContentPath().toFile()).getRight();
    } else {
      thumbnails = null;
    }

    // Set the resource properties relating to content.
    final PdfCharacteristics characteristics;
    if (PDF_MIME_TYPE.equals(resource.getMimeType())) {
      characteristics = findPdfCharacteristics(resource.getContentPath().toFile());
    } else {
      final boolean hasText = resource.getMimeType().startsWith("text/")
          || "application/xhtml+xml".equals(resource.getMimeType());
      characteristics = new PdfCharacteristics(hasText, null);
    }

    // Get the size of the resource
    final long contentSize;
    try {
      contentSize = resource.getContentSize();
    } catch (IOException e) {
      throw new MediaExtractionException(
          "Could not determine the size of the resource " + resource.getResourceUrl(), e);
    }

    // Done
    final TextResourceMetadata metadata = new TextResourceMetadata(resource.getMimeType(),
        resource.getResourceUrl(), contentSize, characteristics.containsText,
        characteristics.resolution, thumbnails);
    return new ResourceExtractionResult(metadata, thumbnails);
  }

  private static PdfCharacteristics findPdfCharacteristics(File content) throws MediaExtractionException {
    PdfReader reader = null;
    try {

      // Set up PDF parsing.
      reader = new PdfReader(content.getAbsolutePath());
      final PdfReaderContentParser parser = new PdfReaderContentParser(reader);
      final PdfListener pdfListener = new PdfListener();

      // Go by each page: if we find the data we need, we can stop.
      for (int i = 1; i <= reader.getNumberOfPages(); i++) {
        parser.processContent(i, pdfListener);
        if (pdfListener.dpi != null && pdfListener.hasText) {
          break;
        }
      }

      // Done.
      return new PdfCharacteristics(pdfListener.hasText, pdfListener.dpi);
    } catch (IOException e) {
      throw new MediaExtractionException("Problem while reading PDF file.", e);
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
  }

  private static class PdfCharacteristics {

    private final boolean containsText;
    private final Integer resolution;

    PdfCharacteristics(boolean containsText, Integer resolution) {
      this.containsText = containsText;
      this.resolution = resolution;
    }
  }

  /**
   * This pdf listener obtains and stores the resolution of the first image it encounters in the
   * PDF, as well as whether any textual content is encountered in the PDF. If it is applied to each
   * page of the PDF in order, it will therefore find the resolution of the PDF's first image.
   */
  private static class PdfListener implements RenderListener {

    private Integer dpi = null;
    private boolean hasText = false;

    @Override
    public void beginTextBlock() {
      // Nothing to do.
    }

    @Override
    public void endTextBlock() {
      // Nothing to do.
    }

    @Override
    public void renderText(TextRenderInfo renderInfo) {
      hasText = hasText || !renderInfo.getText().isEmpty();
    }

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
