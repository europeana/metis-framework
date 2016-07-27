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

import eu.europeana.metis.dereference.OriginalEntity;
import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.service.dao.CacheDao;
import eu.europeana.metis.dereference.service.dao.EntityDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mongo implementation of the DereferencingManagementService
 * Created by ymamakis on 2/11/16.
 */
@Component
public class MongoDereferencingManagementService implements DereferencingManagementService {

    @Autowired
    private VocabularyDao vocabularyDao;
    @Autowired
    private CacheDao cacheDao;
    @Autowired
    private EntityDao entityDao;

    private Logger logger = LogManager.getLogger(MongoDereferencingManagementService.class);


    @Override
    public void saveVocabulary(Vocabulary vocabulary) {
        vocabularyDao.save(vocabulary);
        logger.info("Saved vocabulary with name: " +vocabulary.getName());
    }

    @Override
    public void updateVocabulary(Vocabulary vocabulary) {
        vocabularyDao.update(vocabulary);
        cacheDao.emptyCache();
        logger.info("Updated vocabulary with name: " +vocabulary.getName());
    }

    @Override
    public void deleteVocabulary(Vocabulary vocabulary) {
        vocabularyDao.delete(vocabulary.getURI());
        cacheDao.emptyCache();
        logger.info("Deleted vocabulary with name: " +vocabulary.getName());
    }

    @Override
    public List<Vocabulary> getAllVocabularies() {
        return vocabularyDao.getAll();
    }

    @Override
    public void removeEntity(String uri) {
        cacheDao.delete(uri);
        entityDao.delete(uri);
    }

    @Override
    public void updateEntity(String uri, String xml) {
        cacheDao.delete(uri);
        OriginalEntity entity = new OriginalEntity();
        entity.setURI(uri);
        entity.setXml(xml);
        entityDao.update(uri,entity);

    }

    @Override
    public Vocabulary findByName(String name) {
        return vocabularyDao.findByName(name);
    }

    @Override
    public void emptyCache() {
        cacheDao.emptyCache();
    }


}
