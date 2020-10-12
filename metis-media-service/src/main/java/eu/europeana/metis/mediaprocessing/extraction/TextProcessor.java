package eu.europeana.metis.mediaprocessing.extraction;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.Matrix;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResultImpl;
import eu.europeana.metis.mediaprocessing.model.TextResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.Thumbnail;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Implementation of {@link MediaProcessor} that is designed to handle resources of type {@link
 * eu.europeana.metis.utils.MediaType#TEXT}.
 * </p>
 * <p>
 * Note: if we don't have metadata, we don't return thumbnails either.
 * </p>
 */
class TextProcessor implements MediaProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(TextProcessor.class);

  private static final String PDF_MIME_TYPE = "application/pdf";
  private static final String PNG_MIME_TYPE = "image/png";

  protected static final int DISPLAY_DPI = 72;

  private final ThumbnailGenerator thumbnailGenerator;
  private final PdfToImageConverter pdfToImageConverter;

  /**
   * Constructor.
   *
   * @param thumbnailGenerator An object that can generate thumbnails.
   * @param pdfToImageConverter An object that can create an image of a PDF file.
   */
  TextProcessor(ThumbnailGenerator thumbnailGenerator, PdfToImageConverter pdfToImageConverter) {
    this.thumbnailGenerator = thumbnailGenerator;
    this.pdfToImageConverter = pdfToImageConverter;
  }

  @Override
  public boolean downloadResourceForFullProcessing() {
    return true;
  }

  @Override
  public ResourceExtractionResultImpl copyMetadata(Resource resource, String detectedMimeType) {
    return new ResourceExtractionResultImpl(new TextResourceMetadata(detectedMimeType,
        resource.getResourceUrl(), resource.getProvidedFileSize()));
  }

  boolean generateThumbnailForPdf(Resource resource, boolean mainThumbnailAvailable) {
    return !(mainThumbnailAvailable && resource.getUrlTypes().contains(UrlType.IS_SHOWN_BY));
  }

  @Override
  public ResourceExtractionResultImpl extractMetadata(Resource resource, String detectedMimeType,
          boolean mainThumbnailAvailable) throws MediaExtractionException {

    // Sanity check
    try {
      if (!resource.hasContent()) {
        throw new MediaExtractionException("File does not exist or does not have content.");
      }
    } catch (IOException e) {
      throw new MediaExtractionException("Could not determine whether resource has content.", e);
    }

    // Create thumbnails in case of PDF file.
    final List<Thumbnail> thumbnails;
    if (PDF_MIME_TYPE.equals(detectedMimeType) && generateThumbnailForPdf(resource,
            mainThumbnailAvailable)) {
      final Path pdfImage = pdfToImageConverter.convertToPdf(resource.getContentPath());
      try {
        thumbnails = thumbnailGenerator.generateThumbnails(resource.getResourceUrl(),
                PNG_MIME_TYPE, pdfImage.toFile(), true).getRight();
      } finally {
        pdfToImageConverter.removePdfImageFileSilently(pdfImage);
      }
    } else {
      thumbnails = null;
    }

    // Set the resource properties relating to content.
    final PdfCharacteristics characteristics;
    if (PDF_MIME_TYPE.equals(detectedMimeType)) {
      characteristics = findPdfCharacteristics(resource.getContentFile());
    } else {
      final boolean hasText = detectedMimeType.startsWith("text/")
          || "application/xhtml+xml".equals(detectedMimeType);
      characteristics = new PdfCharacteristics(hasText, null);
    }

    // Get the size of the resource
    final Long contentSize;
    try {
      contentSize = nullIfNegative(resource.getContentSize());
    } catch (RuntimeException | IOException e) {
      closeAllThumbnailsSilently(thumbnails);
      throw new MediaExtractionException(
              "Could not determine the size of the resource " + resource.getResourceUrl(), e);
    }

    // Done
    final TextResourceMetadata metadata = new TextResourceMetadata(detectedMimeType,
        resource.getResourceUrl(), contentSize, characteristics.containsText(),
        nullIfNegative(characteristics.getResolution()), thumbnails);
    return new ResourceExtractionResultImpl(metadata, thumbnails);
  }

  private static void closeAllThumbnailsSilently(List<Thumbnail> thumbnails) {
    if (thumbnails != null) {
      for (Thumbnail thumbnail : thumbnails) {
        try {
          thumbnail.close();
        } catch (IOException e) {
          LOGGER.warn("Could not close thumbnail: {}", thumbnail.getTargetName(), e);
        }
      }
    }
  }

  PdfCharacteristics findPdfCharacteristics(File content) throws MediaExtractionException {
    try (OpenPdfFile openPdf = openPdfFile(content)) {

      // Go by each page: if we find the data we need, we can stop.
      for (int i = 1; i <= openPdf.getNumberOfPages(); i++) {
        openPdf.getPdfParser().processContent(i, openPdf.getPdfListener());
        if (openPdf.getPdfListener().getDpi() != null && openPdf.getPdfListener().hasText()) {
          break;
        }
      }

      // Done.
      return new PdfCharacteristics(openPdf.getPdfListener().hasText(),
          openPdf.getPdfListener().getDpi());
    } catch (IOException e) {
      throw new MediaExtractionException("Problem while reading PDF file.", e);
    }
  }

  OpenPdfFile openPdfFile(File content) throws IOException {
    return new OpenPdfFile(content);
  }

  static class OpenPdfFile implements Closeable {

    private final PdfReader pdfReader;
    private PdfListener pdfListener;
    private PdfReaderContentParser pdfParser;

    OpenPdfFile(File content) throws IOException {
      pdfReader = new PdfReader(content.getAbsolutePath());
    }

    int getNumberOfPages() {
      return pdfReader.getNumberOfPages();
    }

    PdfListener getPdfListener() {
      pdfListener = pdfListener == null ? new PdfListener() : pdfListener;
      return pdfListener;
    }

    PdfReaderContentParser getPdfParser() {
      pdfParser = pdfParser == null ? new PdfReaderContentParser(pdfReader) : pdfParser;
      return pdfParser;
    }

    @Override
    public void close() {
      pdfReader.close();
    }
  }

  static class PdfCharacteristics {

    private final boolean containsText;
    private final Integer resolution;

    PdfCharacteristics(boolean containsText, Integer resolution) {
      this.containsText = containsText;
      this.resolution = resolution;
    }

    boolean containsText() {
      return containsText;
    }

    Integer getResolution() {
      return resolution;
    }
  }

  /**
   * This pdf listener obtains and stores the resolution of the first image it encounters in the
   * PDF, as well as whether any textual content is encountered in the PDF. If it is applied to each
   * page of the PDF in order, it will therefore find the resolution of the PDF's first image.
   */
  static class PdfListener implements RenderListener {

    private Integer dpi; // initially null.
    private boolean hasText; // initially false.

    Integer getDpi() {
      return dpi;
    }

    boolean hasText() {
      return hasText;
    }

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
