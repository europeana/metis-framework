package eu.europeana.metis.data.checker.service.executor;

import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.metis.utils.RestEndpoints;
import eu.europeana.metis.data.checker.service.persistence.RecordIndexingService;
import eu.europeana.metis.transformation.service.TransformationException;
import eu.europeana.metis.transformation.service.XsltTransformer;
import eu.europeana.validation.client.ValidationClient;
import eu.europeana.validation.model.ValidationResult;
import java.util.Date;

public class ValidationUtils {

  private final ValidationClient validationClient;
  private final RecordIndexingService recordIndexingService;
  private final String schemaBeforeTransformation;
  private final String schemaAfterTransformation;
  private final String xsltUrl;

  public ValidationUtils(ValidationClient validationClient, RecordIndexingService recordIndexingService,
      String schemaBeforeTransformation, String schemaAfterTransformation, String metisCoreUri) {
    this.validationClient = validationClient;
    this.recordIndexingService = recordIndexingService;
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

  public void persist(RDF rdf, Date recordDate) throws IndexingException {
    recordIndexingService.createRecord(rdf, recordDate);
  }

  public XsltTransformer createTransformer(String datasetName, String edmCountry,
      String edmLanguage) throws TransformationException {
    return new XsltTransformer(xsltUrl, datasetName, edmCountry, edmLanguage);
  }
}
