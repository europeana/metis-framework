package eu.europeana.metis.preview.service;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.metis.identifier.RestClient;
import eu.europeana.metis.preview.persistence.RecordDao;
import eu.europeana.metis.preview.service.executor.ValidationTask;
import eu.europeana.validation.client.ValidationClient;
import eu.europeana.validation.model.ValidationResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.JiBXException;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.*;

/**
 * The Preview Service implementation to upload (and potentially transform from EDM-External to EDM-Internal) records
 * Created by ymamakis on 9/2/16.
 */
@Component
public class PreviewService {

    private String previewPortalUrl;


    /**
     * Constructor for the preview service
     *
     * @param previewPortalUrl The preview portal URL
     */
    public PreviewService(String previewPortalUrl) throws JiBXException {
        bfact = BindingDirectory.getFactory(RDF.class);
        this.previewPortalUrl = previewPortalUrl;
    }

    private static IBindingFactory bfact;

    @Autowired
    private RecordDao dao;
    @Autowired
    private RestClient identifierClient;
    @Autowired
    private ValidationClient validationClient;

    private static final ExecutorService executor = Executors.newFixedThreadPool(10);
    private static final ExecutorCompletionService cs = new ExecutorCompletionService(executor);
    public PreviewService() throws JiBXException {
        bfact = BindingDirectory.getFactory(RDF.class);
    }

    /**
     * Persist temporarily (24h) records in the preview portal
     *
     * @param records        The records to persist as list of XML strings
     * @param collectionId   The collection id to apply (can be null)
     * @param applyCrosswalk Whether the records are in EDM-External (thus need conversion to EDM-Internal)
     * @return The preview URL of the records along with the result of the validation
     * @throws JiBXException
     * @throws IllegalAccessException
     * @throws IOException
     * @throws InstantiationException
     * @throws SolrServerException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws TransformerException
     * @throws ParserConfigurationException
     */
    public ExtendedValidationResult createRecords(List<String> records, String collectionId, boolean applyCrosswalk, String crosswalkPath) throws JiBXException, IllegalAccessException, IOException, InstantiationException, SolrServerException, NoSuchMethodException, InvocationTargetException, TransformerException, ParserConfigurationException, InterruptedException, ExecutionException {
        if (StringUtils.isEmpty(collectionId)) {
            collectionId = CollectionUtils.generateCollectionId();
        }
        ExtendedValidationResult list = new ExtendedValidationResult();
        list.setSuccess(true);
        List<ValidationResult> results = new CopyOnWriteArrayList<>();
        dao.deleteCollection(collectionId);
        dao.commit();
        for (String record : records) {
            ValidationTask task = new ValidationTask(results, applyCrosswalk, bfact, record, identifierClient,
                    validationClient, dao, collectionId, crosswalkPath, list);
            cs.submit(task);
            Future<ExtendedValidationResult> future = cs.take();
            future.get();
        }
        dao.commit();
        list.setResultList(results);
        list.setPortalUrl(previewPortalUrl + collectionId + "*");
        DateTime date = new DateTime();
        Date nextDay = date.toDateMidnight().plusDays(1).toDate();
        list.setDate(nextDay);
        return list;
    }

    /**
     * Delete records every 24 hrs
     *
     * @throws IOException
     * @throws SolrServerException
     */
    @Scheduled(cron = "00 00 * * * *")
    public void deleteRecords() throws IOException, SolrServerException {
        dao.deleteRecordIdsByTimestamp();
    }
}
