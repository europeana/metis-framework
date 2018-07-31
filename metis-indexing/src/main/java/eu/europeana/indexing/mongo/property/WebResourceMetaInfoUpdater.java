package eu.europeana.indexing.mongo.property;

import java.util.Optional;
import java.util.function.Function;
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
    extends AbstractMongoObjectUpdater<WebResourceMetaInfoImpl> {

  @Override
  protected MongoPropertyUpdater<WebResourceMetaInfoImpl> createPropertyUpdater(
      WebResourceMetaInfoImpl newEntity, MongoServer mongoServer) {
    return MongoPropertyUpdater.createForWebResourceMetaInfo(newEntity, mongoServer);
  }

  @Override
  protected void update(MongoPropertyUpdater<WebResourceMetaInfoImpl> propertyUpdater) {

    // Audio info
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
    propertyUpdater.updateObject("audioMetaInfo.channels",
        createGetter(WebResourceMetaInfoImpl::getAudioMetaInfo, AudioMetaInfo::getChannels));

    // Image info
    propertyUpdater.updateString("imageMetaInfo.mimeType",
        createGetter(WebResourceMetaInfoImpl::getImageMetaInfo, ImageMetaInfo::getMimeType));
    propertyUpdater.updateObject("imageMetaInfo.fileSize",
        createGetter(WebResourceMetaInfoImpl::getImageMetaInfo, ImageMetaInfo::getFileSize));
    propertyUpdater.updateObject("imageMetaInfo.width",
        createGetter(WebResourceMetaInfoImpl::getImageMetaInfo, ImageMetaInfo::getWidth));
    propertyUpdater.updateString("imageMetaInfo.colorSpace",
        createGetter(WebResourceMetaInfoImpl::getImageMetaInfo, ImageMetaInfo::getColorSpace));
    propertyUpdater.updateArray("imageMetaInfo.colorPalette",
        createGetter(WebResourceMetaInfoImpl::getImageMetaInfo, ImageMetaInfo::getColorPalette));
    propertyUpdater.updateObject("imageMetaInfo.orientation",
        createGetter(WebResourceMetaInfoImpl::getImageMetaInfo, ImageMetaInfo::getOrientation));

    // Text info
    propertyUpdater.updateString("textMetaInfo.mimeType",
        createGetter(WebResourceMetaInfoImpl::getTextMetaInfo, TextMetaInfo::getMimeType));
    propertyUpdater.updateObject("textMetaInfo.fileSize",
        createGetter(WebResourceMetaInfoImpl::getTextMetaInfo, TextMetaInfo::getFileSize));
    propertyUpdater.updateObject("textMetaInfo.resolution",
        createGetter(WebResourceMetaInfoImpl::getTextMetaInfo, TextMetaInfo::getResolution));

    // Video info
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

    // The embedded objects themselves should then be removed.
    propertyUpdater.removeObjectIfNeeded("audioMetaInfo",
        WebResourceMetaInfoImpl::getAudioMetaInfo);
    propertyUpdater.removeObjectIfNeeded("imageMetaInfo",
        WebResourceMetaInfoImpl::getImageMetaInfo);
    propertyUpdater.removeObjectIfNeeded("textMetaInfo", WebResourceMetaInfoImpl::getTextMetaInfo);
    propertyUpdater.removeObjectIfNeeded("videoMetaInfo",
        WebResourceMetaInfoImpl::getVideoMetaInfo);
  }

  private <P, I> Function<WebResourceMetaInfoImpl, P> createGetter(
      Function<WebResourceMetaInfoImpl, I> infoGetter, Function<I, P> propertyGetter) {
    final Function<I, P> nullablePropertyGetter =
        info -> Optional.ofNullable(info).map(propertyGetter).orElse(null);
    return infoGetter.andThen(nullablePropertyGetter);
  }
}
