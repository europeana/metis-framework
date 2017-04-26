/*
 * Copyright 2005-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.europeana.enrichment.tagger.postprocessors;

import org.apache.commons.lang.StringUtils;

import eu.europeana.enrichment.api.internal.Term;
import eu.europeana.enrichment.api.internal.TermList;
import eu.europeana.enrichment.tagger.vocabularies.DisambiguationContext;

/**
 * Hierarchical disambiguation logic.
 * 
 * @author Borys Omelayenko
 * 
 */
public class PopulationTermFilter extends TermFilter {

	private static final String VOCAB_ATTR_POPULATION = "population";

	@Override
	public TermList disambiguate(TermList allTerms,
			DisambiguationContext disambiguationContext) throws Exception {

		// disambiguation not needed
		if (allTerms.size() < 2)
			return allTerms;

		Term largestPlace = null;
		long largestPopulation = 0;

		// disambiguate
		TermList selectedTerms = new TermList();
		for (Term term : allTerms) {

			if (largestPlace == null) {
				largestPlace = term;
			}

			String populationString = term.getProperty(VOCAB_ATTR_POPULATION);

			if (populationString != null && !populationString.isEmpty()
					&& StringUtils.isNumeric(populationString)) {
				try {
					long population = Integer.parseInt(populationString);
					if (population > largestPopulation) {
						largestPopulation = population;
						largestPlace = term;
					}
				} finally {
					// just ignore
				}
			}
		}

		// choose the place with the largest population
		if (largestPopulation > 0) {
			selectedTerms = new TermList();
			selectedTerms.add(largestPlace);
			return selectedTerms;
		}

		return allTerms;
	}

}
