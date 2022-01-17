package eu.europeana.indexing.tiers.view;

/**
 * Class containing a view of a tier calculation.
 */
public class RecordTierCalculationView {

  private final RecordTierCalculationSummary recordTierCalculationSummary;
  private final MetadataTierBreakdown metadataTierBreakdown;
  // TODO: 07/01/2022 Add test on the sub-trees for the below two fields with the upcoming tickets MET-4157 and MET-4158
  // TODO: 14/01/2022 Make this final when implemented
  private ContentTierBreakdown contentTierBreakdown;

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

  public void setContentTierBreakdown(ContentTierBreakdown contentTierBreakdown) {
    this.contentTierBreakdown = contentTierBreakdown;
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

