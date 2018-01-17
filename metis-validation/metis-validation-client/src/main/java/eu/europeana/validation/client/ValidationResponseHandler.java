package eu.europeana.validation.client;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

/**
 * Created by ymamakis on 9/30/16.
 */
public class ValidationResponseHandler implements ResponseErrorHandler {
    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        HttpStatus.Series series = response.getStatusCode().series();
        return HttpStatus.Series.CLIENT_ERROR == series || HttpStatus.Series.SERVER_ERROR == series;
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        //DO NOTHING. That way we can handle the response on the client side as a string
    }
}
