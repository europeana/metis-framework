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

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.europeana.enrichment.api.internal.CodeURI;
import eu.europeana.enrichment.api.internal.TermList;
import eu.europeana.enrichment.api.internal.Language.Lang;

/**
 * Intervals, e.g. time periods.
 * 
 * @author Borys Omelayenko
 * 
 */
public class VocabularyOfTime extends AbstractVocabulary {

	public String filterExactTimestamps(String termLabel) {
		final Pattern datePattern = Pattern
				.compile("^(\\d\\d\\d\\d)(-\\d\\d-\\d\\d)?( 00:00:00)?$");
		Matcher m = datePattern.matcher(termLabel);
		if (m.find()) {
			String year = m.group(1);
			return year;
		}
		return termLabel;
	}

	public TermList lookupTerm(String startLabel, String endLabel,
			Lang language, String parentCode) throws Exception {
		TermList result = new TermList();
		String startYear = filterExactTimestamps(startLabel);
		String endYear = filterExactTimestamps(endLabel);

		DisambiguationContext disambiguationContext = new DisambiguationContext(
				language, Lang.en, parentCode == null ? null
						: Collections.singleton(new CodeURI(parentCode)));

		result.add(findByLabel(startYear, disambiguationContext));
		result.add(findByLabel(endYear, disambiguationContext));
		return result;
	}

	public VocabularyOfTime(String name, Lang lang) {
		super(name, lang);
	}

	@Override
	public String onNormaliseLabel(String label, NormaliseCaller caller)
			throws Exception {
		return super.onNormaliseLabel(label, caller).toLowerCase();
	}

}
