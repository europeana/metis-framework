package eu.europeana.metis.dereference.service;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;

/**
 * Dereferencing service
 * Created by ymamakis on 2/11/16.
 */
public interface DereferenceService {

    /**
     * Dereference a URI
     * @param resourceId The resource ID (URI) to dereference
     * @return The dereferenced entity
     */
    EnrichmentResultList dereference(String resourceId) throws TransformerException, JAXBException;
}
