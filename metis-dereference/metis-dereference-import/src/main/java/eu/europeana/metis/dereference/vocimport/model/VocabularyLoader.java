package eu.europeana.metis.dereference.vocimport.model;

import eu.europeana.metis.dereference.vocimport.exception.VocabularyImportException;

/**
 * This class represents a vocabulary loader. It is used to achieve lazy loading (only when the load
 * method is called will the vocabulary be loaded).
 */
public interface VocabularyLoader {

  /**
   * Trigger a loading of the vocabulary. Blocks until the vocabulary is loaded.
   *
   * @return The loaded vocabulary.
   * @throws VocabularyImportException In case there was a problem loading the vocabulary.
   */
  Vocabulary load() throws VocabularyImportException;

}
