package eu.europeana.metis.dereference.service;

import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.service.dao.CacheDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Mongo implementation of the DereferencingManagementService Created by ymamakis on 2/11/16.
 */
@Component
public class MongoDereferencingManagementService implements DereferencingManagementService {

  private final VocabularyDao vocabularyDao;
  private final CacheDao cacheDao;

  /**
   * Constructor.
   * 
   * @param vocabularyDao Access to the vocabularies
   * @param cacheDao Access to the processed entity cache
   */
  @Autowired
  public MongoDereferencingManagementService(VocabularyDao vocabularyDao, CacheDao cacheDao) {
    this.vocabularyDao = vocabularyDao;
    this.cacheDao = cacheDao;
  }

  @Override
  public List<Vocabulary> getAllVocabularies() {
    return vocabularyDao.getAll();
  }

  @Override
  public void emptyCache() {
    cacheDao.emptyCache();
  }
}
