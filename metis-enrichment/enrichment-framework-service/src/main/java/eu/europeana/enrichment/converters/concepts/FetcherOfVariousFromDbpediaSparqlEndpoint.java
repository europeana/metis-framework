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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;

import eu.europeana.enrichment.common.Helper;
import eu.europeana.enrichment.context.Namespaces;
import eu.europeana.enrichment.tagger.vocabularies.VocabularyOfTerms;

/**
 * Tagging (aka semantic enrichment) of records from SOLR.
 * 
 * @author Borys Omelayenko
 * 
 */
public class FetcherOfVariousFromDbpediaSparqlEndpoint {
	protected VocabularyOfTerms vocabularyOfConcepts = new VocabularyOfTerms(
			"dbpedia.selected.concepts", null) {

		@Override
		public String onNormaliseLabel(String label, NormaliseCaller caller)
				throws Exception {
			return label;
		}

	};


	public void fetch() throws Exception {

		String[] dbpediaResources = { "Art", "Architecture", "Art_Deco",
				"Art_Nouveau", "Baroque", "Cubism", "Contemporary_art", "Dada",
				"Digital_art", "Expressionism", "Fine-art_photography",
				"Folk_art", "Futurism", "Impressionism", "Neoclassicism",
				"Pre-Raphaelite_Brotherhood", "Kitsch", "Still_life",
				"Landscape", "Minimalism", "Modernism", "Renaissance",
				"Realism_(arts)", "Romanesque_art", "Romanticism", "Rococo",
				"Pastoral", "Portrait", "Street_art", "Surrealism",
				"Symbolism", "Music", "Theatre", "Painting", "Sculpture",
				"Drawing", "Poster", "Photograph", "Furniture", "Costume",
				"Fashion", "Jewellery", "Porcelain", "Tapestry", "Woodcut" };

		Set<String> repos = new HashSet<String>();
		for (String resource : dbpediaResources) {
			try {
				String languageQuery = makeDbpediaSparqlQuery(resource,
						"sameAs", "<http://www.w3.org/2002/07/owl#sameAs>");
				Repository repo = Helper
						.createRemoteRepository("http://dbpedia.org/sparql");

				TupleQueryResult result = repo.getConnection()
						.prepareTupleQuery(QueryLanguage.SPARQL, languageQuery)
						.evaluate();
				List<String> fields = result.getBindingNames();
				while (result.hasNext()) {
					BindingSet bs = result.next();
					String str = bs.getValue(fields.get(1)).stringValue();
					if (str.contains("dbpedia")) {
						repos.add(StringUtils.substringBeforeLast(str,
								"/resource") + "/sparql");
					}
				}
			} catch (Exception e) {
				System.out.println("dbpedia resource " + resource
						+ " does not have anything under it");
			}
		}
	}

	String makeDbpediaSparqlQuery(String resource, String field, String property) {
		return "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
				+ "PREFIX dbo: <http://dbpedia.org/ontology/> "
				+ "SELECT <http://dbpedia.org/resource/" + resource + "> ?"
				+ field + " WHERE { " + " <http://dbpedia.org/resource/"
				+ resource + "> " + property + " ?" + field + " " + " } ";
	}

	String makeLanguageDbpediaSparqlQuery(String resource, String field,
			String property) {
		return "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
				+ "PREFIX dbo: <http://dbpedia.org/ontology/> " + "SELECT <"
				+ resource + "> ?" + field + " WHERE { " + " <" + resource
				+ "> " + property + " ?" + field + " " + " } ";
	}

	public void save() throws Exception {
		Namespaces ns = new Namespaces();
		ns.addNamespace(
				"http://dbpedia.org/ontology/", "dbpedia");
		Map<String, String> propertiesToExport = new HashMap<String, String>();
		vocabularyOfConcepts
				.saveAsRDF(
						"Selection from DBPedia: various concepts \n"
								+ "Extracted from http://dbpedia.org/snorql/ \n"
								+ "Original data is distributed under the GNU General Public License",
						ns, propertiesToExport, null);
	}

	public static void main(String[] args) throws Exception {
		FetcherOfVariousFromDbpediaSparqlEndpoint fetcher = new FetcherOfVariousFromDbpediaSparqlEndpoint();
		fetcher.fetch();
		fetcher.save();
	}

}
