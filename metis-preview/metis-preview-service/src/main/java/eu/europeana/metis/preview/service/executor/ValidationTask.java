package eu.europeana.metis.preview.service.executor;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.exceptions.MongoRuntimeException;
import eu.europeana.corelib.edm.utils.MongoConstructor;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.metis.dereference.service.xslt.XsltTransformer;
import eu.europeana.metis.identifier.RestClient;
import eu.europeana.metis.preview.model.ExtendedValidationResult;
import eu.europeana.metis.preview.persistence.RecordDao;
import eu.europeana.validation.client.ValidationClient;
import eu.europeana.validation.model.ValidationResult;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Task for the multi-threaded implementation of the validation service
 * Created by ymamakis on 9/23/16.
 */
public class ValidationTask implements Callable {
    private List<ValidationResult> validationResults;
    private boolean applyCrosswalk;
    private IBindingFactory bFact;
    private String record;
    private RestClient identifierClient;
    private ValidationClient validationClient;
    private RecordDao recordDao;
    private String collectionId;
    private String crosswalkPath;
    private ExtendedValidationResult list;

    /**
     * Default constructor of the validation service
     * @param resultsCache A threadsafe List of results
     * @param applyCrosswalk Whether the record needs to be transformed
     * @param bFact The JibX binding factory for the conversion of the XML to RDF class
     * @param record The record to be validated and transformed
     * @param identifierClient The identifier generation REST client connecting to METIS
     * @param validationClient The validation REST client
     * @param recordDao The persistence layer in Solr and Mongo
     * @param collectionId The collection identifier
     * @param crosswalkPath The path where the crosswalk between EDM-External and EDM-Internal resides
     * @param list The ExtendedValidationResult
     */
    public ValidationTask(List<ValidationResult> resultsCache, boolean applyCrosswalk, IBindingFactory bFact,
                          String record, RestClient identifierClient, ValidationClient validationClient,
                          RecordDao recordDao, String collectionId, String crosswalkPath, ExtendedValidationResult list) {
        validationResults = resultsCache;
        this.applyCrosswalk = applyCrosswalk;
        this.bFact = bFact;
        this.record = record;
        this.identifierClient = identifierClient;
        this.validationClient = validationClient;
        this.recordDao = recordDao;
        this.collectionId = collectionId;
        this.crosswalkPath = crosswalkPath;
        this.list = list;
    }

    /**
     * Execution of transformation, id-generation and validation for Europeana Preview Service
     */
    @Override
    public ExtendedValidationResult call() throws IOException, TransformerException, ParserConfigurationException, JiBXException, IllegalAccessException, InstantiationException, SolrServerException, NoSuchMethodException, InvocationTargetException, MongoDBException, MongoRuntimeException {

            IUnmarshallingContext uctx = bFact.createUnmarshallingContext();
            if (applyCrosswalk) {
                XsltTransformer transformer = new XsltTransformer();
                record = transformer.transform(record, FileUtils.readFileToString(new File(this.getClass()
                        .getClassLoader().getResource(crosswalkPath).getFile())));
            }
            ValidationResult result = validationClient.validateRecord("EDM-INTERNAL", record, "undefined");
            if (result.isSuccess()) {
                RDF rdf = (RDF) uctx.unmarshalDocument(new StringReader(record));
                String id = identifierClient.generateIdentifier(collectionId, rdf.getProvidedCHOList().get(0).getAbout()).replace("\"", "");
                if(StringUtils.isNotEmpty(id)) {
                    rdf.getProvidedCHOList().get(0).setAbout(id);
                    FullBeanImpl fBean = new MongoConstructor()
                            .constructFullBean(rdf);
                    fBean.setAbout(id);
                    fBean.setEuropeanaCollectionName(new String[]{collectionId});
                    recordDao.createRecord(fBean);
                } else {
                    ValidationResult result1 =new ValidationResult();
                    result1.setSuccess(false);
                    result1.setRecordId(rdf.getProvidedCHOList().get(0).getAbout());
                    result1.setMessage("Id generation failed. Record not persisted");
                    validationResults.add(result1);
                    list.setSuccess(false);
                }
            } else {
                validationResults.add(result);
                list.setSuccess(false);
            }


        return list;
    }
}
