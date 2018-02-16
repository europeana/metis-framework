package eu.europeana.metis.preview.service.executor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import org.apache.solr.client.solrj.SolrServerException;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.exceptions.MongoRuntimeException;
import eu.europeana.metis.identifier.RestClient;
import eu.europeana.metis.preview.persistence.RecordDao;
import eu.europeana.validation.client.ValidationClient;
import eu.europeana.validation.model.ValidationResult;

public class ValidationUtils {

  private final RestClient identifierClient;
  private final ValidationClient validationClient;
  private final RecordDao recordDao;
  private final String schemaBeforeTransformation;
  private final String schemaAfterTransformation;
  private final String defaultTransformationFile;

  public ValidationUtils(RestClient identifierClient, ValidationClient validationClient,
      RecordDao recordDao, String schemaBeforeTransformation, String schemaAfterTransformation,
      String defaultTransformationFile) {
    this.identifierClient = identifierClient;
    this.validationClient = validationClient;
    this.recordDao = recordDao;
    this.schemaBeforeTransformation = schemaBeforeTransformation;
    this.schemaAfterTransformation = schemaAfterTransformation;
    this.defaultTransformationFile = defaultTransformationFile;
  }

  public ValidationResult validateRecord(String record, boolean needsTransformation) {
    final String currentSchema =
        needsTransformation ? schemaBeforeTransformation : schemaAfterTransformation;
    return validationClient.validateRecord(currentSchema, record);
  }

  public String generateIdentifier(String collectionId, RDF rdf)
      throws UnsupportedEncodingException {
    return identifierClient
        .generateIdentifier(collectionId, rdf.getProvidedCHOList().get(0).getAbout())
        .replace("\"", "");
  }

  public void persistFullBean(FullBean fBean)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
      MongoDBException, MongoRuntimeException, SolrServerException, IOException {
    recordDao.createRecord(fBean);
  }

  public String getDefaultTransformationFile() {
    return defaultTransformationFile;
  }
}
