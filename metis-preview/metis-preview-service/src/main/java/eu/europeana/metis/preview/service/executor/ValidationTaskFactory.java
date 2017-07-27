package eu.europeana.metis.preview.service.executor;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.metis.identifier.RestClient;
import eu.europeana.metis.preview.persistence.RecordDao;
import eu.europeana.validation.client.ValidationClient;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by erikkonijnenburg on 06/07/2017.
 */
@Service
public class ValidationTaskFactory {
  public static final Logger LOGGER = LoggerFactory
      .getLogger(ValidationTask.class);
  private RestClient identifierClient;
  private ValidationClient validationClient;
  private RecordDao recordDao;

  @Autowired
  public ValidationTaskFactory(RestClient identifierClient,
      ValidationClient validationClient, RecordDao recordDao) {
    this.identifierClient = identifierClient;
    this.validationClient = validationClient;
    this.recordDao = recordDao;
  }

  private static final IBindingFactory bindingFactory;

  static {
    IBindingFactory bfactTemp;
    try {
      bfactTemp = BindingDirectory.getFactory(RDF.class);
    } catch (JiBXException e) {
      bfactTemp = null;
      e.printStackTrace();
      LOGGER.error("Unable to get binding factory for RDF.class");
      System.exit(-1);
    }
    bindingFactory = bfactTemp;
  }

  public ValidationTask createValidationTaks(boolean applyCrosswalk,
      String record, String collectionId, String crosswalkPath, boolean requestRecordId) {

    return new ValidationTask(applyCrosswalk, bindingFactory, record, identifierClient, validationClient, recordDao,
       collectionId, crosswalkPath, requestRecordId);
  }

}
