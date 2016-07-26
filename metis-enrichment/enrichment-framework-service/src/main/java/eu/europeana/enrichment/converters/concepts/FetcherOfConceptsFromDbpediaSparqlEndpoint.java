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
package eu.europeana.enrichment.converters.concepts;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import eu.europeana.enrichment.context.Namespaces;
import eu.europeana.enrichment.tagger.vocabularies.VocabularyOfTerms;

/**
 * Tagging (aka semantic enrichment) of records from SOLR.
 * 
 * @author Borys Omelayenko
 * 
 */
public class FetcherOfConceptsFromDbpediaSparqlEndpoint {
	protected VocabularyOfTerms vocabularyOfTerms = new VocabularyOfTerms(
			"dbpedia.world.war.one", null) {

		@Override
		public String onNormaliseLabel(String label, NormaliseCaller caller)
				throws Exception {
			return label;
		}

	};


	public void fetch(String path) throws Exception {

		File cacheDir = new File(path + "/tmp");

		vocabularyOfTerms.loadTermsFromSparqlEndpoint(makeDbpediaSparqlQuery(),
				cacheDir, new URL("http://dbpedia.org/sparql"));
	}

	String makeDbpediaSparqlQuery() {
		return "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
				+ "PREFIX dbo: <http://dbpedia.org/ontology/> "
				+ "SELECT ?battle ?label WHERE { "
				+ " ?battle <http://dbpedia.org/ontology/partOf> <http://dbpedia.org/resource/World_War_I>  . "
				+ " ?battle rdfs:label ?label      }    ";
	}

	public void save() throws Exception {
		Namespaces ns = new Namespaces();
		ns.addNamespace(
				"http://dbpedia.org/ontology/", "dbpedia");
		Map<String, String> resourcePropertiesToExport = new HashMap<String, String>();
		vocabularyOfTerms
				.saveAsRDF(
						"Selection from DBPedia WorldWar I: battles \n"
								+ "Extracted from http://dbpedia.org/snorql/ \n"
								+ "Original data is distributed under the GNU General Public License",
						ns, null,
						resourcePropertiesToExport);
	}

	public static void main(String[] args) throws Exception {
		FetcherOfConceptsFromDbpediaSparqlEndpoint fetcher = new FetcherOfConceptsFromDbpediaSparqlEndpoint();
		Properties props = new Properties();
		props.load(new FileInputStream("enrichment.properties"));
		fetcher.fetch(props.getProperty("vocabulary.path"));
		fetcher.save();
	}

}
