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

import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.rest.client.EnrichmentClient;
import eu.europeana.metis.dereference.OriginalEntity;
import eu.europeana.metis.dereference.ProcessedEntity;
import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.service.dao.CacheDao;
import eu.europeana.metis.dereference.service.dao.EntityDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
import eu.europeana.metis.dereference.service.utils.RdfRetriever;
import eu.europeana.metis.dereference.service.xslt.XsltTransformer;
import java.io.StringReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.List;

/**
 * Mongo implementation of the dereference service
 * Created by ymamakis on 2/11/16.
 */
public class MongoDereferenceService implements DereferenceService {

    Logger logger = LogManager.getLogger(MongoDereferenceService.class);
    @Autowired
    private RdfRetriever retriever;
    @Autowired
    private CacheDao cacheDao;
    @Autowired
    private EntityDao entityDao;
    @Autowired
    private VocabularyDao vocabularyDao;
    @Autowired
    private EnrichmentClient enrichmentClient;

    public MongoDereferenceService(){


    }
    @Override
    public EnrichmentResultList dereference(String uri)
        throws TransformerException, ParserConfigurationException, IOException, JAXBException {
        EnrichmentResultList toReturn= new EnrichmentResultList();
        EnrichmentBase fromEntity = checkInEntityCollection(uri);
        if (fromEntity!=null){
            toReturn.getResult().add(fromEntity);
        }
        String[] splitName = uri.split("/");
        if(splitName.length>3) {
            String vocabularyUri = splitName[0] + "/" + splitName[1] + "/"
                    + splitName[2] + "/";
            List<Vocabulary> vocs = vocabularyDao.getByUri(vocabularyUri);

            if (vocs != null && vocs.size()>0) {

                ProcessedEntity cached = cacheDao.getByUri(uri);
                if (cached != null) {
                    toReturn.getResult().add(deserialize(cached.getXml()));
                    return toReturn;
                }
                Vocabulary vocabulary;
                OriginalEntity originalEntity = entityDao.getByUri(uri);
                if (originalEntity == null) {
                    originalEntity = new OriginalEntity();
                    originalEntity.setURI(uri);
                    String originalXml = retriever.retrieve(uri);
                    originalEntity.setXml(originalXml.contains("<html>") ? null : originalXml);
                    entityDao.save(originalEntity);
                }
                if (originalEntity.getXml() != null) {
                    vocabulary = vocabularyDao.findByEntity(vocs, originalEntity.getXml(),uri);

                    String transformed = null;
                    try {
                        transformed = new XsltTransformer().transform(originalEntity.getXml(), vocabulary.getXslt());
                    } catch (ParserConfigurationException |TransformerException e) {
                        logger.error("Error transforming entity: "+uri +" with message :" +e.getMessage());
                        throw e;
                    }


                    ProcessedEntity entity = new ProcessedEntity();
                    entity.setXml(transformed);
                    entity.setURI(uri);
                    cacheDao.save(entity);

                    toReturn.getResult().add(deserialize(transformed));
                    return toReturn;
                }
            }
        }
        return toReturn;
    }

    private EnrichmentBase checkInEntityCollection(String uri) throws IOException {
        return enrichmentClient.getByUri(uri);
    }

    private EnrichmentBase deserialize(String enrichment) throws JAXBException {
        JAXBContext contextA = JAXBContext.newInstance(Place.class);

        StringReader reader = new StringReader(enrichment);
        Unmarshaller unmarshaller = contextA.createUnmarshaller();
        return (EnrichmentBase) unmarshaller.unmarshal(reader);
    }

}
