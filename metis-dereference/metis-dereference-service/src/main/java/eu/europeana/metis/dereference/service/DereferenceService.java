package eu.europeana.metis.dereference.service;

import java.net.URISyntaxException;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;

/**
 * Dereferencing service Created by ymamakis on 2/11/16.
 */
public interface DereferenceService {

  /**
   * Dereference a URI
   * 
   * @param resourceId The resource ID (URI) to dereference
   * @return The dereferenced entity
   * @throws TransformerException In case the data does not satisfy the expected format.
   * @throws JAXBException In case the data does not specify the expected format.
   * @throws URISyntaxException In case the resource ID could not be read as URI.
   */
  EnrichmentResultList dereference(String resourceId)
      throws TransformerException, JAXBException, URISyntaxException;
}
