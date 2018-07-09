package eu.europeana.metis.data.checker.service.executor;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.data.checker.service.persistence.RecordDao;
import eu.europeana.metis.transformation.service.TransformationException;
import eu.europeana.metis.transformation.service.XsltTransformer;
import eu.europeana.validation.client.ValidationClient;
import eu.europeana.validation.model.ValidationResult;

public class ValidationUtils {

  private final ValidationClient validationClient;
  private final RecordDao recordDao;
  private final String schemaBeforeTransformation;
  private final String schemaAfterTransformation;
  private final String xsltUrl;

  public ValidationUtils(ValidationClient validationClient, RecordDao recordDao,
      String schemaBeforeTransformation, String schemaAfterTransformation, String metisCoreUri) {
    this.validationClient = validationClient;
    this.recordDao = recordDao;
    this.schemaBeforeTransformation = schemaBeforeTransformation;
    this.schemaAfterTransformation = schemaAfterTransformation;
    this.xsltUrl = metisCoreUri + RestEndpoints.DATASETS_XSLT_DEFAULT;
  }

  public ValidationResult validateRecordBeforeTransformation(String record) {
    return validationClient.validateRecord(schemaBeforeTransformation, record);
  }

  public ValidationResult validateRecordAfterTransformation(String record) {
    return validationClient.validateRecord(schemaAfterTransformation, record);
  }

  public void persist(RDF rdf) throws IndexingException {
    recordDao.createRecord(rdf);
  }

  public XsltTransformer createTransformer(String datasetName, String edmCountry,
      String edmLanguage) throws TransformationException {
    return new XsltTransformer(xsltUrl, datasetName, edmCountry, edmLanguage);
  }
}
