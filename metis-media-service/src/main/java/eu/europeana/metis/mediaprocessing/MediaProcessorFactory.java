package eu.europeana.metis.mediaprocessing;

import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.extraction.MediaExtractorImpl;
import eu.europeana.metis.mediaprocessing.linkchecking.LinkCheckerImpl;

/**
 * This factory creates objects for media extraction and link checking. This object is thread-safe.
 * <p>Used by external code such as scripts or ECloud.</p>
 */
public class MediaProcessorFactory {

  /**
   * The default value of the maximum number of times we will follow a redirect. It's currently set
   * to {@value MediaProcessorFactory#DEFAULT_MAX_REDIRECT_COUNT}.
   **/
  public static final int DEFAULT_MAX_REDIRECT_COUNT = 3;

  /**
   * The default value of the maximum amount of time, in seconds, a thumbnail generate command is
   * allowed to take before it is forcibly destroyed (i.e. cancelled). It's currently set to {@value
   * MediaProcessorFactory#DEFAULT_THUMBNAIL_GENERATE_TIMEOUT} seconds.
   */
  public static final int DEFAULT_THUMBNAIL_GENERATE_TIMEOUT = 30;

  /**
   * The default value of the maximum amount of time, in seconds, a audio/video probe command is
   * allowed to take before it is forcibly destroyed (i.e. cancelled). It's currently set to {@value
   * MediaProcessorFactory#DEFAULT_AUDIO_VIDEO_PROBE_TIMEOUT} seconds.
   */
  public static final int DEFAULT_AUDIO_VIDEO_PROBE_TIMEOUT = 60;

  private int maxRedirectCount = DEFAULT_MAX_REDIRECT_COUNT;
  private int thumbnailGenerateTimeout = DEFAULT_THUMBNAIL_GENERATE_TIMEOUT;
  private int audioVideoProbeTimeout = DEFAULT_AUDIO_VIDEO_PROBE_TIMEOUT;

  /**
   * Set the maximum number of times we will follow a redirect. The default (when not calling this
   * method or calling it with a negative number) is {@link MediaProcessorFactory#DEFAULT_MAX_REDIRECT_COUNT}.
   *
   * @param maxRedirectCount The maximum number of times we will follow a redirect.
   */
  public void setMaxRedirectCount(int maxRedirectCount) {
    this.maxRedirectCount = maxRedirectCount < 0 ? DEFAULT_MAX_REDIRECT_COUNT : maxRedirectCount;
  }

  /**
   * Set the timeout for performing command-line IO for thumbnail generation. The default (when not
   * calling this method or calling it with zero or a negative number) is {@link
   * MediaProcessorFactory#DEFAULT_THUMBNAIL_GENERATE_TIMEOUT} seconds.
   *
   * @param thumbnailGenerateTimeout The maximum amount of time, in seconds, a thumbnail generation
   * command is allowed to take before it is forcibly destroyed (i.e. cancelled).
   */
  public void setThumbnailGenerateTimeout(int thumbnailGenerateTimeout) {
    this.thumbnailGenerateTimeout =
        thumbnailGenerateTimeout < 1 ? DEFAULT_THUMBNAIL_GENERATE_TIMEOUT
            : thumbnailGenerateTimeout;
  }

  /**
   * Set the timeout for performing command-line IO for audio/video probing. The default (when not
   * calling this method or calling it with zero or a negative number) is {@link
   * MediaProcessorFactory#DEFAULT_AUDIO_VIDEO_PROBE_TIMEOUT} seconds.
   *
   * @param audioVideoProbeTimeout The maximum amount of time, in seconds, a audio/video probe
   * command is allowed to take before it is forcibly destroyed (i.e. cancelled).
   */
  public void setAudioVideoProbeTimeout(int audioVideoProbeTimeout) {
    this.audioVideoProbeTimeout =
        audioVideoProbeTimeout < 1 ? DEFAULT_AUDIO_VIDEO_PROBE_TIMEOUT : audioVideoProbeTimeout;
  }

  /**
   * Create a media extractor object that can be used to extract media metadata and thumbnails.
   *
   * @return A media extractor.
   * @throws MediaProcessorException In case there was a problem creating the media extractor.
   */
  public MediaExtractor createMediaExtractor() throws MediaProcessorException {
    return MediaExtractorImpl
        .newInstance(maxRedirectCount, thumbnailGenerateTimeout, audioVideoProbeTimeout);
  }

  /**
   * Create a link checker object that can be used to check links.
   *
   * @return A link checker.
   * @throws MediaProcessorException In case there was a problem creating the link checker.
   */
  public LinkChecker createLinkChecker() throws MediaProcessorException {
    return new LinkCheckerImpl(maxRedirectCount);
  }
}
