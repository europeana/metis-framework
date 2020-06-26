package eu.europeana.metis.mediaprocessing.extraction;

import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.model.AbstractResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.AudioResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResultImpl;
import eu.europeana.metis.mediaprocessing.model.VideoResourceMetadata;
import eu.europeana.metis.utils.MediaType;
import io.lindstrom.mpd.MPDParser;
import io.lindstrom.mpd.data.AdaptationSet;
import io.lindstrom.mpd.data.FrameRate;
import io.lindstrom.mpd.data.MPD;
import io.lindstrom.mpd.data.Period;
import io.lindstrom.mpd.data.Representation;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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
    final String command = "ffprobe";
    final String output;
    output = commandExecutor.execute(Collections.singletonList(command), true, message ->
            new MediaProcessorException("Error while looking for ffprobe tools: " + message));
    if (!output.startsWith("ffprobe version 2") && !output.startsWith("ffprobe version 3")) {
      throw new MediaProcessorException("ffprobe 2.x/3.x not found");
    }

    // So it is installed and available.
    return command;
  }

  private static boolean resourceHasContent(Resource resource) throws MediaExtractionException {
    try {
      return resource.hasContent();
    } catch (IOException e) {
      throw new MediaExtractionException("Could not determine whether resource has content.", e);
    }
  }

  private static String validateUrl(String url) throws MediaExtractionException {
    try {
      return new URI(url).toURL().toString();
    } catch (RuntimeException | URISyntaxException | MalformedURLException e) {
      throw new MediaExtractionException("Could not validate URL: " + url, e);
    }
  }

  List<String> createAudioVideoAnalysisCommand(Resource resource) throws MediaExtractionException {
    final String resourceLocation = resourceHasContent(resource) ?
        resource.getContentPath().toString() : validateUrl(resource.getResourceUrl());
    return Arrays.asList(ffprobeCommand, "-v", "quiet", "-print_format",
        "json", "-show_format", "-show_streams", "-hide_banner", resourceLocation);
  }

  @Override
  public boolean downloadResourceForFullProcessing() {
    return false;
  }

  @Override
  public ResourceExtractionResultImpl copyMetadata(Resource resource, String detectedMimeType) {
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

    AbstractResourceMetadata metadata;
    //Check if it's an mpd mimetype
    if ("application/dash+xml".equals(detectedMimeType)) {
      metadata = parseMpdResource(resource, detectedMimeType);
    } else {
      // Execute command
      final Function<String, MediaExtractionException> exceptionProducer = message ->
              new MediaExtractionException("Problem while analyzing audio/video file: " + message);
      final String response = commandExecutor
              .execute(createAudioVideoAnalysisCommand(resource), false, exceptionProducer);

      // Parse command result.
      metadata = parseCommandResponse(resource, detectedMimeType, response);
    }
    return new ResourceExtractionResultImpl(metadata, null);

  }

  private Representation getRepresentationFromMpd(AdaptationSet videoAdaptationSet)
          throws MediaExtractionException {
    // If only one representation available, get that one, otherwise get the first of type video
    Representation videoRepresentation = videoAdaptationSet.getRepresentations().get(0);
    if (videoAdaptationSet.getRepresentations().size() > 1) {
      //Get the one with the highest width*height if possible
      videoRepresentation = videoAdaptationSet.getRepresentations().stream()
              .filter(representation -> representation.getWidth() != null
                      && representation.getHeight() != null).max(Comparator.comparing(
                      representation -> representation.getWidth() * representation.getHeight()))
              .orElse(null);

      //If not max resolution found then get the one that is at least of type video
      if (videoRepresentation == null) {
        videoRepresentation = videoAdaptationSet.getRepresentations().stream()
                .filter(representation -> (representation.getMimeType() != null && representation
                        .getMimeType().startsWith("video")) || representation.getWidth() != null
                        || representation.getHeight() != null)
                .findFirst().orElseThrow(() -> new MediaExtractionException(
                        "Cannot find video representation element in mpd"));
      }
    }
    return videoRepresentation;
  }

  AbstractResourceMetadata parseMpdResource(Resource resource, String detectedMimeType)
      throws MediaExtractionException {

    // Parse the result.
    final MPDParser parser = new MPDParser();
    final MPD mpd;
    // We know where the URL comes from: from the apache library.
    try (@SuppressWarnings("findsecbugs:URLCONNECTION_SSRF_FD") InputStream inputStream = resource
            .getActualLocation().toURL().openStream()) {
      mpd = parser.parse(inputStream);

    } catch (IOException e) {
      throw new MediaExtractionException("Problem while analyzing audio/video file.", e);
    }

    // Analyze the result - get some data structures we will need.
    final Period period = mpd.getPeriods().stream().findFirst()
            .orElseThrow(() -> new MediaExtractionException("Cannot find period element in mpd"));
    final AdaptationSet videoAdaptationSet = period.getAdaptationSets().stream().filter(adaptationSet ->
            Stream.of(adaptationSet.getMimeType(), adaptationSet.getContentType())
                    .filter(Objects::nonNull).findFirst().map(type -> type.startsWith("video"))
                    .orElse(Boolean.FALSE)
    ).findFirst().orElseThrow(() -> new MediaExtractionException(
            "Cannot find video adaptation set element in mpd"));
    final Representation videoRepresentation =getRepresentationFromMpd(videoAdaptationSet);
    final Duration durationValue = mpd.getMediaPresentationDuration();

    //Get value either from the adaptation set or the representation
    final Long widthValue = videoRepresentation.getWidth() == null ?
            videoAdaptationSet.getWidth() : videoRepresentation.getWidth();
    final Long heightValue = videoRepresentation.getHeight() == null ?
            videoAdaptationSet.getHeight() : videoRepresentation.getHeight();
    final FrameRate frameRateValue = videoRepresentation.getFrameRate() == null ?
            videoAdaptationSet.getFrameRate() : videoRepresentation.getFrameRate();
    final String codecNames = videoRepresentation.getCodecs() == null ?
            videoAdaptationSet.getCodecs() : videoRepresentation.getCodecs();

    // Normalize numerical values
    final Integer width = widthValue == null ? null : nullIfNegative(Math.toIntExact(widthValue));
    final Integer height =
            heightValue == null ? null : nullIfNegative(Math.toIntExact(heightValue));
    final Double duration =
            durationValue == null ? null : nullIfNegative((double) durationValue.getSeconds());
    final Double frameRate =
            frameRateValue == null ? null : nullIfNegative(frameRateValue.toDouble());
    final Integer bitRate = nullIfNegative(Math.toIntExact(videoRepresentation.getBandwidth()));

    // Create response object
    return new VideoResourceMetadata(detectedMimeType, resource.getResourceUrl(), null, duration,
            bitRate, width, height, codecNames, frameRate);
  }

  JSONObject readCommandResponseToJson(String response) {
    return new JSONObject(new JSONTokener(response));
  }

  AbstractResourceMetadata parseCommandResponse(Resource resource, String detectedMimeType,
      String response) throws MediaExtractionException {
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
      final Long fileSize = nullIfNegative(format.getLong("size"));
      if (isVideo) {
        // We have a video file
        final JSONObject[] candidates = new JSONObject[]{videoStream, format};
        final Double duration = findDouble("duration", candidates);
        final Integer bitRate = findInt("bit_rate", candidates);
        final Integer width = findInt("width", candidates);
        final Integer height = findInt("height", candidates);
        final String codecName = findString("codec_name", candidates);
        final Double frameRate = calculateFrameRate(findString("avg_frame_rate", candidates));
        metadata = new VideoResourceMetadata(detectedMimeType, resource.getResourceUrl(),
            fileSize, duration, bitRate, width, height, codecName, frameRate);
      } else if (isAudio) {
        // We have an audio file
        final JSONObject[] candidates = new JSONObject[]{audioStream, format};
        final Double duration = findDouble("duration", candidates);
        final Integer bitRate = findInt("bit_rate", candidates);
        final Integer channels = findInt("channels", candidates);
        final Integer sampleRate = findInt("sample_rate", candidates);
        final Integer sampleSize = findInt("bits_per_sample", candidates);
        final String codecName = findString("codec_name", candidates);
        metadata = new AudioResourceMetadata(detectedMimeType, resource.getResourceUrl(),
            fileSize, duration, bitRate, channels, sampleRate, sampleSize, codecName);
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

  private Double calculateFrameRate(String frameRateString) {
    final String[] frameRateParts = frameRateString.split("/");
    final double numerator = Double.parseDouble(frameRateParts[0]);
    final double denominator = Double.parseDouble(frameRateParts[1]);
    @SuppressWarnings("squid:S1244") // We really want to test equality with zero.
    final boolean zeroDenominator = denominator == 0.0;
    if (zeroDenominator) {
      @SuppressWarnings("squid:S1244") // We really want to test equality with zero.
      final boolean zeroNumerator = numerator == 0.0;
      return zeroNumerator ? 0.0 : null;
    }
    return nullIfNegative(numerator / denominator);
  }

  Integer findInt(String key, JSONObject[] candidates) {
    final int result = findValue(key, candidates,
            candidate -> candidate.optInt(key, Integer.MIN_VALUE),
            value -> Integer.MIN_VALUE != value);
    return nullIfNegative(result);
  }

  Double findDouble(String key, JSONObject[] candidates) {
    final double result = findValue(key, candidates,
            candidate -> candidate.optDouble(key, Double.NaN), value -> !value.isNaN());
    return nullIfNegative(result);
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
