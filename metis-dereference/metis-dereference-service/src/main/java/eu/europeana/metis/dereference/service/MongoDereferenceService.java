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
import eu.europeana.enrichment.rest.client.EnrichmentDriver;
import eu.europeana.metis.dereference.OriginalEntity;
import eu.europeana.metis.dereference.ProcessedEntity;
import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.service.dao.CacheDao;
import eu.europeana.metis.dereference.service.dao.EntityDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
import eu.europeana.metis.dereference.service.utils.RdfRetriever;
import eu.europeana.metis.dereference.service.xslt.XsltTransformer;
import org.apache.commons.lang3.StringUtils;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDereferenceService.class);

    private final RdfRetriever rdfRetriever;
    private final CacheDao cacheDao;
    private final EntityDao entityDao;
    private final VocabularyDao vocabularyDao;
    private final EnrichmentDriver enrichmentDriver;

    @Autowired
    public MongoDereferenceService(RdfRetriever rdfRetriever,
        CacheDao cacheDao, EntityDao entityDao,
        VocabularyDao vocabularyDao, EnrichmentDriver enrichmentDriver) {
        this.rdfRetriever = rdfRetriever;
        this.cacheDao = cacheDao;
        this.entityDao = entityDao;
        this.vocabularyDao = vocabularyDao;
        this.enrichmentDriver = enrichmentDriver;
    }

    @Override
    public List<String> dereference(String uri) throws TransformerException, ParserConfigurationException, IOException{
        List<String> toReturn = new ArrayList<>();

        String fromEntity = checkInEntityCollection(uri);
        if (fromEntity!=null){
            toReturn.add(fromEntity);
        }

        String[] splitName = uri.split("/");
        if (splitName.length <= 3) {
            LOGGER.debug("Invalid uri: {}. Returning. ", uri);
            return toReturn;
        }

        String vocabularyUri = splitName[0] + "/" + splitName[1] + "/"
                + splitName[2] + "/";
        List<Vocabulary> vocs = vocabularyDao.getByUri(vocabularyUri);

        if (vocs == null || vocs.size() == 0) {
            LOGGER.debug("No vocabulary found for {}. Returning.", vocabularyUri);
            return toReturn;
        }

        ProcessedEntity cached = readCachedProcessedEntity(uri);
        if (cached != null) {
            LOGGER.debug("Returning cached cached processedEntity for {}. ", uri);
            toReturn.add(cached.getXml());
            return toReturn;
        }

        OriginalEntity originalEntity = ensureOriginalEntity(uri);
        if (originalEntity.getXml() != null) {
            Vocabulary vocabulary = vocabularyDao.findByEntity(vocs, originalEntity.getXml(), uri);

            String transformed;
            try {
                transformed = new XsltTransformer()
                    .transform(originalEntity.getXml(), vocabulary.getXslt());
                toReturn.add(transformed);
            } catch (ParserConfigurationException | TransformerException e) {
                LOGGER.error(
                    "Error transforming entity: " + uri + " with message :" + e.getMessage());
                throw e;
            }
            writeCachedProcessedEntity(uri, transformed);
        }
        return toReturn;
    }

    private void writeCachedProcessedEntity(String uri, String transformed) {
        ProcessedEntity entity = new ProcessedEntity();
        entity.setXml(transformed);
        entity.setURI(uri);
        LOGGER.debug("Creating cached ProcessedEntity for {}. ", uri);
        cacheDao.save(entity);
    }

    private ProcessedEntity readCachedProcessedEntity(String uri) {
        return cacheDao.getByUri(uri);
    }

    private OriginalEntity ensureOriginalEntity(String uri) {
        OriginalEntity originalEntity = entityDao.getByUri(uri);
        if (originalEntity == null) {
            LOGGER.debug("No OriginalEntity found for uri {}. Creating new. ", uri);

            originalEntity = new OriginalEntity();
            originalEntity.setURI(uri);
            String originalXml = rdfRetriever.retrieve(uri);
            originalEntity.setXml(originalXml.contains("<html>") ? null : originalXml);
            entityDao.save(originalEntity);
        }
        return originalEntity;
    }

    private String checkInEntityCollection(String uri) throws IOException{
       String enriched = enrichmentDriver.getByUri(uri,true);
        if (StringUtils.isEmpty(enriched)) {
            return null;
        }
        EntityWrapper wrapper = new ObjectMapper().readValue(enrichmentDriver.getByUri(uri, false), EntityWrapper.class);
        return wrapper == null ? null : wrapper.getContextualEntity();
    }
}
