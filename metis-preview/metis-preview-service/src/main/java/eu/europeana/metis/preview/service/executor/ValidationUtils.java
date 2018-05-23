package eu.europeana.metis.preview.service.executor;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.utils.EuropeanaUriUtils;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.metis.preview.service.persistence.RecordDao;
import eu.europeana.validation.client.ValidationClient;
import eu.europeana.validation.model.ValidationResult;

public class ValidationUtils {

  private final ValidationClient validationClient;
  private final RecordDao recordDao;
  private final String schemaBeforeTransformation;
  private final String schemaAfterTransformation;
  private final String defaultTransformationFile;

  public ValidationUtils(ValidationClient validationClient, RecordDao recordDao,
      String schemaBeforeTransformation, String schemaAfterTransformation,
      String defaultTransformationFile) {
    this.validationClient = validationClient;
    this.recordDao = recordDao;
    this.schemaBeforeTransformation = schemaBeforeTransformation;
    this.schemaAfterTransformation = schemaAfterTransformation;
    this.defaultTransformationFile = defaultTransformationFile;
  }

  public ValidationResult validateRecordBeforeTransformation(String record) {
    return validationClient.validateRecord(schemaBeforeTransformation, record);
  }

  public ValidationResult validateRecordAfterTransformation(String record) {
    return validationClient.validateRecord(schemaAfterTransformation, record);
  }

  public String generateIdentifier(String collectionId, RDF rdf) {
    return EuropeanaUriUtils
        .createSanitizedEuropeanaId(collectionId, rdf.getProvidedCHOList().get(0).getAbout())
        .replace("\"", "");
  }

  public void persist(RDF rdf) throws IndexingException {
    recordDao.createRecord(rdf);
  }

  public String getDefaultTransformationFile() {
    return defaultTransformationFile;
  }
}
