package eu.europeana.enrichment.utils;

import java.io.Closeable;
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
import com.mongodb.MongoClientURI;
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
 * Util class for saving and retrieving TermLists from Mongo It is used to bypass the memory-based
 * Annocultor enrichment, for use within UIM. The TermList uses MongoTerm, MongoTermList, PlaceTerm
 * and PeriodTerm to reflect the stored Entities.
 *
 * @author Yorgos.Mamakis@ kb.nl
 */
public class EnrichmentEntityDao implements Closeable {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnrichmentEntityDao.class);

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

  private final MongoClient mongo;

  private DbAccess dbAccess;

  private static class DbAccess {

    final JacksonDBCollection<ConceptTermList, String> cColl;
    final JacksonDBCollection<PlaceTermList, String> pColl;
    final JacksonDBCollection<TimespanTermList, String> tColl;
    final JacksonDBCollection<AgentTermList, String> aColl;
    final JacksonDBCollection<OrganizationTermList, String> oColl;

    // TODO the DB class is (effectively) deprecated (see MongoClient.getDB), but
    // this object is still needed for MongoJack. Upgrade MongoJack and migrate this
    // object to MongoDatabase.
    final DB db;

    public DbAccess(JacksonDBCollection<ConceptTermList, String> cColl,
        JacksonDBCollection<PlaceTermList, String> pColl,
        JacksonDBCollection<TimespanTermList, String> tColl,
        JacksonDBCollection<AgentTermList, String> aColl,
        JacksonDBCollection<OrganizationTermList, String> oColl, DB db) {
      this.cColl = cColl;
      this.pColl = pColl;
      this.tColl = tColl;
      this.aColl = aColl;
      this.oColl = oColl;
      this.db = db;
    }
  }

  /**
   * @deprecated use {@link #EnrichmentEntityDao(String)}
   * @param host
   * @param port
   */
  public EnrichmentEntityDao(String host, int port) {
    this.mongo = new MongoClient(host, port);
  }

  public EnrichmentEntityDao(String connectionUrl) {
    this.mongo = new MongoClient(new MongoClientURI(connectionUrl));
  }

  @Override
  public void close() {
    mongo.close();
  }

  /*
   * This method is currently called at the start of all incoming public methods.
   */
  private synchronized DbAccess initDbIfNeeded() {
    if (dbAccess != null) {
      return dbAccess;
    }
    try {
      LOGGER.info("Creating Mongo connection to host {}.", mongo.getAddress());

      final DB db = mongo.getDB("annocultor_db"); // See TODO above.

      boolean exist = db.collectionExists(TERMLIST_TABLE);

      final JacksonDBCollection<ConceptTermList, String> cColl = JacksonDBCollection
          .wrap(db.getCollection(TERMLIST_TABLE), ConceptTermList.class, String.class);
      final JacksonDBCollection<AgentTermList, String> aColl = JacksonDBCollection
          .wrap(db.getCollection(TERMLIST_TABLE), AgentTermList.class, String.class);
      final JacksonDBCollection<TimespanTermList, String> tColl = JacksonDBCollection
          .wrap(db.getCollection(TERMLIST_TABLE), TimespanTermList.class, String.class);
      final JacksonDBCollection<PlaceTermList, String> pColl = JacksonDBCollection
          .wrap(db.getCollection(TERMLIST_TABLE), PlaceTermList.class, String.class);
      final JacksonDBCollection<OrganizationTermList, String> oColl = JacksonDBCollection
          .wrap(db.getCollection(TERMLIST_TABLE), OrganizationTermList.class, String.class);

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
      
      dbAccess = new DbAccess(cColl, pColl, tColl, aColl, oColl, db);
      return dbAccess;
          
    } catch (MongoException e) {
      LOGGER.error("Error accessing mongo", e);
      return null;
    }
  }

  /**
   * Delete entities by uri
   *
   * @param uris list of uris to delete
   * @return deleted uris
   */
  public List<String> delete(List<String> uris) {
    final DbAccess dbAccess = initDbIfNeeded();
    List<String> retUris = new ArrayList<>();
    for (String uri : uris) {
      retUris.add(uri);
      retUris.addAll(deletePlaces(dbAccess, uri));
      retUris.addAll(deleteConcepts(dbAccess, uri));
      retUris.addAll(deleteAgents(dbAccess, uri));
      retUris.addAll(deleteTimespan(dbAccess, uri));
      retUris.addAll(deleteOrganizations(dbAccess, uri));
    }
    return retUris;
  }

  private static List<String> deleteTimespan(DbAccess dbAccess, String uri) {
    List<String> retUris = new ArrayList<>();
    dbAccess.tColl.remove(dbAccess.tColl.find().is(TERM_CODE_URI, uri).getQuery());
    JacksonDBCollection<MongoTerm, String> termT =
        JacksonDBCollection.wrap(dbAccess.db.getCollection(TIMESPAN_TABLE), MongoTerm.class, String.class);
    termT.createIndex(
        new BasicDBObject(TERM_LABEL, 1).append(TERM_LANG, 1).append(TERM_CODE_URI, 1),
        new BasicDBObject(UNIQUE_PROPERTY, Boolean.TRUE));
    termT.createIndex(new BasicDBObject(TERM_CODE_URI, 1));
    termT.remove(termT.find().is(TERM_CODE_URI, uri).getQuery());
    DBCursor<TimespanTermList> objT = dbAccess.tColl
        .find(new BasicDBObject(TERM_SAME_AS, uri).append(ENTITY_TYPE_PROPERTY, TIMESPAN_TYPE));
    if (objT.hasNext()) {
      String origT = objT.next().getCodeUri();
      retUris.add(origT);
      dbAccess.tColl.remove(new BasicDBObject(TERM_CODE_URI, origT));
      termT.remove(new BasicDBObject(TERM_CODE_URI, origT));
    }
    return retUris;
  }

  private static List<String> deleteAgents(DbAccess dbAccess, String uri) {
    List<String> retUris = new ArrayList<>();

    dbAccess.aColl.remove(dbAccess.aColl.find().is(TERM_CODE_URI, uri).getQuery());
    JacksonDBCollection<MongoTerm, String> termA =
        JacksonDBCollection.wrap(dbAccess.db.getCollection(AGENT_TABLE), MongoTerm.class, String.class);
    termA.createIndex(
        new BasicDBObject(TERM_LABEL, 1).append(TERM_LANG, 1).append(TERM_CODE_URI, 1),
        new BasicDBObject(UNIQUE_PROPERTY, Boolean.TRUE));
    termA.createIndex(new BasicDBObject(TERM_CODE_URI, 1));
    termA.remove(termA.find().is(TERM_CODE_URI, uri).getQuery());
    DBCursor<AgentTermList> objA =
        dbAccess.aColl.find(new BasicDBObject(TERM_SAME_AS, uri).append(ENTITY_TYPE_PROPERTY, AGENT_TYPE));
    if (objA.hasNext()) {
      String origA = objA.next().getCodeUri();
      retUris.add(origA);
      dbAccess.aColl.remove(new BasicDBObject(TERM_CODE_URI, origA));
      termA.remove(new BasicDBObject(TERM_CODE_URI, origA));
    }
    return retUris;
  }

  public void deleteOrganizations(String uri) {
    deleteOrganizations(initDbIfNeeded(), uri);
  }
  
  // TODO: rename to deleteOrganization
  private static List<String> deleteOrganizations(DbAccess dbAccess, String uri) {
    List<String> retUris = new ArrayList<>();

    dbAccess.oColl.remove(dbAccess.oColl.find().is(TERM_CODE_URI, uri).getQuery());

    JacksonDBCollection<MongoTerm, String> termA = deleteOrganizationTerms(dbAccess, uri);

    termA.createIndex(
        new BasicDBObject(TERM_LABEL, 1).append(TERM_LANG, 1).append(TERM_CODE_URI, 1),
        new BasicDBObject(UNIQUE_PROPERTY, Boolean.TRUE));
    termA.createIndex(new BasicDBObject(TERM_CODE_URI, 1));
    DBCursor<OrganizationTermList> objA = dbAccess.oColl
        .find(new BasicDBObject(TERM_SAME_AS, uri).append(ENTITY_TYPE_PROPERTY, ORGANIZATION_TYPE));
    if (objA.hasNext()) {
      String origA = objA.next().getCodeUri();
      retUris.add(origA);
      dbAccess.oColl.remove(new BasicDBObject(TERM_CODE_URI, origA));
      termA.remove(new BasicDBObject(TERM_CODE_URI, origA));
    }
    return retUris;
  }

  public void deleteOrganizationTerms(String uri) {
    deleteOrganizationTerms(initDbIfNeeded(), uri);
  }
  
  private static JacksonDBCollection<MongoTerm, String> deleteOrganizationTerms(DbAccess dbAccess, String uri) {
    JacksonDBCollection<MongoTerm, String> termA = JacksonDBCollection
        .wrap(dbAccess.db.getCollection(ORGANIZATION_TABLE), MongoTerm.class, String.class);
    termA.remove(termA.find().is(TERM_CODE_URI, uri).getQuery());
    return termA;
  }

  private static List<String> deleteConcepts(DbAccess dbAccess, String uri) {
    List<String> retUris = new ArrayList<>();

    dbAccess.cColl.remove(dbAccess.cColl.find().is(TERM_CODE_URI, uri).getQuery());
    JacksonDBCollection<MongoTerm, String> termC =
        JacksonDBCollection.wrap(dbAccess.db.getCollection(CONCEPT_TABLE), MongoTerm.class, String.class);
    termC.createIndex(
        new BasicDBObject(TERM_LABEL, 1).append(TERM_LANG, 1).append(TERM_CODE_URI, 1),
        new BasicDBObject(UNIQUE_PROPERTY, Boolean.TRUE));
    termC.createIndex(new BasicDBObject(TERM_CODE_URI, 1));
    termC.remove(termC.find().is(TERM_CODE_URI, uri).getQuery());
    DBCursor<ConceptTermList> objC =
        dbAccess.cColl.find(new BasicDBObject(TERM_SAME_AS, uri).append(ENTITY_TYPE_PROPERTY, CONCEPT_TYPE));
    if (objC.hasNext()) {
      String origC = objC.next().getCodeUri();
      retUris.add(origC);
      dbAccess.cColl.remove(new BasicDBObject(TERM_CODE_URI, origC));
      termC.remove(new BasicDBObject(TERM_CODE_URI, origC));
    }
    return retUris;
  }

  private static List<String> deletePlaces(DbAccess dbAccess, String uri) {
    List<String> retUris = new ArrayList<>();

    dbAccess.pColl.remove(dbAccess.pColl.find().is(TERM_CODE_URI, uri).getQuery());
    JacksonDBCollection<MongoTerm, String> termP =
        JacksonDBCollection.wrap(dbAccess.db.getCollection(PLACE_TABLE), MongoTerm.class, String.class);
    termP.createIndex(
        new BasicDBObject(TERM_LABEL, 1).append(TERM_LANG, 1).append(TERM_CODE_URI, 1),
        new BasicDBObject(UNIQUE_PROPERTY, Boolean.TRUE));
    termP.createIndex(new BasicDBObject(TERM_CODE_URI, 1));
    termP.remove(termP.find().is(TERM_CODE_URI, uri).getQuery());
    DBCursor<PlaceTermList> objP =
        dbAccess.pColl.find(new BasicDBObject(TERM_SAME_AS, uri).append(ENTITY_TYPE_PROPERTY, PLACE_TYPE));
    if (objP.hasNext()) {
      String origP = objP.next().getCodeUri();
      retUris.add(origP);
      dbAccess.pColl.remove(new BasicDBObject(TERM_CODE_URI, origP));
      termP.remove(new BasicDBObject(TERM_CODE_URI, origP));
    }
    return retUris;
  }

  /**
   * Find TermList by codeURI
   *
   * @return the term list.
   */
  public MongoTermList<ContextualClassImpl> findByCode(String codeUri, EntityClass entityClass) {
    final DbAccess dbAccess = initDbIfNeeded();
    final MongoTermList<? extends ContextualClassImpl> result;
    switch (entityClass) {
      case CONCEPT:
        result = findConceptByCode(dbAccess, codeUri);
        break;
      case PLACE:
        result = findPlaceByCode(dbAccess, codeUri);
        break;
      case AGENT:
        result = findAgentByCode(dbAccess, codeUri);
        break;
      case TIMESPAN:
        result = findTimespanByCode(dbAccess, codeUri);
        break;
      case ORGANIZATION:
        result = findOrganizationByCode(dbAccess, codeUri);
        break;
      default:
        result = null;
        break;
    }
    return MongoTermList.cast(result);
  }

  private static TimespanTermList findTimespanByCode(DbAccess dbAccess, String codeUri) {
    DBCursor<TimespanTermList> curs = dbAccess.tColl
        .find(new BasicDBObject(ENTITY_TYPE_PROPERTY, TIMESPAN_TYPE)).is(TERM_CODE_URI, codeUri);
    if (curs.hasNext()) {
      return curs.next();
    }
    return null;
  }

  private static AgentTermList findAgentByCode(DbAccess dbAccess, String codeUri) {
    DBCursor<AgentTermList> curs = dbAccess.aColl
        .find(new BasicDBObject(ENTITY_TYPE_PROPERTY, AGENT_TYPE)).is(TERM_CODE_URI, codeUri);
    if (curs.hasNext()) {
      return curs.next();
    }
    return null;
  }

  private static PlaceTermList findPlaceByCode(DbAccess dbAccess, String codeUri) {
    DBCursor<PlaceTermList> curs = dbAccess.pColl
        .find(new BasicDBObject(ENTITY_TYPE_PROPERTY, PLACE_TYPE)).is(TERM_CODE_URI, codeUri);
    if (curs.hasNext()) {
      return curs.next();
    }
    return null;
  }

  private static ConceptTermList findConceptByCode(DbAccess dbAccess, String codeUri) {
    DBCursor<ConceptTermList> curs = dbAccess.cColl
        .find(new BasicDBObject(ENTITY_TYPE_PROPERTY, CONCEPT_TYPE)).is(TERM_CODE_URI, codeUri);
    if (curs.hasNext()) {
      return curs.next();
    }
    return null;
  }

  private static OrganizationTermList findOrganizationByCode(DbAccess dbAccess, String codeUri) {
    DBCursor<OrganizationTermList> curs = dbAccess.oColl
        .find(new BasicDBObject(ENTITY_TYPE_PROPERTY, ORGANIZATION_TYPE)).is(TERM_CODE_URI, codeUri);
    if (curs.hasNext()) {
      return curs.next();
    }
    return null;
  }

  private String getTableName(EntityClass entityClass) {
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

  private static String getTypeName(EntityClass entityClass) {
    final String result;
    if (entityClass == EntityClass.ORGANIZATION) {
      result = ORGANIZATION_TYPE;
    } else {
      throw new IllegalStateException("Unknown entity: " + entityClass);
    }
    return result;
  }

  public List<MongoTerm> getAllMongoTerms(EntityClass entityClass) {
    final DbAccess dbAccess = initDbIfNeeded();
    JacksonDBCollection<MongoTerm, String> collection = JacksonDBCollection
        .wrap(dbAccess.db.getCollection(getTableName(entityClass)), MongoTerm.class, String.class);
    DBCursor<MongoTerm> curs = collection.find();
    return StreamSupport.stream(curs.spliterator(), false).collect(Collectors.toList());
  }

  /**
   * This method stores the provided object in the database. If the _id is present, it will
   * overwrite the existing database record
   */
  public OrganizationTermList storeMongoTermList(
      MongoTermList<? extends ContextualClassImpl> termList) {
    final DbAccess dbAccess = initDbIfNeeded();
    String type = termList.getEntityType();
    // TODO add support for other entity types
    if (ORGANIZATION_TYPE.equals(type)) {
      return saveOrganization(dbAccess, (OrganizationTermList) termList);
    } else {
      throw new IllegalArgumentException(
          "insertion of MongoTermList of type: " + type + " not supported yet!");
    }
  }

  private static OrganizationTermList saveOrganization(DbAccess dbAccess, OrganizationTermList termList) {
    return dbAccess.oColl.save(termList).getSavedObject();
  }

  public int storeEntityLabels(ContextualClassImpl entity, EntityClass entityClass) {
    final DbAccess dbAccess = initDbIfNeeded();
    // select collection
    String collection = getTableName(entityClass);
    JacksonDBCollection<MongoTerm, String> termColl =
        JacksonDBCollection.wrap(dbAccess.db.getCollection(collection), MongoTerm.class, String.class);

    // store terms
    List<MongoTerm> terms = createListOfMongoTerms(entity);
    WriteResult<MongoTerm, String> res = termColl.insert(terms);

    return res.getN();
  }

  private static List<MongoTerm> createListOfMongoTerms(ContextualClassImpl entity) {
    MongoTerm term;
    List<MongoTerm> terms = new ArrayList<>();
    String lang;

    for (Map.Entry<String, List<String>> prefLabel : entity.getPrefLabel().entrySet()) {
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
   * @param entityClass The type of the entity e.g. organization
   * @return the last modified date for passed entity class
   */
  public Date getLastModifiedDate(EntityClass entityClass) {
    final DbAccess dbAccess = initDbIfNeeded();
    DBCursor<OrganizationTermList> cursor =
        dbAccess.oColl.find(new BasicDBObject(ENTITY_TYPE_PROPERTY, getTypeName(entityClass)))
            .sort(DBSort.desc(TERM_MODIFIED)).limit(1);
    // empty results
    if (cursor.size() == 0) {
      return null;
    }
    // last imported item
    OrganizationTermList lastModifiedOrg = cursor.next();

    return lastModifiedOrg.getModified();
  }

}
