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
package eu.europeana.enrichment.tagger.vocabularies;

import java.util.ArrayList;
import java.util.List;

import eu.europeana.enrichment.api.internal.TermList;
import eu.europeana.enrichment.api.internal.Language.Lang;
import eu.europeana.enrichment.tagger.postprocessors.PeopleTermFilter.UlanDisambiguationContext;

/**
 * Directory of people, where each term has its specific context of birth-death
 * years.
 * 
 * @author Borys Omelayenko
 * 
 */
public abstract class VocabularyOfPeople extends AbstractVocabulary {

	/**
	 * Person lookup with with its specific disambiguation context.
	 * 
	 * @param names
	 *            all names
	 * @param birthDate
	 *            date as String (maybe, just a year)
	 * @param deathDate
	 *            date as String (maybe, just a year)
	 * @param liveDate
	 *            a date in live
	 * @param placeOfBirth
	 * @param placeOfDeath
	 * @return person code
	 */
	public TermList lookupPerson(List<String> labels, Lang lang,
			String birthDate, String deathDate, String liveDate,
			String landOfBirth, String placeOfBirth, String landOfDeath,
			String placeOfDeath) throws Exception {
		return findByLabel(labels.get(0),
				liveDate == null ? new UlanDisambiguationContext(birthDate,
						deathDate, false) : new UlanDisambiguationContext(
						liveDate, deathDate, true));
	}

	public TermList lookupPerson(String labels, Lang lang, String birthDate,
			String deathDate, String liveDate, String landOfBirth,
			String placeOfBirth, String landOfDeath, String placeOfDeath)
			throws Exception {
		List<String> l = new ArrayList<String>();
		l.add(labels.toLowerCase());
		return lookupPerson(l, lang, birthDate, deathDate, liveDate,
				landOfBirth, placeOfBirth, landOfDeath, placeOfDeath);
	}

	public VocabularyOfPeople(String name, Lang lang) {
		super(name, lang);

	}

}
