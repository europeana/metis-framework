package eu.europeana.metis.dereference.service;


import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.List;

/**
 * Created by ymamakis on 2/11/16.
 */
public interface DereferenceService {

    /**
     * Dereference a URI
     * @param uri The uri to dereference
     * @return The dereferenced entity
     */
    <T>List<T> dereference(String uri) throws TransformerException, ParserConfigurationException, IOException;
}
