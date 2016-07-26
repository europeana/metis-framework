package eu.europeana.enrichment.migration;

import com.mongodb.*;
import eu.europeana.corelib.solr.entity.AbstractEdmEntityImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.enrichment.api.internal.*;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hgeorgiadis
 *
 *	
 */
public class MongoDatabaseMigration {

	private JacksonDBCollection<ConceptTermList, String> conceptTermListColl_source;
	private JacksonDBCollection<PlaceTermList, String> placeTermListColl_source;
	private JacksonDBCollection<TimespanTermList, String> timespanTermListColl_source;
	private JacksonDBCollection<AgentTermList, String> agentTermListColl_source;

	private JacksonDBCollection<MongoTerm, String> conceptTermColl_source;
	private JacksonDBCollection<MongoTerm, String> placeTermColl_source;
	private JacksonDBCollection<MongoTerm, String> timespanTermColl_source;
	private JacksonDBCollection<MongoTerm, String> agentTermColl_source;

	private DB sourceDB;

	private JacksonDBCollection<ConceptTermList, String> conceptTermListColl_target;
	private JacksonDBCollection<PlaceTermList, String> placeTermListColl_target;
	private JacksonDBCollection<TimespanTermList, String> timespanTermListColl_target;
	private JacksonDBCollection<AgentTermList, String> agentTermListColl_target;

	private JacksonDBCollection<MongoTerm, String> conceptTermColl_target;
	private JacksonDBCollection<MongoTerm, String> placeTermColl_target;
	private JacksonDBCollection<MongoTerm, String> timespanTermColl_target;
	private JacksonDBCollection<MongoTerm, String> agentTermColl_target;

	private JacksonDBCollection<MongoCodeLookup, String> lookupColl_target;
	private JacksonDBCollection<MongoSequence, String> sequenceColl_target;

	private DB targetDB;

	private MongoSequence mongoSequence = null;

	private String sourceHost;
	private int sourcePort;
	private String sourceDBName;
	private String targetHost;
	private int targetPort;
	private String targetDBName;

	private Map<String, String> lookupCodeUri = new HashMap<String, String>();

	private Map<String, String> lookupOriginalCodeUri = new HashMap<String, String>();

	public MongoDatabaseMigration(String sourceHost, int sourcePort, String sourceDBName, String targetHost,
			int targetPort, String targetDBName) throws UnknownHostException {

		this.sourceHost = sourceHost;
		this.sourcePort = sourcePort;
		this.sourceDBName = sourceDBName;
		this.targetHost = targetHost;
		this.targetPort = targetPort;
		this.targetDBName = targetDBName;

		initialiseConnections();
	}

	public void initialiseConnections() throws UnknownHostException {
		MongoClientOptions options = MongoClientOptions.builder().connectTimeout(15000).socketKeepAlive(true).build();

		Mongo mongo = new MongoClient(new ServerAddress(sourceHost, sourcePort), options);

		sourceDB = mongo.getDB(sourceDBName);

		//conceptTermListColl_source = JacksonDBCollection.wrap(sourceDB.getCollection("TermList"), ConceptTermList.class,
		//		String.class);

		placeTermListColl_source = JacksonDBCollection.wrap(sourceDB.getCollection("TermList"), PlaceTermList.class,
				String.class);

		//timespanTermListColl_source = JacksonDBCollection.wrap(sourceDB.getCollection("TermList"),
		//		TimespanTermList.class, String.class);

		//agentTermListColl_source = JacksonDBCollection.wrap(sourceDB.getCollection("TermList"), AgentTermList.class,
		//		String.class);

		//conceptTermColl_source = JacksonDBCollection.wrap(sourceDB.getCollection("concept"), MongoTerm.class,
		//		String.class);

		placeTermColl_source = JacksonDBCollection.wrap(sourceDB.getCollection("place"), MongoTerm.class, String.class);

		//timespanTermColl_source = JacksonDBCollection.wrap(sourceDB.getCollection("period"), MongoTerm.class,
		//		String.class);

		//agentTermColl_source = JacksonDBCollection.wrap(sourceDB.getCollection("people"), MongoTerm.class,
		//		String.class);

		if (!sourceHost.equals(targetHost) || sourcePort != targetPort) {
			mongo = new MongoClient(new ServerAddress(targetHost, targetPort), options);
		}

		targetDB = mongo.getDB(targetDBName);

		/*
		conceptTermListColl_target = JacksonDBCollection.wrap(targetDB.getCollection("TermList"), ConceptTermList.class,
				String.class);
		conceptTermListColl_target.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", true));
*/
		placeTermListColl_target = JacksonDBCollection.wrap(targetDB.getCollection("TermList"), PlaceTermList.class,
				String.class);
		placeTermListColl_target.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", true));
/*
		timespanTermListColl_target = JacksonDBCollection.wrap(targetDB.getCollection("TermList"),
				TimespanTermList.class, String.class);
		timespanTermListColl_target.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", true));

		agentTermListColl_target = JacksonDBCollection.wrap(targetDB.getCollection("TermList"), AgentTermList.class,
				String.class);
		agentTermListColl_target.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", true));

		conceptTermColl_target = JacksonDBCollection.wrap(targetDB.getCollection("concept"), MongoTerm.class,
				String.class);
		conceptTermColl_target.createIndex(new BasicDBObject("label", 1).append("lang", 1).append("codeUri", 1),
				new BasicDBObject("unique", true));
		conceptTermColl_target.createIndex(new BasicDBObject("codeUri", 1));
*/
		placeTermColl_target = JacksonDBCollection.wrap(targetDB.getCollection("place"), MongoTerm.class, String.class);
		placeTermColl_target.createIndex(new BasicDBObject("label", 1).append("lang", 1).append("codeUri", 1),
				new BasicDBObject("unique", true));
		placeTermColl_target.createIndex(new BasicDBObject("codeUri", 1));
/*
		timespanTermColl_target = JacksonDBCollection.wrap(targetDB.getCollection("period"), MongoTerm.class,
				String.class);
		timespanTermColl_target.createIndex(new BasicDBObject("label", 1).append("lang", 1).append("codeUri", 1),
				new BasicDBObject("unique", true));
		timespanTermColl_target.createIndex(new BasicDBObject("codeUri", 1));

		agentTermColl_target = JacksonDBCollection.wrap(targetDB.getCollection("people"), MongoTerm.class,
				String.class);
		agentTermColl_target.createIndex(new BasicDBObject("label", 1).append("lang", 1).append("codeUri", 1),
				new BasicDBObject("unique", true));
		agentTermColl_target.createIndex(new BasicDBObject("codeUri", 1));
*/
		lookupColl_target = JacksonDBCollection.wrap(targetDB.getCollection("lookup"), MongoCodeLookup.class,
				String.class);
		lookupColl_target.createIndex(new BasicDBObject("codeUri", 1).append("originalCodeUri", 1),
				new BasicDBObject("unique", true));
		lookupColl_target.createIndex(new BasicDBObject("originalCodeUri", 1)); // for
																				// reverse
																				// lookup

		sequenceColl_target = JacksonDBCollection.wrap(targetDB.getCollection("sequence"), MongoSequence.class,
				String.class);
	}

	public MongoSequence getMongoSequence() {
		return mongoSequence;
	}

	public void setMongoSequence(MongoSequence mongoSequence) {
		this.mongoSequence = mongoSequence;
	}

	private long nextSequence(ContextualCategory contextualCategory) {

		Long nextSequence = mongoSequence.getNextSequence(contextualCategory);
		mongoSequence.setNextSequence(nextSequence + 1, contextualCategory);
		return nextSequence;

	}

	private <T extends AbstractEdmEntityImpl, G extends MongoTermList<T>> boolean migratePhaseOne(
			JacksonDBCollection<G, String> termListColl_source, JacksonDBCollection<MongoTerm, String> termColl_source,
			JacksonDBCollection<G, String> termListColl_target, JacksonDBCollection<MongoTerm, String> termColl_target,
			ContextualCategory contextualCategory, String termCollection) throws UnknownHostException {
		int skip = 0;
		int counter = 0;
		while (true) {
			try {
				DBCursor<G> curs = termListColl_source
						.find(new BasicDBObject("entityType", contextualCategory.getEntityClass())).skip(skip);
				curs.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);

				while (curs.hasNext()) {
					long nextSequence = nextSequence(contextualCategory);
					counter++;

					G termList = curs.next();
					String newCodeUri = String.format("http://data.europeana.eu/%s/base" +
							"/%d",
							contextualCategory.getLabel(), nextSequence);
					String oldCodeUri = termList.getCodeUri();

					MongoCodeLookup lookup = new MongoCodeLookup();
					lookup.setCodeUri(newCodeUri);
					lookup.setOriginalCodeUri(oldCodeUri);
					lookupColl_target.insert(lookup);

					lookupCodeUri.put(newCodeUri, oldCodeUri);
					lookupOriginalCodeUri.put(oldCodeUri, newCodeUri);

				}
				curs.close();
				break;
			} catch (Exception e) {
				e.printStackTrace();
				initialiseConnections();

				skip = counter - 1;
				continue;
			}
		}
		return true;
	}

	protected void loadCache() {
		DBCursor<MongoCodeLookup> lookupCurs = lookupColl_target.find();
		lookupCurs.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
		while (lookupCurs.hasNext()) {
			MongoCodeLookup lookup = lookupCurs.next();
			addInCache(lookup);
		}
	}

	public void addInCache(MongoCodeLookup lookup) {
		lookupCodeUri.put(lookup.getCodeUri(), lookup.getOriginalCodeUri());
		lookupOriginalCodeUri.put(lookup.getOriginalCodeUri(), lookup.getCodeUri());
	}

	protected String lookupCodeUri(String codeUri) {
		return lookupCodeUri.get(codeUri);
	}

	protected String lookupOriginalCodeUri(String originalCodeUri) {
		return lookupOriginalCodeUri.get(originalCodeUri);
	}

	private <T extends AbstractEdmEntityImpl, G extends MongoTermList<T>> boolean migratePhaseTwo(
			JacksonDBCollection<MongoTerm, String> termColl_source,
			JacksonDBCollection<MongoTerm, String> termColl_target, ContextualCategory contextualCategory, int skip)
					throws UnknownHostException {
		int counter = 0;
		int interval = 0;
		while (true) {
			try {

				DBCursor<MongoTerm> termCurs = termColl_source.find().skip(skip);
				termCurs.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
				while (termCurs.hasNext()) {
					MongoTerm term = termCurs.next();
					counter++;
					interval++;
					if (interval == 1000) {
						System.out.println(
								String.format("Phase 2 (%s): %d ", contextualCategory.getEntityClass(), counter));
						interval = 0;
					}

					term.setId(null);
					String codeUri = lookupOriginalCodeUri(term.getCodeUri());

					if (codeUri != null) {
						term.setCodeUri(codeUri);
						try {
							termColl_target.insert(term);

						} catch (DuplicateKeyException me) {
							// do nothing
							// System.out.println("Duplicate:" +
							// term.toString());
							continue;
						}

					}
				}
				termCurs.close();
				break;
			} catch (Exception e) {
				e.printStackTrace();
				initialiseConnections();
				counter--;
				skip = counter;
				continue;
			}
		}
		return true;

	}

	private <T extends AbstractEdmEntityImpl, G extends MongoTermList<T>, H extends AbstractRepresentationMigration<T>> boolean migratePhaseThree(
			JacksonDBCollection<G, String> termListColl_source, JacksonDBCollection<G, String> termListColl_target,
			H representationMagrition, ContextualCategory contextualCategory, int skip) throws UnknownHostException {
		int counter = 0;
		int interval = 0;
		while (true) {
			try {

				DBCursor<G> curs = termListColl_source
						.find(new BasicDBObject("entityType", contextualCategory.getEntityClass())).skip(skip);
				curs.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);

				while (curs.hasNext()) {
					G termList = curs.next();
					counter++;
					interval++;
					if (interval == 1000) {
						System.out.println(
								String.format("Phase 3 (%s): %d ", contextualCategory.getEntityClass(), counter));
						interval = 0;
					}

					String originalUri = termList.getCodeUri();
					String codeUri = lookupOriginalCodeUri(termList.getCodeUri());
					termList.setCodeUri(codeUri);
					termList.setId(null);

					String parent = termList.getParent();

					if (StringUtils.isNotBlank(parent)) {

						String parentCodeUri = lookupOriginalCodeUri(parent);
						if (parentCodeUri == null) {
							termList.setParent(null);
						} else {
							termList.setParent(parentCodeUri);
						}
					}

					T representation = termList.getRepresentation();
					representationMagrition.migrateRepresentation(codeUri, originalUri, representation);
					termList.setOwlSameAs(((PlaceImpl)representation).getOwlSameAs());
					try {
						termListColl_target.insert(termList);

					} catch (DuplicateKeyException me) {
						continue;
					}

				}
				curs.close();
				break;
			} catch (Exception e) {
				e.printStackTrace();
				initialiseConnections();
				counter--;
				skip = counter;
				continue;
			}
		}
		return true;
	}

	public void migrateAllPhase() throws UnknownHostException {

		mongoSequence = sequenceColl_target.findOne();
		if (mongoSequence == null) {
			mongoSequence = new MongoSequence();
		}

		/*
		migratePhaseOne(conceptTermListColl_source, conceptTermColl_source, conceptTermListColl_target,
				conceptTermColl_target, ContextualCategory.CONCEPT, "concept");
		migratePhaseOne(timespanTermListColl_source, timespanTermColl_source, timespanTermListColl_target,
				timespanTermColl_target, ContextualCategory.TIMESPAN, "period");
		migratePhaseOne(agentTermListColl_source, agentTermColl_source, agentTermListColl_target, agentTermColl_target,
				ContextualCategory.AGENT, "people");*/
		migratePhaseOne(placeTermListColl_source, placeTermColl_source, placeTermListColl_target, placeTermColl_target,
				ContextualCategory.PLACE, "place");

		// loadCache();

		/*
		migratePhaseTwo(conceptTermColl_source, conceptTermColl_target, ContextualCategory.CONCEPT, 0);
		migratePhaseTwo(timespanTermColl_source, timespanTermColl_target, ContextualCategory.TIMESPAN, 0);
		migratePhaseTwo(agentTermColl_source, agentTermColl_target, ContextualCategory.AGENT, 0);
		*/
		migratePhaseTwo(placeTermColl_source, placeTermColl_target, ContextualCategory.PLACE, 0);

		/*migratePhaseThree(conceptTermListColl_source, conceptTermListColl_target,
				new ConceptRepresentationMigration(lookupCodeUri, lookupOriginalCodeUri), ContextualCategory.CONCEPT,
				0);
		migratePhaseThree(timespanTermListColl_source, timespanTermListColl_target,
				new TimespanRepresentationMigration(lookupCodeUri, lookupOriginalCodeUri), ContextualCategory.TIMESPAN,
				0);
		migratePhaseThree(agentTermListColl_source, agentTermListColl_target,
				new AgentRepresentationMigration(lookupCodeUri, lookupOriginalCodeUri), ContextualCategory.AGENT, 0);*/
		migratePhaseThree(placeTermListColl_source, placeTermListColl_target,
				new PlaceRepresentationMigration(lookupCodeUri, lookupOriginalCodeUri), ContextualCategory.PLACE, 0);

		sequenceColl_target.remove(new BasicDBObject());
		mongoSequence.setId(null);
		sequenceColl_target.insert(mongoSequence);

	}

	public ConceptTermList findOneHavingTerms() {
		DBCursor<ConceptTermList> curs = conceptTermListColl_source
				.find(new BasicDBObject("entityType", ContextualCategory.CONCEPT.getEntityClass()));
		curs.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
		ConceptTermList conceptTermList = null;
		while (curs.hasNext()) {

			ConceptTermList termList = curs.next();
			if (termList.getTerms() != null && !termList.getTerms().isEmpty()) {
				conceptTermList = termList;
				break;
			}
		}
		curs.close();
		return conceptTermList;
	}

	public void createSequence(long nextConceptSequence, long nextAgentSequence, long nextPlaceSequence,
			long nextTimespanSequence) {

		mongoSequence = new MongoSequence();
		mongoSequence.setNextConceptSequence(nextConceptSequence);
		mongoSequence.setNextAgentSequence(nextAgentSequence);
		mongoSequence.setNextPlaceSequence(nextPlaceSequence);
		mongoSequence.setNextTimespanSequence(nextTimespanSequence);
		sequenceColl_target.insert(mongoSequence);
	}
	
	
}
