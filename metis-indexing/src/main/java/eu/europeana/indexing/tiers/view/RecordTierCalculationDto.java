package eu.europeana.indexing.tiers.view;

public class RecordTierCalculationDto {

  private RecordTierCalculationSummary recordTierCalculationSummary;
  private ContentTierBreakdown contentTierBreakdown;
  private MetadataTierBreakdown metadataTierBreakdown;

  public RecordTierCalculationDto() {
  }

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

