package eu.europeana.metis.dereference.vocimport;

import eu.europeana.metis.dereference.vocimport.exception.VocabularyImportException;
import eu.europeana.metis.dereference.vocimport.model.VocabularyLoader;

/**
 * Objects that implement this interface provide functionality that can perform the importing of
 * vocabularies.
 *
 * @param <L> The type of the resource identifier (location) that is used.
 */
public interface VocabularyImporter<L> {

  /**
   * Import all vocabularies.
   *
   * @param directoryLocation The location of the directory file.
   * @return The vocabulary loaders (giving access to the vocabularies in a lazy manner).
   * @throws VocabularyImportException In case there was a problem loading the vocabularies.
   */
  Iterable<VocabularyLoader> importVocabularies(String directoryLocation)
          throws VocabularyImportException;

  /**
   * Import all vocabularies.
   *
   * @param directoryLocation The location of the directory file.
   * @return The vocabulary loaders (giving access to the vocabularies in a lazy manner).
   * @throws VocabularyImportException In case there was a problem loading the vocabularies.
   */
  Iterable<VocabularyLoader> importVocabularies(L directoryLocation)
          throws VocabularyImportException;

}
