package eu.europeana.metis.dereference.service;

import eu.europeana.metis.dereference.Vocabulary;
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

  /**
   * Constructor.
   * 
   * @param vocabularyDao Access to the vocabularies
   */
  @Autowired
  public MongoDereferencingManagementService(VocabularyDao vocabularyDao) {
    this.vocabularyDao = vocabularyDao;
  }

  @Override
  public List<Vocabulary> getAllVocabularies() {
    return vocabularyDao.getAll();
  }

  @Override
  public void emptyCache() {
    // TODO JV Nothing to do.
    throw new IllegalStateException();
  }
}
