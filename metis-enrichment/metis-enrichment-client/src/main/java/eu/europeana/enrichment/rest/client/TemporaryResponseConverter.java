package eu.europeana.enrichment.rest.client;

import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import java.io.ByteArrayInputStream;
import java.util.function.Supplier;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.springframework.http.ResponseEntity;

/**
 * This utilities class should be used for parsing the result of dereference and enrichment calls.
 *
 * TODO JV this is a temporary fix to solve a suspected dependency issue with deserialization inside
 * Spring. Originally we passed EnrichmentResultList.class instead of byte[].class to the various
 * {@link org.springframework.web.client.RestTemplate} request execution methods and return the
 * result directly.
 */
public final class TemporaryResponseConverter {

  private TemporaryResponseConverter() {
  }

  /**
   * Convert a byte array to an instance of {@link EnrichmentResultList}.
   *
   * @param requestResult The request result.
   * @return The converted object.
   * @throws JAXBException In case there was a conversion issue.
   */
  static EnrichmentResultList convert(ResponseEntity<byte[]> requestResult) throws JAXBException {
    return convert(requestResult, EnrichmentResultList.class, EnrichmentResultList::new);
  }

  /**
   * Convert a byte array to the expected class.
   *
   * @param requestResult The request result.
   * @param type The class instance of the type of the converted object.
   * @param defaultResult The default value of the converted object, in case the request has no
   * body.
   * @param <T> The type of the converted object.
   * @return The converted object.
   * @throws JAXBException In case there was a conversion issue.
   */
  static <T> T convert(ResponseEntity<byte[]> requestResult, Class<T> type,
          Supplier<T> defaultResult) throws JAXBException {
    if (requestResult.getBody() == null) {
      return defaultResult.get();
    }
    final JAXBContext jaxbContext = JAXBContext.newInstance(type);
    final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    return (T) jaxbUnmarshaller.unmarshal(new ByteArrayInputStream(requestResult.getBody()));
  }
}
