package eu.europeana.metis.dereference.vocimport;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import eu.europeana.metis.dereference.vocimport.exception.VocabularyImportException;
import eu.europeana.metis.dereference.vocimport.model.Vocabulary;
import eu.europeana.metis.dereference.vocimport.model.VocabularyDirectoryEntry;
import eu.europeana.metis.dereference.vocimport.model.VocabularyLoader;
import eu.europeana.metis.dereference.vocimport.model.VocabularyMetadata;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.io.IOUtils;

/**
 * This is the default implementation of the vocabulary importing functionality.
 *
 * @param <L> The type of the resource identifier (location) that is used.
 */
final class VocabularyImporterImpl<L> implements VocabularyImporter<L> {

  private final Converter<L> converter;
  private final SourceReader<L> sourceReader;
  private final LocationResolver<L> resolver;

  VocabularyImporterImpl(Converter<L> converter, SourceReader<L> sourceReader,
          LocationResolver<L> resolver) {
    this.converter = converter;
    this.sourceReader = sourceReader;
    this.resolver = resolver;
  }

  @Override
  public Iterable<VocabularyLoader> importVocabularies(String directoryLocation)
          throws VocabularyImportException {
    try {
      return importVocabularies(converter.convert(directoryLocation));
    } catch (IOException e) {
      throw new VocabularyImportException("Could not convert location '" + directoryLocation + "'.",
              e);
    }
  }

  @Override
  public Iterable<VocabularyLoader> importVocabularies(L directoryLocation)
          throws VocabularyImportException {

    // Obtain the directory entries.
    final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    final VocabularyDirectoryEntry[] directory;
    try (final InputStream input = sourceReader.read(directoryLocation)) {
      directory = mapper.readValue(input, VocabularyDirectoryEntry[].class);
    } catch (IOException e) {
      throw new VocabularyImportException(
              "Could not read vocabulary directory at '" + directoryLocation + "'.", e);
    }

    // Compile the vocabulary loaders
    final List<VocabularyLoader> result = new ArrayList<>();
    for (VocabularyDirectoryEntry entry : directory) {
      final L metadataLocation = resolver.resolve(directoryLocation, entry.getMetadata());
      final L mappingLocation = resolver.resolve(directoryLocation, entry.getMapping());
      result.add(() -> compileVocabularyLoader(metadataLocation, mappingLocation, mapper));
    }

    // Done
    return result;
  }

  private Vocabulary compileVocabularyLoader(L metadataLocation, L mappingLocation,
          ObjectMapper mapper) throws VocabularyImportException {

    // Read the metadata file.
    final VocabularyMetadata metadata;
    try (final InputStream input = sourceReader.read(metadataLocation)) {
      metadata = mapper.readValue(input, VocabularyMetadata.class);
    } catch (IOException e) {
      throw new VocabularyImportException(
              "Could not read vocabulary metadata at '" + metadataLocation + "'.", e);
    }

    // Read the mapping file.
    final String mapping;
    try (final InputStream input = sourceReader.read(mappingLocation)) {
      mapping = IOUtils.toString(input, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new VocabularyImportException(
              "Could not read vocabulary mapping at '" + mappingLocation + "'.", e);
    }

    // Compile the vocabulary.
    return Vocabulary.builder()
            .setName(metadata.getName())
            .setType(metadata.getType())
            .setPaths(metadata.getPaths())
            .setParentIterations(Optional.ofNullable(metadata.getParentIterations()).orElse(0))
            .setSuffix(metadata.getSuffix())
            .setExamples(metadata.getExamples())
            .setCounterExamples(metadata.getCounterExamples())
            .setTransformation(mapping)
            .build();
  }

  /**
   * Implementations of this interface are able to create a location identifier of the given type
   * from a {@link String}.
   *
   * @param <L> The location identifier type.
   */
  @FunctionalInterface
  interface Converter<L> {

    /**
     * Convert the string to a location identifier.
     *
     * @param input The input string.
     * @return The location identifier.
     * @throws IOException In case the identifier could not be created.
     */
    L convert(String input) throws IOException;
  }

  /**
   * Implementations of this interface are able to read a source (identified by the given location
   * identifier) to an input stream.
   *
   * @param <L> The location identifier type
   */
  @FunctionalInterface
  interface SourceReader<L> {

    /**
     * Read a location to an input stream.
     *
     * @param source The location of the file to read.
     * @return An input stream. The caller is responsible for closing the stream.
     * @throws IOException In case the location could not be read.
     */
    InputStream read(L source) throws IOException;
  }

  /**
   * Implementations of this interface are able to resolve a relative location (identified by a
   * {@link String}) against another location identifier.
   *
   * @param <L> The location identifier type.
   */
  @FunctionalInterface
  interface LocationResolver<L> {

    /**
     * Resolve a relative location against the given location. The given location can be assumed to
     * be a file (as opposed to a path/directory) so that essentially the relative location is
     * resolved against the parent of the given location.
     *
     * @param location The location against which to resolve.
     * @param relativeLocation The relative location to resolve.
     * @return The resolved location.
     */
    L resolve(L location, String relativeLocation);
  }
}
