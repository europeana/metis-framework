package eu.europeana.metis.dereference.vocimport;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import eu.europeana.metis.dereference.vocimport.exception.VocabularyImportException;
import eu.europeana.metis.dereference.vocimport.model.Location;
import eu.europeana.metis.dereference.vocimport.model.Vocabulary;
import eu.europeana.metis.dereference.vocimport.model.VocabularyDirectoryEntry;
import eu.europeana.metis.dereference.vocimport.model.VocabularyLoader;
import eu.europeana.metis.dereference.vocimport.model.VocabularyMetadata;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;

/**
 * This is the default implementation of the vocabulary importing functionality.
 */
final class VocabularyCollectionImporterImpl implements VocabularyCollectionImporter {

  private Location directoryLocation;

  VocabularyCollectionImporterImpl(Location directoryLocation) {
    this.directoryLocation = directoryLocation;
  }

  @Override
  public Iterable<VocabularyLoader> importVocabularies()
          throws VocabularyImportException {

    // Obtain the directory entries.
    final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    final VocabularyDirectoryEntry[] directoryEntries;

    try (final InputStream input = directoryLocation.read()) {
      directoryEntries = mapper.readValue(input, VocabularyDirectoryEntry[].class);
    } catch (IOException e) {
      throw new VocabularyImportException(
              "Could not read vocabulary directory at [" + directoryLocation + "].", e);
    }

    // Compile the vocabulary loaders
    final List<VocabularyLoader> result = new ArrayList<>(directoryEntries.length);
    for (VocabularyDirectoryEntry entry : directoryEntries) {
      final Location metadataLocation = directoryLocation.resolve(entry.getMetadata());
      final Location mappingLocation = directoryLocation.resolve(entry.getMapping());
      result.add(() -> loadVocabulary(metadataLocation, mappingLocation, mapper));
    }

    // Done
    return result;
  }

  private Vocabulary loadVocabulary(Location metadataLocation, Location mappingLocation,
          ObjectMapper mapper) throws VocabularyImportException {

    // Read the metadata file.
    final VocabularyMetadata metadata;
    try (final InputStream input = metadataLocation.read()) {
      metadata = mapper.readValue(input, VocabularyMetadata.class);
    } catch (IOException e) {
      throw new VocabularyImportException(
              "Could not read vocabulary metadata at [" + metadataLocation + "].", e);
    }

    // Read the mapping file.
    final String mapping;
    try (final InputStream input = mappingLocation.read()) {
      mapping = IOUtils.toString(input, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new VocabularyImportException(
              "Could not read vocabulary mapping at [" + mappingLocation + "].", e);
    }

    // Compile the vocabulary.
    return Vocabulary.builder()
            .setName(metadata.getName())
            .setTypes(metadata.getTypes())
            .setPaths(metadata.getPaths())
            .setParentIterations(metadata.getParentIterations())
            .setSuffix(metadata.getSuffix())
            .setExamples(metadata.getExamples())
            .setCounterExamples(metadata.getCounterExamples())
            .setTransformation(mapping)
            .setReadableMetadataLocation(metadataLocation.toString())
            .setReadableMappingLocation(mappingLocation.toString())
            .build();
  }

  public Location getDirectoryLocation() {
    return directoryLocation;
  }
}
