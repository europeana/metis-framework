package eu.europeana.metis.mediaprocessing.extraction;

import eu.europeana.metis.schema.jibx.ColorSpaceType;
import java.awt.color.ColorSpace;
import java.io.IOException;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import org.w3c.dom.Node;

/**
 * Reads the source colour-space family.
 */
class ImageColorSpaceReader {

  /**
   * Reads the source color space family from the provided {@link ImageReader}. The method determines the color space by
   * inspecting available metadata or fallback mechanisms based on the image type and associated color model.
   *
   * @param reader the {@link ImageReader} used to read image metadata and type specifications.
   * @return the {@link ColorSpaceType} representing the color space family of the image.
   * @throws IOException if an I/O error occurs while reading the image metadata or type information.
   */
  ColorSpaceType read(ImageReader reader) throws IOException {
    final String name = standardColorSpaceName(reader.getImageMetadata(0));
    return name != null ? fromStandardMetadata(name, reader) : fromRawType(reader.getRawImageType(0));
  }

  private String standardColorSpaceName(IIOMetadata metadata) {
    if (metadata == null || !metadata.isStandardMetadataFormatSupported()) {
      return null;
    }
    final Node tree = metadata.getAsTree("javax_imageio_1.0");
    final Node type = child(child(tree, "Chroma"), "ColorSpaceType");
    return type == null ? null : type.getAttributes().getNamedItem("name").getNodeValue();
  }

  private ColorSpaceType fromStandardMetadata(String name, ImageReader reader) throws IOException {
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
      case "RGB", "YCbCr" -> rgbColorSpace(reader);
      case "PhotoYCC" -> ColorSpaceType.YCC;
      default -> ColorSpaceType.OTHER;
    };
  }

  private ColorSpaceType fromRawType(ImageTypeSpecifier type) {
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
      case ColorSpace.TYPE_YCbCr -> ColorSpaceType.Y_CB_CR;
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
    return colorSpace.isCS_sRGB() ? ColorSpaceType.S_RGB : ColorSpaceType.RGB;
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
