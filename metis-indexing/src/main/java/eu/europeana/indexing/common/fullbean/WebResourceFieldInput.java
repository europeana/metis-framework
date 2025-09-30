package eu.europeana.indexing.common.fullbean;

import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.model.metainfo.ImageOrientation;
import eu.europeana.corelib.edm.model.metainfo.AudioMetaInfoImpl;
import eu.europeana.corelib.edm.model.metainfo.ImageMetaInfoImpl;
import eu.europeana.corelib.edm.model.metainfo.TextMetaInfoImpl;
import eu.europeana.corelib.edm.model.metainfo.ThreeDMetaInfoImpl;
import eu.europeana.corelib.edm.model.metainfo.VideoMetaInfoImpl;
import eu.europeana.corelib.edm.model.metainfo.WebResourceMetaInfoImpl;
import eu.europeana.corelib.solr.entity.WebResourceImpl;
import eu.europeana.metis.schema.jibx.CodecName;
import eu.europeana.metis.schema.jibx.ColorSpaceType;
import eu.europeana.metis.schema.jibx.DoubleType;
import eu.europeana.metis.schema.jibx.Duration;
import eu.europeana.metis.schema.jibx.EdmType;
import eu.europeana.metis.schema.jibx.HasColorSpace;
import eu.europeana.metis.schema.jibx.HasMimeType;
import eu.europeana.metis.schema.jibx.HexBinaryType;
import eu.europeana.metis.schema.jibx.IntegerType;
import eu.europeana.metis.schema.jibx.LongType;
import eu.europeana.metis.schema.jibx.NonNegativeIntegerType;
import eu.europeana.metis.schema.jibx.NonNegativeIntegerWithoutDataTypeType;
import eu.europeana.metis.schema.jibx.OrientationType;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.schema.jibx.Type1;
import eu.europeana.metis.schema.jibx.Type2;
import eu.europeana.metis.schema.jibx.WebResourceType;
import eu.europeana.metis.schema.model.MediaType;
import eu.europeana.metis.schema.model.Orientation;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

/**
 * Converts a {@link WebResourceType} from an {@link RDF} to a
 * {@link WebResourceImpl} for a {@link FullBean}.
 */
final class WebResourceFieldInput implements Function<WebResourceType, WebResourceImpl> {

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

    if (wResourceType.getDigitalSourceType() != null) {
      webResource.setSchemaDigitalSourceType(wResourceType.getDigitalSourceType().getResource());
    }

    if (wResourceType.getIntendedUsageList() != null) {
      webResource.setEdmIntendedUsage(FieldInputUtils.resourceListToArray(wResourceType.getIntendedUsageList()));
    }

    Map<String, List<String>> titleMap =
        FieldInputUtils.createLiteralMapFromList(wResourceType.getTitleList());

    webResource.setDcTitle(titleMap);

    Map<String, List<String>> languageMap =
        FieldInputUtils.createLiteralMapFromList(wResourceType.getLanguageList());

    webResource.setDcLanguage(languageMap);

    Map<String, List<String>> termsTemporalMap =
        FieldInputUtils.createResourceOrLiteralMapFromList(wResourceType.getTemporalList());

    webResource.setDcTermsTemporal(termsTemporalMap);

    if (wResourceType.getSeeAlsoList() != null) {
      webResource.setRdfsSeeAlso(FieldInputUtils.resourceListToArray(wResourceType.getSeeAlsoList()));
    }

    webResource.setWebResourceMetaInfo(createWebResourceMetaInfo(wResourceType));

    return webResource;
  }

  private WebResourceMetaInfoImpl createWebResourceMetaInfo(WebResourceType webResource) {

    // Get the media type and determine meta data creator
    final Optional<String> optionalHasMimeType = Optional.ofNullable(webResource.getHasMimeType())
                                                         .map(HasMimeType::getHasMimeType);

    final MediaType mediaType = optionalHasMimeType.map(hasMimeType -> {
      MediaType adaptedMediaType = MediaType.getMediaType(hasMimeType);
      final EdmType edmType = Optional.ofNullable(webResource.getType1()).map(Type2::getType).orElse(null);
      final boolean isOembedMimeType = hasMimeType.startsWith("application/xml+oembed") || hasMimeType.startsWith("application/json+oembed");
      final boolean isPossibleOembedMediaType = adaptedMediaType == MediaType.TEXT || adaptedMediaType == MediaType.OTHER;
      if (isPossibleOembedMediaType && edmType != null && isOembedMimeType) {
        if (edmType == EdmType.IMAGE) {
          adaptedMediaType = MediaType.IMAGE;
        } else if (edmType == EdmType.VIDEO) {
          adaptedMediaType = MediaType.VIDEO;
        }
      }

      return adaptedMediaType;
    }).orElse(MediaType.OTHER);

    final BiConsumer<WebResourceType, WebResourceMetaInfoImpl> metaDataCreator =
        switch (mediaType) {
          case AUDIO -> this::addAudioMetaInfo;
          case IMAGE -> this::addImageMetaInfo;
          case TEXT -> this::addTextMetaInfo;
          case VIDEO -> this::addVideoMetaInfo;
          case THREE_D -> this::add3DMetaInfo;
          default -> null;
        };

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
    metaInfo.setCodec(convertToString(source.getCodecName()));
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
        .map(HasColorSpace::getHasColorSpace).map(ColorSpaceType::xmlValue).orElse(null));

    final Stream<HexBinaryType> sourceColors = Optional.ofNullable(source.getComponentColorList())
        .stream().flatMap(Collection::stream);
    final String[] targetColors = sourceColors.filter(Objects::nonNull)
        .map(HexBinaryType::getString).filter(StringUtils::isNotBlank)
        .toArray(String[]::new);
    metaInfo.setColorPalette(targetColors.length == 0 ? null : targetColors);

    final Orientation sourceOrientation = Optional.ofNullable(source.getOrientation())
        .map(OrientationType::getString).map(Orientation::getFromNameCaseInsensitive)
        .orElse(null);
    metaInfo.setOrientation(switch (sourceOrientation){
      case PORTRAIT -> ImageOrientation.PORTRAIT;
      case LANDSCAPE -> ImageOrientation.LANDSCAPE;
      case null -> null;
    });

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
    metaInfo.setCodec(convertToString(source.getCodecName()));
    metaInfo.setWidth(convertToInteger(source.getWidth()));
    metaInfo.setHeight(convertToInteger(source.getHeight()));
    metaInfo.setBitRate(convertToInteger(source.getBitRate()));
    metaInfo.setFrameRate(convertToDouble(source.getFrameRate()));
    metaInfo.setDuration(convertToLong(source.getDuration()));

    target.setVideoMetaInfo(metaInfo);
  }

  private void add3DMetaInfo(WebResourceType source, WebResourceMetaInfoImpl target) {
    ThreeDMetaInfoImpl metaInfo = new ThreeDMetaInfoImpl();

    metaInfo.setMimeType(convertToString(source.getHasMimeType()));
    metaInfo.setFileSize(convertToLong(source.getFileByteSize()));
    metaInfo.setPointCount(convertToLong(source.getPointCount()));
    metaInfo.setPolygonCount(convertToLong(source.getPolygonCount()));
    metaInfo.setVerticeCount(convertToLong(source.getVerticeCount()));
    target.setThreeDMetaInfo(metaInfo);
  }

  private static Long convertToLong(Duration duration) {
    if (duration == null || StringUtils.isBlank(duration.getDuration())) {
      return null;
    }
    try {
      return Long.valueOf(duration.getDuration());
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

  private static Long convertToLong(NonNegativeIntegerWithoutDataTypeType data) {
    return Optional.ofNullable(data).map(NonNegativeIntegerWithoutDataTypeType::getInteger)
                   .map(BigInteger::longValue).orElse(null);
  }

  private static String convertToString(CodecName data) {
    return Optional.ofNullable(data).map(CodecName::getCodecName).orElse(null);
  }
}
