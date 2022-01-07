package eu.europeana.indexing.tiers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import eu.europeana.indexing.exception.TierCalculationException;
import eu.europeana.indexing.tiers.view.RecordTierCalculationSummary;
import eu.europeana.indexing.tiers.view.RecordTierCalculationView;
import eu.europeana.indexing.utils.SolrTier;
import eu.europeana.indexing.utils.TestUtils;
import java.io.IOException;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

//These tests can only run with maven and not from the IDE individually because of the Jibx binding limitation
class RecordTierCalculationViewGeneratorTest {

  @Test
  void generate() throws IOException {
    String europeanaRecordString = TestUtils.readFileToString(
        Paths.get("europeana_record_with_technical_metadata.xml").toFile().toString());
    final String europeanaId = "europeanaId";
    final String providerId = "providerId";
    final String contentTier = SolrTier.CONTENT_TIER_2.getTierValue();
    final String metadataTier = SolrTier.METADATA_TIER_B.getTierValue();
    final String providerRecordLink = "https://example-record-link.com";
    final String portalRecordLink = "https://example-portal-record-link.com";
    final RecordTierCalculationViewGenerator recordTierCalculationViewGenerator = new RecordTierCalculationViewGenerator(
        europeanaId, providerId, europeanaRecordString, portalRecordLink, providerRecordLink);

    final RecordTierCalculationView recordTierCalculationView = recordTierCalculationViewGenerator.generate();
    final RecordTierCalculationSummary recordTierCalculationSummary = recordTierCalculationView.getRecordTierCalculationSummary();
    assertEquals(europeanaId, recordTierCalculationSummary.getEuropeanaRecordId());
    assertEquals(providerId, recordTierCalculationSummary.getProviderRecordId());
    assertEquals(contentTier, recordTierCalculationSummary.getContentTier());
    assertEquals(metadataTier, recordTierCalculationSummary.getMetadataTier());
    assertEquals(portalRecordLink, recordTierCalculationSummary.getPortalRecordLink());
    assertEquals(providerRecordLink, recordTierCalculationSummary.getHarvestedRecordLink());

    // TODO: 06/01/2022 Further checkups to add here when the rest of implementation is completed
  }

  @Test
  void generateThrowsTierCalculationException() {
    final RecordTierCalculationViewGenerator recordTierCalculationViewGenerator = Mockito.spy(
        new RecordTierCalculationViewGenerator("", "", "", "", ""));
    assertThrows(TierCalculationException.class, recordTierCalculationViewGenerator::generate);
  }
}