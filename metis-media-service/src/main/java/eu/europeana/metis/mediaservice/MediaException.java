package eu.europeana.metis.mediaservice;

/** General class for exceptions generated in media topology. */
public class MediaException extends Exception {
	
	public final String reportError;
	public final boolean retry;
	
	public MediaException(String message, String reportError, Throwable cause, boolean retry) {
		super(message, cause);
		this.reportError = reportError;
		this.retry = retry;
	}
	
	public MediaException(String message, String reportError, Throwable cause) {
		this(message, reportError, cause, false);
	}
	
	public MediaException(String message, String reportError) {
		this(message, reportError, null, false);
	}
	
	public MediaException(String message, Throwable cause) {
		this(message, null, cause, false);
	}
	
	public MediaException(String message) {
		this(message, null, null, false);
	}
}
