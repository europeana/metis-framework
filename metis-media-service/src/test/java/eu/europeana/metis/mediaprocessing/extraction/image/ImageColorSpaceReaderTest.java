package eu.europeana.metis.mediaprocessing.extraction.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.europeana.metis.schema.jibx.ColorSpaceType;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.InputStream;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileCacheImageInputStream;
import javax.imageio.stream.ImageInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ImageColorSpaceReaderTest {

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({
      "srgb.jpeg,S_RGB",
      "srgb.png,S_RGB",
      "srgb-profile.jpeg,S_RGB",
      "linear-rgb.jpeg,RGB",
      "adobe-rgb.jpeg,RGB",
      "photo-ycc.jpeg,YCC",
      "grayscale.png,GRAYSCALE",
      "grayscale.tiff,GRAYSCALE",
      "cmyk.jpeg,CMYK",
      "ycck.jpeg,CMYK",
      "lab.tiff,LAB",
      "source.svg,S_RGB"
  })
  void readsSourceColorSpaceUsingInstalledImageReader(String fileName, ColorSpaceType expected) throws Exception {
    try (InputStream source = getClass().getResourceAsStream("/images/color-spaces/" + fileName)) {
      assertNotNull(source, "Missing colour-space file: " + fileName);
      try (ImageInputStream input = new FileCacheImageInputStream(source, null)) {
        final Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
        assertTrue(readers.hasNext(), "No ImageIO reader supports fixture: " + fileName);
        final ImageReader reader = readers.next();
        try {
          reader.setInput(input);
          assertEquals(expected, new ImageColorSpaceReader().read(reader));
        } finally {
          reader.dispose();
        }
      }
    }
  }

  @Test
  void doesNotMisreportUnknownSourceSpaceAsRgbDestination() throws Exception {
    final ImageReader reader = mock(ImageReader.class);
    final IIOMetadata metadata = mock(IIOMetadata.class);
    final IIOMetadataNode tree = new IIOMetadataNode("javax_imageio_1.0");
    final IIOMetadataNode chroma = new IIOMetadataNode("Chroma");
    final IIOMetadataNode type = new IIOMetadataNode("ColorSpaceType");
    type.setAttribute("name", "4CLR");
    chroma.appendChild(type);
    tree.appendChild(chroma);
    when(reader.getImageMetadata(0)).thenReturn(metadata);
    when(metadata.isStandardMetadataFormatSupported()).thenReturn(true);
    when(metadata.getAsTree("javax_imageio_1.0")).thenReturn(tree);
    when(reader.getRawImageType(0)).thenReturn(ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB));

    assertEquals(ColorSpaceType.OTHER, new ImageColorSpaceReader().read(reader));
    verify(reader, never()).getRawImageType(0);
    verify(reader, never()).read(0);
  }

  @Test
  void doesNotMisreportRawYcbCrAsSrgbWhenStandardMetadataIsMissing() throws Exception {
    final ImageReader reader = mock(ImageReader.class);
    final ColorSpace colorSpace = mock(ColorSpace.class);
    when(colorSpace.getType()).thenReturn(ColorSpace.TYPE_YCbCr);
    when(colorSpace.getNumComponents()).thenReturn(3);
    final ImageTypeSpecifier type = ImageTypeSpecifier.createInterleaved(
        colorSpace, new int[] {0, 1, 2}, DataBuffer.TYPE_BYTE, false, false);
    when(reader.getRawImageType(0)).thenReturn(type);

    assertEquals(ColorSpaceType.Y_CB_CR, new ImageColorSpaceReader().read(reader));
    verify(reader, never()).read(0);
  }

  @Test
  void returnsUnknownWhenReaderExposesNoSourceColorInformation() throws Exception {
    final ImageReader reader = mock(ImageReader.class);
    assertEquals(ColorSpaceType.OTHER, new ImageColorSpaceReader().read(reader));
    verify(reader, never()).read(0);
  }
}
