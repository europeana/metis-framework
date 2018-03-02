package eu.europeana.validation.rest.exceptions;

/**
 * Created by ymamakis on 2/24/16.
 */
public class ValidationException extends Exception {

    private final String id;

    /**
     * Creates class instance
     */
    public ValidationException() {
        super();
        id = null;
    }

    /**
     * Creates exception instance based on provided parameters
     * @param id record id that causes the problem
     * @param message message provided by validation engine
     */
    public ValidationException(String id, String message) {
        super(message);
        this.id = id;
    }

    public String getId() {
        return this.id;
    }
}
