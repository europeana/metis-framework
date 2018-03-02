package eu.europeana.validation.rest.exceptions;

/**
 * Created by ymamakis on 2/24/16.
 */
public class ServerException extends Exception {


    /**
     * Creates exception instance based on provided {@link Throwable}
     *
     * @param e
     */
    public ServerException(Throwable e) {
        super(e);
    }
}
