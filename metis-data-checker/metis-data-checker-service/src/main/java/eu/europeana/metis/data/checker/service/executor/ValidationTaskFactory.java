package eu.europeana.metis.data.checker.service.executor;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.metis.data.checker.common.model.DatasetProperties;

/**
 * Created by erikkonijnenburg on 06/07/2017.
 */
@Service
public class ValidationTaskFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(ValidationTaskFactory.class);

  private static final IBindingFactory BINDING_FACTORY;

  private final ValidationUtils validationUtils;

  @Autowired
  public ValidationTaskFactory(ValidationUtils validationUtils) {
    this.validationUtils = validationUtils;
  }

  static {
    IBindingFactory bfactTemp;
    try {
      bfactTemp = BindingDirectory.getFactory(RDF.class);
    } catch (JiBXException e) {
      bfactTemp = null;
      LOGGER.error("Unable to get binding factory for RDF.class", e);
    }
    BINDING_FACTORY = bfactTemp;
  }

  public ValidationTask createValidationTask(boolean applyTransformation, String record,
      DatasetProperties datasetProperties) {
    return new ValidationTask(validationUtils, applyTransformation, BINDING_FACTORY, record,
        datasetProperties);
  }
}
