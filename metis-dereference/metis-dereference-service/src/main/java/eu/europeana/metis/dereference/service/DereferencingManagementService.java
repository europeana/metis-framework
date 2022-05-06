package eu.europeana.metis.dereference.service;

import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.vocimport.exception.VocabularyImportException;
import java.net.URL;
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

  /**
   * Empty the cache by resource ID(URI)
   * @param resourceId
   */
  void purgeByResourceId(String resourceId);
  /**
   * Empty the cache by vocabulary ID
   * @param vocabularyId
   */
  void purgeByVocabularyId(String vocabularyId);

  /**
   * Load the vocabularies from an online source. This does NOT purge the cache.
   *
   * @param directoryUrl The online location of the vocabulary directory.
   * @throws VocabularyImportException In case some issue occurred while importing the vocabularies.
   */
  void loadVocabularies(URL directoryUrl) throws VocabularyImportException;
}
