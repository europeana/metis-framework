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
import java.util.Collections;
import java.util.List;

import eu.europeana.enrichment.api.internal.CodeURI;
import eu.europeana.enrichment.api.internal.TermList;
import eu.europeana.enrichment.api.internal.Language.Lang;


/**
 * Standard thesaurus of terms, possibly arranged in a hierarchy, with its specific disambiguation context.
 * 
 * @author Borys Omelayenko
 * 
 */
public class VocabularyOfTerms extends AbstractVocabulary
{

	public TermList lookupTerm(String labels, Lang language, String parentCode) throws Exception
	{
		List<String> l = new ArrayList<String>();
		l.add(labels);
		return lookupTerm(l, language, parentCode);
	}

	public TermList lookupTerm(List<String> labels, Lang language, String parentCode) throws Exception
	{
		TermList result = new TermList();
		for (String label : labels)
		{
			result.add(
					findByLabel(label, 
							new DisambiguationContext(
									language, 
									Lang.en, 
									parentCode == null ? null	: Collections.singleton(new CodeURI(parentCode)))));
		}
		return result;
	}

	
	
	public VocabularyOfTerms(String name, Lang lang)
	{
		super(name, lang);
	}

}
