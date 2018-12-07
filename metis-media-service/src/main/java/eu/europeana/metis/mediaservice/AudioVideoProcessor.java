package eu.europeana.metis.mediaservice;

import eu.europeana.metis.mediaprocessing.model.UrlType;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.model.AudioResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import eu.europeana.metis.mediaprocessing.model.ResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.VideoResourceMetadata;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AudioVideoProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(AudioVideoProcessor.class);

  private static String ffprobeCmd;

  private CommandExecutor ce;

  AudioVideoProcessor(CommandExecutor ce) {
    this.ce = ce;
    init(ce);
  }

  private static synchronized void init(CommandExecutor ce) {
    if (ffprobeCmd != null) {
      return;
    }
    try {
      String output = String.join("", ce.runCommand(Arrays.asList("ffprobe"), true));
      if (!output.startsWith("ffprobe version 2") && !output.startsWith("ffprobe version 3")) {
        throw new RuntimeException("ffprobe 2.x/3.x not found");
      }
      ffprobeCmd = "ffprobe";
    } catch (IOException e) {
      throw new RuntimeException("Error while looking for ffprobe tools", e);
    }
  }

  ResourceExtractionResult processAudioVideo(String url, Set<UrlType> urlTypes,
      String mimeType, File contents) throws MediaExtractionException {

    // Sanity check
    if (!UrlType.shouldExtractMetadata(urlTypes)) {
      return null;
    }

    // Execute command
    List<String> command = Arrays.asList(ffprobeCmd, "-v", "quiet", "-print_format", "json",
        "-show_format", "-show_streams", "-hide_banner",
        contents == null ? url : contents.getPath());
    List<String> resultLines;
    try {
      resultLines = ce.runCommand(command, false);
    } catch (IOException e) {
      throw new MediaExtractionException("Problem while analyzing audio/video file.", e);
    }

    final ResourceMetadata metadata;
    try {

      // Analyze command result
      JSONObject result = new JSONObject(new JSONTokener(String.join("", resultLines)));
      if (contents == null && result.length() == 0) {
        throw new MediaExtractionException("Probably download failed");
      }
      final long fileSize = result.getJSONObject("format").getLong("size");
      final JSONObject videoStream = findStream(result, "video");
      final JSONObject audioStream = findStream(result, "audio");

      // Process the video or audio stream
      if (videoStream != null) {
        final double duration = videoStream.getDouble("duration");
        final int bitRate = videoStream.getInt("bit_rate");
        final int width = videoStream.getInt("width");
        final int height = videoStream.getInt("height");
        final String codecName = videoStream.getString("codec_name");
        final String[] frameRateParts = videoStream.getString("avg_frame_rate").split("/");
        final double frameRate =
            Double.parseDouble(frameRateParts[0]) / Double.parseDouble(frameRateParts[1]);
        metadata = new VideoResourceMetadata(mimeType, url, fileSize, duration, bitRate, width,
            height, codecName, frameRate);
      } else if (audioStream != null) {
        final double duration = audioStream.getDouble("duration");
        final int bitRate = audioStream.getInt("bit_rate");
        final int channels = audioStream.getInt("channels");
        final int sampleRate = audioStream.getInt("sample_rate");
        final int sampleSize = audioStream.getInt("bits_per_sample");
        metadata = new AudioResourceMetadata(mimeType, url, fileSize, duration, bitRate, channels,
            sampleRate, sampleSize);
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

  private JSONObject findStream(JSONObject data, String codecType) {
    for (Object streamObject : data.getJSONArray("streams")) {
      JSONObject stream = (JSONObject) streamObject;
      if (codecType.equals(stream.getString("codec_type"))) {
        return stream;
      }
    }
    return null;
  }

  static void setCommand(String ffprobe) {
    AudioVideoProcessor.ffprobeCmd = ffprobe;
  }
}
