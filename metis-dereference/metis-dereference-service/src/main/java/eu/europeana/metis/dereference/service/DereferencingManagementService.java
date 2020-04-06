package eu.europeana.metis.dereference.service;

import eu.europeana.metis.dereference.Vocabulary;
import java.util.List;

/**
 * Interface for managing vocabularies Created by ymamakis on 2/11/16.
 */
public interface DereferencingManagementService {

  /**
   * List all the vocabularies
   *
   * @return The mapped vocabularies
   */
  List<Vocabulary> getAllVocabularies();

  /**
   * Empty the cache
   */
  void emptyCache();
}
