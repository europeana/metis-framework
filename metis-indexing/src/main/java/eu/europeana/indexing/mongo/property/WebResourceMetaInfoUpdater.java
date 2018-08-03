package eu.europeana.indexing.mongo.property;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.query.Query;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import eu.europeana.corelib.definitions.edm.model.metainfo.AudioMetaInfo;
import eu.europeana.corelib.definitions.edm.model.metainfo.ImageMetaInfo;
import eu.europeana.corelib.definitions.edm.model.metainfo.TextMetaInfo;
import eu.europeana.corelib.definitions.edm.model.metainfo.VideoMetaInfo;
import eu.europeana.corelib.edm.model.metainfo.WebResourceMetaInfoImpl;
import eu.europeana.corelib.storage.MongoServer;

/**
 * Field updater for instances of {@link WebResourceMetaInfoImpl}.
 */
public class WebResourceMetaInfoUpdater
    extends AbstractMongoObjectUpdater<WebResourceMetaInfoImpl, WebResourceInformation> {

  private static final HashFunction HASH_FUNCTION = Hashing.md5();

  @Override
  protected MongoPropertyUpdater<WebResourceMetaInfoImpl> createPropertyUpdater(
      WebResourceMetaInfoImpl newEntity, WebResourceInformation ancestorInformation,
      MongoServer mongoServer) {
    final String hashCode = generateHashCode(ancestorInformation.getWebResourceAbout(),
        ancestorInformation.getRootAbout());
    final Supplier<Query<WebResourceMetaInfoImpl>> querySupplier =
        () -> createQuery(mongoServer, hashCode);
    return new MongoPropertyUpdater<>(newEntity, mongoServer, WebResourceMetaInfoImpl.class,
        querySupplier, null);
  }

  private static Query<WebResourceMetaInfoImpl> createQuery(MongoServer mongoServer, String id) {
    return mongoServer.getDatastore().find(WebResourceMetaInfoImpl.class).field(Mapper.ID_KEY)
        .equal(id);
  }

  // TODO This is code from corelib (eu.europeana.corelib.search.impl.WebMetaInfo). This should be
  // in a common library?
  private static String generateHashCode(String webResourceId, String recordId) {
    return HASH_FUNCTION.newHasher().putString(webResourceId, Charsets.UTF_8)
        .putString("-", Charsets.UTF_8).putString(recordId, Charsets.UTF_8).hash().toString();
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
      propertyUpdater.updateObject("textMetaInfo.rdfType",
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
  }

  private <P, I> Function<WebResourceMetaInfoImpl, P> createGetter(
      Function<WebResourceMetaInfoImpl, I> infoGetter, Function<I, P> propertyGetter) {
    final Function<I, P> nullablePropertyGetter =
        info -> Optional.ofNullable(info).map(propertyGetter).orElse(null);
    return infoGetter.andThen(nullablePropertyGetter);
  }
}
