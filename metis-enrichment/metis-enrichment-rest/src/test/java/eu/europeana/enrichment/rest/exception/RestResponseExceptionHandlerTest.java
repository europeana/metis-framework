package eu.europeana.enrichment.rest.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import eu.europeana.metis.exception.StructuredExceptionWrapper;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;

class RestResponseExceptionHandlerTest {

  private static final RestResponseExceptionHandler REST_RESPONSE_EXCEPTION_HANDLER = new RestResponseExceptionHandler();
  private static final String ERROR_MESSAGE = "error message";
  ArgumentCaptor<Integer> valueCaptor = ArgumentCaptor.forClass(Integer.class);

  @Test
  void testHandleResponse() {
    HttpServletResponse response = mock(HttpServletResponse.class);
    Exception exception = new Exception(ERROR_MESSAGE);

    StructuredExceptionWrapper result =
        REST_RESPONSE_EXCEPTION_HANDLER.handleResponse(response, exception);

    verify(response, times(1)).setStatus(valueCaptor.capture());

    int httpStatusValue = valueCaptor.getValue();
    assertEquals(HttpStatus.BAD_REQUEST.value(), httpStatusValue);
    assertEquals(ERROR_MESSAGE, result.getErrorMessage());
  }

}
