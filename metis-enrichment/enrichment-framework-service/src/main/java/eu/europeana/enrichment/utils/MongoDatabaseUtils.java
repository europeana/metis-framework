package eu.europeana.enrichment.utils;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.enrichment.api.internal.*;
import eu.europeana.enrichment.tagger.vocabularies.*;
import org.apache.commons.lang.StringUtils;
import org.jibx.runtime.JiBXException;
import org.mongojack.DBCursor;
import org.mongojack.DBRef;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Util class for saving and retrieving TermLists from Mongo It is used to
 * bypass the memory-based Annocultor enrichment, for use within UIM. The
 * TermList uses MongoTerm, MongoTermList, PlaceTerm and PeriodTerm to reflect
 * the stored Entities.
 *
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
@SuppressWarnings("rawtypes")
public class MongoDatabaseUtils<T> {

    private static JacksonDBCollection<ConceptTermList, String> cColl;
    private static JacksonDBCollection<PlaceTermList, String> pColl;
    private static JacksonDBCollection<TimespanTermList, String> tColl;
    private static JacksonDBCollection<AgentTermList, String> aColl;
    private static DB db;

    private static Map<String, Map<String, MongoTermList>> memCache = new HashMap<String, Map<String, MongoTermList>>();

    /**
     * Check if DB exists and initialization of the db
     *
     * @return
     */
    public static boolean dbExists(String host, int port) {
        try {
            if (db == null) {

                Mongo mongo = new Mongo(host, port);
                db = mongo.getDB("annocultor_db");
                if (db.collectionExists("TermList")) {
                    cColl = JacksonDBCollection.wrap(
                            db.getCollection("TermList"),
                            ConceptTermList.class, String.class);

                    //cColl.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", true));
                    cColl.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", false));
                    cColl.createIndex(new BasicDBObject("owlSameAs",1), new BasicDBObject("unique",false));
                    aColl = JacksonDBCollection.wrap(
                            db.getCollection("TermList"), AgentTermList.class,
                            String.class);

                    //aColl.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", true));
                    aColl.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", false));
                    aColl.createIndex(new BasicDBObject("owlSameAs",1), new BasicDBObject("unique",false));
                    tColl = JacksonDBCollection.wrap(
                            db.getCollection("TermList"),
                            TimespanTermList.class, String.class);

                    //tColl.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", true));
                    tColl.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", false));
                    tColl.createIndex(new BasicDBObject("owlSameAs",1), new BasicDBObject("unique",false));
                    pColl = JacksonDBCollection.wrap(
                            db.getCollection("TermList"), PlaceTermList.class,
                            String.class);

                    //pColl.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", true));
                    pColl.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", false));
                    pColl.createIndex(new BasicDBObject("owlSameAs",1), new BasicDBObject("unique",false));

                    return true;
                } else {
                    cColl = JacksonDBCollection.wrap(
                            db.getCollection("TermList"),
                            ConceptTermList.class, String.class);

                   // cColl.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", true));
                    pColl.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", false));
                    cColl.createIndex(new BasicDBObject("owlSameAs",1), new BasicDBObject("unique",false));
                    aColl = JacksonDBCollection.wrap(
                            db.getCollection("TermList"), AgentTermList.class,
                            String.class);

                    //aColl.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", true));
                    aColl.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", false));
                    aColl.createIndex(new BasicDBObject("owlSameAs",1), new BasicDBObject("unique",false));
                    tColl = JacksonDBCollection.wrap(
                            db.getCollection("TermList"),
                            TimespanTermList.class, String.class);

                    //tColl.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", true));
                    tColl.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", false));
                    tColl.createIndex(new BasicDBObject("owlSameAs",1), new BasicDBObject("unique",false));
                    pColl = JacksonDBCollection.wrap(
                            db.getCollection("TermList"), PlaceTermList.class,
                            String.class);

                    //pColl.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", true));
                    pColl.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", false));
                    pColl.createIndex(new BasicDBObject("owlSameAs",1), new BasicDBObject("unique",false));
                    return false;
                }
            }
            return true;
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MongoException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Save TermList in mongodB
     *
     * @param dbtabl
     * @param vocabulary
     * @throws JiBXException
     */
    public static void save(String dbtabl, AbstractVocabulary vocabulary)
            throws JiBXException {
        try {
            if (vocabulary instanceof VocabularyOfTime) {
                saveTimespanTerms((VocabularyOfTime) vocabulary);
            } else if (vocabulary instanceof VocabularyOfTerms) {
                saveConceptTerms((VocabularyOfTerms) vocabulary);
            } else if (vocabulary instanceof VocabularyOfPeople) {
                saveAgentTerms((VocabularyOfPeople) vocabulary);
            } else {
                savePlacesTerms((VocabularyOfPlaces) vocabulary);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void emptyCache() {
        memCache.clear();
    }

    public static List<String> delete(List<String> uris) {

        List<String> retUris = new ArrayList<>();
        for (String uri : uris) {
            aColl.remove(aColl.find().is("codeUri", uri).getQuery());
            cColl.remove(cColl.find().is("codeUri", uri).getQuery());
            tColl.remove(tColl.find().is("codeUri", uri).getQuery());
            pColl.remove(pColl.find().is("codeUri", uri).getQuery());
            retUris.add(uri);
            JacksonDBCollection<MongoTerm, String> termP = JacksonDBCollection
                    .wrap(db.getCollection("place"), MongoTerm.class, String.class);
            termP.createIndex(new BasicDBObject("label", 1).append("lang", 1).append("codeUri", 1),
    				new BasicDBObject("unique", true));
            termP.createIndex(new BasicDBObject("codeUri", 1));
            JacksonDBCollection<MongoTerm, String> termC = JacksonDBCollection
                    .wrap(db.getCollection("concept"), MongoTerm.class, String.class);
            termC.createIndex(new BasicDBObject("label", 1).append("lang", 1).append("codeUri", 1),
    				new BasicDBObject("unique", true));
            termC.createIndex(new BasicDBObject("codeUri", 1));
            JacksonDBCollection<MongoTerm, String> termA = JacksonDBCollection
                    .wrap(db.getCollection("people"), MongoTerm.class, String.class);
            termA.createIndex(new BasicDBObject("label", 1).append("lang", 1).append("codeUri", 1),
    				new BasicDBObject("unique", true));
            termA.createIndex(new BasicDBObject("codeUri", 1));
            JacksonDBCollection<MongoTerm, String> termT = JacksonDBCollection
                    .wrap(db.getCollection("period"), MongoTerm.class, String.class);
            termT.createIndex(new BasicDBObject("label", 1).append("lang", 1).append("codeUri", 1),
    				new BasicDBObject("unique", true));
            termT.createIndex(new BasicDBObject("codeUri", 1));


            termP.remove(termP.find().is("codeUri", uri).getQuery());
            termA.remove(termA.find().is("codeUri", uri).getQuery());
            termT.remove(termT.find().is("codeUri", uri).getQuery());
            termC.remove(termC.find().is("codeUri", uri).getQuery());

            DBCursor<AgentTermList> objA = aColl.find(new BasicDBObject("owlSameAs",uri).append("entityType","AgentImpl"));
            if(objA.hasNext()) {
                String origA = objA.next().getCodeUri();
                retUris.add(origA);
                aColl.remove(new BasicDBObject("codeUri",origA));
                termA.remove(new BasicDBObject("codeUri",origA));
            }
            DBCursor<ConceptTermList> objC = cColl.find(new BasicDBObject("owlSameAs",uri).append("entityType","ConceptImpl"));
            if(objC.hasNext()) {
                String origC = objC.next().getCodeUri();
                retUris.add(origC);
                cColl.remove(new BasicDBObject("codeUri",origC));
                termC.remove(new BasicDBObject("codeUri",origC));
            }
            DBCursor<TimespanTermList> objT = tColl.find(new BasicDBObject("owlSameAs",uri).append("entityType","TimespanImpl"));
            if(objT.hasNext()) {
                String origT = objT.next().getCodeUri();
                retUris.add(origT);
                tColl.remove(new BasicDBObject("codeUri",origT));
                termT.remove(new BasicDBObject("codeUri",origT));
            }
            DBCursor<PlaceTermList> objP = pColl.find(new BasicDBObject("owlSameAs",uri).append("entityType","PlaceImpl"));
            if(objP.hasNext()) {
                String origP = objP.next().getCodeUri();
                retUris.add(origP);
                pColl.remove(new BasicDBObject("codeUri",origP));
                termP.remove(new BasicDBObject("codeUri",origP));
            }




        }
        return retUris;
    }

  

    /**
     * Find TermList by codeURI
     *
     * @param codeUri
     * @param dbtable
     * @return
     * @throws MalformedURLException
     */
    public static MongoTermList findByCode(String codeUri, String dbtable)
            throws MalformedURLException {

        if (dbtable.equals("concept")) {
            return findConceptByCode(codeUri, new HashMap<String, MongoTermList>());
        }
        if (dbtable.equals("place")) {
            return findPlaceByCode(codeUri, new HashMap<String, MongoTermList>());
        }
        if (dbtable.equals("people")) {
            return findAgentByCode(codeUri, new HashMap<String, MongoTermList>());
        }
        if (dbtable.equals("period")) {
            return findTimespanByCode(codeUri, new HashMap<String, MongoTermList>());
        }
        return null;
    }

    private static TimespanTermList findTimespanByCode(String codeUri,
            Map<String, MongoTermList> typeMap) {
        DBCursor<TimespanTermList> curs = tColl.find(new BasicDBObject("entityType","TimespanImpl")).is("codeUri", codeUri);
        if (curs.hasNext()) {
            TimespanTermList terms = curs.next();
            typeMap.put(codeUri, terms);
            memCache.put("period", typeMap);
            return terms;
        }
        return null;
    }

    private static MongoTermList findAgentByCode(String codeUri,
            Map<String, MongoTermList> typeMap) {
        DBCursor<AgentTermList> curs = aColl.find(new BasicDBObject("entityType","AgentImpl")).is("codeUri", codeUri);

        if (curs.hasNext()) {
            AgentTermList terms = curs.next();

            return terms;
        }
        return null;
    }

    private static MongoTermList findPlaceByCode(String codeUri,
            Map<String, MongoTermList> typeMap) {
        DBCursor<PlaceTermList> curs = pColl.find(new BasicDBObject("entityType","PlaceImpl")).is("codeUri", codeUri);
        if (curs.hasNext()) {
            PlaceTermList terms = curs.next();

            return terms;
        }
        return null;
    }

    private static MongoTermList findConceptByCode(String codeUri,
            Map<String, MongoTermList> typeMap) {
        DBCursor<ConceptTermList> curs = cColl.find(new BasicDBObject("entityType","ConceptImpl")).is("codeUri", codeUri);
        if (curs.hasNext()) {
            ConceptTermList terms = curs.next();

            return terms;
        }
        return null;
    }

    /**
     * Find terms by label
     *
     * @param label The label to search for
     * @param dbtable The table to search on
     * @return The TermList that corresponds to the label
     * @throws MalformedURLException
     */
    public static MongoTermList findByLabel(String label, String dbtable)
            throws MalformedURLException {
        Map<String, MongoTermList> typeMap = memCache.get(dbtable) != null ? memCache
                .get(dbtable) : new ConcurrentHashMap<String, MongoTermList>();
        if (typeMap.containsKey(label)) {
            return typeMap.get(label.toLowerCase());
        }

        JacksonDBCollection<MongoTerm, String> pColl = JacksonDBCollection
                .wrap(db.getCollection(dbtable), MongoTerm.class, String.class);
        pColl.createIndex(new BasicDBObject("label", 1).append("lang", 1).append("codeUri", 1),
				new BasicDBObject("unique", true));

        DBCursor<MongoTerm> curs = pColl.find()
                .is("label", label.toLowerCase());
        if (curs.hasNext()) {
            MongoTerm mTerm = curs.next();
            MongoTermList t = findByCode(mTerm.getCodeUri(), dbtable);

            return t;
        }
        return null;
    }

    private static void saveConceptTerms(VocabularyOfTerms voc)
            throws IOException, JiBXException {
        // Get all terms by code
        Iterable<TermList> tlList = voc.listAllByCode();

        for (TermList tl : tlList) {
            // Get the first tirm to create the searchable uri
            Term firstTerm = tl.getFirst();
            // Create the mongo term list object
            ConceptTermList termList = new ConceptTermList();
            termList.setCodeUri(firstTerm.getCode());
            ConceptImpl concept = new ConceptImpl();
            concept.setAbout(firstTerm.getCode());

            if (firstTerm.getParent() != null) {
                String[] broader = new String[1];
                termList.setParent(firstTerm.getParent().getCode());
                broader[0] = firstTerm.getParent().getCode();
                concept.setBroader(broader);

            }
            List<DBRef<? extends MongoTerm, String>> pList = new ArrayList<DBRef<? extends MongoTerm, String>>();
            Iterator<Term> iter = tl.iterator();
            Map<String, List<String>> prefLabel = new HashMap<String, List<String>>();
            while (iter.hasNext()) {
                Term term = iter.next();
                MongoTerm pTerm = new MongoTerm();
                pTerm.setCodeUri(term.getCode());
                pTerm.setLabel(term.getLabel().toLowerCase());
                String lang = term.getLang() != null ? term.getLang().getCode()
                        : "def";
                List<String> prefLabelList = prefLabel.get(lang);
                if (prefLabelList == null) {
                    prefLabelList = new ArrayList<String>();
                }
                prefLabelList.add(term.getLabel());
                pTerm.setOriginalLabel(term.getLabel());
                if (term.getLang() != null) {
                    pTerm.setLang(term.getLang().getCode());
                }
                prefLabel.put(lang, prefLabelList);
                JacksonDBCollection<MongoTerm, String> pColl = JacksonDBCollection
                        .wrap(db.getCollection("concept"), MongoTerm.class,
                                String.class);
                pColl.createIndex(new BasicDBObject("label", 1).append("lang", 1).append("codeUri", 1),
        				new BasicDBObject("unique", true));
                pColl.createIndex(new BasicDBObject("codeUri", 1));
                WriteResult<MongoTerm, String> res = pColl.insert(pTerm);
                DBRef<MongoTerm, String> pTermRef = new DBRef<MongoTerm, String>(
                        res.getSavedObject().getId(), "concept");
                pList.add(pTermRef);
            }
            concept.setPrefLabel(prefLabel);
            termList.setTerms(pList);

            termList.setRepresentation(concept);
            termList.setEntityType(ConceptImpl.class.getSimpleName());
            cColl.insert(termList);

        }
    }

    private static void saveAgentTerms(VocabularyOfPeople voc)
            throws IOException, JiBXException {
        // Get all terms by code
        Iterable<TermList> tlList = voc.listAllByCode();

        for (TermList tl : tlList) {
            // Get the first tirm to create the searchable uri
            Term firstTerm = tl.getFirst();
            // Create the mongo term list object
            AgentTermList termList = new AgentTermList();
            termList.setCodeUri(firstTerm.getCode());
            AgentImpl agent = new AgentImpl();
            agent.setAbout(firstTerm.getCode());

            if (firstTerm.getParent() != null) {
                termList.setParent(firstTerm.getParent().getCode());
            }
            List<DBRef<? extends MongoTerm, String>> pList = new ArrayList<DBRef<? extends MongoTerm, String>>();
            Iterator<Term> iter = tl.iterator();
            Map<String, List<String>> plList = new HashMap<String, List<String>>();
            while (iter.hasNext()) {
                Term term = iter.next();
                MongoTerm pTerm = new MongoTerm();
                pTerm.setCodeUri(term.getCode());
                pTerm.setLabel(term.getLabel().toLowerCase());
                String lang = term.getLang() != null ? term.getLang().getCode()
                        : "def";
                List<String> prefLabelList = plList.get(lang);
                if (prefLabelList == null) {
                    prefLabelList = new ArrayList<String>();
                }
                prefLabelList.add(term.getLabel());
                pTerm.setOriginalLabel(term.getLabel());
                if (term.getLang() != null) {
                    pTerm.setLang(term.getLang().getCode());
                }
                plList.put(lang, prefLabelList);
                JacksonDBCollection<MongoTerm, String> pColl = JacksonDBCollection
                        .wrap(db.getCollection("people"), MongoTerm.class,
                                String.class);
                
                pColl.createIndex(new BasicDBObject("label", 1).append("lang", 1).append("codeUri", 1),
        				new BasicDBObject("unique", true));
                pColl.createIndex(new BasicDBObject("codeUri", 1));
                WriteResult<MongoTerm, String> res = pColl.insert(pTerm);
                DBRef<MongoTerm, String> pTermRef = new DBRef<MongoTerm, String>(
                        res.getSavedObject().getId(), "people");
                pList.add(pTermRef);
            }
            termList.setTerms(pList);
            agent.setPrefLabel(plList);

            termList.setRepresentation(agent);
            termList.setEntityType(AgentImpl.class.getSimpleName());
            aColl.insert(termList);

        }
    }
    public static List<MongoTerm> getAllAgents() {
        JacksonDBCollection pColl = JacksonDBCollection.wrap(db.getCollection("people"), MongoTerm.class, String.class);
        DBCursor curs = pColl.find();
        List lst = new ArrayList();
        boolean i = false;

        while(curs.hasNext()) {

                MongoTerm ex = (MongoTerm)curs.next();
                //MongoTermList t = findByCode(ex.getCodeUri(), "people");
                lst.add(ex);

        }

        return lst;
    }

    public static List<MongoTerm> getAllConcepts() {
        JacksonDBCollection pColl = JacksonDBCollection.wrap(db.getCollection("concept"), MongoTerm.class, String.class);
        DBCursor curs = pColl.find();
        List<MongoTerm> lst = new ArrayList<>();

        while(curs.hasNext()) {

                MongoTerm ex = (MongoTerm)curs.next();
              //  MongoTermList t = findByCode(ex.getCodeUri(), "concept");
                lst.add(ex);

        }

        return lst;
    }

    public static List<MongoTerm> getAllPlaces() {
        JacksonDBCollection pColl = JacksonDBCollection.wrap(db.getCollection("place"), MongoTerm.class, String.class);
        DBCursor curs = pColl.find();
        List<MongoTerm> lst = new ArrayList<>();

        while(curs.hasNext()) {

                MongoTerm ex = (MongoTerm)curs.next();
               // MongoTermList t = findByCode(ex.getCodeUri(), "place");
                lst.add(ex);

        }

        return lst;
    }

    public static List<MongoTerm> getAllTimespans() {
        JacksonDBCollection pColl = JacksonDBCollection.wrap(db.getCollection("period"), MongoTerm.class, String.class);
        DBCursor curs = pColl.find();
        List<MongoTerm> lst = new ArrayList<>();

        while(curs.hasNext()) {
            MongoTerm mTerm = (MongoTerm)curs.next();
          //  TimespanTermList t = findTimespanByCode(mTerm.getCodeUri());
            lst.add(mTerm);
        }

        return lst;
    }


    private static TimespanTermList findTimespanByCode(String codeUri) {
        DBCursor curs = (DBCursor)tColl.find().is("codeUri", codeUri);
        if(curs.hasNext()) {
            TimespanTermList terms = (TimespanTermList)curs.next();
            return terms;
        } else {
            return null;
        }
    }
    //Sample conversion code from AgentImpl to AgentTermList
    private static void agentToAgentTermList(AgentImpl agent)
            throws IOException, JiBXException {

        AgentTermList termList = new AgentTermList();
        termList.setCodeUri(agent.getAbout());
        //If it had parents then we should get it now from the dcterms:isPartOf of the Concept and Place or the skos:broader
        //of the Concept and append it here like
        /**
         * String broader = placeImpl.getIsPartOf().get("def").get(0);
         * termList.setParent(broader);
         */

        List<DBRef<? extends MongoTerm, String>> pList = new ArrayList<DBRef<? extends MongoTerm, String>>();

        for (Entry<String, List<String>> prefLabel : agent.getPrefLabel()
                .entrySet()) {
            for (String label : prefLabel.getValue()) {
                MongoTerm pTerm = new MongoTerm();
                pTerm.setCodeUri(agent.getAbout());
                pTerm.setLabel(label.toLowerCase());
                String lang = prefLabel.getKey();

                pTerm.setOriginalLabel(label);

                pTerm.setLang(lang);

                JacksonDBCollection<MongoTerm, String> pColl = JacksonDBCollection
                        .wrap(db.getCollection("people"), MongoTerm.class,
                                String.class);

                pColl.createIndex(new BasicDBObject("label", 1).append("lang", 1).append("codeUri", 1),
        				new BasicDBObject("unique", true));
                pColl.createIndex(new BasicDBObject("codeUri", 1));
                WriteResult<MongoTerm, String> res = pColl.insert(pTerm);
                DBRef<MongoTerm, String> pTermRef = new DBRef<MongoTerm, String>(
                        res.getSavedObject().getId(), "people");
                pList.add(pTermRef);
            }
        }

        termList.setTerms(pList);

        termList.setRepresentation(agent);
        termList.setEntityType(AgentImpl.class.getSimpleName());
        aColl.insert(termList);

    }

    private static void saveTimespanTerms(VocabularyOfTime voc)
            throws IOException, JiBXException {
        // Get all terms by code
        Iterable<TermList> tlList = voc.listAllByCode();

        for (TermList tl : tlList) {
            // Get the first tirm to create the searchable uri
            Term firstTerm = tl.getFirst();
            // Create the mongo term list object
            TimespanTermList termList = new TimespanTermList();
            termList.setCodeUri(firstTerm.getCode());
            TimespanImpl timeSpan = new TimespanImpl();
            timeSpan.setAbout(firstTerm.getCode());

            if (firstTerm.getParent() != null) {
                termList.setParent(firstTerm.getParent().getCode());
                Map<String, List<String>> isPartOf = new HashMap<String, List<String>>();
                List<String> isPartOfList = new ArrayList<String>();
                isPartOfList.add(firstTerm.getParent().getCode());
                isPartOf.put("def", isPartOfList);
                timeSpan.setIsPartOf(isPartOf);
            }
            if (firstTerm.getProperty("begin") != null) {
                Map<String, List<String>> begin = new HashMap<String, List<String>>();
                List<String> beginList = new ArrayList<String>();
                beginList.add(parseDate(firstTerm.getProperty("begin"),
                        "-01-01"));
                begin.put("def", beginList);
                timeSpan.setBegin(begin);
            }
            if (firstTerm.getProperty("end") != null) {
                Map<String, List<String>> end = new HashMap<String, List<String>>();
                List<String> endList = new ArrayList<String>();
                endList.add(parseDate(firstTerm.getProperty("end"), "-12-31"));
                end.put("def", endList);
                timeSpan.setEnd(end);
            }
            List<DBRef<? extends MongoTerm, String>> pList = new ArrayList<DBRef<? extends MongoTerm, String>>();
            Iterator<Term> iter = tl.iterator();
            Map<String, List<String>> plList = new HashMap<String, List<String>>();
            while (iter.hasNext()) {
                Term term = iter.next();
                MongoTerm pTerm = new MongoTerm();
                pTerm.setCodeUri(term.getCode());
                pTerm.setLabel(term.getLabel().toLowerCase());

                pTerm.setOriginalLabel(term.getLabel());
                String lang = term.getLang() != null ? term.getLang().getCode()
                        : "def";
                List<String> prefLabelList = plList.get(lang);
                if (prefLabelList == null) {
                    prefLabelList = new ArrayList<String>();
                }
                prefLabelList.add(term.getLabel());

                if (term.getLang() != null) {
                    pTerm.setLang(term.getLang().getCode());

                }
                plList.put(lang, prefLabelList);

                JacksonDBCollection<MongoTerm, String> pColl = JacksonDBCollection
                        .wrap(db.getCollection("period"), MongoTerm.class,
                                String.class);

                pColl.createIndex(new BasicDBObject("label", 1).append("lang", 1).append("codeUri", 1),
        				new BasicDBObject("unique", true));
                pColl.createIndex(new BasicDBObject("codeUri", 1));
                WriteResult<MongoTerm, String> res = pColl.insert(pTerm);
                DBRef<MongoTerm, String> pTermRef = new DBRef<MongoTerm, String>(
                        res.getSavedObject().getId(), "period");
                pList.add(pTermRef);
            }
            termList.setTerms(pList);
            timeSpan.setPrefLabel(plList);

            termList.setRepresentation(timeSpan);
            termList.setEntityType(TimespanImpl.class.getSimpleName());
            tColl.insert(termList);

        }
    }

    private static void savePlacesTerms(VocabularyOfPlaces voc)
            throws IOException, JiBXException {
        Iterable<TermList> tlList = voc.listAllByCode();

        for (TermList tl : tlList) {
            // Get the first tirm to create the searchable uri
            Term firstTerm = tl.getFirst();
            // Create the mongo term list object
            PlaceTermList termList = new PlaceTermList();
            termList.setCodeUri(firstTerm.getCode());
            PlaceImpl place = new PlaceImpl();
            place.setAbout(firstTerm.getCode());

            if (firstTerm.getParent() != null) {
                termList.setParent(firstTerm.getParent().getCode());
                Map<String, List<String>> isPartOf = new HashMap<String, List<String>>();
                List<String> isPartOfList = new ArrayList<String>();
                isPartOfList.add(firstTerm.getParent().getCode());
                isPartOf.put("def", isPartOfList);
                place.setIsPartOf(isPartOf);
            }
            if (!StringUtils.endsWith(firstTerm.getProperty("division"),
                    "A.PCLI")) {
                place.setLatitude(Float.parseFloat(firstTerm
                        .getProperty("latitude")));
                place.setLongitude(Float.parseFloat(firstTerm
                        .getProperty("longitude")));
            }
            List<DBRef<? extends MongoTerm, String>> pList = new ArrayList<DBRef<? extends MongoTerm, String>>();
            Iterator<Term> iter = tl.iterator();
            Map<String, List<String>> plList = new HashMap<String, List<String>>();
            while (iter.hasNext()) {
                Term term = iter.next();
                MongoTerm pTerm = new MongoTerm();
                pTerm.setCodeUri(term.getCode());
                pTerm.setLabel(term.getLabel().toLowerCase());
                pTerm.setOriginalLabel(term.getLabel());
                String lang = term.getLang() != null ? term.getLang().getCode()
                        : "def";
                List<String> prefLabelList = plList.get(lang);
                if (prefLabelList == null) {
                    prefLabelList = new ArrayList<String>();
                }
                prefLabelList.add(term.getLabel());

                if (term.getLang() != null) {
                    pTerm.setLang(term.getLang().getCode());

                }
                plList.put(lang, prefLabelList);

                JacksonDBCollection<MongoTerm, String> pColl = JacksonDBCollection
                        .wrap(db.getCollection("place"), MongoTerm.class,
                                String.class);
                pColl.createIndex(new BasicDBObject("label", 1).append("lang", 1).append("codeUri", 1),
        				new BasicDBObject("unique", true));
                pColl.createIndex(new BasicDBObject("codeUri", 1));
                WriteResult<MongoTerm, String> res = pColl.insert(pTerm);
                DBRef<MongoTerm, String> pTermRef = new DBRef<MongoTerm, String>(
                        res.getSavedObject().getId(), "place");
                pList.add(pTermRef);
            }
            termList.setTerms(pList);
            place.setPrefLabel(plList);

            termList.setRepresentation(place);
            termList.setEntityType(PlaceImpl.class.getSimpleName());
            pColl.insert(termList);

        }
    }

    private static String parseDate(String dateString,
            String affixToTryOnYearOnly) {
        try {
            if (dateString.length() == 4 && dateString.matches("\\d\\d\\d\\d")) {
                dateString += affixToTryOnYearOnly;
            }
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            if (StringUtils.isEmpty(dateString)) {
                return null;
            }
            return dateFormat.parse(dateString).toString();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

}
