package eu.europeana.metis.dereference.service;

import eu.europeana.metis.dereference.OriginalEntity;
import eu.europeana.metis.dereference.ProcessedEntity;
import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.service.dao.EntityDao;
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
  private final EntityDao<OriginalEntity> originalEntityDao;
  private final EntityDao<ProcessedEntity> processedEntityDao;

  /**
   * Constructor.
   * 
   * @param vocabularyDao Access to the vocabularies
   */
  @Autowired
  public MongoDereferencingManagementService(VocabularyDao vocabularyDao,
          EntityDao<OriginalEntity> originalEntityDao,
          EntityDao<ProcessedEntity> processedEntityDao) {
    this.vocabularyDao = vocabularyDao;
    this.originalEntityDao = originalEntityDao;
    this.processedEntityDao = processedEntityDao;
  }

  @Override
  public List<Vocabulary> getAllVocabularies() {
    return vocabularyDao.getAll();
  }

  @Override
  public void emptyCache() {
    this.processedEntityDao.purgeAll();
    this.originalEntityDao.purgeAll();
  }
}
