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

import eu.europeana.enrichment.api.internal.TermList;
import eu.europeana.enrichment.tagger.vocabularies.DisambiguationContext;

/**
 * Disambiguation: reducing the list of found terms given the disambiguation
 * context. Improves precision.
 * 
 * @author Borys Omelayenko
 * 
 */
public abstract class TermFilter {
	/**
	 * Reduce the list by removing terms irrelevant to the disambiguation
	 * context.
	 * 
	 * @param allTerms
	 *            original list of terms
	 * @param disambiguationContext
	 * @return list of selected terms
	 * @throws Exception
	 */
	public abstract TermList disambiguate(TermList allTerms,
			DisambiguationContext disambiguationContext) throws Exception;
}
