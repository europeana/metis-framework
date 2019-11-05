package eu.europeana.indexing.mongo;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import eu.europeana.corelib.definitions.edm.model.metainfo.ImageOrientation;
import eu.europeana.corelib.edm.model.metainfo.AudioMetaInfoImpl;
import eu.europeana.corelib.edm.model.metainfo.ImageMetaInfoImpl;
import eu.europeana.corelib.edm.model.metainfo.TextMetaInfoImpl;
import eu.europeana.corelib.edm.model.metainfo.VideoMetaInfoImpl;
import eu.europeana.corelib.edm.model.metainfo.WebResourceMetaInfoImpl;
import eu.europeana.indexing.mongo.property.MongoPropertyUpdater;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class WebResourceMetaInfoUpdaterTest extends
    MongoEntityUpdaterTest<WebResourceMetaInfoImpl> {

  @Override
  WebResourceMetaInfoImpl createEmptyMongoEntity() {
    return new WebResourceMetaInfoImpl();
  }

  @Test
  void testUpdateFullObject() {

    // Craete objects for execution
    final WebResourceMetaInfoUpdater updater = new WebResourceMetaInfoUpdater();
    @SuppressWarnings("unchecked") final MongoPropertyUpdater<WebResourceMetaInfoImpl> propertyUpdater = mock(
        MongoPropertyUpdater.class);
    when(propertyUpdater.removeObjectIfNecessary(anyString(), any())).thenReturn(false);
    WebResourceInformation ancestorInformation = new WebResourceInformation("root about",
        "web resource about");

    // Make the call
    updater.update(propertyUpdater, ancestorInformation);

    // Test the type checks
    @SuppressWarnings("unchecked") final ArgumentCaptor<Function<WebResourceMetaInfoImpl, Object>> getterCaptor = ArgumentCaptor
        .forClass(Function.class);
    final WebResourceMetaInfoImpl metaInfo = new WebResourceMetaInfoImpl();
    metaInfo.setAudioMetaInfo(new AudioMetaInfoImpl());
    metaInfo.setImageMetaInfo(new ImageMetaInfoImpl());
    metaInfo.setTextMetaInfo(new TextMetaInfoImpl());
    metaInfo.setVideoMetaInfo(new VideoMetaInfoImpl());
    verify(propertyUpdater, times(1))
        .removeObjectIfNecessary(eq("audioMetaInfo"), getterCaptor.capture());
    assertSame(metaInfo.getAudioMetaInfo(), getterCaptor.getValue().apply(metaInfo));
    verify(propertyUpdater, times(1))
        .removeObjectIfNecessary(eq("imageMetaInfo"), getterCaptor.capture());
    assertSame(metaInfo.getImageMetaInfo(), getterCaptor.getValue().apply(metaInfo));
    verify(propertyUpdater, times(1))
        .removeObjectIfNecessary(eq("textMetaInfo"), getterCaptor.capture());
    assertSame(metaInfo.getTextMetaInfo(), getterCaptor.getValue().apply(metaInfo));
    verify(propertyUpdater, times(1))
        .removeObjectIfNecessary(eq("videoMetaInfo"), getterCaptor.capture());
    assertSame(metaInfo.getVideoMetaInfo(), getterCaptor.getValue().apply(metaInfo));

    // Test all the audio values
    testStringPropertyUpdate(propertyUpdater, "audioMetaInfo.mimeType",
        createSetterForAudio(AudioMetaInfoImpl::setMimeType));
    testObjectPropertyUpdate(propertyUpdater, "audioMetaInfo.fileSize",
        createSetterForAudio(AudioMetaInfoImpl::setFileSize), 10L);
    testStringPropertyUpdate(propertyUpdater,"audioMetaInfo.codec",
        createSetterForAudio(AudioMetaInfoImpl::setCodec));
    testObjectPropertyUpdate(propertyUpdater, "audioMetaInfo.duration",
        createSetterForAudio(AudioMetaInfoImpl::setDuration), 11L);
    testObjectPropertyUpdate(propertyUpdater, "audioMetaInfo.sampleRate",
        createSetterForAudio(AudioMetaInfoImpl::setSampleRate), 12);
    testObjectPropertyUpdate(propertyUpdater, "audioMetaInfo.bitRate",
        createSetterForAudio(AudioMetaInfoImpl::setBitRate), 13);
    testObjectPropertyUpdate(propertyUpdater, "audioMetaInfo.bitDepth",
        createSetterForAudio(AudioMetaInfoImpl::setBitDepth), 14);
    testObjectPropertyUpdate(propertyUpdater, "audioMetaInfo.channels",
        createSetterForAudio(AudioMetaInfoImpl::setChannels), 15);

    // Test all the image values
    testStringPropertyUpdate(propertyUpdater,"imageMetaInfo.mimeType",
        createSetterForImage(ImageMetaInfoImpl::setMimeType));
    testObjectPropertyUpdate(propertyUpdater,"imageMetaInfo.fileSize",
        createSetterForImage(ImageMetaInfoImpl::setFileSize), 20L);
    testObjectPropertyUpdate(propertyUpdater,"imageMetaInfo.height",
        createSetterForImage(ImageMetaInfoImpl::setHeight), 21);
    testObjectPropertyUpdate(propertyUpdater,"imageMetaInfo.width",
        createSetterForImage(ImageMetaInfoImpl::setWidth), 22);
    testStringPropertyUpdate(propertyUpdater,"imageMetaInfo.colorSpace",
        createSetterForImage(ImageMetaInfoImpl::setColorSpace));
    testArrayPropertyUpdate(propertyUpdater,"imageMetaInfo.colorPalette",
        createSetterForImage(ImageMetaInfoImpl::setColorPalette));
    testObjectPropertyUpdate(propertyUpdater,"imageMetaInfo.orientation",
        createSetterForImage(ImageMetaInfoImpl::setOrientation), ImageOrientation.LANDSCAPE);

    // Test all the text values
    testStringPropertyUpdate(propertyUpdater,"textMetaInfo.mimeType",
        createSetterForText(TextMetaInfoImpl::setMimeType));
    testObjectPropertyUpdate(propertyUpdater,"textMetaInfo.fileSize",
        createSetterForText(TextMetaInfoImpl::setFileSize), 30L);
    testObjectPropertyUpdate(propertyUpdater,"textMetaInfo.resolution",
        createSetterForText(TextMetaInfoImpl::setResolution), 31);
    testStringPropertyUpdate(propertyUpdater,"textMetaInfo.rdfType",
        createSetterForText(TextMetaInfoImpl::setRdfType));

    // Test all the video values
    testStringPropertyUpdate(propertyUpdater,"videoMetaInfo.mimeType",
        createSetterForVideo(VideoMetaInfoImpl::setMimeType));
    testObjectPropertyUpdate(propertyUpdater,"videoMetaInfo.fileSize",
        createSetterForVideo(VideoMetaInfoImpl::setFileSize), 40L);
    testStringPropertyUpdate(propertyUpdater,"videoMetaInfo.codec",
        createSetterForVideo(VideoMetaInfoImpl::setCodec));
    testObjectPropertyUpdate(propertyUpdater,"videoMetaInfo.width",
        createSetterForVideo(VideoMetaInfoImpl::setWidth), 41);
    testObjectPropertyUpdate(propertyUpdater,"videoMetaInfo.height",
        createSetterForVideo(VideoMetaInfoImpl::setHeight), 42);
    testObjectPropertyUpdate(propertyUpdater,"videoMetaInfo.bitRate",
        createSetterForVideo(VideoMetaInfoImpl::setBitRate), 43);
    testObjectPropertyUpdate(propertyUpdater,"videoMetaInfo.frameRate",
        createSetterForVideo(VideoMetaInfoImpl::setFrameRate), 44.0);
    testObjectPropertyUpdate(propertyUpdater,"videoMetaInfo.duration",
        createSetterForVideo(VideoMetaInfoImpl::setDuration), 45L);

    // And that should be it.
    verifyNoMoreInteractions(propertyUpdater);
  }

  @Test
  void testUpdateEmptyObject(){

    // Craete objects for execution
    final WebResourceMetaInfoUpdater updater = new WebResourceMetaInfoUpdater();
    @SuppressWarnings("unchecked") final MongoPropertyUpdater<WebResourceMetaInfoImpl> propertyUpdater = mock(
        MongoPropertyUpdater.class);
    when(propertyUpdater.removeObjectIfNecessary(anyString(), any())).thenReturn(true);
    WebResourceInformation ancestorInformation = new WebResourceInformation("root about",
        "web resource about");

    // Make the call
    updater.update(propertyUpdater, ancestorInformation);

    // Test the type checks
    @SuppressWarnings("unchecked") final ArgumentCaptor<Function<WebResourceMetaInfoImpl, Object>> getterCaptor = ArgumentCaptor
        .forClass(Function.class);
    final WebResourceMetaInfoImpl metaInfo = new WebResourceMetaInfoImpl();
    metaInfo.setAudioMetaInfo(new AudioMetaInfoImpl());
    metaInfo.setImageMetaInfo(new ImageMetaInfoImpl());
    metaInfo.setTextMetaInfo(new TextMetaInfoImpl());
    metaInfo.setVideoMetaInfo(new VideoMetaInfoImpl());
    verify(propertyUpdater, times(1))
        .removeObjectIfNecessary(eq("audioMetaInfo"), getterCaptor.capture());
    assertSame(metaInfo.getAudioMetaInfo(), getterCaptor.getValue().apply(metaInfo));
    verify(propertyUpdater, times(1))
        .removeObjectIfNecessary(eq("imageMetaInfo"), getterCaptor.capture());
    assertSame(metaInfo.getImageMetaInfo(), getterCaptor.getValue().apply(metaInfo));
    verify(propertyUpdater, times(1))
        .removeObjectIfNecessary(eq("textMetaInfo"), getterCaptor.capture());
    assertSame(metaInfo.getTextMetaInfo(), getterCaptor.getValue().apply(metaInfo));
    verify(propertyUpdater, times(1))
        .removeObjectIfNecessary(eq("videoMetaInfo"), getterCaptor.capture());
    assertSame(metaInfo.getVideoMetaInfo(), getterCaptor.getValue().apply(metaInfo));

    // And that should be it.
    verifyNoMoreInteractions(propertyUpdater);
  }

  private <F> BiConsumer<WebResourceMetaInfoImpl, F> createSetterForAudio(
      BiConsumer<AudioMetaInfoImpl, F> setter) {
    return (metaInfo, value) -> {
      if (metaInfo.getAudioMetaInfo() == null) {
        metaInfo.setAudioMetaInfo(new AudioMetaInfoImpl());
      }
      setter.accept(metaInfo.getAudioMetaInfo(), value);
    };
  }

  private <F> BiConsumer<WebResourceMetaInfoImpl, F> createSetterForImage(
      BiConsumer<ImageMetaInfoImpl, F> setter) {
    return (metaInfo, value) -> {
      if (metaInfo.getImageMetaInfo() == null) {
        metaInfo.setImageMetaInfo(new ImageMetaInfoImpl());
      }
      setter.accept(metaInfo.getImageMetaInfo(), value);
    };
  }

  private <F> BiConsumer<WebResourceMetaInfoImpl, F> createSetterForText(
      BiConsumer<TextMetaInfoImpl, F> setter) {
    return (metaInfo, value) -> {
      if (metaInfo.getTextMetaInfo() == null) {
        metaInfo.setTextMetaInfo(new TextMetaInfoImpl());
      }
      setter.accept(metaInfo.getTextMetaInfo(), value);
    };
  }

  private <F> BiConsumer<WebResourceMetaInfoImpl, F> createSetterForVideo(
      BiConsumer<VideoMetaInfoImpl, F> setter) {
    return (metaInfo, value) -> {
      if (metaInfo.getVideoMetaInfo() == null) {
        metaInfo.setVideoMetaInfo(new VideoMetaInfoImpl());
      }
      setter.accept(metaInfo.getVideoMetaInfo(), value);
    };
  }
}
