package eu.europeana.metis.mediaprocessing.extraction.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.metis.mediaprocessing.extraction.pdf.PdfMetadataReader.PdfMetadata;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDFormContentStream;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class PdfMetadataReaderTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource({
      "Equal horizontal and vertical DPI, 600, 300, 144, 72, 300",
      "Square placement uses lower vertical DPI, 600, 300, 144, 144, 150",
      "Portrait image uses lower vertical DPI, 144, 216, 36, 108, 144",
      "Fractional DPI rounds up, 601, 301, 144, 72, 301",
      "Large placement produces low DPI, 48, 40, 864, 576, 4",
      "Fractional placement dimensions, 2048, 1536, 345.6, 259.2, 427",
      "Half a DPI rounds to one, 1, 1, 144, 144, 1"
  })
  void preservesLegacyDpiForUnrotatedImages(String scenario, int pixelWidth, int pixelHeight,
      float width, float height, int expectedDpi) throws Exception {
    try (PDDocument document = new PDDocument()) {
      final PDPage pdPage = new PDPage();
      document.addPage(pdPage);
      try (PDPageContentStream content = new PDPageContentStream(document, pdPage)) {
        content.drawImage(image(document, pixelWidth, pixelHeight), 0, 0, width, height);
      }
      assertEquals(expectedDpi, new PdfMetadataReader().read(document).resolution());
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"(Hello) Tj", "[(Hello) -120 (world)] TJ", "(Hello) '", "1 2 (Hello) \"", "( ) Tj"})
  void detectsTextForAllTextShowingOperators(String instruction) throws Exception {
    try (PDDocument document = new PDDocument()) {
      addTextPage(document, instruction);
      final PdfMetadata pdfMetadata = new PdfMetadataReader().read(document);
      assertTrue(pdfMetadata.containsText());
      assertNull(pdfMetadata.resolution());
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "() Tj", "[() -120 ()] TJ", "() '", "1 2 () \""})
  void doesNotTreatEmptyTextOrPositioningAsText(String instruction) throws Exception {
    try (PDDocument document = new PDDocument()) {
      addTextPage(document, instruction);
      final PdfMetadata pdfMetadata = new PdfMetadataReader().read(document);
      assertFalse(pdfMetadata.containsText());
      assertNull(pdfMetadata.resolution());
    }
  }

  @Test
  void readsFirstDrawnImageAndUsesLowerOfHorizontalAndVerticalDpi() throws Exception {
    try (PDDocument document = new PDDocument()) {
      final PDPage pdPage = new PDPage();
      document.addPage(pdPage);
      try (PDPageContentStream content = new PDPageContentStream(document, pdPage)) {
        content.drawImage(image(document, 600, 300), 0, 0, 144, 144);
        content.drawImage(image(document, 600, 300), 0, 0, 72, 72);
      }
      // An unused image resource must not influence which image is considered first.
      pdPage.getResources().put(COSName.getPDFName("Unused"), image(document, 1, 1));
      final PdfMetadata pdfMetadata = new PdfMetadataReader().read(document);
      assertFalse(pdfMetadata.containsText());
      assertEquals(150, pdfMetadata.resolution());
    }
  }

  @Test
  void accountsForRotationReflectionAndPageUserUnits() throws Exception {
    try (PDDocument document = new PDDocument()) {
      final PDPage pdPage = new PDPage();
      pdPage.setUserUnit(2);
      pdPage.setRotation(90);
      document.addPage(pdPage);
      try (PDPageContentStream content = new PDPageContentStream(document, pdPage)) {
        content.drawImage(image(document, 600, 300), new Matrix(0, -144, -72, 0, 200, 200));
      }
      assertEquals(150, new PdfMetadataReader().read(document).resolution());
    }
  }

  @Test
  void handlesMirroredAxisAlignedImages() throws Exception {
    try (PDDocument document = new PDDocument()) {
      final PDPage pdPage = new PDPage();
      document.addPage(pdPage);
      try (PDPageContentStream content = new PDPageContentStream(document, pdPage)) {
        content.drawImage(image(document, 600, 300), new Matrix(-144, 0, 0, 72, 200, 200));
      }
      assertEquals(300, new PdfMetadataReader().read(document).resolution());
    }
  }

  @Test
  void findsTextAndImagesInsideNestedTransformedForms() throws Exception {
    try (PDDocument document = new PDDocument()) {
      final PDFormXObject inner = new PDFormXObject(document);
      inner.setBBox(new PDRectangle(300, 300));
      inner.setResources(new PDResources());
      try (PDFormContentStream content = new PDFormContentStream(inner)) {
        content.beginText();
        content.setFont(new PDType1Font(FontName.HELVETICA), 12);
        content.showText("Inside form");
        content.endText();
        content.drawImage(image(document, 600, 300), 0, 0, 144, 72);
      }
      final PDFormXObject outer = new PDFormXObject(document);
      outer.setBBox(new PDRectangle(600, 600));
      outer.setResources(new PDResources());
      try (PDFormContentStream content = new PDFormContentStream(outer)) {
        content.transform(Matrix.getScaleInstance(2, 2));
        content.drawForm(inner);
      }
      final PDPage pdPage = new PDPage();
      document.addPage(pdPage);
      try (PDPageContentStream content = new PDPageContentStream(document, pdPage)) {
        content.drawForm(outer);
      }
      final PdfMetadata pdfMetadata = new PdfMetadataReader().read(document);
      assertTrue(pdfMetadata.containsText());
      assertEquals(150, pdfMetadata.resolution());
    }
  }

  @Test
  void skipsImagesWithZeroSizePlacement() throws Exception {
    try (PDDocument document = new PDDocument()) {
      final PDPage pdPage = new PDPage();
      document.addPage(pdPage);
      try (PDPageContentStream content = new PDPageContentStream(document, pdPage)) {
        content.drawImage(image(document, 600, 300), 0, 0, 0, 0);
        content.drawImage(image(document, 600, 300), 0, 0, 144, 72);
      }
      assertEquals(300, new PdfMetadataReader().read(document).resolution());
    }
  }

  @Test
  void continuesAcrossPagesAndStopsOnceBothValuesAreKnown() throws Exception {
    try (PDDocument document = new PDDocument()) {
      final PDPage first = new PDPage();
      document.addPage(first);
      try (PDPageContentStream content = new PDPageContentStream(document, first)) {
        content.drawImage(image(document, 600, 300), 0, 0, 144, 72);
      }
      final PDPage second = addTextPage(document, "(Later text) Tj");
      addTextPage(document, "(Not needed) Tj");
      final List<PDPage> visitedPages = new ArrayList<>();
      final PdfMetadataReader reader = new PdfMetadataReader() {
        @Override
        public void processPage(PDPage page) throws IOException {
          visitedPages.add(page);
          super.processPage(page);
        }
      };
      final PdfMetadata pdfMetadata = reader.read(document);
      assertTrue(pdfMetadata.containsText());
      assertEquals(300, pdfMetadata.resolution());
      assertEquals(List.of(first, second), visitedPages);
    }
  }

  @Test
  void continuesAcrossPagesWhenTextPrecedesTheFirstImage() throws Exception {
    try (PDDocument document = new PDDocument()) {
      addTextPage(document, "(First page text) Tj");
      final PDPage imagePage = new PDPage();
      document.addPage(imagePage);
      try (PDPageContentStream content = new PDPageContentStream(document, imagePage)) {
        content.drawImage(image(document, 600, 300), 0, 0, 144, 72);
      }
      final PdfMetadata pdfMetadata = new PdfMetadataReader().read(document);
      assertTrue(pdfMetadata.containsText());
      assertEquals(300, pdfMetadata.resolution());
    }
  }

  @Test
  void resetsMetadataWhenReadingAnotherDocument() throws Exception {
    try (PDDocument first = new PDDocument(); PDDocument second = new PDDocument()) {
      final PDPage page = addTextPage(first, "(Text) Tj");
      try (PDPageContentStream content = new PDPageContentStream(first, page, PDPageContentStream.AppendMode.APPEND, true)) {
        content.drawImage(image(first, 600, 300), 0, 0, 144, 72);
      }
      second.addPage(new PDPage());
      final PdfMetadataReader reader = new PdfMetadataReader();
      assertEquals(new PdfMetadataReader.PdfMetadata(true, 300), reader.read(first));
      assertEquals(new PdfMetadataReader.PdfMetadata(false, null), reader.read(second));
    }
  }

  private PDPage addTextPage(PDDocument document, String instruction) throws IOException {
    final PDPage page = new PDPage();
    document.addPage(page);
    page.setResources(new PDResources());
    page.getResources().put(COSName.getPDFName("F1"), new PDType1Font(FontName.HELVETICA));
    final byte[] bytes = ("BT /F1 12 Tf " + instruction + " ET").getBytes(StandardCharsets.ISO_8859_1);
    page.setContents(new PDStream(document, new ByteArrayInputStream(bytes)));
    return page;
  }

  private PDImageXObject image(PDDocument document, int width, int height) throws IOException {
    final PDImageXObject image = new PDImageXObject(document);
    image.setWidth(width);
    image.setHeight(height);
    // Deliberately omit pixel data: reading technical metadata must not decode the image.
    return image;
  }
}
