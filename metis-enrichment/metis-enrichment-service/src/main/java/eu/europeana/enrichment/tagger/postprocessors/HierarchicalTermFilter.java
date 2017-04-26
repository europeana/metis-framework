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

import eu.europeana.enrichment.api.internal.CodeURI;
import eu.europeana.enrichment.api.internal.Term;
import eu.europeana.enrichment.api.internal.TermList;
import eu.europeana.enrichment.tagger.vocabularies.DisambiguationContext;

/**
 * Hierarchical disambiguation logic.
 * 
 * @author Borys Omelayenko
 * 
 */
public class HierarchicalTermFilter extends TermFilter {

	@Override
	public TermList disambiguate(TermList allTerms,
			DisambiguationContext disambiguationContext) throws Exception {

		// no parents means no disambiguation
		if (disambiguationContext == null
				|| disambiguationContext.getParents() == null
				|| disambiguationContext.getParents().isEmpty())
			return allTerms;

		// disambiguation not needed
		if (allTerms.size() < 2)
			return allTerms;

		TermList selectedTerms = new TermList();
		// disambiguate
		for (Term term : allTerms) {

			for (CodeURI parentCode : disambiguationContext.getParents()) {
				// a null parent would allow children of null to go
				boolean match = false;
				if (parentCode == null) {
					match = (term.getParent() == null);
				} else {
					// non-null parent - search for it in the upper path
					if (term.getParent() != null) {
						match = term.hasParent(parentCode);
					}
				}
				if (match) {
					selectedTerms.add(term);
					break;
				}
			}
		}
		return selectedTerms;
	}

}
