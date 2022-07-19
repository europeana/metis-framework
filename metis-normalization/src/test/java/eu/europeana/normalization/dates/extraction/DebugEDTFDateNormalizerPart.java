package eu.europeana.normalization.dates.extraction;

import eu.europeana.normalization.dates.DatesNormaliser;
import eu.europeana.normalization.dates.Match;
import org.junit.jupiter.api.Test;

public class DebugEDTFDateNormalizerPart {

	@Test
	void extractorsTest() throws Exception {
		DatesNormaliser normaliser = new DatesNormaliser();
		Match match = null;
		//		match = normaliser.normalise("168 B.C.-135 A.D.");
		match = normaliser.normaliseDateProperty("?/1807");
		System.out.println(match.getInput());
		System.out.println(match.getMatchId());
		System.out.println(match.getExtracted().getEdtf().serialize());
	}

}
