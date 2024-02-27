package eu.europeana.indexing.mongo;

import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;
import eu.europeana.corelib.definitions.edm.model.metainfo.AudioMetaInfo;
import eu.europeana.corelib.definitions.edm.model.metainfo.ImageMetaInfo;
import eu.europeana.corelib.definitions.edm.model.metainfo.TextMetaInfo;
import eu.europeana.corelib.definitions.edm.model.metainfo.VideoMetaInfo;
import eu.europeana.corelib.edm.model.metainfo.WebResourceMetaInfoImpl;
import eu.europeana.indexing.mongo.property.MongoObjectManager;
import eu.europeana.indexing.mongo.property.MongoPropertyUpdater;
import eu.europeana.indexing.mongo.property.MongoPropertyUpdaterFactory;
import eu.europeana.metis.mongo.dao.RecordDao;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.xml.bind.DatatypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Field updater for instances of {@link WebResourceMetaInfoImpl}.
 */
public class WebResourceMetaInfoUpdater extends
        AbstractMongoObjectUpdater<WebResourceMetaInfoImpl, WebResourceInformation> implements
        MongoObjectManager<WebResourceMetaInfoImpl, WebResourceInformation> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebResourceMetaInfoUpdater.class);

  @Override
  protected MongoPropertyUpdater<WebResourceMetaInfoImpl> createPropertyUpdater(
      WebResourceMetaInfoImpl newEntity, WebResourceInformation ancestorInformation,
      Date recordDate, Date recordCreationDate, RecordDao mongoServer) {
    final String hashCode = generateHashCode(ancestorInformation.getWebResourceAbout(),
        ancestorInformation.getRootAbout());
    final Supplier<Query<WebResourceMetaInfoImpl>> querySupplier =
        () -> createQuery(mongoServer, hashCode);
    return MongoPropertyUpdaterFactory.createForObjectWithoutAbout(newEntity, mongoServer, querySupplier, null);
  }

  private static Query<WebResourceMetaInfoImpl> createQuery(RecordDao mongoServer, String id) {
    return mongoServer.getDatastore().find(WebResourceMetaInfoImpl.class)
        .filter(Filters.eq("_id", id));
  }

  private static String generateHashCode(String webResourceId, String recordId) {
    String generatedHash = null;
    try {
      // Note: we have no choice but to use MD5, this is agreed upon with the API implementation.
      // The data used are not private and are considered safe
      @SuppressWarnings({"findsecbugs:WEAK_MESSAGE_DIGEST_MD5", "java:S4790"}) final MessageDigest md = MessageDigest.getInstance(
          "MD5");
      byte[] digest = md.digest((webResourceId + "-" + recordId).getBytes(StandardCharsets.UTF_8));
      generatedHash = DatatypeConverter.printHexBinary(digest).toLowerCase(Locale.US);
    } catch (NoSuchAlgorithmException e) {
      //This shouldn't happen
      LOGGER.error("Hashing algorithm does not exist", e);
    }
    return generatedHash;
  }

  @Override
  protected void update(MongoPropertyUpdater<WebResourceMetaInfoImpl> propertyUpdater,
      WebResourceInformation ancestorInformation) {

    // Audio info
    if (!propertyUpdater.removeObjectIfNecessary("audioMetaInfo",
        WebResourceMetaInfoImpl::getAudioMetaInfo)) {
      propertyUpdater.updateString("audioMetaInfo.mimeType",
          createGetter(WebResourceMetaInfoImpl::getAudioMetaInfo, AudioMetaInfo::getMimeType));
      propertyUpdater.updateObject("audioMetaInfo.fileSize",
          createGetter(WebResourceMetaInfoImpl::getAudioMetaInfo, AudioMetaInfo::getFileSize));
      propertyUpdater.updateString("audioMetaInfo.codec",
          createGetter(WebResourceMetaInfoImpl::getAudioMetaInfo, AudioMetaInfo::getCodec));
      propertyUpdater.updateObject("audioMetaInfo.duration",
          createGetter(WebResourceMetaInfoImpl::getAudioMetaInfo, AudioMetaInfo::getDuration));
      propertyUpdater.updateObject("audioMetaInfo.sampleRate",
          createGetter(WebResourceMetaInfoImpl::getAudioMetaInfo, AudioMetaInfo::getSampleRate));
      propertyUpdater.updateObject("audioMetaInfo.bitRate",
          createGetter(WebResourceMetaInfoImpl::getAudioMetaInfo, AudioMetaInfo::getBitRate));
      propertyUpdater.updateObject("audioMetaInfo.bitDepth",
          createGetter(WebResourceMetaInfoImpl::getAudioMetaInfo, AudioMetaInfo::getBitDepth));
      propertyUpdater.updateObject("audioMetaInfo.channels",
          createGetter(WebResourceMetaInfoImpl::getAudioMetaInfo, AudioMetaInfo::getChannels));
    }

    // Image info
    if (!propertyUpdater.removeObjectIfNecessary("imageMetaInfo",
        WebResourceMetaInfoImpl::getImageMetaInfo)) {
      propertyUpdater.updateString("imageMetaInfo.mimeType",
          createGetter(WebResourceMetaInfoImpl::getImageMetaInfo, ImageMetaInfo::getMimeType));
      propertyUpdater.updateObject("imageMetaInfo.fileSize",
          createGetter(WebResourceMetaInfoImpl::getImageMetaInfo, ImageMetaInfo::getFileSize));
      propertyUpdater.updateObject("imageMetaInfo.height",
          createGetter(WebResourceMetaInfoImpl::getImageMetaInfo, ImageMetaInfo::getHeight));
      propertyUpdater.updateObject("imageMetaInfo.width",
          createGetter(WebResourceMetaInfoImpl::getImageMetaInfo, ImageMetaInfo::getWidth));
      propertyUpdater.updateString("imageMetaInfo.colorSpace",
          createGetter(WebResourceMetaInfoImpl::getImageMetaInfo, ImageMetaInfo::getColorSpace));
      propertyUpdater.updateArray("imageMetaInfo.colorPalette",
          createGetter(WebResourceMetaInfoImpl::getImageMetaInfo, ImageMetaInfo::getColorPalette));
      propertyUpdater.updateObject("imageMetaInfo.orientation",
          createGetter(WebResourceMetaInfoImpl::getImageMetaInfo, ImageMetaInfo::getOrientation));
    }

    // Text info
    if (!propertyUpdater.removeObjectIfNecessary("textMetaInfo",
        WebResourceMetaInfoImpl::getTextMetaInfo)) {
      propertyUpdater.updateString("textMetaInfo.mimeType",
          createGetter(WebResourceMetaInfoImpl::getTextMetaInfo, TextMetaInfo::getMimeType));
      propertyUpdater.updateObject("textMetaInfo.fileSize",
          createGetter(WebResourceMetaInfoImpl::getTextMetaInfo, TextMetaInfo::getFileSize));
      propertyUpdater.updateObject("textMetaInfo.resolution",
          createGetter(WebResourceMetaInfoImpl::getTextMetaInfo, TextMetaInfo::getResolution));
      propertyUpdater.updateString("textMetaInfo.rdfType",
          createGetter(WebResourceMetaInfoImpl::getTextMetaInfo, TextMetaInfo::getRdfType));
    }

    // Video info
    if (!propertyUpdater.removeObjectIfNecessary("videoMetaInfo",
        WebResourceMetaInfoImpl::getVideoMetaInfo)) {
      propertyUpdater.updateString("videoMetaInfo.mimeType",
          createGetter(WebResourceMetaInfoImpl::getVideoMetaInfo, VideoMetaInfo::getMimeType));
      propertyUpdater.updateObject("videoMetaInfo.fileSize",
          createGetter(WebResourceMetaInfoImpl::getVideoMetaInfo, VideoMetaInfo::getFileSize));
      propertyUpdater.updateString("videoMetaInfo.codec",
          createGetter(WebResourceMetaInfoImpl::getVideoMetaInfo, VideoMetaInfo::getCodec));
      propertyUpdater.updateObject("videoMetaInfo.width",
          createGetter(WebResourceMetaInfoImpl::getVideoMetaInfo, VideoMetaInfo::getWidth));
      propertyUpdater.updateObject("videoMetaInfo.height",
          createGetter(WebResourceMetaInfoImpl::getVideoMetaInfo, VideoMetaInfo::getHeight));
      propertyUpdater.updateObject("videoMetaInfo.bitRate",
          createGetter(WebResourceMetaInfoImpl::getVideoMetaInfo, VideoMetaInfo::getBitRate));
      propertyUpdater.updateObject("videoMetaInfo.frameRate",
          createGetter(WebResourceMetaInfoImpl::getVideoMetaInfo, VideoMetaInfo::getFrameRate));
      propertyUpdater.updateObject("videoMetaInfo.duration",
          createGetter(WebResourceMetaInfoImpl::getVideoMetaInfo, VideoMetaInfo::getDuration));
    }

    // 3D info
    if (!propertyUpdater.removeObjectIfNecessary("threeDMetaInfo",
        WebResourceMetaInfoImpl::getTextMetaInfo)) {
      propertyUpdater.updateString("textMetaInfo.mimeType",
          createGetter(WebResourceMetaInfoImpl::getTextMetaInfo, TextMetaInfo::getMimeType));
      propertyUpdater.updateObject("textMetaInfo.fileSize",
          createGetter(WebResourceMetaInfoImpl::getTextMetaInfo, TextMetaInfo::getFileSize));
      propertyUpdater.updateObject("textMetaInfo.resolution",
          createGetter(WebResourceMetaInfoImpl::getTextMetaInfo, TextMetaInfo::getResolution));
      propertyUpdater.updateString("textMetaInfo.rdfType",
          createGetter(WebResourceMetaInfoImpl::getTextMetaInfo, TextMetaInfo::getRdfType));
    }
  }

  private <P, I> Function<WebResourceMetaInfoImpl, P> createGetter(
      Function<WebResourceMetaInfoImpl, I> infoGetter, Function<I, P> propertyGetter) {
    final Function<I, P> nullablePropertyGetter =
        info -> Optional.ofNullable(info).map(propertyGetter).orElse(null);
    return infoGetter.andThen(nullablePropertyGetter);
  }

  @Override
  public void delete(WebResourceInformation ancestorInformation, RecordDao mongoServer) {
    final String hashCode = generateHashCode(ancestorInformation.getWebResourceAbout(),
            ancestorInformation.getRootAbout());
    createQuery(mongoServer, hashCode).delete();
  }
}
