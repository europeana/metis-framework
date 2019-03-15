package eu.europeana.metis.mediaprocessing;

import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.extraction.MediaExtractorImpl;
import eu.europeana.metis.mediaprocessing.linkchecking.LinkCheckerImpl;

/**
 * This factory creates objects for media extraction and link checking.
 * <p>Used by external like scripts or ECloud.</p>
 */
public class MediaProcessorFactory {

  /**
   * The default value of the maximum number of times we will follow a redirect. It's currently set
   * to {@value MediaProcessorFactory#DEFAULT_MAX_REDIRECT_COUNT}.
   **/
  public static final int DEFAULT_MAX_REDIRECT_COUNT = 3;

  /**
   * The default value of the maximum number of processes that can do command-line IO at any given
   * time. This maximum will hold for individual processors created using this class, not in total.
   * It's currently set to {@value MediaProcessorFactory#DEFAULT_COMMAND_THREAD_POOL_SIZE}.
   */
  public static final int DEFAULT_COMMAND_THREAD_POOL_SIZE = 2;

  private int maxRedirectCount = DEFAULT_MAX_REDIRECT_COUNT;

  private int commandThreadPoolSize = DEFAULT_COMMAND_THREAD_POOL_SIZE;

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
   * Set the maximum number of processes that can do command-line IO at any given time (per
   * processor created using this factory). The default (when not calling this method or calling it
   * with a negative number) is {@link MediaProcessorFactory#DEFAULT_COMMAND_THREAD_POOL_SIZE}.
   *
   * @param commandThreadPoolSize The maximum number of processes that can do command-line IO.
   */
  public void setCommandIOThreadPoolSize(int commandThreadPoolSize) {
    this.commandThreadPoolSize =
        commandThreadPoolSize < 1 ? DEFAULT_COMMAND_THREAD_POOL_SIZE : commandThreadPoolSize;
  }

  /**
   * Create a media extractor object that can be used to extract media metadata and thumbnails.
   *
   * @return A media extractor.
   * @throws MediaProcessorException In case there was a problem creating the media extractor.
   */
  public MediaExtractor createMediaExtractor() throws MediaProcessorException {
    return MediaExtractorImpl.newInstance(maxRedirectCount, commandThreadPoolSize);
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
