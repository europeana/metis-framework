package eu.europeana.metis.dereference.vocimport;

import eu.europeana.metis.dereference.vocimport.exception.VocabularyImportException;
import eu.europeana.metis.dereference.vocimport.model.VocabularyLoader;

/**
 * Objects that implement this interface provide functionality that can perform the importing of
 * vocabularies.
 */
public interface VocabularyCollectionImporter {

  /**
   * Import all vocabularies.
   *
   * @return The vocabulary loaders (giving access to the vocabularies in a lazy manner).
   * @throws VocabularyImportException In case there was a problem loading the vocabularies.
   */
  Iterable<VocabularyLoader> importVocabularies() throws VocabularyImportException;

}
