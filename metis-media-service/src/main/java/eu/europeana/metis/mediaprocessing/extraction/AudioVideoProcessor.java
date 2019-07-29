package eu.europeana.metis.mediaprocessing.extraction;

import eu.europeana.metis.mediaprocessing.exception.CommandExecutionException;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.model.AbstractResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.AudioResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResultImpl;
import eu.europeana.metis.mediaprocessing.model.VideoResourceMetadata;
import eu.europeana.metis.utils.MediaType;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Implementation of {@link MediaProcessor} that is designed to handle resources of types {@link
 * eu.europeana.metis.utils.MediaType#AUDIO} and {@link eu.europeana.metis.utils.MediaType#VIDEO}.
 * </p>
 * <p>
 * Note: No thumbnails are created for audio or video files.
 * </p>
 */
class AudioVideoProcessor implements MediaProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(AudioVideoProcessor.class);

  private static String globalFfprobeCommand;

  private final CommandExecutor commandExecutor;
  private final String ffprobeCommand;

  /**
   * Constructor. This is a wrapper for {@link AudioVideoProcessor#AudioVideoProcessor(CommandExecutor,
   * String)} where the property is detected. It is advisable to use this constructor for
   * non-testing purposes.
   *
   * @param commandExecutor A command executor.
   * @throws MediaProcessorException In case the properties could not be initialized.
   */
  AudioVideoProcessor(CommandExecutor commandExecutor) throws MediaProcessorException {
    this(commandExecutor, getGlobalFfprobeCommand(commandExecutor));
  }

  /**
   * Constructor.
   *
   * @param commandExecutor A command executor.
   * @param ffprobeCommand The ffprobe command (how to trigger ffprobe).
   */
  AudioVideoProcessor(CommandExecutor commandExecutor, String ffprobeCommand) {
    this.commandExecutor = commandExecutor;
    this.ffprobeCommand = ffprobeCommand;
  }

  private static String getGlobalFfprobeCommand(CommandExecutor commandExecutor)
      throws MediaProcessorException {
    synchronized (AudioVideoProcessor.class) {
      if (globalFfprobeCommand == null) {
        globalFfprobeCommand = discoverFfprobeCommand(commandExecutor);
      }
      return globalFfprobeCommand;
    }
  }

  static String discoverFfprobeCommand(CommandExecutor commandExecutor)
      throws MediaProcessorException {

    // Check whether ffprobe is installed.
    final String output;
    try {
      output = String.join("", commandExecutor.execute(Collections.singletonList("ffprobe"), true));
    } catch (CommandExecutionException e) {
      throw new MediaProcessorException("Error while looking for ffprobe tools", e);
    }
    if (!output.startsWith("ffprobe version 2") && !output.startsWith("ffprobe version 3")) {
      throw new MediaProcessorException("ffprobe 2.x/3.x not found");
    }

    // So it is installed and available.
    return "ffprobe";
  }

  private static boolean resourceHasContent(Resource resource) throws MediaExtractionException {
    try {
      return resource.hasContent();
    } catch (IOException e) {
      throw new MediaExtractionException("Could not determine whether resource has content.", e);
    }
  }

  List<String> createAudioVideoAnalysisCommand(Resource resource) throws MediaExtractionException {
    final String resourceLocation = resourceHasContent(resource) ?
        resource.getContentPath().toString() : resource.getResourceUrl();
    return Arrays.asList(ffprobeCommand, "-v", "quiet", "-print_format",
        "json", "-show_format", "-show_streams", "-hide_banner", resourceLocation);
  }

  @Override
  public boolean downloadResourceForFullProcessing() {
    return false;
  }

  @Override
  public ResourceExtractionResult copyMetadata(Resource resource, String detectedMimeType) {
    final AbstractResourceMetadata metadata;
    switch (MediaType.getMediaType(detectedMimeType)) {
      case AUDIO:
        metadata = new AudioResourceMetadata(detectedMimeType, resource.getResourceUrl(),
            resource.getProvidedFileSize());
        break;
      case VIDEO:
        metadata = new VideoResourceMetadata(detectedMimeType, resource.getResourceUrl(),
            resource.getProvidedFileSize());
        break;
      default:
        metadata = null;
        break;
    }
    return metadata == null ? null : new ResourceExtractionResultImpl(metadata);
  }

  @Override
  public ResourceExtractionResultImpl extractMetadata(Resource resource, String detectedMimeType)
      throws MediaExtractionException {

    // Execute command
    final List<String> response;
    try {
      response = commandExecutor.execute(createAudioVideoAnalysisCommand(resource), false);
    } catch (CommandExecutionException e) {
      throw new MediaExtractionException("Problem while analyzing audio/video file.", e);
    }

    // Parse command result.
    final AbstractResourceMetadata metadata = parseCommandResponse(resource, detectedMimeType,
        response);
    return new ResourceExtractionResultImpl(metadata, null);
  }

  JSONObject readCommandResponseToJson(List<String> response) {
    final String jsonString = String.join("", response);
    return new JSONObject(new JSONTokener(jsonString));
  }

  AbstractResourceMetadata parseCommandResponse(Resource resource, String detectedMimeType,
      List<String> response) throws MediaExtractionException {
    try {

      // Analyze command result
      final JSONObject result = readCommandResponseToJson(response);
      if (!resourceHasContent(resource) && result.length() == 0) {
        throw new MediaExtractionException(
            "Analysis of this media file revealed no metadata. Probably it could not be downloaded.");
      }
      final JSONObject format = result.getJSONObject("format");
      final JSONObject videoStream = findStream(result, "video");
      final JSONObject audioStream = findStream(result, "audio");
      final boolean isAudio = audioStream != null;
      final boolean isVideo = videoStream != null;

      // Process the video or audio stream and create metadata
      final AbstractResourceMetadata metadata;
      final long fileSize = format.getLong("size");
      if (isVideo) {
        // We have a video file
        final JSONObject[] candidates = new JSONObject[]{videoStream, format};
        final double duration = findDouble("duration", candidates);
        final int bitRate = findInt("bit_rate", candidates);
        final int width = findInt("width", candidates);
        final int height = findInt("height", candidates);
        final String codecName = findString("codec_name", candidates);
        final String[] frameRateParts = findString("avg_frame_rate", candidates).split("/");
        final double frameRate =
            Double.parseDouble(frameRateParts[0]) / Double.parseDouble(frameRateParts[1]);
        metadata = new VideoResourceMetadata(detectedMimeType, resource.getResourceUrl(),
            fileSize, duration, bitRate, width, height, codecName, frameRate);
      } else if (isAudio) {
        // We have an audio file
        final JSONObject[] candidates = new JSONObject[]{audioStream, format};
        final double duration = findDouble("duration", candidates);
        final int bitRate = findInt("bit_rate", candidates);
        final int channels = findInt("channels", candidates);
        final int sampleRate = findInt("sample_rate", candidates);
        final int sampleSize = findInt("bits_per_sample", candidates);
        metadata = new AudioResourceMetadata(detectedMimeType, resource.getResourceUrl(),
            fileSize, duration, bitRate, channels, sampleRate, sampleSize);
      } else {
        throw new MediaExtractionException("No media streams");
      }

      // Done
      return metadata;

    } catch (RuntimeException e) {
      LOGGER.info("Could not parse ffprobe response:\n" + StringUtils.join(response, "\n"), e);
      throw new MediaExtractionException("File seems to be corrupted", e);
    }
  }

  int findInt(String key, JSONObject[] candidates) {
    return findValue(key, candidates, candidate -> candidate.optInt(key, Integer.MIN_VALUE),
        value -> Integer.MIN_VALUE != value);
  }

  double findDouble(String key, JSONObject[] candidates) {
    return findValue(key, candidates, candidate -> candidate.optDouble(key, Double.NaN),
        value -> !value.isNaN());
  }

  String findString(String key, JSONObject[] candidates) {
    return findValue(key, candidates, candidate -> candidate.optString(key, StringUtils.EMPTY),
        StringUtils::isNotBlank);
  }

  <T> T findValue(String key, JSONObject[] candidates, Function<JSONObject, T> valueGetter,
      Predicate<T> valueValidator) {
    return Stream.of(candidates).map(valueGetter).filter(valueValidator).findFirst()
        .orElseThrow(() -> new JSONException("Could not find value for field: " + key));
  }

  JSONObject findStream(JSONObject data, String codecType) {
    for (Object streamObject : data.getJSONArray("streams")) {
      final JSONObject stream = (JSONObject) streamObject;
      if (codecType.equals(stream.getString("codec_type"))) {
        return stream;
      }
    }
    return null;
  }
}
