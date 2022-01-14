package eu.europeana.indexing.tiers.view;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class RecordTierCalculationViewTest {

  @Test
  void objectCreationTest() {
    final RecordTierCalculationView recordTierCalculationView = new RecordTierCalculationView(new RecordTierCalculationSummary(),
        new ContentTierBreakdown(), new MetadataTierBreakdown());
    recordTierCalculationView.setContentTierBreakdown(new ContentTierBreakdown());

    assertNotNull(recordTierCalculationView.getRecordTierCalculationSummary());
    assertNotNull(recordTierCalculationView.getContentTierBreakdown());
    assertNotNull(recordTierCalculationView.getMetadataTierBreakdown());
  }

}