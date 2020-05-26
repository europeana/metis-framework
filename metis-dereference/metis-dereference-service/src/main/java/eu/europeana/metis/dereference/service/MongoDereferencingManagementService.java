package eu.europeana.metis.dereference.service;

import eu.europeana.metis.dereference.ContextualClass;
import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.service.dao.ProcessedEntityDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
import eu.europeana.metis.dereference.vocimport.VocabularyCollectionImporterFactory;
import eu.europeana.metis.dereference.vocimport.exception.VocabularyImportException;
import eu.europeana.metis.dereference.vocimport.model.VocabularyLoader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Mongo implementation of the DereferencingManagementService Created by ymamakis on 2/11/16.
 */
@Component
public class MongoDereferencingManagementService implements DereferencingManagementService {

  private final VocabularyDao vocabularyDao;
  private final ProcessedEntityDao processedEntityDao;

  /**
   * Constructor.
   *
   * @param vocabularyDao Access to the vocabularies
   */
  @Autowired
  public MongoDereferencingManagementService(VocabularyDao vocabularyDao,
          ProcessedEntityDao processedEntityDao) {
    this.vocabularyDao = vocabularyDao;
    this.processedEntityDao = processedEntityDao;
  }

  @Override
  public List<Vocabulary> getAllVocabularies() {
    return vocabularyDao.getAll();
  }

  @Override
  public void emptyCache() {
    this.processedEntityDao.purgeAll();
  }

  @Override
  public void loadVocabularies(URI directoryUrl) throws VocabularyImportException {

    // Import the vocabularies
    final List<Vocabulary> vocabularies = new ArrayList<>();
    final Iterable<VocabularyLoader> vocabularyLoaders = new VocabularyCollectionImporterFactory()
            .createImporter(directoryUrl).importVocabularies();
    for (VocabularyLoader vocabularyLoader : vocabularyLoaders) {
      vocabularies.add(convertVocabulary(vocabularyLoader.load()));
    }

    // All vocabularies are loaded well. Now we replace the vocabularies.
    vocabularyDao.replaceAll(vocabularies);
  }

  private static Vocabulary convertVocabulary(
          eu.europeana.metis.dereference.vocimport.model.Vocabulary input) {

    // Find the type.
    final ContextualClass type;
    switch (input.getType()) {
      case AGENT:
        type = ContextualClass.AGENT;
        break;
      case CONCEPT:
        type = ContextualClass.CONCEPT;
        break;
      case PLACE:
        type = ContextualClass.PLACE;
        break;
      case TIMESTAMP:
        type = ContextualClass.TIMESPAN;
        break;
      default:
        throw new IllegalStateException("Unexpected type: " + input.getType().name());
    }

    // Perform the conversion
    final Vocabulary vocabulary=new Vocabulary();
    vocabulary.setName(input.getName());
    vocabulary.setType(type);
    vocabulary.setUris(input.getPaths());
    vocabulary.setIterations(input.getParentIterations());
    vocabulary.setSuffix(input.getSuffix());
    vocabulary.setXslt(input.getTransformation());

    // Done
    return vocabulary;
  }
}
