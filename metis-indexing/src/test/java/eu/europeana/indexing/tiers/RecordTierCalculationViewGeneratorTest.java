package eu.europeana.indexing.tiers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import eu.europeana.indexing.exception.TierCalculationException;
import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.tiers.model.Tier;
import eu.europeana.indexing.tiers.view.ProcessingError;
import eu.europeana.indexing.tiers.view.RecordTierCalculationSummary;
import eu.europeana.indexing.tiers.view.RecordTierCalculationView;
import eu.europeana.indexing.utils.TestUtils;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RecordTierCalculationViewGeneratorTest {

  @Test
  void generate() throws IOException {
    String europeanaRecordString = TestUtils.readFileToString(
        Paths.get("europeana_record_with_technical_metadata.xml").toFile().toString());
    final String europeanaId = "europeanaId";
    final String providerId = "providerId";
    final Tier contentTier = MediaTier.T2;
    final Tier metadataTier = MetadataTier.TB;
    final String portalRecordLink = "https://example-portal-record-link.com";
    final ProcessingError processingError1 = new ProcessingError("Error1", "Stacktrace1");
    final ProcessingError processingError2 = new ProcessingError("Error2", "Stacktrace2");

    final RecordTierCalculationViewGenerator recordTierCalculationViewGenerator =
        new RecordTierCalculationViewGenerator(europeanaId, providerId, europeanaRecordString, portalRecordLink,
            List.of(processingError1, processingError2));

    final RecordTierCalculationView recordTierCalculationView = recordTierCalculationViewGenerator.generate();
    final RecordTierCalculationSummary recordTierCalculationSummary = recordTierCalculationView.getRecordTierCalculationSummary();
    assertEquals(europeanaId, recordTierCalculationSummary.getEuropeanaRecordId());
    assertEquals(providerId, recordTierCalculationSummary.getProviderRecordId());
    assertEquals(contentTier, recordTierCalculationSummary.getContentTier());
    assertEquals(metadataTier, recordTierCalculationSummary.getMetadataTier());
    assertEquals(portalRecordLink, recordTierCalculationSummary.getPortalRecordLink());
  }

  @Test
  void generateThrowsTierCalculationException() {
    final RecordTierCalculationViewGenerator recordTierCalculationViewGenerator = Mockito.spy(
        new RecordTierCalculationViewGenerator("", "", "", "", null));
    assertThrows(TierCalculationException.class, recordTierCalculationViewGenerator::generate);
  }
}
