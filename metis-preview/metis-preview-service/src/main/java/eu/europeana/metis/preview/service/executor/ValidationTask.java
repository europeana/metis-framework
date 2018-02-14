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
import eu.europeana.metis.identifier.RestClient;
import eu.europeana.metis.preview.persistence.RecordDao;
import eu.europeana.validation.client.ValidationClient;
import eu.europeana.validation.model.ValidationResult;

/**
 * Task for the multi-threaded implementation of the validation service
 * Created by ymamakis on 9/23/16.
 */
public class ValidationTask implements Callable<ValidationTaskResult> {

    public static final Logger LOGGER = LoggerFactory
            .getLogger(ValidationTask.class);

    private boolean applyCrosswalk;
    private IBindingFactory bFact;
    private String record;
    private RestClient identifierClient;
    private ValidationClient validationClient;
    private RecordDao recordDao;
    private String collectionId;
    private String crosswalkPath;
    private boolean requestRecordId;

    /**
     * Default constructor of the validation service
     *
     * @param applyCrosswalk   Whether the record needs to be transformed
     * @param bFact            The JibX binding factory for the conversion of the XML to RDF class
     * @param record           The record to be validated and transformed
     * @param identifierClient The identifier generation REST client connecting to METIS
     * @param validationClient The validation REST client
     * @param recordDao        The persistence layer in Solr and Mongo
     * @param collectionId     The collection identifier
     * @param crosswalkPath    The path where the crosswalk between EDM-External and EDM-Internal resides
     * @param requestRecordId  Whether the request IDs are to be returned.
     */
    public ValidationTask(boolean applyCrosswalk, IBindingFactory bFact,
                          String record, RestClient identifierClient, ValidationClient validationClient,
                          RecordDao recordDao, String collectionId, String crosswalkPath, boolean requestRecordId) {
        this.applyCrosswalk = applyCrosswalk;
        this.bFact = bFact;
        this.record = record;
        this.identifierClient = identifierClient;
        this.validationClient = validationClient;
        this.recordDao = recordDao;
        this.collectionId = collectionId;
        this.crosswalkPath = crosswalkPath;
        this.requestRecordId = requestRecordId;
    }

    /**
     * Execution of transformation, id-generation and validation for Europeana Preview Service
     */
    @Override
    public ValidationTaskResult call()
            throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, JiBXException, ParserConfigurationException, MongoRuntimeException, IllegalAccessException, MongoDBException, TransformerException, SolrServerException {
        ValidationExecutor validationExecutor = new ValidationExecutor();
        try {
            validationExecutor.invoke();
        } catch (Exception ex) {
            LOGGER.error("An error occurred while validating", ex);
            throw ex;
        }
        return new ValidationTaskResult(record,
                validationExecutor.getValidationResult(),
                validationExecutor.isValidationSuccess());
    }

    private class ValidationExecutor {

        public static final String SCHEMANAME = "EDM-INTERNAL";

        private ValidationResult validationResult;
        private boolean validationSuccess;

        public ValidationResult getValidationResult() {
            return validationResult;
        }

        public boolean isValidationSuccess() {
            return validationSuccess;
        }

        private String transformRecord()
            throws TransformerException, ParserConfigurationException, IOException {
            String tempRecord;
            XsltTransformer transformer = new XsltTransformer();
            tempRecord = transformer.transform(record, FileUtils.readFileToString(new File(this.getClass()
                    .getClassLoader().getResource(crosswalkPath).getFile())));
            return tempRecord;
        }

        public ValidationExecutor invoke()
                throws JiBXException, TransformerException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SolrServerException, NoSuchMethodException, InvocationTargetException, MongoDBException, MongoRuntimeException {
            IUnmarshallingContext uctx = bFact.createUnmarshallingContext();
            if (applyCrosswalk) {
                record = transformRecord();
            }

            validationResult = validationClient.validateRecord(SCHEMANAME, record);

            validationSuccess = false;

            if (validationResult.isSuccess()) {
                RDF rdf = (RDF) uctx.unmarshalDocument(new StringReader(record));
                String id = identifierClient
                        .generateIdentifier(collectionId, rdf.getProvidedCHOList().get(0).getAbout())
                        .replace("\"", "");

                if (StringUtils.isNotEmpty(id)) {
                    rdf.getProvidedCHOList().get(0).setAbout(id);
                    FullBeanImpl fBean = new MongoConstructor()
                            .constructFullBean(rdf);
                    fBean.setAbout(id);
                    fBean.setEuropeanaCollectionName(new String[]{collectionId});
                    recordDao.createRecord(fBean);
                    if (requestRecordId) {
                        record = fBean.getAbout();
                    }
                    validationSuccess = true;
                } else {
                    validationResult = new ValidationResult();
                    validationResult.setSuccess(false);
                    validationResult.setRecordId(rdf.getProvidedCHOList().get(0).getAbout());
                    validationResult.setMessage("Id generation failed. Record not persisted");
                }
            }
            return this;
        }
    }
}
