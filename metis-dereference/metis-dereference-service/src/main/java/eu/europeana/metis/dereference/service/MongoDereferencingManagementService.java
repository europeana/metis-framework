package eu.europeana.metis.dereference.service;

import eu.europeana.metis.dereference.OriginalEntity;
import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.service.dao.CacheDao;
import eu.europeana.metis.dereference.service.dao.EntityDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Mongo implementation of the DereferencingManagementService Created by ymamakis on 2/11/16.
 */
@Component
public class MongoDereferencingManagementService implements DereferencingManagementService {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MongoDereferencingManagementService.class);
  private final VocabularyDao vocabularyDao;
  private final CacheDao cacheDao;
  private final EntityDao entityDao;

  /**
   * Constructor.
   * 
   * @param vocabularyDao Access to the vocabularies
   * @param cacheDao Access to the processed entity cache
   * @param entityDao Access to the original entity cache
   */
  @Autowired
  public MongoDereferencingManagementService(VocabularyDao vocabularyDao, CacheDao cacheDao,
      EntityDao entityDao) {
    this.vocabularyDao = vocabularyDao;
    this.cacheDao = cacheDao;
    this.entityDao = entityDao;
  }


  @Override
  public void saveVocabulary(Vocabulary vocabulary) {
    vocabularyDao.save(vocabulary);
    LOGGER.info("Saved vocabulary with name: {}", vocabulary.getName());
  }

  @Override
  public void updateVocabulary(Vocabulary vocabulary) {
    vocabularyDao.update(vocabulary);
    cacheDao.emptyCache();
    LOGGER.info("Updated vocabulary with name: {}", vocabulary.getName());
  }

  @Override
  public void deleteVocabulary(String name) {
    vocabularyDao.delete(name);
    cacheDao.emptyCache();
    LOGGER.info("Deleted vocabulary with name: {}", name);
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
    entityDao.update(uri, entity);
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
