package eu.europeana.indexing.tiers.view;

/**
 * Class containing a view of a tier calculation.
 */
public class RecordTierCalculationView {

  private RecordTierCalculationSummary recordTierCalculationSummary;
  // TODO: 07/01/2022 Add test on the sub-trees for the below two fields with the upcoming tickets MET-4157 and MET-4158
  private ContentTierBreakdown contentTierBreakdown;
  private MetadataTierBreakdown metadataTierBreakdown;

  public RecordTierCalculationSummary getRecordTierCalculationSummary() {
    return recordTierCalculationSummary;
  }

  public void setRecordTierCalculationSummary(
      RecordTierCalculationSummary recordTierCalculationSummary) {
    this.recordTierCalculationSummary = recordTierCalculationSummary;
  }

  public ContentTierBreakdown getContentTierBreakdown() {
    return contentTierBreakdown;
  }

  public void setContentTierBreakdown(ContentTierBreakdown contentTierBreakdown) {
    this.contentTierBreakdown = contentTierBreakdown;
  }

  public MetadataTierBreakdown getMetadataTierBreakdown() {
    return metadataTierBreakdown;
  }

  public void setMetadataTierBreakdown(MetadataTierBreakdown metadataTierBreakdown) {
    this.metadataTierBreakdown = metadataTierBreakdown;
  }
}

