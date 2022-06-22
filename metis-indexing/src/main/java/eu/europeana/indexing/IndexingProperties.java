package eu.europeana.indexing;

import eu.europeana.metis.schema.jibx.EdmType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This class contains all properties that affect the behavior of the indexing functionality, it is the input class for all
 * indexing methods in {@link Indexer} (and {@link IndexerPool}).
 */
public class IndexingProperties {

  private final Date recordDate;
  private final boolean preserveUpdateAndCreateTimesFromRdf;
  private final List<String> datasetIdsForRedirection;
  private final boolean performRedirects;
  private final boolean performTierCalculation;
  private final EnumSet<EdmType> typesEnabledForTierCalculation;

  /**
   * Constructor.
   *
   * @param recordDate The date that would represent the created/updated date of a record. Can be null.
   * @param preserveUpdateAndCreateTimesFromRdf This determines whether this indexer should use the updated and created times from
   * the incoming RDFs, or whether it computes its own.
   * @param datasetIdsForRedirection The dataset ids that their records need to be redirected. Can be null.
   * @param performRedirects flag that indicates whether redirect should be performed.
   * @param performTierCalculation flag that indicates whether tier calculation should be performed.
   */
  public IndexingProperties(Date recordDate, boolean preserveUpdateAndCreateTimesFromRdf, List<String> datasetIdsForRedirection,
      boolean performRedirects, boolean performTierCalculation) {
    this(recordDate, preserveUpdateAndCreateTimesFromRdf, datasetIdsForRedirection, performRedirects, performTierCalculation,
        EnumSet.allOf(EdmType.class));
  }

  /**
   * Constructor allowing specific types for tier re-calculation.
   *
   * @param recordDate The date that would represent the created/updated date of a record. Can be null.
   * @param preserveUpdateAndCreateTimesFromRdf This determines whether this indexer should use the updated and created times from
   * the incoming RDFs, or whether it computes its own.
   * @param datasetIdsForRedirection The dataset ids that their records need to be redirected. Can be null.
   * @param performRedirects flag that indicates whether redirect should be performed.
   * @param performTierCalculation flag that indicates whether tier calculation should be performed.
   * @param typesEnabledForTierCalculation the types enabled for tier calculation if enabled.
   */
  public IndexingProperties(Date recordDate, boolean preserveUpdateAndCreateTimesFromRdf,
      List<String> datasetIdsForRedirection, boolean performRedirects,
      boolean performTierCalculation, Set<EdmType> typesEnabledForTierCalculation) {
    this.recordDate = recordDate == null ? null : new Date(recordDate.getTime());
    this.preserveUpdateAndCreateTimesFromRdf = preserveUpdateAndCreateTimesFromRdf;
    this.datasetIdsForRedirection = Optional.ofNullable(datasetIdsForRedirection)
                                            .<List<String>>map(ArrayList::new).orElseGet(Collections::emptyList);
    this.performRedirects = performRedirects;
    this.performTierCalculation = performTierCalculation;
    this.typesEnabledForTierCalculation = EnumSet.copyOf(typesEnabledForTierCalculation);
  }

  /**
   * @return The date that would represent the created/updated date of a record. Can be null.
   */
  public Date getRecordDate() {
    return recordDate == null ? null : new Date(recordDate.getTime());
  }

  /**
   * @return Whether this indexer should use the updated and created times from the incoming RDFs, or whether it computes its own.
   */
  public boolean isPreserveUpdateAndCreateTimesFromRdf() {
    return preserveUpdateAndCreateTimesFromRdf;
  }

  /**
   * @return The dataset ids that their records need to be redirected. Is not null.
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

  /**
   * @return the edm types for tier re-calculation if enabled
   */
  public Set<EdmType> getTypesEnabledForTierCalculation() {
    return Collections.unmodifiableSet(typesEnabledForTierCalculation);
  }
}
