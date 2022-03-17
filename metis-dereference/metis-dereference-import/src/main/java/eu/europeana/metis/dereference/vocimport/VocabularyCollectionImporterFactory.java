package eu.europeana.metis.dereference.vocimport;

import eu.europeana.metis.dereference.vocimport.model.Location;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This class is the factory for instances of {@link VocabularyCollectionImporter}.
 */
public class VocabularyCollectionImporterFactory {

  /**
   * Create a vocabulary importer for remote web addresses, indicated by instances of {@link URI}. Note that this method can only
   * be used for locations that are also a valid {@link java.net.URL}.
   *
   * @param directoryLocation The location of the directory to import.
   * @return A vocabulary importer.
   */
  public VocabularyCollectionImporter createImporter(URI directoryLocation) {
    return new VocabularyCollectionImporterImpl(new UriLocation(directoryLocation));
  }

  /**
   * Create a vocabulary importer for local files, indicated by instances of {@link Path}.
   *
   * @param directoryLocation The location of the directory file to import.
   * @return A vocabulary importer.
   */
  public VocabularyCollectionImporter createImporter(Path directoryLocation) {
    return createImporter(null, directoryLocation);
  }

  /**
   * Create a vocabulary importer for local files, indicated by instances of {@link Path}. This
   * method provides a way to set a base directory that will be assumed known (so that output and
   * logs will only include the relative location).
   *
   * @param baseDirectory The base directory of the project or collection. Can be null.
   * @param directoryLocation The full location of the directory file to import.
   * @return A vocabulary importer.
   */
  public VocabularyCollectionImporter createImporter(Path baseDirectory, Path directoryLocation) {
    return new VocabularyCollectionImporterImpl(new PathLocation(baseDirectory, directoryLocation));
  }

  private static final class UriLocation implements Location {

    private final URI uri;

    UriLocation(URI uri) {
      this.uri = uri;
    }

    @Override
    public InputStream read() throws IOException {
      return uri.toURL().openStream();
    }

    @Override
    public Location resolve(String relativeLocation) {
      return new UriLocation(uri.resolve(relativeLocation));
    }

    @Override
    public String toString() {
      return uri.toString();
    }
  }

  private static final class PathLocation implements Location {

    private final Path baseDirectory;
    private final Path fullPath;

    PathLocation(Path baseDirectory, Path fullPath) {
      this.baseDirectory = baseDirectory;
      this.fullPath = fullPath;
    }

    @Override
    public InputStream read() throws IOException {
      return Files.newInputStream(fullPath);
    }

    @Override
    public Location resolve(String relativeLocation) {
      return new PathLocation(baseDirectory, fullPath.getParent().resolve(relativeLocation));
    }

    @Override
    public String toString() {
      return (baseDirectory == null ? fullPath : baseDirectory.relativize(fullPath)).toString();
    }
  }
}
