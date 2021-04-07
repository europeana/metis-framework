package eu.europeana.indexing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * This class contains all properties that affect the behavior of the indexing functionality, it is
 * the input class for all indexing methods in {@link Indexer} (and {@link IndexerPool}).
 */
public class IndexingProperties {

  private final Date recordDate;
  private final boolean preserveUpdateAndCreateTimesFromRdf;
  private final List<String> datasetIdsForRedirection;
  private final boolean performRedirects;
  private final boolean performTierCalculation;

  /**
   * Constructor.
   *
   * @param recordDate The date that would represent the created/updated date of a record.
   * @param preserveUpdateAndCreateTimesFromRdf This determines whether this indexer should use the
   * updated and created times from the incoming RDFs, or whether it computes its own.
   * @param datasetIdsForRedirection The dataset ids that their records need to be redirected.
   * @param performRedirects flag that indicates whether redirect should be performed.
   * @param performTierCalculation flag that indicates whether tier calculation should be
   * performed.
   */
  public IndexingProperties(Date recordDate, boolean preserveUpdateAndCreateTimesFromRdf,
          List<String> datasetIdsForRedirection, boolean performRedirects,
          boolean performTierCalculation) {
    this.recordDate = recordDate;
    this.preserveUpdateAndCreateTimesFromRdf = preserveUpdateAndCreateTimesFromRdf;
    this.datasetIdsForRedirection = Optional.ofNullable(datasetIdsForRedirection)
            .<List<String>>map(ArrayList::new).orElse(Collections.emptyList());
    this.performRedirects = performRedirects;
    this.performTierCalculation = performTierCalculation;
  }

  /**
   * @return The date that would represent the created/updated date of a record.
   */
  public Date getRecordDate() {
    return recordDate;
  }

  /**
   * @return Whether this indexer should use the updated and created times from the incoming RDFs,
   * or whether it computes its own.
   */
  public boolean isPreserveUpdateAndCreateTimesFromRdf() {
    return preserveUpdateAndCreateTimesFromRdf;
  }

  /**
   * @return The dataset ids that their records need to be redirected.
   */
  public List<String> getDatasetIdsForRedirection() {
    return Collections.unmodifiableList(datasetIdsForRedirection);
  }

  /**
   * @return Whether redirect should be performed.
   */
  public boolean isPerformRedirects() {
    return performRedirects;
  }

  /**
   * @return Whether tier calculation should be performed.
   */
  public boolean isPerformTierCalculation() {
    return performTierCalculation;
  }
}
