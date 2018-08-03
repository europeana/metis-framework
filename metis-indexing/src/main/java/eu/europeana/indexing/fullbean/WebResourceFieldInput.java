package eu.europeana.indexing.fullbean;

import eu.europeana.corelib.definitions.jibx.Type1;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import eu.europeana.corelib.definitions.edm.model.metainfo.ImageOrientation;
import eu.europeana.corelib.definitions.jibx.CodecName;
import eu.europeana.corelib.definitions.jibx.ColorSpaceType;
import eu.europeana.corelib.definitions.jibx.DoubleType;
import eu.europeana.corelib.definitions.jibx.Duration;
import eu.europeana.corelib.definitions.jibx.HasColorSpace;
import eu.europeana.corelib.definitions.jibx.HasMimeType;
import eu.europeana.corelib.definitions.jibx.HexBinaryType;
import eu.europeana.corelib.definitions.jibx.IntegerType;
import eu.europeana.corelib.definitions.jibx.LongType;
import eu.europeana.corelib.definitions.jibx.NonNegativeIntegerType;
import eu.europeana.corelib.definitions.jibx.OrientationType;
import eu.europeana.corelib.definitions.jibx.WebResourceType;
import eu.europeana.corelib.edm.model.metainfo.AudioMetaInfoImpl;
import eu.europeana.corelib.edm.model.metainfo.ImageMetaInfoImpl;
import eu.europeana.corelib.edm.model.metainfo.TextMetaInfoImpl;
import eu.europeana.corelib.edm.model.metainfo.VideoMetaInfoImpl;
import eu.europeana.corelib.edm.model.metainfo.WebResourceMetaInfoImpl;
import eu.europeana.corelib.solr.entity.WebResourceImpl;
import eu.europeana.indexing.utils.MediaType;

/**
 * Converts a {@link WebResourceType} from an {@link eu.europeana.corelib.definitions.jibx.RDF} to a
 * {@link WebResourceImpl} for a {@link eu.europeana.corelib.definitions.edm.beans.FullBean}.
 */
class WebResourceFieldInput implements Function<WebResourceType, WebResourceImpl> {

  // TODO These values come from eu.europeana.metis.mediaservice.WebResource.Orientation.
  // We should reuse these values from there.
  private static final String ORIENTATION_LANDSCAPE = "LANDSCAPE";
  private static final String ORIENTATION_PORTRAIT = "PORTRAIT";

  @Override
  public WebResourceImpl apply(WebResourceType wResourceType) {

    WebResourceImpl webResource = new WebResourceImpl();
    webResource.setAbout(wResourceType.getAbout());

    Map<String, List<String>> desMap =
        FieldInputUtils.createResourceOrLiteralMapFromList(wResourceType.getDescriptionList());

    webResource.setDcDescription(desMap);

    Map<String, List<String>> forMap =
        FieldInputUtils.createResourceOrLiteralMapFromList(wResourceType.getFormatList());

    webResource.setDcFormat(forMap);

    Map<String, List<String>> sourceMap =
        FieldInputUtils.createResourceOrLiteralMapFromList(wResourceType.getSourceList());

    webResource.setDcSource(sourceMap);

    Map<String, List<String>> conformsToMap =
        FieldInputUtils.createResourceOrLiteralMapFromList(wResourceType.getConformsToList());

    webResource.setDctermsConformsTo(conformsToMap);

    Map<String, List<String>> createdMap =
        FieldInputUtils.createResourceOrLiteralMapFromList(wResourceType.getCreatedList());

    webResource.setDctermsCreated(createdMap);

    Map<String, List<String>> extentMap =
        FieldInputUtils.createResourceOrLiteralMapFromList(wResourceType.getExtentList());

    webResource.setDctermsExtent(extentMap);

    Map<String, List<String>> hasPartMap =
        FieldInputUtils.createResourceOrLiteralMapFromList(wResourceType.getHasPartList());

    webResource.setDctermsHasPart(hasPartMap);

    Map<String, List<String>> isFormatOfMap =
        FieldInputUtils.createResourceOrLiteralMapFromList(wResourceType.getIsFormatOfList());

    webResource.setDctermsIsFormatOf(isFormatOfMap);

    Map<String, List<String>> isPartOfMap = FieldInputUtils
        .createResourceOrLiteralMapFromList(wResourceType.getIsPartOfList());

    webResource.setDctermsIsPartOf(isPartOfMap);

    Map<String, List<String>> issuedMap =
        FieldInputUtils.createResourceOrLiteralMapFromList(wResourceType.getIssuedList());

    webResource.setDctermsIssued(issuedMap);
    Map<String, List<String>> rightMap =
        FieldInputUtils.createResourceOrLiteralMapFromList(wResourceType.getRightList());

    webResource.setWebResourceDcRights(rightMap);

    Map<String, List<String>> typeMap =
        FieldInputUtils.createResourceOrLiteralMapFromList(wResourceType.getTypeList());
    webResource.setDcType(typeMap);

    Map<String, List<String>> edmRightsMap =
        FieldInputUtils.createResourceMapFromString(wResourceType.getRights());

    webResource.setWebResourceEdmRights(edmRightsMap);

    if (wResourceType.getIsNextInSequence() != null) {
      webResource.setIsNextInSequence(wResourceType.getIsNextInSequence().getResource());
    }
    if (wResourceType.getSameAList() != null) {
      webResource.setOwlSameAs(FieldInputUtils.resourceListToArray(wResourceType.getSameAList()));
    }

    webResource.setDcCreator(
        FieldInputUtils.createResourceOrLiteralMapFromList(wResourceType.getCreatorList()));

    if (wResourceType.getHasServiceList() != null) {
      webResource.setSvcsHasService(
          FieldInputUtils.resourceListToArray(wResourceType.getHasServiceList()));
    }
    if (wResourceType.getIsReferencedByList() != null) {
      webResource.setDctermsIsReferencedBy(
          FieldInputUtils.resourceOrLiteralListToArray(wResourceType.getIsReferencedByList()));
    }
    if (wResourceType.getPreview() != null) {
      webResource.setEdmPreview(wResourceType.getPreview().getResource());
    }

    webResource.setWebResourceMetaInfo(createWebresourceMetaInfo(wResourceType));

    return webResource;
  }

  private WebResourceMetaInfoImpl createWebresourceMetaInfo(WebResourceType webResource) {

    // Get the media type and determine meta data creator
    final MediaType mediaType = Optional.ofNullable(webResource.getHasMimeType())
        .map(HasMimeType::getHasMimeType).map(MediaType::getMediaType).orElse(MediaType.OTHER);
    final BiConsumer<WebResourceType, WebResourceMetaInfoImpl> metaDataCreator;
    switch (mediaType) {
      case AUDIO:
        metaDataCreator = this::addAudioMetaInfo;
        break;
      case IMAGE:
        metaDataCreator = this::addImageMetaInfo;
        break;
      case TEXT:
        metaDataCreator = this::addTextMetaInfo;
        break;
      case VIDEO:
        metaDataCreator = this::addVideoMetaInfo;
        break;
      default:
        metaDataCreator = null;
    }

    // If we have a creator, use it.
    if (metaDataCreator != null) {
      final WebResourceMetaInfoImpl result = new WebResourceMetaInfoImpl();
      metaDataCreator.accept(webResource, result);
      return result;
    }

    // Otherwise, return null.
    return null;
  }

  private void addAudioMetaInfo(WebResourceType source, WebResourceMetaInfoImpl target) {
    AudioMetaInfoImpl metaInfo = new AudioMetaInfoImpl();

    metaInfo.setMimeType(convertToString(source.getHasMimeType()));
    metaInfo.setFileSize(convertToLong(source.getFileByteSize()));

    metaInfo.setDuration(convertToLong(source.getDuration()));
    metaInfo.setSampleRate(convertToInteger(source.getSampleRate()));
    metaInfo.setBitRate(convertToInteger(source.getBitRate()));
    metaInfo.setBitDepth(convertToInteger(source.getSampleSize()));
    metaInfo.setChannels(convertToInteger(source.getAudioChannelNumber()));

    target.setAudioMetaInfo(metaInfo);
  }

  private void addImageMetaInfo(WebResourceType source, WebResourceMetaInfoImpl target) {
    ImageMetaInfoImpl metaInfo = new ImageMetaInfoImpl();

    metaInfo.setMimeType(convertToString(source.getHasMimeType()));
    metaInfo.setFileSize(convertToLong(source.getFileByteSize()));

    metaInfo.setHeight(convertToInteger(source.getHeight()));
    metaInfo.setWidth(convertToInteger(source.getWidth()));
    metaInfo.setColorSpace(Optional.ofNullable(source.getHasColorSpace())
        .map(HasColorSpace::getHasColorSpace).map(ColorSpaceType::xmlValue)
        .map(value -> "grayscale".equals(value) ? "Gray" : value).orElse(null));
    // TODO: 3-8-18 Gray is used because of backwards compatibility but the actual value defined in the xsd is grayscale

    final Stream<HexBinaryType> sourceColors = Optional.ofNullable(source.getComponentColorList())
        .map(List::stream).orElse(Stream.empty());
    final String[] targetColors = sourceColors.filter(Objects::nonNull)
        .map(HexBinaryType::getString).filter(StringUtils::isNotBlank).toArray(String[]::new);
    metaInfo.setColorPalette(targetColors.length == 0 ? null : targetColors);

    final String sourceOrientation =
        Optional.ofNullable(source.getOrientation()).map(OrientationType::getString).orElse(null);
    final ImageOrientation targetOrientation;
    if (ORIENTATION_LANDSCAPE.equalsIgnoreCase(sourceOrientation)) {
      targetOrientation = ImageOrientation.LANDSCAPE;
    } else if (ORIENTATION_PORTRAIT.equalsIgnoreCase(sourceOrientation)) {
      targetOrientation = ImageOrientation.PORTRAIT;
    } else {
      targetOrientation = null;
    }
    metaInfo.setOrientation(targetOrientation);

    target.setImageMetaInfo(metaInfo);
  }

  private void addTextMetaInfo(WebResourceType source, WebResourceMetaInfoImpl target) {
    TextMetaInfoImpl metaInfo = new TextMetaInfoImpl();

    metaInfo.setMimeType(convertToString(source.getHasMimeType()));
    metaInfo.setFileSize(convertToLong(source.getFileByteSize()));
    metaInfo.setResolution(convertToInteger(source.getSpatialResolution()));
    metaInfo.setRdfType(Optional.ofNullable(source.getType()).map(Type1::getResource).orElse(null));

    target.setTextMetaInfo(metaInfo);
  }

  private void addVideoMetaInfo(WebResourceType source, WebResourceMetaInfoImpl target) {
    VideoMetaInfoImpl metaInfo = new VideoMetaInfoImpl();

    metaInfo.setMimeType(convertToString(source.getHasMimeType()));
    metaInfo.setFileSize(convertToLong(source.getFileByteSize()));

    metaInfo.setCodec(
        Optional.ofNullable(source.getCodecName()).map(CodecName::getCodecName).orElse(null));
    metaInfo.setWidth(convertToInteger(source.getWidth()));
    metaInfo.setHeight(convertToInteger(source.getHeight()));
    metaInfo.setBitRate(convertToInteger(source.getBitRate()));
    metaInfo.setFrameRate(convertToDouble(source.getFrameRate()));
    metaInfo.setDuration(convertToLong(source.getDuration()));

    target.setVideoMetaInfo(metaInfo);
  }

  private static Long convertToLong(Duration duration) {
    if (duration == null || StringUtils.isBlank(duration.getDuration())) {
      return null;
    }
    try {
      return Long.parseLong(duration.getDuration());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static String convertToString(HasMimeType data) {
    return Optional.ofNullable(data).map(HasMimeType::getHasMimeType).orElse(null);
  }

  private static Double convertToDouble(DoubleType data) {
    return Optional.ofNullable(data).map(DoubleType::getDouble).orElse(null);
  }

  private static Long convertToLong(LongType data) {
    return Optional.ofNullable(data).map(LongType::getLong).orElse(null);
  }

  private static Integer convertToInteger(IntegerType data) {
    return Optional.ofNullable(data).map(IntegerType::getLong).map(Long::intValue).orElse(null);
  }

  private static Integer convertToInteger(NonNegativeIntegerType data) {
    return Optional.ofNullable(data).map(NonNegativeIntegerType::getInteger)
        .map(BigInteger::intValue).orElse(null);
  }
}
