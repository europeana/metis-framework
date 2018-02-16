package eu.europeana.metis.preview.service.executor;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.exceptions.MongoRuntimeException;
import eu.europeana.corelib.edm.utils.MongoConstructor;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.metis.dereference.service.xslt.XsltTransformer;
import eu.europeana.validation.model.ValidationResult;

/**
 * Task for the multi-threaded implementation of the validation service
 * Created by ymamakis on 9/23/16.
 */
public class ValidationTask implements Callable<ValidationTaskResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationTask.class);

    private final ValidationUtils validationUtils;
    private final boolean applyCrosswalk;
    private final IBindingFactory bFact;
    private final String incomingRecord;
    private final String collectionId;
    private final String crosswalkPath;
    private final boolean requestRecordId;

    /**
     * Default constructor of the validation service
     *
     * @param validationUtils  Utils class for validation tasks
     * @param applyCrosswalk   Whether the record needs to be transformed
     * @param bFact            The JibX binding factory for the conversion of the XML to RDF class
     * @param incomingRecord   The record to be validated and transformed
     * @param collectionId     The collection identifier
     * @param crosswalkPath    The path where the crosswalk between EDM-External and EDM-Internal resides
     * @param requestRecordId  Whether the request IDs are to be returned.
     */
    public ValidationTask(ValidationUtils validationUtils, boolean applyCrosswalk, IBindingFactory bFact,
                          String incomingRecord, String collectionId, String crosswalkPath, boolean requestRecordId) {
        this.validationUtils = validationUtils;
        this.applyCrosswalk = applyCrosswalk;
        this.bFact = bFact;
        this.incomingRecord = incomingRecord;
        this.collectionId = collectionId;
        this.crosswalkPath = crosswalkPath;
        this.requestRecordId = requestRecordId;
    }

    /**
     * Execution of transformation, id-generation and validation for Europeana Preview Service
     */
    @Override
    public ValidationTaskResult call()
                throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException,
                JiBXException, ParserConfigurationException, MongoRuntimeException, IllegalAccessException,
                MongoDBException, TransformerException, SolrServerException {
        try {
            return invoke();
        } catch (Exception ex) {
            LOGGER.error("An error occurred while validating", ex);
            throw ex;
        }
    }
    
  private String transformRecord()
      throws TransformerException, ParserConfigurationException, IOException {
    String tempRecord;
    XsltTransformer transformer = new XsltTransformer();
    tempRecord = transformer.transform(incomingRecord, FileUtils.readFileToString(
        new File(this.getClass().getClassLoader().getResource(crosswalkPath).getFile())));
    return tempRecord;
  }

  private ValidationTaskResult invoke()
      throws JiBXException, TransformerException, ParserConfigurationException, IOException,
      InstantiationException, IllegalAccessException, SolrServerException, NoSuchMethodException,
      InvocationTargetException, MongoDBException, MongoRuntimeException {

    // Validate the input
    final ValidationResult validationResult =
        validationUtils.validateRecord(incomingRecord, applyCrosswalk);

    // If successful, we handle the result.
    final ValidationTaskResult result;
    if (validationResult.isSuccess()) {
      result = handleValidatedResult(validationResult);
    } else {
      result = new ValidationTaskResult(null, validationResult, false);
    }

    // Done
    return result;
  }

  private ValidationTaskResult handleValidatedResult(final ValidationResult validationResult)
      throws JiBXException, TransformerException, ParserConfigurationException, IOException,
      InstantiationException, IllegalAccessException, SolrServerException, NoSuchMethodException,
      InvocationTargetException, MongoDBException, MongoRuntimeException {
    
    // Transform the record (apply crosswalk) if necessary.
    final String resultRecord = applyCrosswalk ? transformRecord() : incomingRecord;
    
    // Convert record to RDF and obtain record ID.
    final IUnmarshallingContext uctx = bFact.createUnmarshallingContext();
    final RDF rdf = (RDF) uctx.unmarshalDocument(new StringReader(resultRecord));
    final String recordId = validationUtils.generateIdentifier(collectionId, rdf);

    // Obtain the result.
    final ValidationTaskResult result;
    if (StringUtils.isNotEmpty(recordId)) {
      
      // If we have obtained a record ID we return a successful result.
      rdf.getProvidedCHOList().get(0).setAbout(recordId);
      final FullBeanImpl fBean = new MongoConstructor().constructFullBean(rdf);
      fBean.setAbout(recordId);
      fBean.setEuropeanaCollectionName(new String[] {collectionId});
      validationUtils.persistFullBean(fBean);
      // TODO JOCHEN if !requestRecordId, should we return the ENTIRE record? Or nothing?
      result = new ValidationTaskResult(requestRecordId ? recordId : resultRecord, validationResult, true);
    } else {
      
      // If we couldn't obtain a record ID we return a failed result.
      ValidationResult noIdValidationResult = new ValidationResult();
      noIdValidationResult.setSuccess(false);
      noIdValidationResult.setRecordId(rdf.getProvidedCHOList().get(0).getAbout());
      noIdValidationResult.setMessage("Id generation failed. Record not persisted");
      result = new ValidationTaskResult(resultRecord, noIdValidationResult, false);
    }
    
    // Done
    return result;
  }
}
