package eu.europeana.metis.preview.service.executor;

import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.exceptions.MongoRuntimeException;
import eu.europeana.corelib.edm.exceptions.MongoUpdateException;
import eu.europeana.corelib.utils.EuropeanaUriUtils;
import eu.europeana.metis.preview.persistence.RecordDao;
import eu.europeana.validation.client.ValidationClient;
import eu.europeana.validation.model.ValidationResult;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.apache.solr.client.solrj.SolrServerException;

public class ValidationUtils {

  private final ValidationClient validationClient;
  private final RecordDao recordDao;
  private final String schemaBeforeTransformation;
  private final String schemaAfterTransformation;
  private final String defaultTransformationFile;

  public ValidationUtils(ValidationClient validationClient,
      RecordDao recordDao, String schemaBeforeTransformation, String schemaAfterTransformation,
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

  public void persistFullBean(FullBean fBean)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
      MongoDBException, MongoRuntimeException, SolrServerException, IOException, MongoUpdateException {
    recordDao.createRecord(fBean);
  }

  public String getDefaultTransformationFile() {
    return defaultTransformationFile;
  }
}
