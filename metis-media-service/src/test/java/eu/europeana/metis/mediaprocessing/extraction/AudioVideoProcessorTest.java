package eu.europeana.metis.mediaprocessing.extraction;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.WireMockServer;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.model.AbstractResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.AudioResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResultImpl;
import eu.europeana.metis.mediaprocessing.model.VideoResourceMetadata;
import eu.europeana.metis.network.NetworkUtil;
import eu.europeana.metis.schema.model.MediaType;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AudioVideoProcessorTest {

  private static final String FF_PROBE_COMMAND = "ffprobe command";
  private static CommandExecutor commandExecutor;
  private static AudioVideoProcessor audioVideoProcessor;
  private static int portForWireMock = 9999;

  static {
    try {
      portForWireMock = new NetworkUtil().getAvailableLocalPort();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static WireMockServer wireMockServer;

  @BeforeAll
  static void createMocks() {
    commandExecutor = mock(CommandExecutor.class);
    audioVideoProcessor = spy(new AudioVideoProcessor(commandExecutor, FF_PROBE_COMMAND));
    wireMockServer = new WireMockServer(wireMockConfig().port(portForWireMock));
    wireMockServer.start();
  }

  @BeforeEach
  void resetMocks() {
    reset(commandExecutor, audioVideoProcessor);
  }

  @Test
  void testDiscoverFfprobeCommand() throws MediaProcessorException {

    // ffprobe command
    final String ffprobeCommand = "ffprobe";

    // Test ffprobe 4
    doReturn("ffprobe version 4.4.4-0ubuntu0.18.04.1 Copyright (c) 2007-2018 the FFmpeg developers")
        .when(commandExecutor).execute(eq(Collections.singletonList(ffprobeCommand)), anyMap(), eq(true), any());
    assertEquals(ffprobeCommand, AudioVideoProcessor.discoverFfprobeCommand(commandExecutor));

    // Test ffprobe 3
    doReturn("ffprobe version 3.4.4-0ubuntu0.18.04.1 Copyright (c) 2007-2018 the FFmpeg developers")
        .when(commandExecutor).execute(eq(Collections.singletonList(ffprobeCommand)), anyMap(), eq(true), any());
    assertEquals(ffprobeCommand, AudioVideoProcessor.discoverFfprobeCommand(commandExecutor));

    // Test ffprobe 2
    doReturn("ffprobe version 2.4.4-0ubuntu0.18.04.1 Copyright (c) 2007-2018 the FFmpeg developers")
        .when(commandExecutor).execute(eq(Collections.singletonList(ffprobeCommand)), anyMap(), eq(true), any());
    assertEquals(ffprobeCommand, AudioVideoProcessor.discoverFfprobeCommand(commandExecutor));

    // Test other commands
    doReturn("ffprobe version 1.4.4-0ubuntu0.18.04.1 Copyright (c) 2007-2018 the FFmpeg developers")
        .when(commandExecutor).execute(eq(Collections.singletonList(ffprobeCommand)), anyMap(), eq(true), any());
    assertThrows(MediaProcessorException.class,
        () -> AudioVideoProcessor.discoverFfprobeCommand(commandExecutor));
    doReturn("ffprobe version 5.4.4-0ubuntu0.18.04.1 Copyright (c) 2007-2018 the FFmpeg developers")
        .when(commandExecutor).execute(eq(Collections.singletonList(ffprobeCommand)), anyMap(), eq(true), any());
    assertThrows(MediaProcessorException.class,
        () -> AudioVideoProcessor.discoverFfprobeCommand(commandExecutor));
    doReturn("Other command").when(commandExecutor)
                             .execute(eq(Collections.singletonList(ffprobeCommand)), anyMap(), eq(true), any());
    assertThrows(MediaProcessorException.class,
        () -> AudioVideoProcessor.discoverFfprobeCommand(commandExecutor));

    // Test command execution exception
    doThrow(new MediaProcessorException("", null)).when(commandExecutor)
                                                  .execute(eq(Collections.singletonList(ffprobeCommand)), anyMap(), eq(true),
                                                      any());
    assertThrows(MediaProcessorException.class,
        () -> AudioVideoProcessor.discoverFfprobeCommand(commandExecutor));
  }

  @Test
  void testCreateAudioVideoAnalysisCommand() throws IOException, MediaExtractionException {

    // Create resource
    final Resource resource = mock(Resource.class);
    doReturn("https://valid.url.nl/test").when(resource).getResourceUrl();
    doReturn(Paths.get("content path")).when(resource).getContentPath();

    // test resource with content
    doReturn(true).when(resource).hasContent();
    final List<String> resultWithContent = audioVideoProcessor
        .createAudioVideoAnalysisCommand(resource);
    final List<String> expectedWithContent = Arrays
        .asList(FF_PROBE_COMMAND, "-v", "quiet", "-print_format", "json", "-show_format",
            "-show_streams", "-hide_banner", resource.getContentPath().toString());
    assertEquals(expectedWithContent, resultWithContent);

    // test resource without content
    doReturn(false).when(resource).hasContent();
    final List<String> resultWithoutContent = audioVideoProcessor
        .createAudioVideoAnalysisCommand(resource);
    final List<String> expectedWithoutContent = Arrays
        .asList(FF_PROBE_COMMAND, "-v", "quiet", "-print_format", "json", "-show_format",
            "-show_streams", "-hide_banner", resource.getResourceUrl());
    assertEquals(expectedWithoutContent, resultWithoutContent);

    // test if resource does not have a valid URL.
    doReturn("not valid").when(resource).getResourceUrl();
    assertThrows(MediaExtractionException.class,
        () -> audioVideoProcessor.createAudioVideoAnalysisCommand(resource));
    doReturn("valid.without.prefix.nl/").when(resource).getResourceUrl();
    assertThrows(MediaExtractionException.class,
        () -> audioVideoProcessor.createAudioVideoAnalysisCommand(resource));
    doReturn("https://invalid.characters.nl/!@#$%^&*()_").when(resource).getResourceUrl();
    assertThrows(MediaExtractionException.class,
        () -> audioVideoProcessor.createAudioVideoAnalysisCommand(resource));
    doReturn("https://valid.url.nl/test").when(resource).getResourceUrl();

    // test if hasContent fails.
    doThrow(new IOException()).when(resource).hasContent();
    assertThrows(MediaExtractionException.class,
        () -> audioVideoProcessor.createAudioVideoAnalysisCommand(resource));
  }

  @Test
  void testFindStream() {

    // Set up streams
    final JSONObject stream1 = mock(JSONObject.class);
    doReturn("audio").when(stream1).getString("codec_type");
    final JSONObject stream2 = mock(JSONObject.class);
    doReturn("video").when(stream2).getString("codec_type");
    final JSONArray streams = new JSONArray(Arrays.asList(stream1, stream2));
    final JSONObject object = mock(JSONObject.class);
    doReturn(streams).when(object).getJSONArray("streams");

    // Verify behaviour
    assertSame(stream1, audioVideoProcessor.findStream(object, "audio"));
    assertSame(stream2, audioVideoProcessor.findStream(object, "video"));
    assertNull(audioVideoProcessor.findStream(object, "test"));
  }

  @Test
  void testFindValue() {

    // Initialize objects
    final JSONObject object1 = mock(JSONObject.class);
    final JSONObject object2 = mock(JSONObject.class);
    final JSONObject[] objects = new JSONObject[]{object1, object2};
    final String key = "key";
    final String value = "test value";

    // Test first object only
    final Function<JSONObject, String> firstOnly = object -> object == object1 ? value : null;
    assertEquals(value, audioVideoProcessor.findValue(objects, firstOnly, Objects::nonNull));

    // Test second object only
    final Function<JSONObject, String> secondOnly = object -> object == object2 ? value : null;
    assertEquals(value, audioVideoProcessor.findValue(objects, secondOnly, Objects::nonNull));

    // Test both
    final Function<JSONObject, String> both = object -> value;
    assertEquals(value, audioVideoProcessor.findValue(objects, both, Objects::nonNull));

    // Test neither
    final Function<JSONObject, String> neither = object -> null;
    assertNull(audioVideoProcessor.findValue(objects, neither, Objects::nonNull));
  }

  @Test
  void testFindInt() {

    // Set up values
    final JSONObject object = mock(JSONObject.class);
    final JSONObject[] objects = new JSONObject[]{object};
    final String key = "key";
    final int value = 1;

    // Check available
    doReturn(value).when(object).optInt(eq(key), anyInt());
    assertEquals(value, audioVideoProcessor.findInt(key, objects));

    // Check not available
    doAnswer(invocation -> invocation.getArgument(1)).when(object).optInt(eq(key), anyInt());
    assertNull(audioVideoProcessor.findInt(key, objects));
  }

  @Test
  void testFindLong(){
    final JSONObject object = mock(JSONObject.class);
    final JSONObject[] objects = new JSONObject[]{object};
    final String key = "key";
    final long value = 1L;

    doReturn(value).when(object).optLong(eq(key), anyLong());
    assertEquals(value, audioVideoProcessor.findLong(key, objects));

    doReturn(Long.MIN_VALUE).when(object).optLong(eq(key), anyLong());
    assertNull(audioVideoProcessor.findLong(key, objects));
  }

  @Test
  void testFindDouble() {

    // Set up values
    final JSONObject object = mock(JSONObject.class);
    final JSONObject[] objects = new JSONObject[]{object};
    final String key = "key";
    final double value = 1.0;

    // Check available
    doReturn(value).when(object).optDouble(eq(key), anyDouble());
    assertEquals(value, audioVideoProcessor.findDouble(key, objects));

    // Check not available
    doReturn(Double.NaN).when(object).optDouble(eq(key), anyDouble());
    assertNull(audioVideoProcessor.findDouble(key, objects));
  }

  @Test
  void testFindString() {

    // Set up values
    final JSONObject object = mock(JSONObject.class);
    final JSONObject[] objects = new JSONObject[]{object};
    final String key = "key";
    final String value = "value";

    // Check available
    doReturn(value).when(object).optString(eq(key), anyString());
    assertEquals(value, audioVideoProcessor.findString(key, objects));

    // Check not available
    doReturn(StringUtils.EMPTY).when(object).optString(eq(key), anyString());
    assertNull(audioVideoProcessor.findString(key, objects));
  }

  @Test
  void testParseCommandResponseForAudio() throws MediaExtractionException, IOException {

    // Create resource
    final Resource resource = mock(Resource.class);
    doReturn("resource url").when(resource).getResourceUrl();
    doReturn("mime type").when(resource).getProvidedMimeType();
    doReturn(true).when(resource).hasContent();
    final String detectedMimeType = "detected mime type";

    // Create json objects
    final String commandResponse = "response";
    final JSONObject object = mock(JSONObject.class);
    doReturn(object).when(audioVideoProcessor).readCommandResponseToJson(commandResponse);
    final JSONObject format = mock(JSONObject.class);
    doReturn(format).when(object).getJSONObject("format");
    final JSONObject[] formatAsArray = new JSONObject[]{format};
    final JSONObject audioStream = mock(JSONObject.class);
    doReturn(audioStream).when(audioVideoProcessor).findStream(object, "audio");
    doReturn(null).when(audioVideoProcessor).findStream(object, "video");
    final JSONObject[] candidates = new JSONObject[]{audioStream, format};

    // Set properties
    final Long size = 7205015L;
    final Integer sampleRate = 44100;
    final Integer channels = 2;
    final Integer bitsPerSample = 8;
    final Double duration = 180.062050;
    final Integer bitRate = 320000;
    doReturn(size).when(audioVideoProcessor).findLong(eq("size"), eq(formatAsArray));
    doReturn(sampleRate).when(audioVideoProcessor).findInt(eq("sample_rate"), eq(candidates));
    doReturn(channels).when(audioVideoProcessor).findInt(eq("channels"), eq(candidates));
    doReturn(bitsPerSample).when(audioVideoProcessor)
                           .findInt(eq("bits_per_sample"), eq(candidates));
    doReturn(duration).when(audioVideoProcessor).findDouble(eq("duration"), eq(candidates));
    doReturn(bitRate).when(audioVideoProcessor).findInt(eq("bit_rate"), eq(candidates));
    doReturn("aac").when(audioVideoProcessor).findString(eq("codec_name"), eq(candidates));

    // Run and verify
    final AbstractResourceMetadata abstractMetadata = audioVideoProcessor
        .parseCommandResponse(resource, detectedMimeType, commandResponse);
    assertTrue(abstractMetadata instanceof AudioResourceMetadata);
    final AudioResourceMetadata metadata = (AudioResourceMetadata) abstractMetadata;
    assertEquals(metadata.getMimeType(), detectedMimeType);
    assertEquals(metadata.getResourceUrl(), resource.getResourceUrl());
    assertTrue(metadata.getThumbnailTargetNames().isEmpty());
    assertEquals(size, metadata.getContentSize());
    assertEquals(bitRate, metadata.getBitRate());
    assertEquals(channels, metadata.getChannels());
    assertEquals(duration, metadata.getDuration());
    assertEquals(sampleRate, metadata.getSampleRate());
    assertEquals(bitsPerSample, metadata.getSampleSize());
    assertEquals("aac", metadata.getCodecName());

  }

  @Test
  void testParseCommandResponseForVideo() throws MediaExtractionException, IOException {

    // Create resource
    final Resource resource = mock(Resource.class);
    doReturn("resource url").when(resource).getResourceUrl();
    doReturn("mime type").when(resource).getProvidedMimeType();
    doReturn(true).when(resource).hasContent();
    final String detectedMimeType = "detected mime type";

    // Create json objects
    final String commandResponse = "response";
    final JSONObject object = mock(JSONObject.class);
    doReturn(object).when(audioVideoProcessor).readCommandResponseToJson(commandResponse);
    final JSONObject format = mock(JSONObject.class);
    doReturn(format).when(object).getJSONObject("format");
    final JSONObject[] formatAsArray = new JSONObject[]{format};
    final JSONObject audioStream = mock(JSONObject.class);
    doReturn(audioStream).when(audioVideoProcessor).findStream(object, "audio");
    final JSONObject videoStream = mock(JSONObject.class);
    doReturn(videoStream).when(audioVideoProcessor).findStream(object, "video");
    final JSONObject[] candidates = new JSONObject[]{videoStream, format};

    // Set properties
    final Long size = 92224193L;
    final Integer width = 640;
    final Integer height = 480;
    final Double duration = 1007.240000;
    final Integer bitRate = 595283;
    final int frameRateNumerator = 629150;
    final int frameRateDenominator = 25181;
    doReturn(size).when(audioVideoProcessor).findLong(eq("size"), eq(formatAsArray));
    doReturn(width).when(audioVideoProcessor).findInt(eq("width"), eq(candidates));
    doReturn(height).when(audioVideoProcessor).findInt(eq("height"), eq(candidates));
    doReturn("h264").when(audioVideoProcessor).findString(eq("codec_name"), eq(candidates));
    doReturn(duration).when(audioVideoProcessor).findDouble(eq("duration"), eq(candidates));
    doReturn(bitRate).when(audioVideoProcessor).findInt(eq("bit_rate"), eq(candidates));
    doReturn(frameRateNumerator + "/" + frameRateDenominator).when(audioVideoProcessor)
                                                             .findString(eq("avg_frame_rate"), eq(candidates));

    // Run and verify
    final AbstractResourceMetadata abstractMetadata = audioVideoProcessor
        .parseCommandResponse(resource, detectedMimeType, commandResponse);
    assertTrue(abstractMetadata instanceof VideoResourceMetadata);
    final VideoResourceMetadata metadata = (VideoResourceMetadata) abstractMetadata;
    assertEquals(metadata.getMimeType(), detectedMimeType);
    assertEquals(metadata.getResourceUrl(), resource.getResourceUrl());
    assertTrue(metadata.getThumbnailTargetNames().isEmpty());
    assertEquals(size, metadata.getContentSize());
    assertEquals(bitRate, metadata.getBitRate());
    assertEquals("h264", metadata.getCodecName());
    assertEquals(duration, metadata.getDuration());
    final Double frameRate = ((double) frameRateNumerator) / frameRateDenominator;
    assertEquals(frameRate, metadata.getFrameRate());
    assertEquals(height, metadata.getHeight());
    assertEquals(width, metadata.getWidth());

    // Try various options for the frame rate
    doReturn("0/0").when(audioVideoProcessor).findString(eq("avg_frame_rate"), eq(candidates));
    final AbstractResourceMetadata metadataWith0FrameRate = audioVideoProcessor
        .parseCommandResponse(resource, detectedMimeType, commandResponse);
    assertEquals(Double.valueOf(0.0), ((VideoResourceMetadata) metadataWith0FrameRate).getFrameRate());
    doReturn("1/0").when(audioVideoProcessor).findString(eq("avg_frame_rate"), eq(candidates));
    final AbstractResourceMetadata metadataWithInvalidFrameRate = audioVideoProcessor
        .parseCommandResponse(resource, detectedMimeType, commandResponse);
    assertNull(((VideoResourceMetadata) metadataWithInvalidFrameRate).getFrameRate());
  }

  @Test
  void testParseCommandResponseErrors() throws MediaExtractionException, IOException {

    // Create resource
    final Resource resource = mock(Resource.class);
    doReturn(true).when(resource).hasContent();
    final String detectedMimeType = "detected mime type";

    // Create json objects
    final String commandResponse = "response";
    final JSONObject object = mock(JSONObject.class);
    doReturn(object).when(audioVideoProcessor).readCommandResponseToJson(commandResponse);
    final JSONObject format = mock(JSONObject.class);
    doReturn(format).when(object).getJSONObject("format");
    final JSONObject audioStream = mock(JSONObject.class);
    doReturn(audioStream).when(audioVideoProcessor).findStream(object, "audio");
    doReturn(null).when(audioVideoProcessor).findStream(object, "video");

    // Set properties
    doReturn(1L).when(format).getLong("size");
    doReturn(1).when(audioVideoProcessor).findInt(any(), any());
    doReturn(1.0).when(audioVideoProcessor).findDouble(any(), any());
    doReturn("1").when(audioVideoProcessor).findString(any(), any());

    // Verify that all is well
    assertNotNull(
        audioVideoProcessor.parseCommandResponse(resource, detectedMimeType, commandResponse));

    // The resource has no content
    doReturn(false).when(resource).hasContent();
    doReturn(0).when(object).length();
    assertThrows(MediaExtractionException.class,
        () -> audioVideoProcessor
            .parseCommandResponse(resource, detectedMimeType, commandResponse));
    doReturn(true).when(resource).hasContent();

    // The right streams are not found
    doReturn(null).when(audioVideoProcessor).findStream(object, "audio");
    assertThrows(MediaExtractionException.class,
        () -> audioVideoProcessor
            .parseCommandResponse(resource, detectedMimeType, commandResponse));
    doReturn(audioStream).when(audioVideoProcessor).findStream(object, "audio");

    // A value could not be found.
    doThrow(JSONException.class).when(audioVideoProcessor).findInt(any(), any());
    assertThrows(MediaExtractionException.class,
        () -> audioVideoProcessor
            .parseCommandResponse(resource, detectedMimeType, commandResponse));
    doReturn(1).when(audioVideoProcessor).findInt(any(), any());

    // Verify that all is well again
    assertNotNull(
        audioVideoProcessor.parseCommandResponse(resource, detectedMimeType, commandResponse));
  }

  @Test
  void testParseMpdResource() throws MediaExtractionException, URISyntaxException {
    final Resource resource = mock(Resource.class);
    when(resource.getActualLocation())
        .thenReturn(new URI("http://127.0.0.1:" + portForWireMock + "/test-url.mpd"));

    wireMockServer.stubFor(get(urlEqualTo("/test-url.mpd"))
        .withHeader("Accept", containing("text"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/xml")
            .withBodyFile("example.mpd")));

    final AbstractResourceMetadata metadata = audioVideoProcessor
        .parseMpdResource(resource, "application/xml");

    assertEquals(480, ((VideoResourceMetadata) metadata).getWidth().intValue());
    assertEquals(360, ((VideoResourceMetadata) metadata).getHeight().intValue());
  }

  @Test
  void testDownloadResourceForFullProcessing() {
    assertFalse(audioVideoProcessor.downloadResourceForFullProcessing());
  }

  @Test
  void testCopy() {

    // Create resource
    final Resource resource = mock(Resource.class);
    final Long fileSize = 12345L;
    final String url = "test url";
    doReturn(fileSize).when(resource).getProvidedFileSize();
    doReturn(url).when(resource).getResourceUrl();

    // Make call for audio type
    final String detectedAudioMimeType = "audio/detected mime type";
    assertEquals(MediaType.AUDIO, MediaType.getMediaType(detectedAudioMimeType));
    final ResourceExtractionResultImpl audioResult = audioVideoProcessor
        .copyMetadata(resource, detectedAudioMimeType);

    // Verify
    assertNotNull(audioResult);
    assertNotNull(audioResult.getOriginalMetadata());
    assertTrue(audioResult.getOriginalMetadata() instanceof AudioResourceMetadata);
    assertEquals(detectedAudioMimeType, audioResult.getOriginalMetadata().getMimeType());
    assertEquals(fileSize, audioResult.getOriginalMetadata().getContentSize());
    assertEquals(url, audioResult.getOriginalMetadata().getResourceUrl());
    assertNull(audioResult.getThumbnails());
    assertTrue(audioResult.getOriginalMetadata().getThumbnailTargetNames().isEmpty());
    assertNull(((AudioResourceMetadata) audioResult.getOriginalMetadata()).getSampleSize());
    assertNull(((AudioResourceMetadata) audioResult.getOriginalMetadata()).getSampleRate());
    assertNull(((AudioResourceMetadata) audioResult.getOriginalMetadata()).getDuration());
    assertNull(((AudioResourceMetadata) audioResult.getOriginalMetadata()).getChannels());
    assertNull(((AudioResourceMetadata) audioResult.getOriginalMetadata()).getBitRate());
    assertNull(((AudioResourceMetadata) audioResult.getOriginalMetadata()).getCodecName());

    // Mime type for video
    final String detectedVideoMimeType = "video/detected mime type";
    assertEquals(MediaType.VIDEO, MediaType.getMediaType(detectedVideoMimeType));
    final ResourceExtractionResultImpl videoResult = audioVideoProcessor
        .copyMetadata(resource, detectedVideoMimeType);

    // Verify
    assertNotNull(videoResult);
    assertNotNull(videoResult.getOriginalMetadata());
    assertTrue(videoResult.getOriginalMetadata() instanceof VideoResourceMetadata);
    assertEquals(detectedVideoMimeType, videoResult.getOriginalMetadata().getMimeType());
    assertEquals(fileSize, videoResult.getOriginalMetadata().getContentSize());
    assertEquals(url, videoResult.getOriginalMetadata().getResourceUrl());
    assertNull(videoResult.getThumbnails());
    assertTrue(videoResult.getOriginalMetadata().getThumbnailTargetNames().isEmpty());
    assertNull(((VideoResourceMetadata) videoResult.getOriginalMetadata()).getWidth());
    assertNull(((VideoResourceMetadata) videoResult.getOriginalMetadata()).getHeight());
    assertNull(((VideoResourceMetadata) videoResult.getOriginalMetadata()).getFrameRate());
    assertNull(((VideoResourceMetadata) videoResult.getOriginalMetadata()).getCodecName());
    assertNull(((VideoResourceMetadata) videoResult.getOriginalMetadata()).getBitRate());
    assertNull(((VideoResourceMetadata) videoResult.getOriginalMetadata()).getDuration());

    // Other mime type
    final String detectedOtherMimeType = "detected other mime type";
    assertEquals(MediaType.OTHER, MediaType.getMediaType(detectedOtherMimeType));
    assertNull(audioVideoProcessor.copyMetadata(resource, detectedOtherMimeType));
  }

  @Test
  void testExtract() throws IOException, MediaExtractionException {

    // Create resource
    final Resource resource = mock(Resource.class);
    doReturn(true).when(resource).hasContent();
    final String detectedMimeType = "detected mime type";

    // Prepare processor
    final List<String> command = Collections.emptyList();
    doReturn(command).when(audioVideoProcessor).createAudioVideoAnalysisCommand(resource);
    final String response = "response";
    doReturn(response).when(commandExecutor).execute(eq(command), anyMap(), eq(false), any());
    final AbstractResourceMetadata metadata = mock(AbstractResourceMetadata.class);
    doReturn(metadata).when(audioVideoProcessor)
                      .parseCommandResponse(resource, detectedMimeType, response);

    // Check that all is well
    final ResourceExtractionResultImpl result = audioVideoProcessor
        .extractMetadata(resource, detectedMimeType, true);
    assertEquals(metadata, result.getOriginalMetadata());
    assertNull(result.getThumbnails());

    // In case there was a command execution issue
    doThrow(new MediaExtractionException("", null)).when(commandExecutor).execute(eq(command), anyMap(), eq(false), any());
    assertThrows(MediaExtractionException.class,
        () -> audioVideoProcessor.extractMetadata(resource, detectedMimeType, true));
    doReturn(response).when(commandExecutor).execute(eq(command), anyMap(), eq(false), any());

    // Check that all is well again
    assertNotNull(audioVideoProcessor.extractMetadata(resource, detectedMimeType, true));
  }
}
