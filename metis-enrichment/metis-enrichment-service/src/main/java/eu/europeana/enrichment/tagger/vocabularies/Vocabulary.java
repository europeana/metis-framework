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

import java.io.File;
import java.util.Collection;
import java.util.Set;

import eu.europeana.enrichment.api.internal.CodeURI;
import eu.europeana.enrichment.api.internal.Term;
import eu.europeana.enrichment.api.internal.TermList;
import eu.europeana.enrichment.api.internal.Language.Lang;

/**
 * A vocabulary. User vocabularies should extend specific
 * <code>GenericVocabularyOfXXXX</code> classes.
 * 
 * @author Borys Omelayenko
 * 
 */

public abstract interface Vocabulary {

	/**
	 * The single point to put a new term into vocabulary.
	 * 
	 * @param term
	 * @throws Exception
	 */
	public void putTerm(Term term) throws Exception;

	public enum NormaliseCaller {
		load, query
	};

	/**
	 * Normalize term label on load and query. A typical use would be to convert
	 * it to low case. The normalizations are not persisted.
	 * 
	 * @param label
	 * @return
	 * @throws Exception
	 */
	public String onNormaliseLabel(String label, NormaliseCaller caller)
			throws Exception;

	public Iterable<TermList> listAllByCode();

	/**
	 * Called when the vocabulary term is loaded from the RDF file into memory.
	 * 
	 * @param label
	 * @return
	 */
	public Collection<Term> expandVocabularyTermOnLoad(Term term)
			throws Exception;

	/**
	 * Just a string to identify the vocabulary in reports.
	 * 
	 * @return
	 */
	public abstract String getVocabularyName();

	public static enum FileSign {
		pattern, fileName
	}

	/**
	 * Load from (several) files.
	 * 
	 * @param sign
	 *            specifies if the provided names are regular expressions or
	 *            list of file names.
	 * @throws Exception
	 */
	public void loadVocabulary(File tmpDir, FileSign sign, String... file)
			throws Exception;

	/**
	 * Is applied to the code received from RDF query and before this code is
	 * written to the cache file. Is used to prefix codes, or do other
	 * processing that cannot be done with the RDF query language.
	 * 
	 * @param code
	 * @return
	 */
	public String onLoadTermCode(String code) throws Exception;

	/**
	 * Executes a SeRQL query and loads it result into vocabulary. The query
	 * should have three compulsory result fields: <li>term URI</li> <li>parent
	 * URI</li> <li>labels, merged together if more than one is returned.</li>
	 * 
	 * 
	 * 
	 * @return
	 * @throws Exception
	 */
	@Deprecated
	public void loadTermsSeRQL(String query, File cacheDir, File dir,
			String... filePatterns) throws Exception;

	public void loadTermsSPARQL(String query, File cacheDir, File dir,
			String... filePatterns) throws Exception;

	public void loadTermPropertiesSPARQL(String attributeName, String query,
			File cacheDir, File dir, String... filePatterns) throws Exception;

	/**
	 * Find term up given the unique term code.
	 * 
	 * @param code
	 *            code of the term
	 * @return
	 * @throws Exception
	 */
	public TermList findByCode(CodeURI code) throws Exception;

	/**
	 * Find term given the label.
	 * 
	 * @param label
	 * @param lang
	 * @return
	 */
	public TermList findByLabel(String label,
			DisambiguationContext disambiguationContext) throws Exception;


	/**
	 * A wrap-up to {@link #findByCode(String, Lang)}
	 * 
	 * @param code
	 * @return
	 * @throws Exception
	 */
	public String findLabel(CodeURI code) throws Exception;

	public void init() throws Exception;

	/**
	 * Set of all term codes.
	 */
	public Set<CodeURI> codeSet();

	/**
	 * Set of all term labels.
	 * 
	 * @return
	 */
	public Set<String> labelSet();

}
