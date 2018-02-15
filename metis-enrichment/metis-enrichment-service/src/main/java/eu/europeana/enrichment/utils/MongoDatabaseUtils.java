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

import eu.europeana.enrichment.api.internal.OrganizationTermList;
import java.util.ArrayList;
import java.util.List;
import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;
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
import eu.europeana.enrichment.api.internal.PlaceTermList;
import eu.europeana.enrichment.api.internal.TimespanTermList;


/**
 * Util class for saving and retrieving TermLists from Mongo It is used to bypass the memory-based
 * Annocultor enrichment, for use within UIM. The TermList uses MongoTerm, MongoTermList, PlaceTerm
 * and PeriodTerm to reflect the stored Entities.
 *
 * @author Yorgos.Mamakis@ kb.nl
 */
public class MongoDatabaseUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoDatabaseUtils.class);

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

  private static JacksonDBCollection<ConceptTermList, String> cColl;
  private static JacksonDBCollection<PlaceTermList, String> pColl;
  private static JacksonDBCollection<TimespanTermList, String> tColl;
  private static JacksonDBCollection<AgentTermList, String> aColl;
  private static JacksonDBCollection<OrganizationTermList, String> oColl;
  
  // TODO the DB class is (effectively) deprecated (see MongoClient.getDB), but
  // this object is still needed for MongoJack. Upgrade MongoJack and migrate this 
  // object to MongoDatabase.
  private static DB db;

  private MongoDatabaseUtils() {}
  
  /**
   * Check if DB exists and initialization of the db
   * 
   * @param host
   * @param port
   * @return whether the DB exists.
   */
  public static synchronized boolean dbExists(String host, int port) {
    if (db != null) {
      return true;
    }
    try(MongoClient mongo = new MongoClient(host, port)) {
      LOGGER.info("Creating Mongo connection to host {}.", host);
      
      db = mongo.getDB("annocultor_db"); // See TODO above.

      boolean exist = db.collectionExists(TERMLIST_TABLE);
      cColl = JacksonDBCollection.wrap(
          db.getCollection(TERMLIST_TABLE),
          ConceptTermList.class, String.class);

      aColl = JacksonDBCollection.wrap(
          db.getCollection(TERMLIST_TABLE), AgentTermList.class,
          String.class);

      tColl = JacksonDBCollection.wrap(
          db.getCollection(TERMLIST_TABLE),
          TimespanTermList.class, String.class);

      pColl = JacksonDBCollection.wrap(
          db.getCollection(TERMLIST_TABLE), PlaceTermList.class,
          String.class);

      oColl = JacksonDBCollection.wrap(
          db.getCollection(ORGANIZATION_TABLE), OrganizationTermList.class,
          String.class);

      if (!exist) {
        pColl.createIndex(new BasicDBObject(TERM_CODE_URI, 1), new BasicDBObject(UNIQUE_PROPERTY, false));
        cColl.createIndex(new BasicDBObject(TERM_SAME_AS, 1), new BasicDBObject(UNIQUE_PROPERTY, false));

        aColl.createIndex(new BasicDBObject(TERM_CODE_URI, 1), new BasicDBObject(UNIQUE_PROPERTY, false));
        aColl.createIndex(new BasicDBObject(TERM_SAME_AS, 1), new BasicDBObject(UNIQUE_PROPERTY, false));

        tColl.createIndex(new BasicDBObject(TERM_CODE_URI, 1), new BasicDBObject(UNIQUE_PROPERTY, false));
        tColl.createIndex(new BasicDBObject(TERM_SAME_AS, 1), new BasicDBObject(UNIQUE_PROPERTY, false));

        pColl.createIndex(new BasicDBObject(TERM_CODE_URI, 1), new BasicDBObject(UNIQUE_PROPERTY, false));
        pColl.createIndex(new BasicDBObject(TERM_SAME_AS, 1), new BasicDBObject(UNIQUE_PROPERTY, false));

        oColl.createIndex(new BasicDBObject(TERM_CODE_URI, 1), new BasicDBObject(UNIQUE_PROPERTY, false));
        oColl.createIndex(new BasicDBObject(TERM_SAME_AS, 1), new BasicDBObject(UNIQUE_PROPERTY, false));
      }
      return exist;
    } catch (MongoException e) {
      LOGGER.error("Error accessing mongo", e);
    }
    return false;
  }

  /**
   * Delete entities by uri
   *
   * @param uris list of uris to delete
   * @return deleted uris
   */
  public static List<String> delete(List<String> uris) {
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

  private static List<String> deleteTimespan( String uri) {
    List<String> retUris = new ArrayList<>();
    tColl.remove(tColl.find().is(TERM_CODE_URI, uri).getQuery());
    JacksonDBCollection<MongoTerm, String> termT = JacksonDBCollection
        .wrap(db.getCollection(TIMESPAN_TABLE), MongoTerm.class, String.class);
    termT.createIndex(new BasicDBObject(TERM_LABEL, 1).append(TERM_LANG, 1).append(TERM_CODE_URI, 1),
        new BasicDBObject(UNIQUE_PROPERTY, true));
    termT.createIndex(new BasicDBObject(TERM_CODE_URI, 1));
    termT.remove(termT.find().is(TERM_CODE_URI, uri).getQuery());
    DBCursor<TimespanTermList> objT = tColl
        .find(new BasicDBObject(TERM_SAME_AS, uri).append(ENTITY_TYPE_PROPERTY, TIMESPAN_TYPE));
    if (objT.hasNext()) {
      String origT = objT.next().getCodeUri();
      retUris.add(origT);
      tColl.remove(new BasicDBObject(TERM_CODE_URI, origT));
      termT.remove(new BasicDBObject(TERM_CODE_URI, origT));
    }
    return retUris;
  }

  private static List<String> deleteAgents(String uri) {
    List<String> retUris = new ArrayList<>();

    aColl.remove(aColl.find().is(TERM_CODE_URI, uri).getQuery());
    JacksonDBCollection<MongoTerm, String> termA = JacksonDBCollection
        .wrap(db.getCollection(AGENT_TABLE), MongoTerm.class, String.class);
    termA.createIndex(new BasicDBObject(TERM_LABEL, 1).append(TERM_LANG, 1).append(TERM_CODE_URI, 1),
        new BasicDBObject(UNIQUE_PROPERTY, true));
    termA.createIndex(new BasicDBObject(TERM_CODE_URI, 1));
    termA.remove(termA.find().is(TERM_CODE_URI, uri).getQuery());
    DBCursor<AgentTermList> objA = aColl
        .find(new BasicDBObject(TERM_SAME_AS, uri).append(ENTITY_TYPE_PROPERTY, AGENT_TYPE));
    if (objA.hasNext()) {
      String origA = objA.next().getCodeUri();
      retUris.add(origA);
      aColl.remove(new BasicDBObject(TERM_CODE_URI, origA));
      termA.remove(new BasicDBObject(TERM_CODE_URI, origA));
    }
    return retUris;
  }

  private static List<String> deleteOrganizations(String uri) {
    List<String> retUris = new ArrayList<>();

    oColl.remove(oColl.find().is(TERM_CODE_URI, uri).getQuery());
    JacksonDBCollection<MongoTerm, String> termA = JacksonDBCollection
        .wrap(db.getCollection(ORGANIZATION_TABLE), MongoTerm.class, String.class);
    termA.createIndex(new BasicDBObject(TERM_LABEL, 1).append(TERM_LANG, 1).append(TERM_CODE_URI, 1),
        new BasicDBObject(UNIQUE_PROPERTY, true));
    termA.createIndex(new BasicDBObject(TERM_CODE_URI, 1));
    termA.remove(termA.find().is(TERM_CODE_URI, uri).getQuery());
    DBCursor<OrganizationTermList> objA = oColl
        .find(new BasicDBObject(TERM_SAME_AS, uri).append(ENTITY_TYPE_PROPERTY, ORGANIZATION_TYPE));
    if (objA.hasNext()) {
      String origA = objA.next().getCodeUri();
      retUris.add(origA);
      oColl.remove(new BasicDBObject(TERM_CODE_URI, origA));
      termA.remove(new BasicDBObject(TERM_CODE_URI, origA));
    }
    return retUris;
  }

  private static List<String> deleteConcepts(String uri) {
    List<String> retUris = new ArrayList<>();

    cColl.remove(cColl.find().is(TERM_CODE_URI, uri).getQuery());
    JacksonDBCollection<MongoTerm, String> termC = JacksonDBCollection
        .wrap(db.getCollection(CONCEPT_TABLE), MongoTerm.class, String.class);
    termC.createIndex(new BasicDBObject(TERM_LABEL, 1).append(TERM_LANG, 1).append(TERM_CODE_URI, 1),
        new BasicDBObject(UNIQUE_PROPERTY, true));
    termC.createIndex(new BasicDBObject(TERM_CODE_URI, 1));
    termC.remove(termC.find().is(TERM_CODE_URI, uri).getQuery());
    DBCursor<ConceptTermList> objC = cColl
        .find(new BasicDBObject(TERM_SAME_AS, uri).append(ENTITY_TYPE_PROPERTY, CONCEPT_TYPE));
    if (objC.hasNext()) {
      String origC = objC.next().getCodeUri();
      retUris.add(origC);
      cColl.remove(new BasicDBObject(TERM_CODE_URI, origC));
      termC.remove(new BasicDBObject(TERM_CODE_URI, origC));
    }
    return retUris;
  }

  private static List<String> deletePlaces(String uri) {
    List<String> retUris = new ArrayList<>();

    pColl.remove(pColl.find().is(TERM_CODE_URI, uri).getQuery());
    JacksonDBCollection<MongoTerm, String> termP = JacksonDBCollection
        .wrap(db.getCollection(PLACE_TABLE), MongoTerm.class, String.class);
    termP.createIndex(new BasicDBObject(TERM_LABEL, 1).append(TERM_LANG, 1).append(TERM_CODE_URI, 1),
        new BasicDBObject(UNIQUE_PROPERTY, true));
    termP.createIndex(new BasicDBObject(TERM_CODE_URI, 1));
    termP.remove(termP.find().is(TERM_CODE_URI, uri).getQuery());
    DBCursor<PlaceTermList> objP = pColl
        .find(new BasicDBObject(TERM_SAME_AS, uri).append(ENTITY_TYPE_PROPERTY, PLACE_TYPE));
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
  public static MongoTermList<ContextualClassImpl> findByCode(String codeUri, EntityClass entityClass) {
    final MongoTermList<? extends ContextualClassImpl> result;
    switch (entityClass) {
      case CONCEPT:
        result = findConceptByCode(codeUri);
        break;
      case PLACE:
        result = findPlaceByCode(codeUri);
        break;
      case AGENT:
        result = findAgentByCode(codeUri);
        break;
      case TIMESPAN:
        result = findTimespanByCode(codeUri);
        break;
      case ORGANIZATION:
        result = findOrganizationByCode(codeUri);
        break;
      default:
        result = null;
        break;
    }
    return MongoTermList.cast(result);
  }

  private static TimespanTermList findTimespanByCode(String codeUri) {
    DBCursor<TimespanTermList> curs = tColl.find(new BasicDBObject(ENTITY_TYPE_PROPERTY, TIMESPAN_TYPE))
        .is(TERM_CODE_URI, codeUri);
    if (curs.hasNext()) {
      return curs.next();
    }
    return null;
  }

  private static AgentTermList findAgentByCode(String codeUri) {
    DBCursor<AgentTermList> curs = aColl.find(new BasicDBObject(ENTITY_TYPE_PROPERTY, AGENT_TYPE))
        .is(TERM_CODE_URI, codeUri);

    if (curs.hasNext()) {
      return curs.next();
    }
    return null;
  }

  private static PlaceTermList findPlaceByCode(String codeUri) {
    DBCursor<PlaceTermList> curs = pColl.find(new BasicDBObject(ENTITY_TYPE_PROPERTY, PLACE_TYPE))
        .is(TERM_CODE_URI, codeUri);
    if (curs.hasNext()) {
      return curs.next();
    }
    return null;
  }

  private static ConceptTermList findConceptByCode(String codeUri) {
    DBCursor<ConceptTermList> curs = cColl.find(new BasicDBObject(ENTITY_TYPE_PROPERTY, CONCEPT_TYPE))
        .is(TERM_CODE_URI, codeUri);
    if (curs.hasNext()) {
      return curs.next();
    }
    return null;
  }

  private static OrganizationTermList findOrganizationByCode(String codeUri) {
    DBCursor<OrganizationTermList> curs = oColl.find(new BasicDBObject(ENTITY_TYPE_PROPERTY, ORGANIZATION_TYPE))
        .is(TERM_CODE_URI, codeUri);
    if (curs.hasNext()) {
      return curs.next();
    }
    return null;
  }

  private static String getTableName(EntityClass entityClass) {
    final String result;
    switch (entityClass) {
      case AGENT:
        result = AGENT_TABLE;
        break;
      case CONCEPT:
        result = CONCEPT_TABLE;
        break;
      case PLACE:
        result = PLACE_TABLE;
        break;
      case TIMESPAN:
        result = TIMESPAN_TABLE;
        break;
      case ORGANIZATION:
        result = ORGANIZATION_TABLE;
        break;
      default:
        throw new IllegalStateException("Unknown entity: " + entityClass);
    }
    return result;
  }

  public static List<MongoTerm> getAllMongoTerms(EntityClass entityClass) {
    JacksonDBCollection<MongoTerm, String> pColl = JacksonDBCollection
        .wrap(db.getCollection(getTableName(entityClass)), MongoTerm.class, String.class);
    DBCursor<MongoTerm> curs = pColl.find();
    List<MongoTerm> lst = new ArrayList<>();

    while (curs.hasNext()) {
      MongoTerm mTerm = curs.next();
      lst.add(mTerm);
    }

    return lst;
  }
}
