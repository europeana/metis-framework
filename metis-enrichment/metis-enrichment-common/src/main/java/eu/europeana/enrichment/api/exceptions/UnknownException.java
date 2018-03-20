package eu.europeana.enrichment.api.exceptions;
/**
 * Generic Exception not covered by the standard Jackson Exceptions
 * @author Yorgos.Mamakis@ europeana.eu
 *
 */
public class UnknownException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7418853794840766813L;

	/**
	 * A generic non-standard Exception
	 * @param The message caused by the original exception
	 */
	public UnknownException(String message){
		super(message);
	}
}
