package eu.europeana.normalization.language.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Normalize failed due to unexpected error")
public class NormalizationException extends RuntimeException {

    public NormalizationException(String message, String value) {
        super("Normalize value: " + value + " with root cause:\n" + message);
    }

}
