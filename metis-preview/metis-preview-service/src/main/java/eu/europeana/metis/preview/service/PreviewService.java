package eu.europeana.metis.preview.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.google.common.base.Strings;
import eu.europeana.metis.preview.common.exception.PreviewServiceException;
import eu.europeana.metis.preview.common.model.ExtendedValidationResult;
import eu.europeana.metis.preview.persistence.RecordDao;
import eu.europeana.metis.preview.service.executor.ValidationTask;
import eu.europeana.metis.preview.service.executor.ValidationTaskFactory;
import eu.europeana.metis.preview.service.executor.ValidationTaskResult;
import eu.europeana.validation.model.ValidationResult;

/**
 * The Preview Service implementation to upload (and potentially transform from EDM-External to EDM-Internal) records
 * Created by ymamakis on 9/2/16.
 */
@Service
public class PreviewService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PreviewService.class);
  private final ExecutorService executor;

  private final String previewUrl;
  private final RecordDao dao;
  private final ValidationTaskFactory factory;

  /**
   * Constructor for the preview service
   *
   * @param previewServiceConfig The configuration for the preview servide
   * @param dao The DAO for records.
   * @param factory The factory for creating validation tasks.
   */
  @Autowired
  public PreviewService(PreviewServiceConfig previewServiceConfig, RecordDao dao,
      ValidationTaskFactory factory) {
    this.previewUrl = previewServiceConfig.getPreviewUrl();
    this.dao = dao;
    this.factory = factory;
    this.executor = Executors.newFixedThreadPool(previewServiceConfig.getThreadCount());
  }


  /**
   * Persist temporarily (24h) records in the preview portal
   *
   * @param records The records to persist as list of XML strings
   * @param collectionId The collection id to apply (can not be null).
   * @param applyCrosswalk Whether the records are in EDM-External (thus need conversion to
   * EDM-Internal)
   * @param crosswalkPath The path of the conversion XSL from EDM-External to EDM-Internal. Can be
   * null, in which case the default will be used.
   * @param individualRecords Whether we need to return the IDs of the individual records.
   * @return The preview URL of the records along with the result of the validation
   * @throws PreviewServiceException an error occured while
   */
  public ExtendedValidationResult createRecords(List<String> records, final String collectionId,
      boolean applyCrosswalk, String crosswalkPath, boolean individualRecords)
      throws PreviewServiceException {

    // Create the tasks
    final Function<String, ValidationTask> validationTaskCreator = record -> factory
        .createValidationTask(applyCrosswalk, record, collectionId, crosswalkPath);
    final List<ValidationTask> tasks = records.stream().map(validationTaskCreator)
        .collect(Collectors.toList());

    // Schedule the tasks.
    final ExecutorCompletionService<ValidationTaskResult> executorCompletionService = new ExecutorCompletionService<>(
        executor);
    final List<Future<ValidationTaskResult>> taskResultFutures = tasks.stream()
        .map(executorCompletionService::submit).collect(Collectors.toList());

    // Wait until the tasks are finished.
    final List<ValidationTaskResult> taskResults = waitForTasksToComplete(taskResultFutures);

    // Commit the changes made by the tasks.
    try {
      dao.commit();
    } catch (IOException | SolrServerException e) {
      LOGGER.error("Updating search engine failed", e);
      throw new PreviewServiceException("Updating search engine failed", e);
    }

    // Done: compile the results.
    return compileResult(taskResults, collectionId, individualRecords);
  }

  private List<ValidationTaskResult> waitForTasksToComplete(
      List<Future<ValidationTaskResult>> taskResultFutures) throws PreviewServiceException {
    final List<ValidationTaskResult> taskResults;
    try {
      int counter = 1;
      taskResults = new ArrayList<>(taskResultFutures.size());
      for (Future<ValidationTaskResult> taskResultFuture : taskResultFutures) {
        LOGGER.info("Retrieving validation result {} of {}.", counter, taskResultFutures.size());
        taskResults.add(taskResultFuture.get());
      }
    } catch (InterruptedException e) {
      LOGGER.error("Processing validations interrupted", e);
      Thread.currentThread().interrupt();
      throw new PreviewServiceException("Processing validations was interrupted", e);
    } catch (ExecutionException e) {
      LOGGER.error("Executing validations failed", e);
      throw new PreviewServiceException("Executing validations failed", e);
    }
    return taskResults;
  }

  private ExtendedValidationResult compileResult(final List<ValidationTaskResult> taskResults,
      String collectionId, boolean includeRecordIds) {

    // Obtain the failed results as list of validation results.
    final List<ValidationResult> failedResults =
        taskResults.stream().filter(result -> !result.isSuccess())
            .map(ValidationTaskResult::getValidationResult).collect(Collectors.toList());

    // Obtain the succeeded results as list of record IDs.
    final List<String> succeededResults;
    if (includeRecordIds) {
      succeededResults = taskResults.stream().filter(ValidationTaskResult::isSuccess)
          .map(ValidationTaskResult::getRecordId).filter(record -> !Strings.isNullOrEmpty(record))
          .collect(Collectors.toList());
    } else {
      succeededResults = null;
    }

    // Compile the validation result object.
    final ExtendedValidationResult extendedValidationResult = new ExtendedValidationResult();
    extendedValidationResult.setResultList(failedResults);
    extendedValidationResult.setSuccess(failedResults.isEmpty());
    extendedValidationResult.setRecords(succeededResults);
    extendedValidationResult.setPortalUrl(this.previewUrl + collectionId + "*");
    extendedValidationResult.setDate(new Date());

    // Done.
    return extendedValidationResult;
  }

  /**
   * Delete records at midnight every 24 hrs
   */
  @Scheduled(cron = "00 00 00 * * *")
  public void deleteRecords() throws IOException, SolrServerException {
    dao.deleteRecordIdsByTimestamp();
  }

  @PreDestroy
  public void close() {
    if (executor != null) {
      executor.shutdown();
    }
  }
}
