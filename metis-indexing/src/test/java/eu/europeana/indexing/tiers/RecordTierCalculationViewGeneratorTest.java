package eu.europeana.indexing.tiers;

import eu.europeana.indexing.tiers.view.RecordTierCalculationSummary;
import eu.europeana.indexing.tiers.view.RecordTierCalculationView;
import eu.europeana.indexing.utils.TestUtils;
import java.io.IOException;
import java.nio.file.Paths;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

//These tests can only run with maven and not from the IDE individually because of the Jibx binding limitation
class RecordTierCalculationViewGeneratorTest {

  @Test
  void generate() throws IOException {
    String europeanaRecordString = TestUtils.readFileToString(Paths.get("europeana_record_with_technical_metadata.xml").toFile().toString());
    final String europeanaId = "europeanaId";
    final String providerId = "providerId";
    final String providerRecordLink = "https://example-record-link.com";
    final String portalRecordLink = "https://example-portal-record-link.com";
    final RecordTierCalculationViewGenerator recordTierCalculationViewGenerator = new RecordTierCalculationViewGenerator(
        europeanaId, providerId, europeanaRecordString, portalRecordLink, providerRecordLink);

    final RecordTierCalculationView recordTierCalculationView = recordTierCalculationViewGenerator.generate();
    final RecordTierCalculationSummary recordTierCalculationSummary = recordTierCalculationView.getRecordTierCalculationSummary();
    Assertions.assertEquals("2", recordTierCalculationSummary.getContentTier());
    Assertions.assertEquals("B", recordTierCalculationSummary.getMetadataTier());
    Assertions.assertEquals(europeanaId, recordTierCalculationSummary.getEuropeanaRecordId());
    Assertions.assertEquals(providerId, recordTierCalculationSummary.getProviderRecordId());
    Assertions.assertEquals(portalRecordLink, recordTierCalculationSummary.getPortalRecordLink());
    Assertions.assertEquals(providerRecordLink, recordTierCalculationSummary.getHarvestedRecordLink());

    // TODO: 06/01/2022 Further checkups to add here when the rest of implementation is completed
  }
}