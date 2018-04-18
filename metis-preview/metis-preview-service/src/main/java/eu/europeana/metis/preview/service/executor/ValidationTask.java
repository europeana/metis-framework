package eu.europeana.metis.preview.service.executor;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.exceptions.MongoRuntimeException;
import eu.europeana.corelib.edm.exceptions.MongoUpdateException;
import eu.europeana.corelib.edm.utils.MongoConstructor;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.validation.model.ValidationResult;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  /**
   * Default constructor of the validation service
   *
   * @param validationUtils Utils class for validation tasks
   * @param applyCrosswalk Whether the record needs to be transformed
   * @param bFact The JibX binding factory for the conversion of the XML to RDF class
   * @param incomingRecord The record to be validated and transformed
   * @param collectionId The collection identifier
   * @param crosswalkPath The path where the crosswalk between EDM-External and EDM-Internal
   * resides. Can be Null, in which case the default is used.
   */
  public ValidationTask(ValidationUtils validationUtils, boolean applyCrosswalk,
      IBindingFactory bFact,
      String incomingRecord, String collectionId, String crosswalkPath) {
    this.validationUtils = validationUtils;
    this.applyCrosswalk = applyCrosswalk;
    this.bFact = bFact;
    this.incomingRecord = incomingRecord;
    this.collectionId = collectionId;
    this.crosswalkPath = crosswalkPath;
  }

  /**
   * Execution of transformation, id-generation and validation for Europeana Preview Service
 * @throws MongoUpdateException 
   */
  @Override
  public ValidationTaskResult call()
      throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, JiBXException,
      IllegalAccessException, MongoRuntimeException, MongoDBException, TransformerException, SolrServerException, MongoUpdateException {
    try {
      return invoke();
    } catch (JiBXException | IOException | IllegalAccessException | InstantiationException | NoSuchMethodException |
        SolrServerException | InvocationTargetException | TransformerException | MongoDBException | MongoRuntimeException e) {
      LOGGER.error("An error occurred while validating", e);
      throw e;
    }
  }

  private String transformRecord() throws TransformerException, IOException {

    // Obtain the XSL transform.
    String transformationFilePath;
    if (StringUtils.isEmpty(crosswalkPath)) {
      transformationFilePath = validationUtils.getDefaultTransformationFile();
    } else {
      transformationFilePath = crosswalkPath;
    }
    final File transformationFile = new File(
        Thread.currentThread().getContextClassLoader().getResource(transformationFilePath)
            .getFile());
    final String xslTransform = FileUtils
        .readFileToString(transformationFile, "UTF-8");

    // Perform the XSL transformation on the incoming record.
    final Source xsltSource = new StreamSource(new StringReader(xslTransform));
    final Transformer transformer = TransformerFactory.newInstance().newTransformer(xsltSource);
    final Source recordSource = new StreamSource(new StringReader(incomingRecord));
    final StringWriter stringWriter = new StringWriter();
    transformer.transform(recordSource, new StreamResult(stringWriter));
    return stringWriter.toString();
  }

  private ValidationTaskResult invoke() throws JiBXException, TransformerException, IOException,
      InstantiationException, IllegalAccessException, SolrServerException, NoSuchMethodException,
      InvocationTargetException, MongoDBException, MongoRuntimeException, MongoUpdateException {

    final ValidationResult validationResult =
        applyCrosswalk ? validationUtils.validateRecordBeforeTransformation(incomingRecord)
            : validationUtils.validateRecordAfterTransformation(incomingRecord);

    // If successful, we handle the result.
    ValidationTaskResult result;
    if (validationResult.isSuccess()) {
      result = handleValidatedResult(validationResult);
    } else {
      result = new ValidationTaskResult(null, validationResult, false);
    }

    // Done
    return result;
  }

  private ValidationTaskResult handleValidatedResult(final ValidationResult validationResult)
      throws JiBXException, TransformerException, IOException, InstantiationException,
      IllegalAccessException, SolrServerException, NoSuchMethodException, InvocationTargetException,
      MongoDBException, MongoRuntimeException, MongoUpdateException {

    // Transform the record (apply crosswalk) if necessary.
    final String resultRecord = applyCrosswalk ? transformRecord() : incomingRecord;

    // Convert record to RDF and obtain record ID.
    final IUnmarshallingContext uctx = bFact.createUnmarshallingContext();
    final RDF rdf = (RDF) uctx.unmarshalDocument(new StringReader(resultRecord));
    final String recordId = validationUtils.generateIdentifier(collectionId, rdf);

    // Obtain the result.
    ValidationTaskResult result;
    if (StringUtils.isNotEmpty(recordId)) {

      // If we have obtained a record ID we return a successful result.
      rdf.getProvidedCHOList().get(0).setAbout(recordId);
      final FullBeanImpl fBean = new MongoConstructor().constructFullBean(rdf);
      fBean.setAbout(recordId);
      fBean.setEuropeanaCollectionName(new String[]{collectionId});
      validationUtils.persistFullBean(fBean);
      result = new ValidationTaskResult(recordId, validationResult, true);
    } else {

      // If we couldn't obtain a record ID we return a failed result.
      ValidationResult noIdValidationResult = new ValidationResult();
      noIdValidationResult.setSuccess(false);
      noIdValidationResult.setRecordId(rdf.getProvidedCHOList().get(0).getAbout());
      noIdValidationResult.setMessage("Id generation failed. Record not persisted");
      result = new ValidationTaskResult(null, noIdValidationResult, false);
    }

    // Done
    return result;
  }
}
