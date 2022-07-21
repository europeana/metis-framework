package eu.europeana.normalization.dates.extraction;

import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.DatesNormalizer;
import org.junit.jupiter.api.Test;

public class DebugEDTFDateNormalizerPart {

	@Test
	void extractorsTest() throws Exception {
		DatesNormalizer normaliser = new DatesNormalizer();
		DateNormalizationResult dateNormalizationResult = null;
		//		match = normaliser.normalise("168 B.C.-135 A.D.");
		dateNormalizationResult = normaliser.normalizeDateProperty("?/1807");
		System.out.println(dateNormalizationResult.getOriginalInput());
		System.out.println(dateNormalizationResult.getMatchId());
		System.out.println(dateNormalizationResult.getNormalizedEdtfDateWithLabel().getEdtfDate().serialize());
	}

}
