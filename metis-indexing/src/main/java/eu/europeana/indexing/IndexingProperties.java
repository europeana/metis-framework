package eu.europeana.indexing;

import eu.europeana.indexing.tiers.TierCalculationMode;
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
  private final TierCalculationMode tierCalculationMode;
  private final EnumSet<EdmType> typesEnabledForTierCalculation;

  /**
   * Instantiates a new Indexing properties.
   *
   * @param recordDate the record date
   * @param preserveUpdateAndCreateTimesFromRdf This regulates whether we should preserve (use) the
   * updated and created dates that are set in the input record or if they should be recomputed
   * using any equivalent record that is currently in the database.
   * @param datasetIdsForRedirection the dataset ids for redirection
   * @param performRedirects the perform redirects
   * @param tierCalculationMode the tier calculation mode
   */
  public IndexingProperties(Date recordDate, boolean preserveUpdateAndCreateTimesFromRdf, List<String> datasetIdsForRedirection,
      boolean performRedirects, TierCalculationMode tierCalculationMode) {
    this(recordDate, preserveUpdateAndCreateTimesFromRdf, datasetIdsForRedirection, performRedirects, tierCalculationMode,
        EnumSet.allOf(EdmType.class));
  }

  /**
   * Instantiates a new Indexing properties.
   *
   * @param recordDate the record date
   * @param preserveUpdateAndCreateTimesFromRdf This regulates whether we should preserve (use) the
   * updated and created dates that are set in the input record or if they should be recomputed
   * using any equivalent record that is currently in the database.
   * @param datasetIdsForRedirection the dataset ids for redirection
   * @param performRedirects the perform redirects
   * @param tierCalculationMode the tier calculation mode
   * @param typesEnabledForTierCalculation the types enabled for tier calculation
   */
  public IndexingProperties(Date recordDate, boolean preserveUpdateAndCreateTimesFromRdf, List<String> datasetIdsForRedirection,
      boolean performRedirects, TierCalculationMode tierCalculationMode, Set<EdmType> typesEnabledForTierCalculation) {
    this.recordDate = recordDate == null ? null : new Date(recordDate.getTime());
    this.preserveUpdateAndCreateTimesFromRdf = preserveUpdateAndCreateTimesFromRdf;
    this.datasetIdsForRedirection = Optional.ofNullable(datasetIdsForRedirection)
                                            .<List<String>>map(ArrayList::new).orElseGet(Collections::emptyList);
    this.performRedirects = performRedirects;
    this.typesEnabledForTierCalculation = EnumSet.copyOf(typesEnabledForTierCalculation);
    this.tierCalculationMode = tierCalculationMode;
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
   * @return the tier calculation mode
   */
  public TierCalculationMode getTierCalculationMode() {
    return tierCalculationMode;
  }

  /**
   * @return the edm types for tier re-calculation if enabled
   */
  public Set<EdmType> getTypesEnabledForTierCalculation() {
    return Collections.unmodifiableSet(typesEnabledForTierCalculation);
  }
}
