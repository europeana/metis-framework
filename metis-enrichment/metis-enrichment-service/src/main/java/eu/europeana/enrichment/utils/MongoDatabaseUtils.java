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

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import eu.europeana.enrichment.api.internal.AgentTermList;
import eu.europeana.enrichment.api.internal.ConceptTermList;
import eu.europeana.enrichment.api.internal.MongoTerm;
import eu.europeana.enrichment.api.internal.MongoTermList;
import eu.europeana.enrichment.api.internal.PlaceTermList;
import eu.europeana.enrichment.api.internal.TimespanTermList;
import java.util.ArrayList;
import java.util.List;
import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Util class for saving and retrieving TermLists from Mongo It is used to bypass the memory-based
 * Annocultor enrichment, for use within UIM. The TermList uses MongoTerm, MongoTermList, PlaceTerm
 * and PeriodTerm to reflect the stored Entities.
 *
 * @author Yorgos.Mamakis@ kb.nl
 */
public class MongoDatabaseUtils<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoDatabaseUtils.class);
  
  private static final String AGENT_TABLE = "people";
  private static final String CONCEPT_TABLE = "concept";
  private static final String PLACE_TABLE = "place";
  private static final String TIMESPAN_TABLE = "period";

  private static JacksonDBCollection<ConceptTermList, String> cColl;
  private static JacksonDBCollection<PlaceTermList, String> pColl;
  private static JacksonDBCollection<TimespanTermList, String> tColl;
  private static JacksonDBCollection<AgentTermList, String> aColl;
  private static DB db;

  /**
   * Check if DB exists and initialization of the db
   */
  public static boolean dbExists(String host, int port) {
    try {
      if (db != null) {
        return true;
      }
      System.out.println(host);
      Mongo mongo = new Mongo(host, port);
      db = mongo.getDB("annocultor_db");

      boolean exist = db.collectionExists("TermList");
      cColl = JacksonDBCollection.wrap(
          db.getCollection("TermList"),
          ConceptTermList.class, String.class);

      aColl = JacksonDBCollection.wrap(
          db.getCollection("TermList"), AgentTermList.class,
          String.class);

      tColl = JacksonDBCollection.wrap(
          db.getCollection("TermList"),
          TimespanTermList.class, String.class);

      pColl = JacksonDBCollection.wrap(
          db.getCollection("TermList"), PlaceTermList.class,
          String.class);

      if (!exist) {
        pColl.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", false));
        cColl.createIndex(new BasicDBObject("owlSameAs", 1), new BasicDBObject("unique", false));

        aColl.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", false));
        aColl.createIndex(new BasicDBObject("owlSameAs", 1), new BasicDBObject("unique", false));

        tColl.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", false));
        tColl.createIndex(new BasicDBObject("owlSameAs", 1), new BasicDBObject("unique", false));

        pColl.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", false));
        pColl.createIndex(new BasicDBObject("owlSameAs", 1), new BasicDBObject("unique", false));

      }
      return exist;
    } catch (MongoException e) {
      LOGGER.error("Error accessing mongo", e);
      e.printStackTrace();
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
    }
    return retUris;
  }

  private static List<String> deleteTimespan( String uri) {
    List<String> retUris = new ArrayList<>();
    tColl.remove(tColl.find().is("codeUri", uri).getQuery());
    JacksonDBCollection<MongoTerm, String> termT = JacksonDBCollection
        .wrap(db.getCollection(TIMESPAN_TABLE), MongoTerm.class, String.class);
    termT.createIndex(new BasicDBObject("label", 1).append("lang", 1).append("codeUri", 1),
        new BasicDBObject("unique", true));
    termT.createIndex(new BasicDBObject("codeUri", 1));
    termT.remove(termT.find().is("codeUri", uri).getQuery());
    DBCursor<TimespanTermList> objT = tColl
        .find(new BasicDBObject("owlSameAs", uri).append("entityType", "TimespanImpl"));
    if (objT.hasNext()) {
      String origT = objT.next().getCodeUri();
      retUris.add(origT);
      tColl.remove(new BasicDBObject("codeUri", origT));
      termT.remove(new BasicDBObject("codeUri", origT));
    }
    return retUris;
  }

  private static List<String> deleteAgents(String uri) {
    List<String> retUris = new ArrayList<>();

    aColl.remove(aColl.find().is("codeUri", uri).getQuery());
    JacksonDBCollection<MongoTerm, String> termA = JacksonDBCollection
        .wrap(db.getCollection(AGENT_TABLE), MongoTerm.class, String.class);
    termA.createIndex(new BasicDBObject("label", 1).append("lang", 1).append("codeUri", 1),
        new BasicDBObject("unique", true));
    termA.createIndex(new BasicDBObject("codeUri", 1));
    termA.remove(termA.find().is("codeUri", uri).getQuery());
    DBCursor<AgentTermList> objA = aColl
        .find(new BasicDBObject("owlSameAs", uri).append("entityType", "AgentImpl"));
    if (objA.hasNext()) {
      String origA = objA.next().getCodeUri();
      retUris.add(origA);
      aColl.remove(new BasicDBObject("codeUri", origA));
      termA.remove(new BasicDBObject("codeUri", origA));
    }
    return retUris;
  }

  private static List<String> deleteConcepts(String uri) {
    List<String> retUris = new ArrayList<>();

    cColl.remove(cColl.find().is("codeUri", uri).getQuery());
    JacksonDBCollection<MongoTerm, String> termC = JacksonDBCollection
        .wrap(db.getCollection(CONCEPT_TABLE), MongoTerm.class, String.class);
    termC.createIndex(new BasicDBObject("label", 1).append("lang", 1).append("codeUri", 1),
        new BasicDBObject("unique", true));
    termC.createIndex(new BasicDBObject("codeUri", 1));
    termC.remove(termC.find().is("codeUri", uri).getQuery());
    DBCursor<ConceptTermList> objC = cColl
        .find(new BasicDBObject("owlSameAs", uri).append("entityType", "ConceptImpl"));
    if (objC.hasNext()) {
      String origC = objC.next().getCodeUri();
      retUris.add(origC);
      cColl.remove(new BasicDBObject("codeUri", origC));
      termC.remove(new BasicDBObject("codeUri", origC));
    }
    return retUris;
  }

  private static List<String> deletePlaces(String uri) {
    List<String> retUris = new ArrayList<>();

    pColl.remove(pColl.find().is("codeUri", uri).getQuery());
    JacksonDBCollection<MongoTerm, String> termP = JacksonDBCollection
        .wrap(db.getCollection(PLACE_TABLE), MongoTerm.class, String.class);
    termP.createIndex(new BasicDBObject("label", 1).append("lang", 1).append("codeUri", 1),
        new BasicDBObject("unique", true));
    termP.createIndex(new BasicDBObject("codeUri", 1));
    termP.remove(termP.find().is("codeUri", uri).getQuery());
    DBCursor<PlaceTermList> objP = pColl
        .find(new BasicDBObject("owlSameAs", uri).append("entityType", "PlaceImpl"));
    if (objP.hasNext()) {
      String origP = objP.next().getCodeUri();
      retUris.add(origP);
      pColl.remove(new BasicDBObject("codeUri", origP));
      termP.remove(new BasicDBObject("codeUri", origP));
    }
    return retUris;
  }

  /**
   * Find TermList by codeURI
   */
  public static MongoTermList<?> findByCode(String codeUri, EntityClass entityClass) {
    switch (entityClass) {
      case CONCEPT:
        return findConceptByCode(codeUri);
      case PLACE:
        return findPlaceByCode(codeUri);
      case AGENT:
        return findAgentByCode(codeUri);
      case TIMESPAN:
        return findTimespanByCode(codeUri);
      default:
        return null;
    }
  }

  private static TimespanTermList findTimespanByCode(String codeUri) {
    DBCursor<TimespanTermList> curs = tColl.find(new BasicDBObject("entityType", "TimespanImpl"))
        .is("codeUri", codeUri);
    if (curs.hasNext()) {
      return curs.next();
    }
    return null;
  }

  private static AgentTermList findAgentByCode(String codeUri) {
    DBCursor<AgentTermList> curs = aColl.find(new BasicDBObject("entityType", "AgentImpl"))
        .is("codeUri", codeUri);

    if (curs.hasNext()) {
      return curs.next();
    }
    return null;
  }

  private static PlaceTermList findPlaceByCode(String codeUri) {
    DBCursor<PlaceTermList> curs = pColl.find(new BasicDBObject("entityType", "PlaceImpl"))
        .is("codeUri", codeUri);
    if (curs.hasNext()) {
      return curs.next();
    }
    return null;
  }

  private static ConceptTermList findConceptByCode(String codeUri) {
    DBCursor<ConceptTermList> curs = cColl.find(new BasicDBObject("entityType", "ConceptImpl"))
        .is("codeUri", codeUri);
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
