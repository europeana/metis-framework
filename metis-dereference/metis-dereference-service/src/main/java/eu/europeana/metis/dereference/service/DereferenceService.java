package eu.europeana.metis.dereference.service;

import eu.europeana.enrichment.api.external.DereferenceResultStatus;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import java.net.URISyntaxException;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Dereferencing service Created by ymamakis on 2/11/16.
 */
public interface DereferenceService {

  /**
   * Dereference a URI
   *
   * @param resourceId The resource ID (URI) to dereference
   * @return The dereferenced entity (or multiple in case of parent entities). List is not null, but
   * could be empty.
   * @throws TransformerException In case the data does not satisfy the expected format.
   * @throws JAXBException In case the data does not specify the expected format.
   * @throws URISyntaxException In case the resource ID could not be read as URI.
   */
  Pair<List<EnrichmentBase>, DereferenceResultStatus> dereference(String resourceId)
      throws TransformerException, JAXBException, URISyntaxException;
}
