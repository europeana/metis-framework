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

    // Import and validate the vocabularies
    final List<Vocabulary> vocabularies = new ArrayList<>();
    final VocabularyCollectionImporter importer = new VocabularyCollectionImporterFactory()
            .createImporter(directoryUrl);
    final VocabularyCollectionValidator validator = new VocabularyCollectionValidatorImpl(importer,
            true, true, true);
    validator.validateVocabularyOnly(vocabulary -> vocabularies.add(convertVocabulary(vocabulary)));

    // All vocabularies are loaded well. Now we replace the vocabularies.
    vocabularyDao.replaceAll(vocabularies);
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
