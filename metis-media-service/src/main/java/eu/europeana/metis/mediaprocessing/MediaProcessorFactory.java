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
  public static final int DEFAULT_THUMBNAIL_GENERATE_TIMEOUT = 20;

  /**
   * The default value of the maximum amount of time, in seconds, a audio/video probe command is
   * allowed to take before it is forcibly destroyed (i.e. cancelled). It's currently set to {@value
   * MediaProcessorFactory#DEFAULT_AUDIO_VIDEO_PROBE_TIMEOUT} seconds.
   */
  public static final int DEFAULT_AUDIO_VIDEO_PROBE_TIMEOUT = 60;

  /**
   * The default value of the maximum amount of time, in milliseconds, we wait for a connection to a
   * resource before timing out. It's currently set to {@value MediaProcessorFactory#DEFAULT_RESOURCE_CONNECT_TIMEOUT}
   * milliseconds.
   */
  public static final int DEFAULT_RESOURCE_CONNECT_TIMEOUT = 10_000;

  /**
   * The default value of the maximum amount of time, in milliseconds, we wait for the response
   * before timing out. It's currently set to {@value MediaProcessorFactory#DEFAULT_RESOURCE_RESPONSE_TIMEOUT}
   * milliseconds.
   */
  public static final int DEFAULT_RESOURCE_RESPONSE_TIMEOUT = 20_000;

  /**
   * The default value of the maximum amount of time, in milliseconds, we allow the download to take
   * before timing out. It's currently set to {@value MediaProcessorFactory#DEFAULT_RESOURCE_DOWNLOAD_TIMEOUT}
   * milliseconds.
   */
  public static final int DEFAULT_RESOURCE_DOWNLOAD_TIMEOUT = 600_000;

  private int maxRedirectCount = DEFAULT_MAX_REDIRECT_COUNT;
  private int thumbnailGenerateTimeout = DEFAULT_THUMBNAIL_GENERATE_TIMEOUT;
  private int audioVideoProbeTimeout = DEFAULT_AUDIO_VIDEO_PROBE_TIMEOUT;
  private int resourceConnectTimeout = DEFAULT_RESOURCE_CONNECT_TIMEOUT;
  private int resourceResponseTimeout = DEFAULT_RESOURCE_RESPONSE_TIMEOUT;
  private int resourceDownloadTimeout = DEFAULT_RESOURCE_DOWNLOAD_TIMEOUT;

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
   * Retained for API compatibility. Image and PDF thumbnail processing now runs in Java and
   * has no external-command timeout, so this setting has no effect.
   *
   * @param thumbnailGenerateTimeout The former thumbnail command timeout in seconds.
   * @deprecated Image and PDF thumbnails no longer use external commands.
   */
  @Deprecated
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
   * Set the maximum amount of time, in milliseconds, we wait for a connection to a resource before
   * timing out. The default (when not calling this method or calling it with zero or a negative
   * number) is {@value MediaProcessorFactory#DEFAULT_RESOURCE_CONNECT_TIMEOUT} milliseconds.
   *
   * @param resourceConnectTimeout The maximum amount of time, in milliseconds, we wait for a
   * connection to a resource before timing out.
   */
  public void setResourceConnectTimeout(int resourceConnectTimeout) {
    this.resourceConnectTimeout =
        resourceConnectTimeout < 1 ? DEFAULT_RESOURCE_CONNECT_TIMEOUT : resourceConnectTimeout;
  }

  /**
   * Set the maximum amount of time, in milliseconds, we wait for the response. The default (when
   * not calling this method or calling it with zero or a negative number) is {@value
   * MediaProcessorFactory#DEFAULT_RESOURCE_RESPONSE_TIMEOUT} milliseconds.
   *
   * @param resourceResponseTimeout The maximum amount of time, in milliseconds, we wait for the
   * response before timing out.
   */
  public void setResourceResponseTimeout(int resourceResponseTimeout) {
    this.resourceResponseTimeout =
            resourceResponseTimeout < 1 ? DEFAULT_RESOURCE_RESPONSE_TIMEOUT : resourceResponseTimeout;
  }

  /**
   * Set the resource response timeout.
   *
   * @param resourceSocketTimeout the timeout.
   * @see #setResourceResponseTimeout(int) setResourceResponseTimeout(int)
   * @deprecated Use {@link #setResourceResponseTimeout(int)} instead.
   */
  @Deprecated
  public void setResourceSocketTimeout(int resourceSocketTimeout) {
    this.setResourceResponseTimeout(resourceSocketTimeout);
  }

  /**
   * Set the maximum amount of time, in milliseconds, we allow the download to take before timing
   * out. The default (when not calling this method or calling it with zero or a negative number) is
   * {@value MediaProcessorFactory#DEFAULT_RESOURCE_DOWNLOAD_TIMEOUT} milliseconds.
   *
   * @param resourceDownloadTimeout The maximum amount of time, in milliseconds, we allow the
   * download to take before timing out.
   */
  public void setResourceDownloadTimeout(int resourceDownloadTimeout) {
    this.resourceDownloadTimeout =
        resourceDownloadTimeout < 1 ? DEFAULT_RESOURCE_DOWNLOAD_TIMEOUT : resourceDownloadTimeout;
  }

  /**
   * Create a media extractor object that can be used to extract media metadata and thumbnails.
   *
   * @return A media extractor.
   * @throws MediaProcessorException In case there was a problem creating the media extractor.
   */
  public MediaExtractor createMediaExtractor() throws MediaProcessorException {
    return new MediaExtractorImpl(maxRedirectCount, thumbnailGenerateTimeout,
        audioVideoProbeTimeout, resourceConnectTimeout, resourceResponseTimeout,
        resourceDownloadTimeout);
  }

  /**
   * Create a link checker object that can be used to check links.
   *
   * @return A link checker.
   */
  public LinkChecker createLinkChecker() {
    return new LinkCheckerImpl(maxRedirectCount);
  }
}
