package eu.europeana.indexing.tiers.view;

import static org.junit.jupiter.api.Assertions.*;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.tiers.model.Tier;
import org.junit.jupiter.api.Test;

class RecordTierCalculationSummaryTest {

  @Test
  void objectCreationTest(){
    final RecordTierCalculationSummary recordTierCalculationSummary = new RecordTierCalculationSummary();

    assertNull(recordTierCalculationSummary.getEuropeanaRecordId());
    assertNull(recordTierCalculationSummary.getProviderRecordId());
    assertNull(recordTierCalculationSummary.getContentTier());
    assertNull(recordTierCalculationSummary.getMetadataTier());
    assertNull(recordTierCalculationSummary.getPortalRecordLink());
    assertNull(recordTierCalculationSummary.getHarvestedRecordLink());

    final String europeanaId = "europeanaId";
    final String providerId = "providerId";
    final Tier contentTier = MediaTier.T4;
    final Tier metadataTier = MetadataTier.TA;
    final String portalRecordLink = "https://example-portal-record-link.org";
    final String harvestedRecordLink = "https://example-harvest-record-link.org";
    recordTierCalculationSummary.setEuropeanaRecordId(europeanaId);
    recordTierCalculationSummary.setProviderRecordId(providerId);
    recordTierCalculationSummary.setContentTier(contentTier);
    recordTierCalculationSummary.setMetadataTier(metadataTier);
    recordTierCalculationSummary.setPortalRecordLink(portalRecordLink);
    recordTierCalculationSummary.setHarvestedRecordLink(harvestedRecordLink);

    assertEquals(europeanaId, recordTierCalculationSummary.getEuropeanaRecordId());
    assertEquals(providerId, recordTierCalculationSummary.getProviderRecordId());
    assertEquals(contentTier, recordTierCalculationSummary.getContentTier());
    assertEquals(metadataTier, recordTierCalculationSummary.getMetadataTier());
    assertEquals(portalRecordLink, recordTierCalculationSummary.getPortalRecordLink());
    assertEquals(harvestedRecordLink, recordTierCalculationSummary.getHarvestedRecordLink());
  }
}
