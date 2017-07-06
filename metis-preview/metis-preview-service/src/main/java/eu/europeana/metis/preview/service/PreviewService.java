package eu.europeana.metis.preview.service;

import com.google.common.base.Strings;
import eu.europeana.metis.preview.model.ExtendedValidationResult;
import eu.europeana.metis.preview.persistence.RecordDao;
import eu.europeana.metis.preview.service.executor.ValidationTask;
import eu.europeana.metis.preview.service.executor.ValidationTask.ValidationTaskResult;
import eu.europeana.metis.preview.service.executor.ValidationTaskFactory;
import eu.europeana.validation.model.ValidationResult;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.annotation.PreDestroy;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.jetbrains.annotations.NotNull;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.JiBXException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * The Preview Service implementation to upload (and potentially transform from EDM-External to EDM-Internal) records
 * Created by ymamakis on 9/2/16.
 */
@Service
public class PreviewService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreviewService.class);
    private static IBindingFactory bfact = null;
    private static ExecutorService executor = null;
    private static ExecutorCompletionService cs = null;

    private PreviewServiceConfig previewServiceConfig;
    private RecordDao dao;
    private ValidationTaskFactory factory;

    /**
     * Constructor for the preview service
     *
     * @param previewServiceConfig The configuration for the preview servide
     */
    @Autowired
    public PreviewService(PreviewServiceConfig previewServiceConfig, RecordDao dao, ValidationTaskFactory factory) {
        this.previewServiceConfig = previewServiceConfig;
        this.dao = dao;
        this.factory = factory;
        if (executor == null) {
            executor = Executors.newFixedThreadPool(previewServiceConfig.getThreadCount());
        }
        if (cs == null) {
            cs = new ExecutorCompletionService(executor);
        }
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
    public ExtendedValidationResult createRecords(List<String> records, String collectionId, boolean applyCrosswalk, String crosswalkPath, boolean individualRecords) throws JiBXException, IllegalAccessException, IOException, InstantiationException, SolrServerException, NoSuchMethodException, InvocationTargetException, TransformerException, ParserConfigurationException, InterruptedException, ExecutionException {
        if (StringUtils.isEmpty(collectionId)) {
            collectionId = CollectionUtils.generateCollectionId();
        }

        dao.deleteCollection(collectionId);
        dao.commit();

        ScheduleValidationTasks(records, collectionId, applyCrosswalk, crosswalkPath, individualRecords);
        ExtendedValidationResult returnList = waitForValidationsToFinishAndRetrieveResults(records.size(), collectionId);

        dao.commit();

        return returnList;
    }

    private ExtendedValidationResult waitForValidationsToFinishAndRetrieveResults(int numberOfSubmittedTasks, String collectionId)
        throws InterruptedException, ExecutionException {

        List<ValidationResult> results = new ArrayList<>();
        ExtendedValidationResult extendedValidationResult =  buildExtendedValidationResult();

        for(int i=0; i<numberOfSubmittedTasks; i++) {
            Future<ValidationTaskResult> future = cs.take();
            ValidationTaskResult result = future.get();

            if (!result.isSuccess()) {
                results.add(result.getValidationResult());
                extendedValidationResult.setSuccess(false);
            }

            if (!Strings.isNullOrEmpty(result.getRecordId())) {
                extendedValidationResult.getRecords().add(result.getRecordId());
            }

            LOGGER.info("Retrieving validation result {}", i);
        }
        extendedValidationResult.setResultList(results);
        extendedValidationResult.setPortalUrl(previewServiceConfig.getPreviewUrl() + collectionId + "*");
        DateTime date = new DateTime();
        Date nextDay = date.toDate();
        extendedValidationResult.setDate(nextDay);

        return extendedValidationResult;
    }

    private void ScheduleValidationTasks(
        List<String> records, String collectionId,  boolean applyCrosswalk, String crosswalkPath, boolean individualRecords) {

        for (int i=0;i<records.size(); i++) {
            ValidationTask task = factory.createValidationTaks(applyCrosswalk, records.get(i), collectionId, crosswalkPath, individualRecords);

            LOGGER.info("Submiting validation of record {}", i);
            cs.submit(task);
        }
    }

    @NotNull
    private ExtendedValidationResult buildExtendedValidationResult() {
        ExtendedValidationResult list = new ExtendedValidationResult();
        List<String> ids = new CopyOnWriteArrayList<>();
        list.setRecords(ids);
        list.setSuccess(true);
        return list;
    }

    /**
     * Delete records at midnight every 24 hrs
     *
     * @throws IOException
     * @throws SolrServerException
     */
    @Scheduled(cron = "00 00 00 * * *")
    public void deleteRecords() throws IOException, SolrServerException {
        dao.deleteRecordIdsByTimestamp();
    }

    @PreDestroy
    public void close()
    {
        if (executor != null)
            executor.shutdown();
    }
}
