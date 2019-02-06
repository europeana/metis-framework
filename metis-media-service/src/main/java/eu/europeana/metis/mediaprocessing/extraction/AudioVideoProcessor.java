package eu.europeana.metis.mediaprocessing.extraction;

import eu.europeana.metis.mediaprocessing.exception.CommandExecutionException;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.model.AbstractResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.AudioResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import eu.europeana.metis.mediaprocessing.model.VideoResourceMetadata;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Implementation of {@link MediaProcessor} that is designed to handle resources of types
 * {@link ResourceType#AUDIO} and {@link ResourceType#VIDEO}.
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
   * Constructor. This is a wrapper for
   * {@link AudioVideoProcessor#AudioVideoProcessor(CommandExecutor, String)} where the property is
   * detected. It is advisable to use this constructor for non-testing purposes.
   * 
   * @param commandExecutor A command executor.
   * @throws MediaProcessorException In case the properties could not be initialized.
   */
  AudioVideoProcessor(CommandExecutor commandExecutor) throws MediaProcessorException {
    this(commandExecutor, initFfprobe(commandExecutor));
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

  private static synchronized String initFfprobe(CommandExecutor ce)
      throws MediaProcessorException {

    // If it is already set, we are done.
    if (globalFfprobeCommand != null) {
      return globalFfprobeCommand;
    }

    // Check whether ffprobe is installed.
    final String output;
    try {
      output = String.join("", ce.execute(Collections.singletonList("ffprobe"), true));
    } catch (CommandExecutionException e) {
      throw new MediaProcessorException("Error while looking for ffprobe tools", e);
    }
    if (!output.startsWith("ffprobe version 2") && !output.startsWith("ffprobe version 3")) {
      throw new MediaProcessorException("ffprobe 2.x/3.x not found");
    }

    // So it is installed and available.
    globalFfprobeCommand = "ffprobe";
    return globalFfprobeCommand;
  }

  @Override
  public ResourceExtractionResult process(Resource resource) throws MediaExtractionException {

    // Sanity check
    if (!UrlType.shouldExtractMetadata(resource.getUrlTypes())) {
      return null;
    }

    // Determine whether the resource has content.
    final boolean resourceHasContent;
    try {
      resourceHasContent = resource.hasContent();
    } catch (IOException e) {
      throw new MediaExtractionException("Could not determine whether resource has content.", e);
    }

    // Execute command
    final List<String> command = Arrays.asList(ffprobeCommand, "-v", "quiet", "-print_format",
        "json", "-show_format", "-show_streams", "-hide_banner",
        resourceHasContent ? resource.getContentPath().toString() : resource.getResourceUrl());
    final List<String> resultLines;
    try {
      resultLines = commandExecutor.execute(command, false);
    } catch (CommandExecutionException e) {
      throw new MediaExtractionException("Problem while analyzing audio/video file.", e);
    }

    // Parse command result.
    final AbstractResourceMetadata metadata;
    try {

      // Analyze command result
      final JSONObject result = new JSONObject(new JSONTokener(String.join("", resultLines)));
      if (!resourceHasContent && result.length() == 0) {
        throw new MediaExtractionException("Probably download failed");
      }
      final JSONObject format = result.getJSONObject("format");
      final JSONObject videoStream = findStream(result, "video");
      final JSONObject audioStream = findStream(result, "audio");

      // Process the video or audio stream
      final long fileSize = format.getLong("size");
      if (videoStream != null) {

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
        metadata = new VideoResourceMetadata(resource.getMimeType(), resource.getResourceUrl(),
            fileSize, duration, bitRate, width, height, codecName, frameRate);
      } else if (audioStream != null) {

        // We have an audio file
        final JSONObject[] candidates = new JSONObject[]{audioStream, format};
        final double duration = findDouble("duration", candidates);
        final int bitRate = findInt("bit_rate", candidates);
        final int channels = findInt("channels", candidates);
        final int sampleRate = findInt("sample_rate", candidates);
        final int sampleSize = findInt("bits_per_sample", candidates);
        metadata = new AudioResourceMetadata(resource.getMimeType(), resource.getResourceUrl(),
            fileSize, duration, bitRate, channels, sampleRate, sampleSize);
      } else {
        throw new MediaExtractionException("No media streams");
      }
    } catch (RuntimeException e) {
      LOGGER.info("Could not parse ffprobe response:\n" + StringUtils.join(resultLines, "\n"), e);
      throw new MediaExtractionException("File seems to be corrupted", e);
    }

    // Done
    return new ResourceExtractionResult(metadata, null);
  }

  private static int findInt(String key, JSONObject[] candidates) {
    final int defaultValue = Integer.MIN_VALUE;
    return Stream.of(candidates).map(candidate -> candidate.optInt(key, defaultValue))
        .filter(value -> defaultValue != value).findFirst()
        .orElseThrow(() -> new JSONException("Could not find integer for field: " + key));
  }

  private static double findDouble(String key, JSONObject[] candidates) {
    return Stream.of(candidates).map(candidate -> candidate.optDouble(key, Double.NaN))
        .filter(value -> !Double.isNaN(value)).findFirst()
        .orElseThrow(() -> new JSONException("Could not find double for field: " + key));
  }

  private static String findString(String key, JSONObject[] candidates) {
    return Stream.of(candidates).map(candidate -> candidate.optString(key, ""))
        .filter(StringUtils::isNotBlank).findFirst()
        .orElseThrow(() -> new JSONException("Could not find String for field: " + key));
  }

  private JSONObject findStream(JSONObject data, String codecType) {
    for (Object streamObject : data.getJSONArray("streams")) {
      final JSONObject stream = (JSONObject) streamObject;
      if (codecType.equals(stream.getString("codec_type"))) {
        return stream;
      }
    }
    return null;
  }
}
