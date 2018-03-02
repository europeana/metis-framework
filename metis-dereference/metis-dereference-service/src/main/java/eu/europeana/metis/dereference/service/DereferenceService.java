package eu.europeana.metis.dereference.service;

import java.io.IOException;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;

/**
 * Dereferencing service
 * Created by ymamakis on 2/11/16.
 */
public interface DereferenceService {

    /**
     * Dereference a URI
     * @param uri The uri to dereference
     * @return The dereferenced entity
     */
    <T>EnrichmentResultList dereference(String uri)
        throws TransformerException, ParserConfigurationException, IOException, JAXBException;
}
