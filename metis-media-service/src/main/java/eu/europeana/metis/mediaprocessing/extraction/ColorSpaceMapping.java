package eu.europeana.metis.mediaprocessing.extraction;

import eu.europeana.corelib.definitions.jibx.ColorSpaceType;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import java.util.Arrays;

/**
 * Enum for the supported values of image color space. It consists of a mapping of the ImageMagick
 * names of color spaces to their equivalent instances of {@link ColorSpaceType}.
 */
public enum ColorSpaceMapping {

  CMY("CMY", ColorSpaceType.CMY),
  CMYK("CMYK", ColorSpaceType.CMYK),
  GRAYSCALE("Gray", ColorSpaceType.GRAYSCALE),
  HCL("HCL", ColorSpaceType.HCL),
  HC_LP("HCLp", ColorSpaceType.HC_LP),
  HSB("HSB", ColorSpaceType.HSB),
  HSI("HSI", ColorSpaceType.HSI),
  HSL("HSL", ColorSpaceType.HSL),
  HSV("HSV", ColorSpaceType.HSV),
  HWB("HWB", ColorSpaceType.HWB),
  LAB("Lab", ColorSpaceType.LAB),
  CIE_LAB("CIELab", ColorSpaceType.LAB),
  LCH("LCH", ColorSpaceType.LC_HAB),
  LC_HAB("LCHab", ColorSpaceType.LC_HAB),
  LC_HUV("LCHuv", ColorSpaceType.LC_HUV),
  LMS("LMS", ColorSpaceType.LMS),
  LOG("Log", ColorSpaceType.LOG),
  LUV("Luv", ColorSpaceType.LUV),
  OHTA("OHTA", ColorSpaceType.OHTA),
  REC601_LUMA("Rec601Luma", ColorSpaceType.REC601_LUMA),
  REC601_Y_CB_CR("Rec601YCbCr", ColorSpaceType.REC601_Y_CB_CR),
  REC709_LUMA("Rec709Luma", ColorSpaceType.REC709_LUMA),
  REC709_Y_CB_CR("Rec709YCbCr", ColorSpaceType.REC709_Y_CB_CR),
  RGB("RGB", ColorSpaceType.RGB),
  SC_RGB("scRGB", ColorSpaceType.SC_RGB),
  S_RGB("sRGB", ColorSpaceType.S_RGB),
  XYZ("XYZ", ColorSpaceType.XYZ),
  XY_Y("xyY", ColorSpaceType.XY_Y),
  Y_CB_CR("YCbCr", ColorSpaceType.Y_CB_CR),
  Y_DB_DR("YDbDr", ColorSpaceType.Y_DB_DR),
  YCC("YCC", ColorSpaceType.YCC),
  YIQ("YIQ", ColorSpaceType.YIQ),
  Y_PB_PR("YPbPr", ColorSpaceType.Y_PB_PR),
  YUV("YUV", ColorSpaceType.YUV),
  TRANSPARENT("Transparent", ColorSpaceType.OTHER),
  UNDEFINED("Undefined", ColorSpaceType.OTHER);

  private final String imageMagickName;
  private final ColorSpaceType mappedColorSpace;

  ColorSpaceMapping(String imageMagickName, ColorSpaceType mappedColorSpace) {
    this.imageMagickName = imageMagickName;
    this.mappedColorSpace = mappedColorSpace;
  }

  /**
   * This method  performs the mapping of the ImageMagick name of the color space to an instance of
   * {@link ColorSpaceType}. If the name is unknown, {@link ColorSpaceType#OTHER} will be returned.
   *
   * @param imageMagickName The ImageMagickName for which to obtain the mapping. Can not be null.
   * @return The mapped {@link ColorSpaceType}. Is not null.
   * @throws MediaExtractionException In case the ImageMagick name is null.
   */
  public static ColorSpaceType getColorSpaceType(String imageMagickName)
      throws MediaExtractionException {
    if (imageMagickName == null) {
      throw new MediaExtractionException("Color space can not be null.");
    }
    return Arrays.stream(ColorSpaceMapping.values())
        .filter(mapping -> imageMagickName.equalsIgnoreCase(mapping.imageMagickName))
        .map(mapping -> mapping.mappedColorSpace).findAny().orElse(ColorSpaceType.OTHER);
  }
}
