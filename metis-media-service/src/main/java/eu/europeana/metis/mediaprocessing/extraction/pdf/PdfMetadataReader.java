package eu.europeana.metis.mediaprocessing.extraction.pdf;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.OperatorName;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.util.Matrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class for extracting metadata from PDF documents, such as the presence of textual content and the resolution of
 * images. This class extends {@code PDFTextStripper} to analyze PDF content and identify specific data without decoding all text
 * or collecting text strings.
 */
class PdfMetadataReader extends PDFTextStripper {

  private static final Logger LOGGER = LoggerFactory.getLogger(PdfMetadataReader.class);
  private static final int POINTS_PER_INCH = 72;

  private boolean containsText;
  private Integer resolution;

  /**
   * Finds non-empty text and the first valid positive image resolution in page content order. The resolution belongs to that
   * image's placement on the page; it is not a resolution shared by the entire document.
   *
   * @param document The open document to inspect. The caller retains ownership.
   * @return Whether text was found and the image resolution in DPI, or null if no valid resolution was found.
   * @throws IOException If the document's content streams cannot be processed.
   */
  PdfMetadata read(PDDocument document) throws IOException {
    setShouldSeparateByBeads(false);
    containsText = false;
    resolution = null;
    writeText(document, Writer.nullWriter());
    return new PdfMetadata(containsText, resolution);
  }

  @Override
  protected void processPages(PDPageTree pages) throws IOException {
    for (PDPage page : pages) {
      processPage(page);
      if (containsText && resolution != null) {
        break;
      }
    }
  }

  @Override
  protected void showText(byte[] text) {
    // Preserve non-empty text detection, including whitespace, without decoding glyphs or collecting text.
    containsText = containsText || text.length > 0;
  }

  @Override
  protected void processOperator(Operator operator, List<COSBase> operands) throws IOException {
    if (resolution == null) {
      try {
        readImageResolution(operator, operands);
      } catch (IOException e) {
        LOGGER.info("Could not read PDF image dimensions", e);
      }
    }
    super.processOperator(operator, operands);
  }

  /**
   * Reads image pixel dimensions from a drawing operator without decoding the image pixels. Non-image operators leave the result
   * unchanged.
   *
   * @param operator The current PDF content-stream operator.
   * @param operands The operator's operands, including the resource name for a named image.
   * @throws IOException If a referenced image resource cannot be read.
   */
  private void readImageResolution(Operator operator, List<COSBase> operands) throws IOException {
    // DRAW_OBJECT(Do) is a drawn referenced named XObject. We check if it is an image.
    if (OperatorName.DRAW_OBJECT.equals(operator.getName()) && !operands.isEmpty()
        && operands.getFirst() instanceof COSName name && getResources().isImageXObject(name)) {
      final COSDictionary image = getResources().getXObject(name).getCOSObject();
      setResolution(image.getInt(COSName.WIDTH), image.getInt(COSName.HEIGHT));
    } else if (OperatorName.BEGIN_INLINE_IMAGE.equals(operator.getName())) {
      // BEGIN_INLINE_IMAGE(BI) embeds an image directly in the content stream(not referenced). We check Width/Height and fall back to W/H keys otherwise 0.
      final COSDictionary cosDictionary = operator.getImageParameters();
      setResolution(cosDictionary.getInt(COSName.WIDTH, COSName.W, 0),
          cosDictionary.getInt(COSName.HEIGHT, COSName.H, 0));
    }
  }

  /**
   * Calculates effective DPI from an image's pixel dimensions and its current placement on the page. The transformation matrix
   * maps the image's unit square into page coordinates. Its axis lengths give the displayed width and height. Page UserUnit
   * converts those lengths to points, with 72 points per inch.
   * <p>
   * Use the lower horizontal or vertical DPI, preserving the existing resolution policy for images scaled differently on each
   * axis. Invalid dimensions and non-positive or unrepresentable rounded DPI values leave the result unset so a later image can
   * supply it.
   *
   * @param pixelWidth The image width in pixels.
   * @param pixelHeight The image height in pixels.
   */
  private void setResolution(int pixelWidth, int pixelHeight) {
    // We retrieve the scaling factors from the current transformation matrix. Not just the diagonal entries.
    final Matrix placementMatrix = getGraphicsState().getCurrentTransformationMatrix();
    final double displayedWidthInPoints = Math.abs(placementMatrix.getScalingFactorX()) * (double) getCurrentPage().getUserUnit();
    final double displayedHeightInPoints = Math.abs(placementMatrix.getScalingFactorY()) * (double) getCurrentPage().getUserUnit();
    if (pixelWidth <= 0 || pixelHeight <= 0 || displayedWidthInPoints <= 0 || displayedHeightInPoints <= 0) {
      return;
    }
    // DPI = pixels * 72 / displayed points.
    final double horizontalDpi = pixelWidth * (double) POINTS_PER_INCH / displayedWidthInPoints;
    final double verticalDpi = pixelHeight * (double) POINTS_PER_INCH / displayedHeightInPoints;
    // Round the lower horizontal or vertical DPI to a whole number.
    final long dpi = Math.round(Math.min(horizontalDpi, verticalDpi));
    if (dpi > 0 && dpi <= Integer.MAX_VALUE) {
      resolution = (int) dpi;
    }
  }

  record PdfMetadata(boolean containsText, Integer resolution) {

  }
}
