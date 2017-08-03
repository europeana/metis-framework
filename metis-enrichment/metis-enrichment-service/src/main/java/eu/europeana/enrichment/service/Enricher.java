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
package eu.europeana.enrichment.service;

import eu.europeana.enrichment.api.Factory;
import eu.europeana.enrichment.api.ObjectRule;
import eu.europeana.enrichment.api.Task;
import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.metis.utils.InputValue;
import eu.europeana.enrichment.context.Namespaces;
import eu.europeana.enrichment.path.Path;
import eu.europeana.enrichment.rest.client.EnrichmentProxyClient;
import eu.europeana.enrichment.rules.ObjectRuleImpl;
import eu.europeana.enrichment.tagger.vocabularies.VocabularyOfPeople;
import eu.europeana.enrichment.tagger.vocabularies.VocabularyOfPlaces;
import eu.europeana.enrichment.tagger.vocabularies.VocabularyOfTerms;
import eu.europeana.enrichment.tagger.vocabularies.VocabularyOfTime;
import eu.europeana.enrichment.utils.MongoDatabaseUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tagging (aka semantic enrichment) of records from SOLR with built-in
 * vocabularies.
 * 
 * @author Borys Omelayenko
 * @author Yorgos.Mamakis@ europeana.eu
 */
public class Enricher {

	private final String mongoHost;
	private final int mongoPort;
	@Autowired
	private RedisInternalEnricher enricher;
	@Autowired
	private EnrichmentProxyClient client;
	/**
	 * Main enrichment method
	 * 
	 * @param values
	 *            The values to enrich
	 * @return The resulting enrichment List
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 */
	public List<EntityWrapper> tagExternal(List<InputValue> values)
			throws JsonGenerationException, JsonMappingException, IOException {
		List<EntityWrapper> entities = new ArrayList<EntityWrapper>();
		entities.addAll(enricher.tag(values));
		return entities;
	}

	public void recreate(){
		client.populate();
	}

	public EntityWrapper getByUri(String uri){
		try{
			return enricher.getByUri(uri);
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public void clearCache() {
		MongoDatabaseUtils.emptyCache();
	}

	protected VocabularyOfTime vocabularyOfPeriods = new VocabularyOfTime(
			"vocabularyOfTime", null) {

		@Override
		public String onNormaliseLabel(String label, NormaliseCaller caller)
				throws Exception {
			return label.toLowerCase();
		}

		@Override
		protected void logMessage(String message) throws IOException {
		}

	};

	protected VocabularyOfPlaces vocabularyOfPlaces = new VocabularyOfPlaces(
			"vocabularyOfPlaces", null) {

		@Override
		public String onNormaliseLabel(String label, NormaliseCaller caller)
				throws Exception {
			return label.toLowerCase();
		}

		@Override
		protected void logMessage(String message) throws IOException {
		}

	};

	protected VocabularyOfTerms vocabularyOfTerms = new VocabularyOfTerms(
			"vocabularyOfConcepts", null) {

		@Override
		public String onNormaliseLabel(String label, NormaliseCaller caller)
				throws Exception {
			return StringUtils.lowerCase(label);
		}

		@Override
		protected void logMessage(String message) throws IOException {
		}

	};

	protected VocabularyOfPeople vocabularyOfPeople = new VocabularyOfPeople(
			"vocabularyOfActors", null) {

		@Override
		public String onNormaliseLabel(String label, NormaliseCaller caller)
				throws Exception {
			return StringUtils.lowerCase(label);
		}

		@Override
		protected void logMessage(String message) throws IOException {
		}

	};

	Task task;

	ObjectRule objectRule;


	private String path;

	public Enricher(String path, String mongoHost, int mongoPort) {
		this.path = path;
		this.mongoHost = mongoHost;
		this.mongoPort = mongoPort;
	}

	private String makePlaceCoordinateQuery(String property) {
		return "PREFIX places: <http://www.w3.org/2003/01/geo/wgs84_pos#> "
				+ "SELECT ?code ?" + property + " " + "WHERE { ?code places:"
				+ property + " ?" + property + " }";
	}

	private String makePlacePropertyQuery(String property) {
		return "PREFIX places: <http://www.europeana.eu/resolve/ontology/> "
				+ "SELECT ?code ?" + property + " " + "WHERE { ?code places:"
				+ property + " ?" + property + " }";
	}

	private String makeTimePropertyQuery(String property) {
		return "PREFIX time: <http://semium.org/time/> "
				+ "SELECT ?code ?endpoint " + "WHERE { ?code time:" + property
				+ " ?endpoint} ";
	}

	private String makePeoplePropertyQuery(String property) {
		return "PREFIX people: <http://dbpedia.org/ontology/> "
				+ "SELECT ?code ?" + property + " " + "WHERE { ?code people:"
				+ property + " ?" + property + " }";
	}

	/**
	 * Initialization method of the Enricher. Should be called in order to
	 * connect to the database. If the database is not existing it will
	 * reconstruct it by fecthing data from a folder that vocabularies exist).
	 * This should be modified
	 * 
	 * TODO: Remove the Environment dependency
	 * 
	 * @param name
	 *            - The name of the enrichment session to use
	 * @param args
	 *            - Override the connection details
	 * @throws Exception
	 */
	public void init(String name, String... args) throws Exception {

		task = Factory.makeTask(name, "", "Solr tagging with time and place",
				Namespaces.ANNOCULTOR_CONVERTER);
		objectRule = ObjectRuleImpl.makeObjectRule(task, new Path(""),
				new Path(""), new Path(""), null, false);

		if (!MongoDatabaseUtils.dbExists(mongoHost, mongoPort)) {
			//enricher = new RedisInternalEnricher(host);
			File cacheDir = new File(path + "/tmp");
			File baseDir = new File(path);
			String placeFiles = "places/EU/*.rdf";
			String countryFiles = "places/countries/*.rdf";
			vocabularyOfPlaces.loadTermsSPARQL(
					vocabularyOfPlaces.makeTermsQuery("dcterms:isPartOf"),
					cacheDir, baseDir, placeFiles, countryFiles);
			vocabularyOfPlaces.loadTermPropertiesSPARQL("population",
					makePlacePropertyQuery("population"), cacheDir, baseDir,
					placeFiles, countryFiles);
			vocabularyOfPlaces.loadTermPropertiesSPARQL("division",
					makePlacePropertyQuery("division"), cacheDir, baseDir,
					placeFiles, countryFiles);
			vocabularyOfPlaces.loadTermPropertiesSPARQL("latitude",
					makePlaceCoordinateQuery("lat"), cacheDir, baseDir,
					placeFiles, countryFiles);
			vocabularyOfPlaces.loadTermPropertiesSPARQL("longitude",
					makePlaceCoordinateQuery("long"), cacheDir, baseDir,
					placeFiles, countryFiles);
			vocabularyOfPlaces.loadTermPropertiesSPARQL("country",
					makePlacePropertyQuery("country"), cacheDir, baseDir,
					placeFiles, countryFiles);
			/*vocabularyOfPlaces.loadTermPropertiesSPARQL("skos",
					makePlacePropertyQuery("country"), cacheDir, baseDir,
					placeFiles, countryFiles);*/
			MongoDatabaseUtils.save("place", vocabularyOfPlaces);
			String timeFiles = "time/*.rdf";
			vocabularyOfPeriods.loadTermsSPARQL(
					vocabularyOfPeriods.makeTermsQuery("dcterms:isPartOf"),
					cacheDir, baseDir, timeFiles);
			vocabularyOfPeriods.loadTermPropertiesSPARQL("begin",
					makeTimePropertyQuery("beginDate"), cacheDir, baseDir,
					timeFiles);
			vocabularyOfPeriods.loadTermPropertiesSPARQL("end",
					makeTimePropertyQuery("endDate"), cacheDir, baseDir,
					timeFiles);
			MongoDatabaseUtils.save("period", vocabularyOfPeriods);
			vocabularyOfTerms.loadTermsSPARQL(
					vocabularyOfTerms.makeTermsQuery("skos:broader"), cacheDir,
					baseDir, "concepts/gemet/gemet*.rdf");
			vocabularyOfTerms.loadTermsSPARQL(
					vocabularyOfTerms.makeTermsQuery("skos:broader"), cacheDir,
					baseDir, "concepts/wikipedia/*.rdf");

			MongoDatabaseUtils.save("concept", vocabularyOfTerms);

			/*String peopleFiles = "people/*.rdf";
			vocabularyOfPeople.loadTermsSPARQL(
					vocabularyOfPeople.makeTermsQuery("dcterms:isPartOf"),
					cacheDir, baseDir, peopleFiles);
			vocabularyOfPeople.loadTermPropertiesSPARQL("birth",
					makePeoplePropertyQuery("birth"), cacheDir, baseDir,
					peopleFiles);
			vocabularyOfPeople.loadTermPropertiesSPARQL("death",
					makePeoplePropertyQuery("death"), cacheDir, baseDir,
					peopleFiles);

			MongoDatabaseUtils.save("people", vocabularyOfPeople);*/
		}

	}
	public RedisInternalEnricher getEnricher(){
		return enricher;
	}
}
