package eu.europeana.metis.mediaprocessing.exception;

// TODO Should also allow multiple MediaExceptions to be wrapped in one MediaProcessorException?
// MediaException pertains to one media. This pertains to a run of the media processor.
public class MediaProcessorException extends Exception {

  /** This class implements {@link java.io.Serializable}. **/
  private static final long serialVersionUID = 8090383001647258984L;

  public MediaProcessorException(String message) {
    super(message);
  }

  public MediaProcessorException(String message, Exception cause) {
    super(message, cause);
  }

  public MediaProcessorException(Exception cause) {
    super(cause);
  }
}
