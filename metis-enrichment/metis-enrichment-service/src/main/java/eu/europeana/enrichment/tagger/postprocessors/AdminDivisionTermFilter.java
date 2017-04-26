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

import eu.europeana.enrichment.api.internal.Term;
import eu.europeana.enrichment.api.internal.TermList;
import eu.europeana.enrichment.tagger.vocabularies.DisambiguationContext;

/**
 * Hierarchical disambiguation by administrative division; many places are named
 * the same and nested into each other: cities into regions, etc.
 * 
 * @author Borys Omelayenko
 * 
 */
public class AdminDivisionTermFilter extends TermFilter {

	private static final String VOCAB_ATTR_DIVISION = "division";
	private static final String VOCAB_ATTR_COUNTRY = "country";

	@Override
	public TermList disambiguate(TermList allTerms,
			DisambiguationContext disambiguationContext) throws Exception {

		// disambiguation not needed
		if (allTerms.size() < 2)
			return allTerms;

		if (allTerms.isSameLabels()) {

			TermList selectedTerms = new TermList();
			Term largestPlace = null;
			String largestAdminDivision = null;

			// disambiguate
			for (Term term : allTerms) {

				if (largestPlace == null) {
					largestPlace = term;
				}

				String adminDivision = term.getProperty(VOCAB_ATTR_DIVISION);
				String countryTerm = term.getProperty(VOCAB_ATTR_COUNTRY);
				String countryLargest = largestPlace
						.getProperty(VOCAB_ATTR_COUNTRY);
				if (countryTerm == null) {
					return allTerms;
				}

				// same name, same country
				if (countryTerm.equals(countryLargest)) {

					// should all have admin division
					if (adminDivision == null || adminDivision.isEmpty()) {
						return allTerms;
					}

					if (largestAdminDivision == null
							|| adminDivision
									.compareToIgnoreCase(largestAdminDivision) > 0) {
						largestAdminDivision = adminDivision;
						largestPlace = term;
					}
				}
			}

			// choose the place with the largest population
			if (largestAdminDivision != null && largestPlace != null) {
				selectedTerms = new TermList();
				selectedTerms.add(largestPlace);
				return selectedTerms;
			}
		}
		return allTerms;
	}
}