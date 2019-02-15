package eu.europeana.metis.mediaprocessing.extraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;

import eu.europeana.metis.mediaprocessing.exception.CommandExecutionException;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.model.AbstractResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.AudioResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import eu.europeana.metis.mediaprocessing.model.VideoResourceMetadata;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
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

  @BeforeAll
  static void prepare() {
    commandExecutor = mock(CommandExecutor.class);
    audioVideoProcessor = spy(new AudioVideoProcessor(commandExecutor, FF_PROBE_COMMAND));
  }

  @BeforeEach
  void resetMocks() {
    reset(commandExecutor);
    doReturn(true).when(audioVideoProcessor).shouldExtractMetadata(notNull());
  }

  @Test
  void testDiscoverFfprobeCommand() throws CommandExecutionException, MediaProcessorException {

    // ffprobe command
    final String ffprobeCommand = "ffprobe";

    // Test ffprobe 3
    doReturn(Collections.singletonList(
        "ffprobe version 3.4.4-0ubuntu0.18.04.1 Copyright (c) 2007-2018 the FFmpeg developers"))
        .when(commandExecutor).execute(eq(Collections.singletonList(ffprobeCommand)), eq(true));
    assertEquals(ffprobeCommand, AudioVideoProcessor.discoverFfprobeCommand(commandExecutor));

    // Test ffprobe 2
    doReturn(Collections.singletonList(
        "ffprobe version 2.4.4-0ubuntu0.18.04.1 Copyright (c) 2007-2018 the FFmpeg developers"))
        .when(commandExecutor).execute(eq(Collections.singletonList(ffprobeCommand)), eq(true));
    assertEquals(ffprobeCommand, AudioVideoProcessor.discoverFfprobeCommand(commandExecutor));

    // Test other commands
    doReturn(Collections.singletonList(
        "ffprobe version 1.4.4-0ubuntu0.18.04.1 Copyright (c) 2007-2018 the FFmpeg developers"))
        .when(commandExecutor).execute(eq(Collections.singletonList(ffprobeCommand)), eq(true));
    assertThrows(MediaProcessorException.class,
        () -> AudioVideoProcessor.discoverFfprobeCommand(commandExecutor));
    doReturn(Collections.singletonList("Other command")).when(commandExecutor)
        .execute(eq(Collections.singletonList(ffprobeCommand)), eq(true));
    assertThrows(MediaProcessorException.class,
        () -> AudioVideoProcessor.discoverFfprobeCommand(commandExecutor));

    // Test command execution exception
    doThrow(new CommandExecutionException("", null)).when(commandExecutor)
        .execute(eq(Collections.singletonList(ffprobeCommand)), eq(true));
    assertThrows(MediaProcessorException.class,
        () -> AudioVideoProcessor.discoverFfprobeCommand(commandExecutor));
  }

  @Test
  void testCreateAudioVideoAnalysisCommand() throws IOException, MediaExtractionException {

    // Create resource
    final Resource resource = mock(Resource.class);
    doReturn("resource url").when(resource).getResourceUrl();
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

    // test with exception
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
    assertEquals(value, audioVideoProcessor.findValue(key, objects, firstOnly, Objects::nonNull));

    // Test second object only
    final Function<JSONObject, String> secondOnly = object -> object == object2 ? value : null;
    assertEquals(value, audioVideoProcessor.findValue(key, objects, secondOnly, Objects::nonNull));

    // Test both
    final Function<JSONObject, String> both = object -> value;
    assertEquals(value, audioVideoProcessor.findValue(key, objects, both, Objects::nonNull));

    // Test neither
    final Function<JSONObject, String> neither = object -> null;
    assertThrows(JSONException.class,
        () -> audioVideoProcessor.findValue(key, objects, neither, Objects::nonNull));
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
    assertThrows(JSONException.class, () -> audioVideoProcessor.findInt(key, objects));
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
    doAnswer(invocation -> invocation.getArgument(1)).when(object).optDouble(eq(key), anyDouble());
    assertThrows(JSONException.class, () -> audioVideoProcessor.findDouble(key, objects));
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
    doAnswer(invocation -> invocation.getArgument(1)).when(object).optString(eq(key), anyString());
    assertThrows(JSONException.class, () -> audioVideoProcessor.findString(key, objects));
  }

  @Test
  void testParseCommandResponseForAudio() throws MediaExtractionException, IOException {

    // Create resource
    final Resource resource = mock(Resource.class);
    doReturn("resource url").when(resource).getResourceUrl();
    doReturn("mime type").when(resource).getMimeType();
    doReturn(true).when(resource).hasContent();
    final String detectedMimeType = "detected mime type";

    // Create json objects
    final List<String> commandResponse = Collections.emptyList();
    final JSONObject object = mock(JSONObject.class);
    doReturn(object).when(audioVideoProcessor).readCommandResponseToJson(commandResponse);
    final JSONObject format = mock(JSONObject.class);
    doReturn(format).when(object).getJSONObject("format");
    final JSONObject audioStream = mock(JSONObject.class);
    doReturn(audioStream).when(audioVideoProcessor).findStream(object, "audio");
    doReturn(null).when(audioVideoProcessor).findStream(object, "video");
    final JSONObject[] candidates = new JSONObject[]{audioStream, format};

    // Set properties
    doReturn(7205015L).when(format).getLong("size");
    doReturn(44100).when(audioVideoProcessor).findInt(eq("sample_rate"), eq(candidates));
    doReturn(2).when(audioVideoProcessor).findInt(eq("channels"), eq(candidates));
    doReturn(8).when(audioVideoProcessor).findInt(eq("bits_per_sample"), eq(candidates));
    doReturn(180.062050).when(audioVideoProcessor).findDouble(eq("duration"), eq(candidates));
    doReturn(320000).when(audioVideoProcessor).findInt(eq("bit_rate"), eq(candidates));

    // Run and verify
    final AbstractResourceMetadata abstractMetadata = audioVideoProcessor
        .parseCommandResponse(resource, detectedMimeType, commandResponse);
    assertTrue(abstractMetadata instanceof AudioResourceMetadata);
    final AudioResourceMetadata metadata = (AudioResourceMetadata) abstractMetadata;
    assertEquals(metadata.getMimeType(), detectedMimeType);
    assertEquals(metadata.getResourceUrl(), resource.getResourceUrl());
    assertTrue(metadata.getThumbnailTargetNames().isEmpty());
    assertEquals(7205015L, metadata.getContentSize());
    assertEquals(320000, metadata.getBitRate());
    assertEquals(2, metadata.getChannels());
    assertEquals(180.062050, metadata.getDuration());
    assertEquals(44100, metadata.getSampleRate());
    assertEquals(8, metadata.getSampleSize());
  }

  @Test
  void testParseCommandResponseForVideo() throws MediaExtractionException, IOException {

    // Create resource
    final Resource resource = mock(Resource.class);
    doReturn("resource url").when(resource).getResourceUrl();
    doReturn("mime type").when(resource).getMimeType();
    doReturn(true).when(resource).hasContent();
    final String detectedMimeType = "detected mime type";

    // Create json objects
    final List<String> commandResponse = Collections.emptyList();
    final JSONObject object = mock(JSONObject.class);
    doReturn(object).when(audioVideoProcessor).readCommandResponseToJson(commandResponse);
    final JSONObject format = mock(JSONObject.class);
    doReturn(format).when(object).getJSONObject("format");
    final JSONObject audioStream = mock(JSONObject.class);
    doReturn(audioStream).when(audioVideoProcessor).findStream(object, "audio");
    final JSONObject videoStream = mock(JSONObject.class);
    doReturn(videoStream).when(audioVideoProcessor).findStream(object, "video");
    final JSONObject[] candidates = new JSONObject[]{videoStream, format};

    // Set properties
    doReturn(92224193L).when(format).getLong("size");
    doReturn(640).when(audioVideoProcessor).findInt(eq("width"), eq(candidates));
    doReturn(480).when(audioVideoProcessor).findInt(eq("height"), eq(candidates));
    doReturn("h264").when(audioVideoProcessor).findString(eq("codec_name"), eq(candidates));
    doReturn(1007.240000).when(audioVideoProcessor).findDouble(eq("duration"), eq(candidates));
    doReturn(595283).when(audioVideoProcessor).findInt(eq("bit_rate"), eq(candidates));
    doReturn("629150/25181").when(audioVideoProcessor)
        .findString(eq("avg_frame_rate"), eq(candidates));

    // Run and verify
    final AbstractResourceMetadata abstractMetadata = audioVideoProcessor
        .parseCommandResponse(resource, detectedMimeType, commandResponse);
    assertTrue(abstractMetadata instanceof VideoResourceMetadata);
    final VideoResourceMetadata metadata = (VideoResourceMetadata) abstractMetadata;
    assertEquals(metadata.getMimeType(), detectedMimeType);
    assertEquals(metadata.getResourceUrl(), resource.getResourceUrl());
    assertTrue(metadata.getThumbnailTargetNames().isEmpty());
    assertEquals(92224193L, metadata.getContentSize());
    assertEquals(595283, metadata.getBitRate());
    assertEquals("h264", metadata.getCodecName());
    assertEquals(1007.240000, metadata.getDuration());
    assertEquals(629150.0 / 25181.0, metadata.getFrameRate());
    assertEquals(480, metadata.getHeight());
    assertEquals(640, metadata.getWidth());
  }

  @Test
  void testParseCommandResponseErrors() throws MediaExtractionException, IOException {

    // Create resource
    final Resource resource = mock(Resource.class);
    doReturn(true).when(resource).hasContent();
    final String detectedMimeType = "detected mime type";

    // Create json objects
    final List<String> commandResponse = Collections.emptyList();
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
  void testProcess() throws IOException, MediaExtractionException, CommandExecutionException {

    // Create resource
    final Resource resource = mock(Resource.class);
    doReturn(true).when(resource).hasContent();
    final String detectedMimeType = "detected mime type";

    // Prepare processor
    doReturn(true).when(audioVideoProcessor).shouldExtractMetadata(resource);
    final List<String> command = Collections.emptyList();
    doReturn(command).when(audioVideoProcessor).createAudioVideoAnalysisCommand(resource);
    final List<String> response = Collections.emptyList();
    doReturn(response).when(commandExecutor).execute(command, false);
    final AbstractResourceMetadata metadata = mock(AbstractResourceMetadata.class);
    doReturn(metadata).when(audioVideoProcessor)
        .parseCommandResponse(resource, detectedMimeType, response);

    // Check that all is well
    final ResourceExtractionResult result = audioVideoProcessor.process(resource, detectedMimeType);
    assertEquals(metadata, result.getOriginalMetadata());
    assertNull(result.getThumbnails());

    // In case we should not extract metadata at all
    doReturn(false).when(audioVideoProcessor).shouldExtractMetadata(resource);
    assertNull(audioVideoProcessor.process(resource, detectedMimeType));
    doReturn(true).when(audioVideoProcessor).shouldExtractMetadata(resource);

    // In case there was a command execution issue
    doThrow(new CommandExecutionException("", null)).when(commandExecutor).execute(command, false);
    assertThrows(MediaExtractionException.class,
        () -> audioVideoProcessor.process(resource, detectedMimeType));
    doReturn(response).when(commandExecutor).execute(command, false);

    // Check that all is well again
    assertNotNull(audioVideoProcessor.process(resource, detectedMimeType));
  }
}
