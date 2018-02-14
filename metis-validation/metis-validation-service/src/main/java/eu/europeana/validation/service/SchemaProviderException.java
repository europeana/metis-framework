package eu.europeana.validation.service;

/**
 * Created by pwozniak on 12/22/17
 */
public class SchemaProviderException extends Exception {

    private static final long serialVersionUID = -6861990081785732650L;

    /**
     * Cretes {@link SchemaProviderException} from provided {@link Throwable}
     *
     * @param t
     */
    public SchemaProviderException(Throwable t) {
        super(t);
    }

    /**
     * Cretes {@link SchemaProviderException} from provided message.
     *
     * @param message
     */
    public SchemaProviderException(String message) {
        super(message);
    }

    /**
     * Cretes {@link SchemaProviderException} from provided message and {@link Throwable} object.
     *
     * @param message
     * @param t
     */
    public SchemaProviderException(String message, Throwable t) {
        super(message, t);
    }
}
