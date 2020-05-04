package eu.europeana.metis.dereference.vocimport;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class is the factory for instances of {@link VocabularyImporter}.
 */
public class VocabularyImporterFactory {

  /**
   * Create a vocabulary importer for remote web addresses, indicated by instances of {@link URI}.
   * Note that this method can only be used for locations that are also a valid {@link
   * java.net.URL}.
   *
   * @return A vocabulary importer.
   */
  public VocabularyImporter<URI> createImporterForUrls() {
    return new VocabularyImporterImpl<>(URI::create, uri -> uri.toURL().openStream(),
            (location, relativeLocation) -> location.resolve(relativeLocation));
  }

  /**
   * Create a vocabulary importer for local files, indicated by instances of {@link Path}.
   *
   * @return A vocabulary importer.
   */
  public VocabularyImporter<Path> createImporterForFiles() {
    return new VocabularyImporterImpl<>(Paths::get, Files::newInputStream,
            (location, relativeLocation) -> location.getParent().resolve(relativeLocation));
  }
}
