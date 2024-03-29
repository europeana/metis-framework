package eu.europeana.patternanalysis;

import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.patternanalysis.exception.PatternAnalysisException;
import eu.europeana.patternanalysis.view.DatasetProblemPatternAnalysis;
import eu.europeana.patternanalysis.view.ProblemPattern;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interface with all methods required for a pattern analysis service
 *
 * @param <T> the type of the execution step
 * @param <K> the type of the execution point
 */
public interface PatternAnalysisService<T, K> {

  /**
   * Initializes the pattern analysis execution to create a unique execution point.
   * <p>This method should be called at the beginning(pre-processing) of the dataset execution once</p>
   *
   * @param datasetId the datasetId
   * @param executionStep the constant value of the step (Similar to eu.europeana.metis.core.workflow.plugins.PluginType from
   * metis-core and eu.europeana.metis.sandbox.common.Step from metis-sandbox
   * @param executionTimestamp the execution timestamp for the execution of the dataset(this should be the same for all records).
   * @return the execution point that can be used on other calls
   */
  K initializePatternAnalysisExecution(String datasetId, T executionStep, LocalDateTime executionTimestamp);

  /**
   * Generates the analysis of the record in RDF format.
   * <p>
   * It will compute patterns and store all relevant information in the database
   * </p>
   *
   * @param executionPoint the execution point
   * @param rdfRecord the rdf record
   * @throws PatternAnalysisException if an error occurred during the analysis
   */
  void generateRecordPatternAnalysis(K executionPoint, RDF rdfRecord)
      throws PatternAnalysisException;

  /**
   * Generates the analysis of the record in String format.
   * <p>
   * It will compute patterns and store all relevant information in the database
   * </p>
   *
   * @param executionPoint the execution point
   * @param rdfRecord the rdf record
   * @throws PatternAnalysisException if an error occurred during the analysis
   */
  void generateRecordPatternAnalysis(K executionPoint, String rdfRecord)
      throws PatternAnalysisException;

  /**
   * Finalizes the computation of the analysis for the dataset.
   * <p>This method should be called at the end(post-processing) of the dataset execution, to perform the final calculations</p>
   *
   * @param executionPoint the execution point
   * @throws PatternAnalysisException if an error occurred during the analysis
   */
  void finalizeDatasetPatternAnalysis(K executionPoint)
      throws PatternAnalysisException;

  /**
   * Get the Dataset pattern analysis for a specific execution.
   * <p>
   * This method will generate the dataset pattern analysis for a dataset and a specific execution from the data in the database.
   * An in memory cache could be implemented internally.
   * </p>
   *
   * @param datasetId the dataset identifier
   * @param executionStep the execution step
   * @param executionTimestamp the execution timestamp
   * @return the dataset pattern analysis
   */
  Optional<DatasetProblemPatternAnalysis<T>> getDatasetPatternAnalysis(String datasetId, T executionStep,
      LocalDateTime executionTimestamp);

  /**
   * Get a list of problem patterns for a particular record without storing them in the database.
   * <p>Internally this method could check first if the analysis is present in the database and retrieve that.
   * If not, it should generate it on the fly. An in memory cache could be implemented internally.
   * </p>
   *
   * @param rdfRecord the RDF record
   * @return the list of problem patterns
   */
  List<ProblemPattern> getRecordPatternAnalysis(RDF rdfRecord);
}
