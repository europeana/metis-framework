package eu.europeana.metis.dereference.service;

import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.service.dao.ProcessedEntityDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
import eu.europeana.metis.dereference.vocimport.VocabularyCollectionImporter;
import eu.europeana.metis.dereference.vocimport.VocabularyCollectionImporterFactory;
import eu.europeana.metis.dereference.vocimport.VocabularyCollectionValidator;
import eu.europeana.metis.dereference.vocimport.VocabularyCollectionValidatorImpl;
import eu.europeana.metis.dereference.vocimport.exception.VocabularyImportException;

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
  private final VocabularyCollectionImporterFactory vocabularyCollectionImporterFactory;

  /**
   * Constructor.
   *
   * @param vocabularyDao Access to the vocabularies
   */
  @Autowired
  public MongoDereferencingManagementService(VocabularyDao vocabularyDao,
          ProcessedEntityDao processedEntityDao, VocabularyCollectionImporterFactory vocabularyCollectionImporterFactory) {
    this.vocabularyDao = vocabularyDao;
    this.processedEntityDao = processedEntityDao;
    this.vocabularyCollectionImporterFactory = vocabularyCollectionImporterFactory;
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

    try {
    // Import and validate the vocabularies
    final List<Vocabulary> vocabularies = new ArrayList<>();
    final VocabularyCollectionImporter importer = vocabularyCollectionImporterFactory
            .createImporter(directoryUrl);
    final VocabularyCollectionValidator validator = new VocabularyCollectionValidatorImpl(importer,
            true, true, true);
    validator.validateVocabularyOnly(vocabulary -> vocabularies.add(convertVocabulary(vocabulary)));

    // All vocabularies are loaded well. Now we replace the vocabularies.
    vocabularyDao.replaceAll(vocabularies);
    } catch (VocabularyImportException e) {
      throw new VocabularyImportException("An error as occurred while loading the vocabularies", e);
    }
  }

  private static Vocabulary convertVocabulary(
          eu.europeana.metis.dereference.vocimport.model.Vocabulary input) {
    final Vocabulary vocabulary = new Vocabulary();
    vocabulary.setName(input.getName());
    vocabulary.setUris(input.getPaths());
    vocabulary.setIterations(input.getParentIterations());
    vocabulary.setSuffix(input.getSuffix());
    vocabulary.setXslt(input.getTransformation());
    return vocabulary;
  }
}
