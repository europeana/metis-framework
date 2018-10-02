package eu.europeana.metis.mediaservice;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.Matrix;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;

class TextProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(TextProcessor.class);

  private static final int DISPLAY_DPI = 72;

  private final ThumbnailGenerator thumbnailGenerator;

  TextProcessor(ThumbnailGenerator thumbnailGenerator) {
    this.thumbnailGenerator = thumbnailGenerator;
  }

  static boolean isText(String mimeType) {
    switch (mimeType) {
      case "application/xml":
      case "application/rtf":
      case "application/epub":
      case "application/pdf":
        return true;
      default:
        return mimeType.startsWith("text/");
    }
  }

  void processText(String url, Collection<UrlType> urlTypes, String mimeType, File content,
      EdmObject edm) throws IOException, MediaException {

    // Sanity checks
    if (!UrlType.shouldExtractMetadata(urlTypes)) {
      return;
    }
    if (content == null) {
      throw new MediaException("File content is null", "File content cannot be null");
    }
    
    // Create thumbnails in case of PDF file.
    if ("application/pdf".equals(mimeType)) {
      thumbnailGenerator.generateThumbnails(url, mimeType, content);
    }

    // Set the resource type and size
    final WebResource resource = edm.getWebResource(url);
    resource.setMimeType(mimeType);
    resource.setFileSize(content.length());

    // Set the resource properties relating to content.
    if ("application/pdf".equals(mimeType)) {
      boolean containsText = false;
      Integer resolution = null;
      PdfReader reader = new PdfReader(content.getAbsolutePath());
      try {
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
      } finally {
        reader.close();
      }
      resource.setContainsText(containsText);
      resource.setResolution(resolution);
    } else {
      resource.setContainsText(mimeType.startsWith("text/"));
      resource.setResolution(null);
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
