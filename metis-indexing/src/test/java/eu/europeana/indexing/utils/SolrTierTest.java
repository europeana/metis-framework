package eu.europeana.indexing.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import eu.europeana.indexing.solr.EdmLabel;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class SolrTierTest {

  @Test
  void checkValues() {
    final long metadataTiers = Arrays.stream(SolrTier.values())
        .filter(solrTier -> solrTier.getTierLabel().equals(EdmLabel.METADATA_TIER))
        .count();
    final long contentTiers = Arrays.stream(SolrTier.values())
        .filter(solrTier -> solrTier.getTierLabel().equals(EdmLabel.CONTENT_TIER))
        .count();
    assertEquals(4, metadataTiers);
    assertEquals(5, contentTiers);

    final SolrTier metadataTier0 = SolrTier.METADATA_TIER_0;
    assertEquals(RdfTier.METADATA_TIER_0, metadataTier0.getTier());
    assertEquals("0", metadataTier0.getTierValue());

    final SolrTier metadataTierA = SolrTier.METADATA_TIER_A;
    assertEquals(RdfTier.METADATA_TIER_A, metadataTierA.getTier());
    assertEquals("A", metadataTierA.getTierValue());

    final SolrTier metadataTierB = SolrTier.METADATA_TIER_B;
    assertEquals(RdfTier.METADATA_TIER_B, metadataTierB.getTier());
    assertEquals("B", metadataTierB.getTierValue());

    final SolrTier metadataTierC = SolrTier.METADATA_TIER_C;
    assertEquals(RdfTier.METADATA_TIER_C, metadataTierC.getTier());
    assertEquals("C", metadataTierC.getTierValue());

    final SolrTier contentTier0 = SolrTier.CONTENT_TIER_0;
    assertEquals(RdfTier.CONTENT_TIER_0, contentTier0.getTier());
    assertEquals("0", contentTier0.getTierValue());

    final SolrTier contentTier1 = SolrTier.CONTENT_TIER_1;
    assertEquals(RdfTier.CONTENT_TIER_1, contentTier1.getTier());
    assertEquals("1", contentTier1.getTierValue());

    final SolrTier contentTier2 = SolrTier.CONTENT_TIER_2;
    assertEquals(RdfTier.CONTENT_TIER_2, contentTier2.getTier());
    assertEquals("2", contentTier2.getTierValue());

    final SolrTier contentTier3 = SolrTier.CONTENT_TIER_3;
    assertEquals(RdfTier.CONTENT_TIER_3, contentTier3.getTier());
    assertEquals("3", contentTier3.getTierValue());

    final SolrTier contentTier4 = SolrTier.CONTENT_TIER_4;
    assertEquals(RdfTier.CONTENT_TIER_4, contentTier4.getTier());
    assertEquals("4", contentTier4.getTierValue());

  }

}