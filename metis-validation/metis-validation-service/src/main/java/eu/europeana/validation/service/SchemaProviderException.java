package eu.europeana.validation.service;

/**
 * Created by pwozniak on 12/22/17
 */
public class SchemaProviderException extends Exception {


    public SchemaProviderException(Throwable t) {
        super(t);
    }

    public SchemaProviderException(String message){
        super(message);
    }

    public SchemaProviderException(String message, Throwable t) {
        super(message, t);
    }
}
