package eu.europeana.metis.dereference.vocimport;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This class is the factory for instances of {@link VocabularyCollectionImporter}.
 */
public class VocabularyCollectionImporterFactory {

  /**
   * Create a vocabulary importer for remote web addresses, indicated by instances of {@link URI}.
   * Note that this method can only be used for locations that are also a valid {@link
   * java.net.URL}.
   *
   * @param directoryLocation The location of the directory to import.
   * @return A vocabulary importer.
   */
  public VocabularyCollectionImporter createImporter(URI directoryLocation) {
    return new VocabularyCollectionImporterImpl<>(uri -> uri.toURL().openStream(),
            (location, relativeLocation) -> location.resolve(relativeLocation), directoryLocation);
  }

  /**
   * Create a vocabulary importer for local files, indicated by instances of {@link Path}.
   *
   * @return A vocabulary importer.
   */
  public VocabularyCollectionImporter createImporter(Path directoryLocation) {
    return new VocabularyCollectionImporterImpl<>(Files::newInputStream,
            (location, relativeLocation) -> location.getParent().resolve(relativeLocation),
            directoryLocation);
  }
}
