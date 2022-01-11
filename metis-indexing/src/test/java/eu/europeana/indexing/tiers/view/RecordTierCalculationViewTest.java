package eu.europeana.indexing.tiers.view;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class RecordTierCalculationViewTest {

  @Test
  void objectCreationTest() {
    final RecordTierCalculationView recordTierCalculationView = new RecordTierCalculationView();

    assertNull(recordTierCalculationView.getRecordTierCalculationSummary());
    assertNull(recordTierCalculationView.getContentTierBreakdown());
    assertNull(recordTierCalculationView.getMetadataTierBreakdown());

    recordTierCalculationView.setRecordTierCalculationSummary(new RecordTierCalculationSummary());
    recordTierCalculationView.setContentTierBreakdown(new ContentTierBreakdown());
    recordTierCalculationView.setMetadataTierBreakdown(new MetadataTierBreakdown());

    assertNotNull(recordTierCalculationView.getRecordTierCalculationSummary());
    assertNotNull(recordTierCalculationView.getContentTierBreakdown());
    assertNotNull(recordTierCalculationView.getMetadataTierBreakdown());
  }

}