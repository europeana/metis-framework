/*
 * Copyright 2005-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.europeana.enrichment.tagger.postprocessors;

import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.math.NumberUtils;

import eu.europeana.enrichment.api.internal.Term;
import eu.europeana.enrichment.api.internal.TermList;
import eu.europeana.enrichment.tagger.vocabularies.DisambiguationContext;

/**
 * Disambiguating people.
 * 
 * @author Borys Omelayenko
 * 
 */
public abstract class PeopleTermFilter extends HierarchicalTermFilter {

	public static class UlanDisambiguationContext extends DisambiguationContext {

		String birthDate;
		String deathDate;
		boolean isLiveDate;

		public UlanDisambiguationContext(String birthDate, String deathDate,
				boolean isLiveDate) {
			super(null, null, null);
			this.birthDate = birthDate;
			this.deathDate = deathDate;
			this.isLiveDate = isLiveDate;
		}

	}

	// now
	Calendar now = new GregorianCalendar();

	@Override
	public TermList disambiguate(TermList terms,
			DisambiguationContext disambiguationContext) throws Exception {

		// disambiguation not needed
		if (terms.size() < 2)
			return terms;

		TermList results = new TermList();

		if (!(disambiguationContext instanceof UlanDisambiguationContext))
			throw new Exception(
					"Expected ULAN-specific disambiguation context. Apperantly, Ulan.disambiguate was called outside Ulan lookupPerson.");

		UlanDisambiguationContext udc = (UlanDisambiguationContext) disambiguationContext;

		for (Term term : terms) {
			// ulan years
			Set<Integer> ulanBirthYears = years("birth", term);
			Set<Integer> ulanDeathYears = years("death", term);
			// 100 years was added to ULAN when no death date was there
			if (!ulanBirthYears.isEmpty()
					&& !ulanDeathYears.isEmpty()
					&& ulanBirthYears.iterator().next() + 100 == ulanDeathYears
							.iterator().next()) {
				ulanDeathYears.clear();
			}
			// request years
			int reqBirthYear = 0;
			int reqDeathYear = 0;

			if (udc.birthDate != null) {
				if (udc.birthDate.matches("^\\d\\d\\d\\d(\\-(.+))?"))
					reqBirthYear = Integer.parseInt(udc.birthDate.substring(0,
							4));
				if (reqBirthYear >= now.get(Calendar.YEAR)) {
					reqBirthYear = 0;
				}
			}
			if (udc.deathDate != null) {
				if (udc.deathDate.matches("^\\d\\d\\d\\d(\\-(.+))?"))
					reqDeathYear = Integer.parseInt(udc.deathDate.substring(0,
							4));
				if (reqDeathYear >= now.get(Calendar.YEAR)) {
					reqDeathYear = 0;
				}
			}

			// comparing the ulan dates with the requested dates
			if (udc.isLiveDate) {
				if (checkDates(ulanBirthYears, ulanDeathYears, reqBirthYear,
						reqDeathYear, 3, true))
				// reqBirthYear should be a year of ULAN life
				// if (reqBirthYear >= Collections.min(ulanBirthYears)
				// && (ulanDeathYears.isEmpty() || reqBirthYear <=
				// Collections.max(ulanDeathYears)))
				{
					results.add(term);
					term.setDisambiguatingComment(String
							.format("requested year of life %d matched ULAN lifetime (%s-%s)",
									reqBirthYear, toString(ulanBirthYears),
									toString(ulanDeathYears)));
				}
			} else {
				// requested years should match ULAN years
				if (reqBirthYear == 0 && reqDeathYear == 0) {
					// no years
					results.add(term);
					term.setDisambiguatingComment("Matching name only, no life dates provided");
				} else {
					// check years
					if (checkDates(ulanBirthYears, ulanDeathYears,
							reqBirthYear, reqDeathYear, 3, false)) {
						results.add(term);
						term.setDisambiguatingComment(String
								.format("requested lifetime (%d-%d) matched ULAN lifetime (%s-%s)",
										reqBirthYear, reqDeathYear,
										toString(ulanBirthYears),
										toString(ulanDeathYears)));
					}
				}

				// Unambiguous choice that fails date check
				if (terms.size() == 1 && results.isEmpty()) {
					// do a very relaxed check
					// if (checkDates(ulanBirthYears, ulanDeathYears,
					// reqBirthYear, reqDeathYear, 25, false))
					{
						results.add(term);
						term.setConfidenceComment(String
								.format("requested lifetime (%d-%d) DID NOT really match ULAN lifetime (%s-%s)",
										reqBirthYear, reqDeathYear,
										toString(ulanBirthYears),
										toString(ulanDeathYears)));
						term.setDisambiguatingComment("Maching name, and relaxed match on life dates");
					}
				}

			}
		}
		return (results.size() == 1) ? results : new TermList();
	}

	final static int allDeadYear = 1850;

	private boolean checkDates(Set<Integer> ulanBirthYears,
			Set<Integer> ulanDeathYears, int reqBirthYear, int reqDeathYear,
			int toleranceMultiplier, boolean lifeDate) {
		// tolerance of 1 year per 100 years back
		int toleranceOnDeathYear = (2000 - reqDeathYear) * toleranceMultiplier
				/ 100;
		int toleranceOnBirthYear = (2000 - reqBirthYear) * toleranceMultiplier
				/ 100;

		if (lifeDate) {
			return (reqBirthYear >= (Collections.min(ulanBirthYears) - toleranceOnBirthYear))
					&& (ulanDeathYears.isEmpty() || reqBirthYear <= (Collections
							.max(ulanDeathYears) + toleranceOnDeathYear));
		} else {
			// old people should have death year
			if (reqBirthYear <= allDeadYear
					&& (ulanDeathYears == null || ulanDeathYears.isEmpty()))
				return false;

			// if present, birth year should match
			if (reqBirthYear > 0) {
				if (!(ulanBirthYears.isEmpty() || (reqBirthYear >= (Collections
						.min(ulanBirthYears) - toleranceOnBirthYear))
						&& (reqBirthYear <= (Collections.max(ulanBirthYears) + toleranceOnBirthYear))))
					return false;
			}

			// if present, death year should match
			if (reqDeathYear != 0) {
				// young guys have their right to be alive
				if (!(reqBirthYear > allDeadYear && ulanDeathYears.isEmpty())) {
					if (!(ulanDeathYears.isEmpty() || (reqDeathYear >= (Collections
							.min(ulanDeathYears) - toleranceOnDeathYear))
							&& (reqDeathYear <= (Collections
									.max(ulanDeathYears) + toleranceOnDeathYear))))
						return false;
				}
			}
		}
		return true;
	}

	private String toString(Set<Integer> s) {
		String result = "";
		for (Integer i : s) {
			if (result.length() != 0)
				result += ",";
			result += i;
		}
		return result;
	}

	private Set<Integer> years(String property, Term term) throws Exception {
		Set<Integer> result = new HashSet<Integer>();
		String y = term.getProperty(property);
		int year = NumberUtils.toInt(y);
		// all future dates are wrong, ignore
		if (year < now.get(Calendar.YEAR) && year != 0) {
			result.add(year);
		}
		return result;
	}

}