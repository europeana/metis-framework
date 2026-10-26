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

  private void readImageResolution(Operator operator, List<COSBase> operands) throws IOException {
    if (OperatorName.DRAW_OBJECT.equals(operator.getName()) && !operands.isEmpty()
        && operands.getFirst() instanceof COSName name && getResources().isImageXObject(name)) {
      final COSDictionary image = getResources().getXObject(name).getCOSObject();
      setResolution(image.getInt(COSName.WIDTH), image.getInt(COSName.HEIGHT));
    } else if (OperatorName.BEGIN_INLINE_IMAGE.equals(operator.getName())) {
      final COSDictionary cosDictionary = operator.getImageParameters();
      setResolution(cosDictionary.getInt(COSName.WIDTH, COSName.W, 0),
          cosDictionary.getInt(COSName.HEIGHT, COSName.H, 0));
    }
  }

  private void setResolution(int pixelWidth, int pixelHeight) {
    final Matrix placement = getGraphicsState().getCurrentTransformationMatrix();
    final double displayedWidthInPoints = Math.abs(placement.getScalingFactorX()) * (double) getCurrentPage().getUserUnit();
    final double displayedHeightInPoints = Math.abs(placement.getScalingFactorY()) * (double) getCurrentPage().getUserUnit();
    if (pixelWidth <= 0 || pixelHeight <= 0 || displayedWidthInPoints <= 0 || displayedHeightInPoints <= 0) {
      return;
    }
    final long dpi = Math.round(Math.min(pixelWidth * (double) POINTS_PER_INCH / displayedWidthInPoints,
        pixelHeight * (double) POINTS_PER_INCH / displayedHeightInPoints));
    if (dpi > 0 && dpi <= Integer.MAX_VALUE) {
      resolution = (int) dpi;
    }
  }

  record PdfMetadata(boolean containsText, Integer resolution) {

  }
}
