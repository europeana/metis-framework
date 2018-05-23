/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.enrichment.utils;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.mongojack.DBCursor;
import org.mongojack.DBSort;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import eu.europeana.corelib.solr.entity.ContextualClassImpl;
import eu.europeana.enrichment.api.internal.AgentTermList;
import eu.europeana.enrichment.api.internal.ConceptTermList;
import eu.europeana.enrichment.api.internal.MongoTerm;
import eu.europeana.enrichment.api.internal.MongoTermList;
import eu.europeana.enrichment.api.internal.OrganizationTermList;
import eu.europeana.enrichment.api.internal.PlaceTermList;
import eu.europeana.enrichment.api.internal.TimespanTermList;

/**
 * Util class for saving and retrieving TermLists from Mongo It is used to
 * bypass the memory-based Annocultor enrichment, for use within UIM. The
 * TermList uses MongoTerm, MongoTermList, PlaceTerm and PeriodTerm to reflect
 * the stored Entities.
 *
 * @author Yorgos.Mamakis@ kb.nl
 */
public class EnrichmentEntityDao implements Closeable {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(EnrichmentEntityDao.class);

	private static final String ENTITY_TYPE_PROPERTY = "entityType";
	private static final String PLACE_TYPE = "PlaceImpl";
	private static final String CONCEPT_TYPE = "ConceptImpl";
	private static final String AGENT_TYPE = "AgentImpl";
	private static final String TIMESPAN_TYPE = "TimespanImpl";
	private static final String ORGANIZATION_TYPE = "OrganizationImpl";

	private static final String AGENT_TABLE = "people";
	private static final String CONCEPT_TABLE = "concept";
	private static final String PLACE_TABLE = "place";
	private static final String TIMESPAN_TABLE = "period";
	private static final String ORGANIZATION_TABLE = "organization";
	private static final String TERMLIST_TABLE = "TermList";

	private static final String UNIQUE_PROPERTY = "unique";
	private static final String TERM_SAME_AS = "owlSameAs";
	private static final String TERM_CODE_URI = "codeUri";
	private static final String TERM_LANG = "lang";
	private static final String TERM_LABEL = "label";
	private static final String TERM_MODIFIED = "modified";
	
	private JacksonDBCollection<ConceptTermList, String> cColl;
	private JacksonDBCollection<PlaceTermList, String> pColl;
	private JacksonDBCollection<TimespanTermList, String> tColl;
	private JacksonDBCollection<AgentTermList, String> aColl;
	private JacksonDBCollection<OrganizationTermList, String> oColl;

  	// TODO the DB class is (effectively) deprecated (see MongoClient.getDB), but
  	// this object is still needed for MongoJack. Upgrade MongoJack and migrate this 
	// object to MongoDatabase.
  	private DB db;
  	
  	private final MongoClient mongo;

    public EnrichmentEntityDao(String host, int port) {
      this.mongo = new MongoClient(host, port);
    }
  
    @Override
    public void close() throws IOException {
      mongo.close();
    }

    /*
     * This method is currently called at the start of all incoming public methods.
     */
	private synchronized void initDbIfNeeded() {
		if (db != null) {
			return;
		}
		try {
			LOGGER.info("Creating Mongo connection to host {}.", mongo.getAddress());

			db = mongo.getDB("annocultor_db"); // See TODO above.

			boolean exist = db.collectionExists(TERMLIST_TABLE);

			cColl = JacksonDBCollection.wrap(db.getCollection(TERMLIST_TABLE),
					ConceptTermList.class, String.class);

			aColl = JacksonDBCollection.wrap(db.getCollection(TERMLIST_TABLE),
					AgentTermList.class, String.class);

			tColl = JacksonDBCollection.wrap(db.getCollection(TERMLIST_TABLE),
					TimespanTermList.class, String.class);

			pColl = JacksonDBCollection.wrap(db.getCollection(TERMLIST_TABLE),
					PlaceTermList.class, String.class);

			oColl = JacksonDBCollection.wrap(db.getCollection(TERMLIST_TABLE),
					OrganizationTermList.class, String.class);

			// TODO: Sergiu looks like the following commands need to be
			// updated. All c,a,t,p,oColl are mapped to the TermList table
			// so there is a lot o redundancy in the commands. Shouldn't these
			// collections be mapped to the corresponding
			// concept/agent/timespan/person/organization tables?
			if (!exist) {
				cColl.createIndex(new BasicDBObject(TERM_CODE_URI, 1),
						new BasicDBObject(UNIQUE_PROPERTY, Boolean.TRUE));
				cColl.createIndex(new BasicDBObject(TERM_SAME_AS, 1),
						new BasicDBObject(UNIQUE_PROPERTY, Boolean.FALSE));
				cColl.createIndex(new BasicDBObject(ENTITY_TYPE_PROPERTY, 1),
						new BasicDBObject(UNIQUE_PROPERTY, Boolean.FALSE));

				aColl.createIndex(new BasicDBObject(TERM_CODE_URI, 1),
						new BasicDBObject(UNIQUE_PROPERTY, Boolean.TRUE));
				aColl.createIndex(new BasicDBObject(TERM_SAME_AS, 1),
						new BasicDBObject(UNIQUE_PROPERTY, Boolean.FALSE));
				aColl.createIndex(new BasicDBObject(ENTITY_TYPE_PROPERTY, 1),
						new BasicDBObject(UNIQUE_PROPERTY, Boolean.FALSE));

				tColl.createIndex(new BasicDBObject(TERM_CODE_URI, 1),
						new BasicDBObject(UNIQUE_PROPERTY, Boolean.TRUE));
				tColl.createIndex(new BasicDBObject(TERM_SAME_AS, 1),
						new BasicDBObject(UNIQUE_PROPERTY, Boolean.FALSE));
				tColl.createIndex(new BasicDBObject(ENTITY_TYPE_PROPERTY, 1),
						new BasicDBObject(UNIQUE_PROPERTY, Boolean.FALSE));

				pColl.createIndex(new BasicDBObject(TERM_CODE_URI, 1),
						new BasicDBObject(UNIQUE_PROPERTY, Boolean.TRUE));
				pColl.createIndex(new BasicDBObject(TERM_SAME_AS, 1),
						new BasicDBObject(UNIQUE_PROPERTY, Boolean.FALSE));
				pColl.createIndex(new BasicDBObject(ENTITY_TYPE_PROPERTY, 1),
						new BasicDBObject(UNIQUE_PROPERTY, Boolean.FALSE));

				oColl.createIndex(new BasicDBObject(TERM_CODE_URI, 1),
						new BasicDBObject(UNIQUE_PROPERTY, Boolean.TRUE));
				oColl.createIndex(new BasicDBObject(TERM_SAME_AS, 1),
						new BasicDBObject(UNIQUE_PROPERTY, Boolean.FALSE));
				oColl.createIndex(new BasicDBObject(ENTITY_TYPE_PROPERTY, 1),
						new BasicDBObject(UNIQUE_PROPERTY, Boolean.FALSE));
			}
		} catch (MongoException e) {
			LOGGER.error("Error accessing mongo", e);
		}
	}

	/**
	 * Delete entities by uri
	 *
	 * @param uris
	 *            list of uris to delete
	 * @return deleted uris
	 */
	public List<String> delete(List<String> uris) {
	    initDbIfNeeded();	  
		List<String> retUris = new ArrayList<>();
		for (String uri : uris) {
			retUris.add(uri);
			retUris.addAll(deletePlaces(uri));
			retUris.addAll(deleteConcepts(uri));
			retUris.addAll(deleteAgents(uri));
			retUris.addAll(deleteTimespan(uri));
			retUris.addAll(deleteOrganizations(uri));
		}
		return retUris;
	}

	private List<String> deleteTimespan(String uri) {
		List<String> retUris = new ArrayList<>();
		tColl.remove(tColl.find().is(TERM_CODE_URI, uri).getQuery());
		JacksonDBCollection<MongoTerm, String> termT = JacksonDBCollection.wrap(
				db.getCollection(TIMESPAN_TABLE), MongoTerm.class,
				String.class);
		termT.createIndex(
				new BasicDBObject(TERM_LABEL, 1).append(TERM_LANG, 1)
						.append(TERM_CODE_URI, 1),
				new BasicDBObject(UNIQUE_PROPERTY, Boolean.TRUE));
		termT.createIndex(new BasicDBObject(TERM_CODE_URI, 1));
		termT.remove(termT.find().is(TERM_CODE_URI, uri).getQuery());
		DBCursor<TimespanTermList> objT = tColl
				.find(new BasicDBObject(TERM_SAME_AS, uri)
						.append(ENTITY_TYPE_PROPERTY, TIMESPAN_TYPE));
		if (objT.hasNext()) {
			String origT = objT.next().getCodeUri();
			retUris.add(origT);
			tColl.remove(new BasicDBObject(TERM_CODE_URI, origT));
			termT.remove(new BasicDBObject(TERM_CODE_URI, origT));
		}
		return retUris;
	}

	private List<String> deleteAgents(String uri) {
		List<String> retUris = new ArrayList<>();

		aColl.remove(aColl.find().is(TERM_CODE_URI, uri).getQuery());
		JacksonDBCollection<MongoTerm, String> termA = JacksonDBCollection.wrap(
				db.getCollection(AGENT_TABLE), MongoTerm.class, String.class);
		termA.createIndex(
				new BasicDBObject(TERM_LABEL, 1).append(TERM_LANG, 1)
						.append(TERM_CODE_URI, 1),
				new BasicDBObject(UNIQUE_PROPERTY, Boolean.TRUE));
		termA.createIndex(new BasicDBObject(TERM_CODE_URI, 1));
		termA.remove(termA.find().is(TERM_CODE_URI, uri).getQuery());
		DBCursor<AgentTermList> objA = aColl
				.find(new BasicDBObject(TERM_SAME_AS, uri)
						.append(ENTITY_TYPE_PROPERTY, AGENT_TYPE));
		if (objA.hasNext()) {
			String origA = objA.next().getCodeUri();
			retUris.add(origA);
			aColl.remove(new BasicDBObject(TERM_CODE_URI, origA));
			termA.remove(new BasicDBObject(TERM_CODE_URI, origA));
		}
		return retUris;
	}

	// TODO: rename to deleteOrganization
	public List<String> deleteOrganizations(String uri) {
	    initDbIfNeeded();     
		List<String> retUris = new ArrayList<>();

		oColl.remove(oColl.find().is(TERM_CODE_URI, uri).getQuery());
		
		JacksonDBCollection<MongoTerm, String> termA = deleteOrganizationTerms(
				uri);
		
		termA.createIndex(
				new BasicDBObject(TERM_LABEL, 1).append(TERM_LANG, 1)
						.append(TERM_CODE_URI, 1),
				new BasicDBObject(UNIQUE_PROPERTY, Boolean.TRUE));
		termA.createIndex(new BasicDBObject(TERM_CODE_URI, 1));
		DBCursor<OrganizationTermList> objA = oColl
				.find(new BasicDBObject(TERM_SAME_AS, uri)
						.append(ENTITY_TYPE_PROPERTY, ORGANIZATION_TYPE));
		if (objA.hasNext()) {
			String origA = objA.next().getCodeUri();
			retUris.add(origA);
			oColl.remove(new BasicDBObject(TERM_CODE_URI, origA));
			termA.remove(new BasicDBObject(TERM_CODE_URI, origA));
		}
		return retUris;
	}

	public JacksonDBCollection<MongoTerm, String> deleteOrganizationTerms(
			String uri) {
	    initDbIfNeeded();     
		JacksonDBCollection<MongoTerm, String> termA = JacksonDBCollection.wrap(
				db.getCollection(ORGANIZATION_TABLE), MongoTerm.class,
				String.class);
		termA.remove(termA.find().is(TERM_CODE_URI, uri).getQuery());
		return termA;
	}

	private List<String> deleteConcepts(String uri) {
		List<String> retUris = new ArrayList<>();

		cColl.remove(cColl.find().is(TERM_CODE_URI, uri).getQuery());
		JacksonDBCollection<MongoTerm, String> termC = JacksonDBCollection.wrap(
				db.getCollection(CONCEPT_TABLE), MongoTerm.class, String.class);
		termC.createIndex(
				new BasicDBObject(TERM_LABEL, 1).append(TERM_LANG, 1)
						.append(TERM_CODE_URI, 1),
				new BasicDBObject(UNIQUE_PROPERTY, Boolean.TRUE));
		termC.createIndex(new BasicDBObject(TERM_CODE_URI, 1));
		termC.remove(termC.find().is(TERM_CODE_URI, uri).getQuery());
		DBCursor<ConceptTermList> objC = cColl
				.find(new BasicDBObject(TERM_SAME_AS, uri)
						.append(ENTITY_TYPE_PROPERTY, CONCEPT_TYPE));
		if (objC.hasNext()) {
			String origC = objC.next().getCodeUri();
			retUris.add(origC);
			cColl.remove(new BasicDBObject(TERM_CODE_URI, origC));
			termC.remove(new BasicDBObject(TERM_CODE_URI, origC));
		}
		return retUris;
	}

	private List<String> deletePlaces(String uri) {
		List<String> retUris = new ArrayList<>();

		pColl.remove(pColl.find().is(TERM_CODE_URI, uri).getQuery());
		JacksonDBCollection<MongoTerm, String> termP = JacksonDBCollection.wrap(
				db.getCollection(PLACE_TABLE), MongoTerm.class, String.class);
		termP.createIndex(
				new BasicDBObject(TERM_LABEL, 1).append(TERM_LANG, 1)
						.append(TERM_CODE_URI, 1),
				new BasicDBObject(UNIQUE_PROPERTY, Boolean.TRUE));
		termP.createIndex(new BasicDBObject(TERM_CODE_URI, 1));
		termP.remove(termP.find().is(TERM_CODE_URI, uri).getQuery());
		DBCursor<PlaceTermList> objP = pColl
				.find(new BasicDBObject(TERM_SAME_AS, uri)
						.append(ENTITY_TYPE_PROPERTY, PLACE_TYPE));
		if (objP.hasNext()) {
			String origP = objP.next().getCodeUri();
			retUris.add(origP);
			pColl.remove(new BasicDBObject(TERM_CODE_URI, origP));
			termP.remove(new BasicDBObject(TERM_CODE_URI, origP));
		}
		return retUris;
	}

	/**
	 * Find TermList by codeURI
	 * 
	 * @param codeUri
	 * @param entityClass
	 * @return the term list.
	 */
	public MongoTermList<ContextualClassImpl> findByCode(String codeUri,
			EntityClass entityClass) {
	    initDbIfNeeded();     
		final MongoTermList<? extends ContextualClassImpl> result;
		switch (entityClass) {
			case CONCEPT :
				result = findConceptByCode(codeUri);
				break;
			case PLACE :
				result = findPlaceByCode(codeUri);
				break;
			case AGENT :
				result = findAgentByCode(codeUri);
				break;
			case TIMESPAN :
				result = findTimespanByCode(codeUri);
				break;
			case ORGANIZATION :
				result = findOrganizationByCode(codeUri);
				break;
			default :
				result = null;
				break;
		}
		return MongoTermList.cast(result);
	}

	private TimespanTermList findTimespanByCode(String codeUri) {
		DBCursor<TimespanTermList> curs = tColl
				.find(new BasicDBObject(ENTITY_TYPE_PROPERTY, TIMESPAN_TYPE))
				.is(TERM_CODE_URI, codeUri);
		if (curs.hasNext()) {
			return curs.next();
		}
		return null;
	}

	private AgentTermList findAgentByCode(String codeUri) {
		DBCursor<AgentTermList> curs = aColl
				.find(new BasicDBObject(ENTITY_TYPE_PROPERTY, AGENT_TYPE))
				.is(TERM_CODE_URI, codeUri);

		if (curs.hasNext()) {
			return curs.next();
		}
		return null;
	}

	private PlaceTermList findPlaceByCode(String codeUri) {
		DBCursor<PlaceTermList> curs = pColl
				.find(new BasicDBObject(ENTITY_TYPE_PROPERTY, PLACE_TYPE))
				.is(TERM_CODE_URI, codeUri);
		if (curs.hasNext()) {
			return curs.next();
		}
		return null;
	}

	private ConceptTermList findConceptByCode(String codeUri) {
		DBCursor<ConceptTermList> curs = cColl
				.find(new BasicDBObject(ENTITY_TYPE_PROPERTY, CONCEPT_TYPE))
				.is(TERM_CODE_URI, codeUri);
		if (curs.hasNext()) {
			return curs.next();
		}
		return null;
	}

	private OrganizationTermList findOrganizationByCode(String codeUri) {
		DBCursor<OrganizationTermList> curs = oColl.find(
				new BasicDBObject(ENTITY_TYPE_PROPERTY, ORGANIZATION_TYPE))
				.is(TERM_CODE_URI, codeUri);
		if (curs.hasNext()) {
			return curs.next();
		}
		return null;
	}

	private String getTableName(EntityClass entityClass) {
		final String result;
		switch (entityClass) {
			case AGENT :
				result = AGENT_TABLE;
				break;
			case CONCEPT :
				result = CONCEPT_TABLE;
				break;
			case PLACE :
				result = PLACE_TABLE;
				break;
			case TIMESPAN :
				result = TIMESPAN_TABLE;
				break;
			case ORGANIZATION :
				result = ORGANIZATION_TABLE;
				break;
			default :
				throw new IllegalStateException(
						"Unknown entity: " + entityClass);
		}
		return result;
	}

	private static String getTypeName(EntityClass entityClass) {
		final String result;
		switch (entityClass) {
			case ORGANIZATION :
				result = ORGANIZATION_TYPE;
				break;
			default :
				throw new IllegalStateException(
						"Unknown entity: " + entityClass);
		}
		return result;
	}

	public List<MongoTerm> getAllMongoTerms(EntityClass entityClass) {
	    initDbIfNeeded();     
		JacksonDBCollection<MongoTerm, String> pColl = JacksonDBCollection.wrap(
				db.getCollection(getTableName(entityClass)), MongoTerm.class,
				String.class);
		DBCursor<MongoTerm> curs = pColl.find();
		return StreamSupport.stream(curs.spliterator(), false).collect(Collectors.toList());
	}

	/**
	 * This method stores the provided object in the database. If the _id is present, it will overwrite the existing database record
	 * @param termList
	 * @return
	 */
	public MongoTermList<? extends ContextualClassImpl> storeMongoTermList(
			MongoTermList<? extends ContextualClassImpl> termList) {
        initDbIfNeeded();     
		String type = termList.getEntityType();
		switch (type) {
			case ORGANIZATION_TYPE :
				return saveOrganization((OrganizationTermList) termList);

			default : // TODO add support for other entity types
				throw new IllegalArgumentException(
						"insertion of MongoTermList of type: " + type
								+ " not supported yet!");

		}

	}

	private OrganizationTermList saveOrganization(
			OrganizationTermList termList) {
		return oColl.save(termList).getSavedObject();
	}

	public int storeEntityLabels(ContextualClassImpl entity,
			EntityClass entityClass) {
	    initDbIfNeeded();     
		// select collection
		String collection = getTableName(entityClass);
		JacksonDBCollection<MongoTerm, String> termColl = JacksonDBCollection
				.wrap(db.getCollection(collection), MongoTerm.class,
						String.class);

		// store terms
		List<MongoTerm> terms = createListOfMongoTerms(entity);
		WriteResult<MongoTerm, String> res = termColl.insert(terms);

		return res.getN();
	}

	private static List<MongoTerm> createListOfMongoTerms(
			ContextualClassImpl entity) {
		MongoTerm term;
		List<MongoTerm> terms = new ArrayList<MongoTerm>();
		String lang;

		for (Map.Entry<String, List<String>> prefLabel : entity.getPrefLabel()
				.entrySet()) {
			for (String label : prefLabel.getValue()) {
				term = new MongoTerm();
				term.setCodeUri(entity.getAbout());
				term.setLabel(label.toLowerCase());
				lang = prefLabel.getKey();

				term.setOriginalLabel(label);
				term.setLang(lang);
				terms.add(term);
			}
		}
		return terms;
	}

	/**
	 * This method returns last modified date for passed entity class.
	 * 
	 * @param entityClass
	 *            The type of the entity e.g. organization
	 * @return the last modified date for passed entity class
	 */
	public Date getLastModifiedDate(EntityClass entityClass) {
	    initDbIfNeeded();     
		DBCursor<OrganizationTermList> cursor = oColl
				.find(new BasicDBObject(ENTITY_TYPE_PROPERTY,
						getTypeName(entityClass)))
				.sort(DBSort.desc(TERM_MODIFIED)).limit(1);
		// empty results
		if (cursor.size() == 0)
			return null;
		// last imported item
		OrganizationTermList lastModifiedOrg = cursor.next();

		return lastModifiedOrg.getModified();
	}

}
