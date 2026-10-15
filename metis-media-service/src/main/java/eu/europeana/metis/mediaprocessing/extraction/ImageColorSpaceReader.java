package eu.europeana.metis.mediaprocessing.extraction;

import eu.europeana.metis.schema.jibx.ColorSpaceType;
import java.awt.color.ColorSpace;
import java.io.IOException;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import org.w3c.dom.Node;

/** Reads the source colour-space family without inspecting decoded or resized pixels. */
class ImageColorSpaceReader {

  ColorSpaceType read(ImageReader reader) throws IOException {
    final IIOMetadata metadata = reader.getImageMetadata(0);
    if (metadata != null && metadata.isStandardMetadataFormatSupported()) {
      final Node tree = metadata.getAsTree("javax_imageio_1.0");
      final Node chroma = child(tree, "Chroma");
      final Node type = child(chroma, "ColorSpaceType");
      if (type != null) {
        final String name = type.getAttributes().getNamedItem("name").getNodeValue();
        return switch (name) {
          case "GRAY" -> ColorSpaceType.GRAYSCALE;
          case "CMYK", "YCCK" -> ColorSpaceType.CMYK;
          case "CMY" -> ColorSpaceType.CMY;
          case "Lab" -> ColorSpaceType.LAB;
          case "Luv" -> ColorSpaceType.LUV;
          case "XYZ" -> ColorSpaceType.XYZ;
          case "Yxy" -> ColorSpaceType.XY_Y;
          case "HSV" -> ColorSpaceType.HSV;
          case "HLS" -> ColorSpaceType.HSL;
          // Preserve the existing broad RGB/sRGB classification for RGB and JPEG encodings.
          case "RGB", "YCbCr", "PhotoYCC" -> rgbColorSpace(reader);
          // An unknown source space must not become RGB just because the decoder offers RGB output.
          default -> ColorSpaceType.OTHER;
        };
      }
    }

    // Readers may omit standard metadata (for example, SVG). Consult their source image type,
    // which may include an embedded ICC profile, rather than the converted BufferedImage.
    final ImageTypeSpecifier type = reader.getRawImageType(0);
    if (type == null) {
      return ColorSpaceType.OTHER;
    }
    final ColorSpace colorSpace = type.getColorModel().getColorSpace();
    return switch (colorSpace.getType()) {
      case ColorSpace.TYPE_GRAY -> ColorSpaceType.GRAYSCALE;
      case ColorSpace.TYPE_CMYK -> ColorSpaceType.CMYK;
      case ColorSpace.TYPE_CMY -> ColorSpaceType.CMY;
      case ColorSpace.TYPE_Lab -> ColorSpaceType.LAB;
      case ColorSpace.TYPE_Luv -> ColorSpaceType.LUV;
      case ColorSpace.TYPE_XYZ -> ColorSpaceType.XYZ;
      case ColorSpace.TYPE_Yxy -> ColorSpaceType.XY_Y;
      case ColorSpace.TYPE_YCbCr -> ColorSpaceType.S_RGB;
      case ColorSpace.TYPE_RGB -> rgbColorSpace(colorSpace);
      case ColorSpace.TYPE_HSV -> ColorSpaceType.HSV;
      case ColorSpace.TYPE_HLS -> ColorSpaceType.HSL;
      default -> ColorSpaceType.OTHER;
    };
  }

  private ColorSpaceType rgbColorSpace(ImageReader reader) throws IOException {
    final ImageTypeSpecifier type = reader.getRawImageType(0);
    return type == null ? ColorSpaceType.S_RGB : rgbColorSpace(type.getColorModel().getColorSpace());
  }

  private ColorSpaceType rgbColorSpace(ColorSpace colorSpace) {
    // Retain the existing broad RGB/sRGB classification; this does not report an ICC profile's name.
    return colorSpace.equals(ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB)) ? ColorSpaceType.RGB : ColorSpaceType.S_RGB;
  }

  private Node child(Node parent, String name) {
    if (parent != null) {
      for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
        if (name.equals(node.getNodeName())) {
          return node;
        }
      }
    }
    return null;
  }
}
