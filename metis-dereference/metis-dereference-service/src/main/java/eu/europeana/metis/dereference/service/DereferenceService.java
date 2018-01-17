/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.metis.dereference.service;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import eu.europeana.metis.common.model.EnrichmentResultList;

import java.io.IOException;

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
