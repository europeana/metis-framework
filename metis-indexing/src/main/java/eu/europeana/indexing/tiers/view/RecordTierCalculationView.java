package eu.europeana.indexing.tiers.view;

/**
 * Class containing a view of a tier calculation.
 */
public class RecordTierCalculationView {

  private final RecordTierCalculationSummary recordTierCalculationSummary;
  private final MetadataTierBreakdown metadataTierBreakdown;
  private final ContentTierBreakdown contentTierBreakdown;

  /**
   * Constructor with required parameters.
   *
   * @param recordTierCalculationSummary the record tier calculation summary
   * @param contentTierBreakdown the content tier breakdown
   * @param metadataTierBreakdown the metadata tier breakdown
   */
  public RecordTierCalculationView(RecordTierCalculationSummary recordTierCalculationSummary,
      ContentTierBreakdown contentTierBreakdown, MetadataTierBreakdown metadataTierBreakdown) {
    this.recordTierCalculationSummary = recordTierCalculationSummary;
    this.contentTierBreakdown = contentTierBreakdown;
    this.metadataTierBreakdown = metadataTierBreakdown;
  }

  public RecordTierCalculationSummary getRecordTierCalculationSummary() {
    return recordTierCalculationSummary;
  }

  public ContentTierBreakdown getContentTierBreakdown() {
    return contentTierBreakdown;
  }

  public MetadataTierBreakdown getMetadataTierBreakdown() {
    return metadataTierBreakdown;
  }
}

