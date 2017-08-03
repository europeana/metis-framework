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

import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.rest.client.EnrichmentClient;
import eu.europeana.metis.dereference.OriginalEntity;
import eu.europeana.metis.dereference.ProcessedEntity;
import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.service.dao.CacheDao;
import eu.europeana.metis.dereference.service.dao.EntityDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
import eu.europeana.metis.dereference.service.utils.RdfRetriever;
import eu.europeana.metis.dereference.service.xslt.XsltTransformer;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.ArrayList;
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
    public List<String> dereference(String uri) throws TransformerException, ParserConfigurationException, IOException{
        List<String> toReturn= null;
        String fromEntity = checkInEntityCollection(uri);
        if (fromEntity!=null){
            toReturn = new ArrayList<>();
            toReturn.add(fromEntity);
        }
        String[] splitName = uri.split("/");
        if(splitName.length>3) {
            String vocabularyUri = splitName[0] + "/" + splitName[1] + "/"
                    + splitName[2] + "/";
            List<Vocabulary> vocs = vocabularyDao.getByUri(vocabularyUri);

            if (vocs != null && vocs.size()>0) {

                ProcessedEntity cached = cacheDao.getByUri(uri);
                if (cached != null) {
                    if(toReturn==null){
                        toReturn = new ArrayList<>();

                    }
                    toReturn.add(cached.getXml());
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
                    if(toReturn==null){
                        toReturn=new ArrayList<>();
                    }
                    toReturn.add(transformed);
                    return toReturn;
                }
            }
        }
        return toReturn;
    }

    private String checkInEntityCollection(String uri) throws IOException{
       String enriched = enrichmentClient.getByUri(uri,true);
        if(StringUtils.isNotEmpty(enriched)) {
            EntityWrapper wrapper = new ObjectMapper().readValue(
                enrichmentClient.getByUri(uri, false), EntityWrapper.class);

            return wrapper != null ? wrapper.getContextualEntity() : null;
        }
        return null;
    }

}
