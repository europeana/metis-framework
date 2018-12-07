package eu.europeana.metis.mediaprocessing;

import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.temp.TemporaryMediaProcessor;

/**
 * This factory creates objects for media extraction and link checking.
 */
public class MediaProcessorFactory {

  /**
   * The default value of the maximum number of times we will follow a redirect. It's currently set
   * to {@value MediaProcessorFactory#DEFAULT_MAX_REDIRECT_COUNT};
   **/
  public static final int DEFAULT_MAX_REDIRECT_COUNT = 3;

  private int maxRedirectCount = DEFAULT_MAX_REDIRECT_COUNT;

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
   * Create a media extractor object that can be used to extract media metadata and thumbnails.
   *
   * @return A media extractor.
   * @throws MediaProcessorException In case there was a problem creating the media extractor.
   */
  public MediaExtractor createMediaExtractor() throws MediaProcessorException {
    return new TemporaryMediaProcessor(maxRedirectCount);
  }

  /**
   * Create a link checker object that can be used to check links.
   *
   * @return A link checker.
   * @throws MediaProcessorException In case there was a problem creating the link checker.
   */
  public LinkChecker createLinkChecker() throws MediaProcessorException {
    return new TemporaryMediaProcessor(maxRedirectCount);
  }
}
