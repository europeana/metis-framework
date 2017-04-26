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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europeana.enrichment.api.Common;
import eu.europeana.enrichment.api.internal.CodeURI;
import eu.europeana.enrichment.api.internal.Language.Lang;
import eu.europeana.enrichment.api.internal.Term;
import eu.europeana.enrichment.api.internal.TermList;
import eu.europeana.enrichment.common.Helper;
import eu.europeana.enrichment.common.Utils;
import eu.europeana.enrichment.context.Concepts.SKOS;
import eu.europeana.enrichment.context.Namespaces;
import eu.europeana.enrichment.path.Path;
import eu.europeana.enrichment.tagger.postprocessors.HierarchicalTermFilter;
import eu.europeana.enrichment.tagger.postprocessors.LanguageTermFilter;
import eu.europeana.enrichment.tagger.postprocessors.TermFilter;
import eu.europeana.enrichment.triple.LiteralValue;
import eu.europeana.enrichment.triple.Property;
import eu.europeana.enrichment.triple.ResourceValue;
import eu.europeana.enrichment.triple.Triple;
import eu.europeana.enrichment.utils.SesameWriter;

/**
 * Encapsulates a vocabulary with basic loading and search functionalities.
 * 
 * @author Borys Omelayenko
 * 
 */
public abstract class AbstractVocabulary implements Vocabulary {

	private Logger log = LoggerFactory.getLogger(getClass().getName());

	protected void logMessage(String message) throws IOException {

		log.info(message);
	}

	public boolean isLoaded = false;

	protected String name;

	// storage for terms
	private Map<String, TermList> termsByLabels = new HashMap<String, TermList>();
	private Map<CodeURI, TermList> termsByCodes = new HashMap<CodeURI, TermList>();

	protected Lang lang;

	private List<TermFilter> disambiguators = new ArrayList<TermFilter>();

	public void addDisambiguator(TermFilter disambiguator) {
		disambiguators.add(disambiguator);
	}

	public void clearDisambiguators() {
		disambiguators.clear();
	}

	public AbstractVocabulary(String name, Lang lang) {
		this.name = name;
		this.lang = lang;
		addDisambiguator(new LanguageTermFilter());
		addDisambiguator(new HierarchicalTermFilter());
		try {
			init();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void init() throws Exception {
		// should be overridden
	}

	@Override
	public final Set<CodeURI> codeSet() {
		return termsByCodes.keySet();
	}

	@Override
	public final Set<String> labelSet() {
		return termsByLabels.keySet();
	}

	@Override
	public final TermList findByCode(CodeURI code) throws Exception {
		// get all terms this code
		TermList terms = termsByCodes.get(code);
		return terms == null ? new TermList() : terms;
	}

	@Override
	public final TermList findByLabel(String label,
			DisambiguationContext disambiguationContext) throws Exception {
		String normalisedLabel = null;
		try {
			// get all terms this label
			normalisedLabel = onNormaliseLabel(label, NormaliseCaller.query);
			TermList terms = termsByLabels.get(normalisedLabel);
			return applyDisambiguators(terms, disambiguationContext);
		} catch (Exception e) {
			throw new Exception("Exception on find label: " + label
					+ ", normalised: " + normalisedLabel + ", context "
					+ disambiguationContext, e);
		}
	}

	@Override
	public final String findLabel(CodeURI code) throws Exception {
		Term term = findByCode(code).getFirst();
		return term == null ? null : term.getLabel();
	}

	public final TermList applyDisambiguators(TermList terms,
			DisambiguationContext disambiguationContext) throws Exception {
		if (terms == null) {
			return new TermList();
		}
		TermList result = terms;
		for (TermFilter disambiguator : disambiguators) {
			try {
				result = disambiguator.disambiguate(result,
						disambiguationContext);
			} catch (Exception e) {
				throw new Exception("TermFilter " + disambiguator, e);
			}
		}
		return result;
	}

	@Override
	public final Iterable<TermList> listAllByCode() {
		return termsByCodes.values();
	}

	@Override
	public final void putTerm(Term term) throws Exception {
		if (term.getLabel().length() == 0)
			throw new Exception("Empty label in term " + term);
		if (term.getCode().length() == 0)
			throw new Exception("Empty code in term " + term);
		if (!term.getCode().startsWith("http://"))
			throw new Exception(
					"Unexpected term code (does not start with http://) in term "
							+ term);

		// terms by label
		TermList listSameLabel = termsByLabels.get(term.getLabel());
		if (listSameLabel == null) {
			listSameLabel = new TermList();
		}
		listSameLabel.add(term);
		termsByLabels.put(term.getLabel(), listSameLabel);

		// terms by code
		TermList listSameCode = termsByCodes.get(new CodeURI(term.getCode()));
		if (listSameCode == null) {
			listSameCode = new TermList();
		}
		listSameCode.add(term);
		termsByCodes.put(new CodeURI(term.getCode()), listSameCode);
	}

	@Override
	public final void loadTermsSPARQL(String query, File cacheDir,
			File baseDir, String... filePatterns) throws Exception {
		loadTerms(query, false, cacheDir, baseDir.getCanonicalPath(),
				filePatterns);
	}

	@Override
	public final void loadTermsSeRQL(String query, File cacheDir, File baseDir,
			String... filePatterns) throws Exception {
		loadTerms(query, true, cacheDir, baseDir.getCanonicalPath(),
				filePatterns);
	}

	public final void loadTermsFromSparqlEndpoint(String query, File cacheDir,
			URL sparqlEndpoint) throws Exception {
		loadTerms(query, true, cacheDir, sparqlEndpoint.toExternalForm());
	}

	private void loadTerms(String query, boolean isSesame, File cacheDir,
			String baseDirName, String... filePatterns) throws Exception {
		VocabularySerializer handler = new VocabularySerializer(this,
				this.getVocabularyName());
		File baseDir = new File(baseDirName);

		List<String> locations = new ArrayList<String>();

		// normalizing files to prevent problems with hashes in cache file names
		// when run from a different environment
		for (String pattern : filePatterns) {
			for (File file : Utils.expandFileTemplateFrom(baseDir, pattern)) {
				locations.add(file.getCanonicalPath());
			}

		}

		String locationDescriptionForExceptionLogging = "Expanded from dir "
				+ baseDirName + " pattern "
				+ StringUtils.join(filePatterns, ",") + " to "
				+ StringUtils.join(locations, ";");

		if (baseDirName.startsWith("http://") && filePatterns.length == 0) {
			locations.add(baseDirName);
			locationDescriptionForExceptionLogging += "Loading from SPARQL endpoint "
					+ baseDirName;
		}
		loadMap(locations, locationDescriptionForExceptionLogging, this.name,
				query, isSesame, cacheDir, handler);

		//reconstructParents(handler);

		// if (codeSet().size() == 0)
		// throw new Exception("No terms loaded to vocabulary " + name);

		StopWatch timeElapsed = new StopWatch();
		timeElapsed.start();

		timeElapsed.stop();
		// log.info("Parsed " + name + " in " + timeElapsed + " ms");
	}

	/**
	 * Loads an RDF file and returns a objectRule, a query result. First query
	 * variable is the key, the rest are the values. Creates file
	 * <code>sfqc.123.456.txt</code> with query results and reuses it later when
	 * possible. Duplicate results are merged with a semicolon <code>;</code>
	 * 
	 * @return
	 * @throws Exception
	 */
	private void loadMap(List<String> locations,
			String locationDescriptionForExceptionLogging, String signature,
			String query, boolean isSesame, File cacheDir,
			VocabularySerializer handler) throws Exception {
		if (!cacheDir.exists())
			throw new IOException("Cache dir does not exist: "
					+ cacheDir.getCanonicalPath());

		File cacheFileTerms = makeCacheFileName("cache.", signature, locations,
				query, cacheDir);
		File cacheFileParents = makeCacheFileName("cacheparents.", signature,
				locations, query, cacheDir);

		boolean wasLoadedFromCache = loadFromCache(locations,
				locationDescriptionForExceptionLogging, handler,
				cacheFileTerms, cacheFileParents);

		if (!wasLoadedFromCache) {

			Repository rdf = createRepository(locations);
			populateRepositoryWithTerms(query, isSesame, handler, rdf);
			reconstructParents(handler);

			saveToCache(locations, query, cacheDir, handler, cacheFileTerms,
					cacheFileParents);
		}
	}

	File makeCacheFileName(String prefix, String signature,
			List<String> locations, String query, File cacheDir)
			throws Exception {
		File cacheFileTerms = new File(cacheDir, prefix + signature + ".q"
				+ query.hashCode() + ".f"
				+ StringUtils.join(locations, ";").hashCode() + ".txt");
		return cacheFileTerms;
	}

	boolean loadFromCache(List<String> locations,
			String locationDescriptionForExceptionLogging,
			VocabularySerializer handler, File cacheFileTerms,
			File cacheFileParents) throws Exception {

		long lastModified = 0;
		for (String location : locations) {
			File file = new File(location);
			if (file.lastModified() > lastModified) {
				lastModified = file.lastModified();
			}
		}

		if (cacheFileTerms.exists()
				&& cacheFileTerms.lastModified() > lastModified
				&& cacheFileParents.exists()) {
			logMessage("Recovered cached query result from file "
					+ cacheFileTerms);
			StopWatch timeElapsed = new StopWatch();
			timeElapsed.start();

			VocabularySerializer.SerializedProperties props = new VocabularySerializer.SerializedProperties();
			props.getTerms().load(
					new BufferedInputStream(
							new FileInputStream(cacheFileTerms), 1024 * 10));
			props.getParents().load(
					new BufferedInputStream(new FileInputStream(
							cacheFileParents), 1024 * 10));
			handler.deserializeFromProperties(props);

			timeElapsed.stop();
			logMessage("Loaded " + props.getTerms().size() + " term codes in "
					+ timeElapsed + " ms");
			return true;
		}

		if (!cacheFileTerms.exists() || !cacheFileParents.exists()) {
			logMessage("No cache file " + cacheFileTerms.getCanonicalPath()
					+ " found, loading RDF from the following files");
			logMessage(locationDescriptionForExceptionLogging);
		}

		if (cacheFileTerms.exists()
				&& !(cacheFileTerms.lastModified() > lastModified)) {
			logMessage("Cache file "
					+ cacheFileTerms.getCanonicalPath()
					+ " is older than RDF files, loading RDF from the following files");
		}
		return false;
	}

	Repository createRepository(List<String> locations) throws Exception {

		Repository rdf;
		if (locations.size() == 1 && locations.get(0).startsWith("http://")) {
			rdf = Helper.createRemoteRepository(locations.get(0));
		} else {
			File[] files = new File[locations.size()];
			for (int i = 0; i < locations.size(); i++) {
				files[i] = new File(locations.get(i));
			}
			rdf = Helper.createLocalRepository();
			Helper.importRDFXMLFile(rdf, "http://localhost/namespace", files);
		}
		return rdf;
	}

	void populateRepositoryWithTerms(String query, boolean isSesame,
			VocabularySerializer handler, Repository rdf) throws Exception {
		// extracting map
		RepositoryConnection con = rdf.getConnection();
		try {
			TupleQueryResult result = con.prepareTupleQuery(
					isSesame ? QueryLanguage.SERQL : QueryLanguage.SPARQL,
					query).evaluate();
			List<String> bindingNames = result.getBindingNames();
			try {
				while (result.hasNext()) {
					BindingSet bindingSet = result.next();
					if (bindingSet.size() > 3)
						throw new Exception(
								"Query returned too many fields: the following three are expected: term URI, (parent URI,) label");
					if (bindingSet.size() < 2)
						throw new Exception(
								"Query returned too few fields: the following three are expected: term URI, (parent URI,) label");

					CodeURI termURI = new CodeURI(onLoadTermCode(bindingSet
							.getValue(bindingNames.get(0)).stringValue()));

					CodeURI parentURI = null;
					if (bindingNames.size() == 3
							&& bindingSet.getValue(bindingNames.get(1)) != null) {
						parentURI = new CodeURI(onLoadTermCode(bindingSet
								.getValue(bindingNames.get(1)).stringValue()));
					}

					Value valueRaw = bindingSet.getValue(bindingNames
							.get(bindingNames.size() - 1));

					if (valueRaw instanceof LiteralImpl) {
						LiteralImpl valueLiteral = (LiteralImpl) valueRaw;

						Lang parsedLang = null;
						try {
							parsedLang = Lang.parseLang(valueLiteral
									.getLanguage());
						} catch (Exception e) {
							logMessage(e.getMessage());
						}
						String label = valueLiteral.getLabel();
						if (!StringUtils.isBlank(label)) {
							handler.parseValue(termURI, parentURI, label,
									parsedLang);
						}
					} else
						handler.parseValue(termURI, parentURI,
								valueRaw.stringValue());
				}
			} finally {
				result.close();
			}
		} finally {
			con.close();
		}
	}

	void saveToCache(List<String> locations, String query, File cacheDir,
			VocabularySerializer handler, File cacheFileTerms,
			File cacheFileParents) throws Exception, IOException,
			FileNotFoundException {
		// save query result
		if (cacheDir != null) {
			String fileNameList = "\n#" + StringUtils.join(locations, "\n#");

			VocabularySerializer.SerializedProperties props = handler
					.serializeToProperties();
			props.getTerms().store(
					new FileOutputStream(cacheFileTerms),
					String.format("%s \n# \n# %s \n# Total: %d \n#",
							query.replaceAll("\\n", "\n#"), fileNameList,
							codeSet().size()));
			props.getParents().store(new FileOutputStream(cacheFileParents),
					"Parents");
		}
	}

	private void reconstructParents(VocabularySerializer handler)
			throws Exception {
		// reconstruct parents
		for (TermList terms : listAllByCode()) {
			for (Term term : terms) {
				String parentCode = handler.getParentCode(term.getCode());
				if (parentCode != null) {
					try {
						TermList parents = findByCode(new CodeURI(parentCode));
						if (!parents.isEmpty()) {
							Term parentTerm = parents.getFirst();
							term.setParent(parentTerm);
						}
					} catch (Exception e) {
						throw new Exception("One term " + term.getCode()
								+ " with parent " + parentCode, e);
					}
				}
			}
		}
	}

	@Override
	public final String getVocabularyName() {
		return name;
	}

	/*
	 * LOAD
	 */

	@Override
	public String onNormaliseLabel(String label, NormaliseCaller caller)
			throws Exception {
		label = label.trim();
		label = Common.removeDiacritics(label);
		return label;
	}

	private static final List<Term> EMPTY_COLL = new LinkedList<Term>();

	@Override
	public Collection<Term> expandVocabularyTermOnLoad(Term term)
			throws Exception {
		return EMPTY_COLL;
	}

	@Override
	public void loadVocabulary(File tmpDir, FileSign sign, String... file)
			throws Exception {
		isLoaded = true;
	}

	@Override
	public void loadTermPropertiesSPARQL(String propertyName, String query,
			File cacheDir, File baseDir, String... filePatterns)
			throws Exception {
		VocabularyOfTerms attributeVocabulary = makeVocabularyToLoadTermProperties(propertyName);

		attributeVocabulary.loadTermsSPARQL(query, cacheDir, baseDir,
				filePatterns);

		applyTermPropertiesToTerms(propertyName, attributeVocabulary);
	}

	VocabularyOfTerms makeVocabularyToLoadTermProperties(String propertyName) {
		VocabularyOfTerms attributeVocabulary = new VocabularyOfTerms(name
				+ "_A_" + propertyName, null);
		attributeVocabulary.clearDisambiguators();
		return attributeVocabulary;
	}

	public void loadTermPropertiesFromSparqlEndpoint(String propertyName,
			String query, File cacheDir, URL sparqlEndpoint) throws Exception {
		VocabularyOfTerms attributeVocabulary = makeVocabularyToLoadTermProperties(propertyName);

		attributeVocabulary.loadTermsFromSparqlEndpoint(query, cacheDir,
				sparqlEndpoint);

		applyTermPropertiesToTerms(propertyName, attributeVocabulary);
	}

	void applyTermPropertiesToTerms(String propertyName,
			VocabularyOfTerms attributeVocabulary) throws Exception {

		for (TermList terms : listAllByCode()) {
			for (Term term : terms) {
				TermList propertyTerms = attributeVocabulary
						.findByCode(new CodeURI(term.getCode()));
				for (Term propertyTerm : propertyTerms) {
					term.setProperty(propertyName, propertyTerm.getLabel());
				}
			}
		}
	}

	@Override
	public String onLoadTermCode(String code) throws Exception {
		return code;
	}

	public String makeTermsQuery(String isPartOfProperty) {
		return "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "
				+ "PREFIX dcterms: <http://purl.org/dc/terms/> "
				+ "SELECT ?code ?parent ?label " + "WHERE { "
				+ "  { ?code skos:prefLabel ?label . OPTIONAL { ?code "
				+ isPartOfProperty + " ?parent }} " + "UNION "
				+ "  { ?code skos:altLabel ?label . OPTIONAL { ?code "
				+ isPartOfProperty + " ?parent }} " + "} ";
	}

	public void saveAsRDF(
			String description,
			Namespaces namespaces,
			Map<String, String> literalTermPropertiesToExportMapNameToRdfProperty,
			Map<String, String> resourceTermPropertiesToExportMapNameToRdfProperty)
			throws Exception {
		SesameWriter rdfWriter = SesameWriter.createRDFXMLWriter(new File(name
				+ ".rdf"), namespaces, name, description, 1024, 1024);
		rdfWriter.startRDF();

		for (CodeURI code : termsByCodes.keySet()) {
			TermList terms = findByCode(code);
			for (Term term : terms) {
				rdfWriter.handleTriple(new Triple(code.getUri(),
						SKOS.LABEL_PREFERRED, new LiteralValue(term.getLabel(),
								term.getLang()), null));
				// literals
				if (literalTermPropertiesToExportMapNameToRdfProperty != null) {
					for (String propertyName : literalTermPropertiesToExportMapNameToRdfProperty
							.keySet()) {
						if (propertyName != null) {
							String propertyValue = term
									.getProperty(propertyName);
							if (!StringUtils.isBlank(propertyValue)) {
								String rdfPropertyName = literalTermPropertiesToExportMapNameToRdfProperty
										.get(propertyName);
								rdfWriter.handleTriple(new Triple(
										code.getUri(), new Property(new Path(
												rdfPropertyName, namespaces)),
										new LiteralValue(propertyValue), null));
							}
						}
					}
				}
				// resources
				if (resourceTermPropertiesToExportMapNameToRdfProperty != null) {
					for (String propertyName : resourceTermPropertiesToExportMapNameToRdfProperty
							.keySet()) {
						if (propertyName != null) {
							String propertyValue = term
									.getProperty(propertyName);
							if (!StringUtils.isBlank(propertyValue)) {
								String rdfPropertyName = resourceTermPropertiesToExportMapNameToRdfProperty
										.get(propertyName);
								rdfWriter
										.handleTriple(new Triple(
												code.getUri(),
												new Property(new Path(
														rdfPropertyName,
														namespaces)),
												new ResourceValue(propertyValue),
												null));
							}
						}
					}
				}
			}
		}

		rdfWriter.endRDF();
	}
}
