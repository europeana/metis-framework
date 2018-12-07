package eu.europeana.metis.mediaprocessing;

import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.temp.TemporaryMediaProcessor;

public class MediaProcessorFactory {

  private static final int DEFAULT_REDIRECT_COUNT = 3;
  private static final int DEFAULT_GENERAL_CONNECTION_LIMIT = 200;
  private static final int DEFAULT_CONNECTION_LIMIT_PER_SOURCE = 4;

  private int redirectCount = DEFAULT_REDIRECT_COUNT;
  private int generalConnectionLimit = DEFAULT_GENERAL_CONNECTION_LIMIT;
  private int connectionLimitPerSource = DEFAULT_CONNECTION_LIMIT_PER_SOURCE;

  public void setRedirectCount(int redirectCount) {
    this.redirectCount = redirectCount;
  }

  @Deprecated
  public void setConnectionLimitPerSource(int connectionLimitPerSource) {
    this.connectionLimitPerSource = connectionLimitPerSource;
  }

  @Deprecated
  public void setGeneralConnectionLimit(int generalConnectionLimit) {
    this.generalConnectionLimit = generalConnectionLimit;
  }

  public MediaExtractor createMediaExtractor() throws MediaProcessorException {
    return new TemporaryMediaProcessor(redirectCount, generalConnectionLimit,
        connectionLimitPerSource);
  }

  public LinkChecker createLinkChecker() throws MediaProcessorException {
    return new TemporaryMediaProcessor(redirectCount, generalConnectionLimit,
        connectionLimitPerSource);
  }
}
