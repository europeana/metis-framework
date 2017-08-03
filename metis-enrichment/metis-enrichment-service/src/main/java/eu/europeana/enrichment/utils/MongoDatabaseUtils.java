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
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;


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
                System.out.println(host);
                Mongo mongo = new Mongo(host, port);
                db = mongo.getDB("annocultor_db");
                if (db.collectionExists("TermList")) {
                    cColl = JacksonDBCollection.wrap(
                            db.getCollection("TermList"),
                            ConceptTermList.class, String.class);

                    //cColl.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", true));
                    //cColl.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", false));
                    //cColl.createIndex(new BasicDBObject("owlSameAs",1), new BasicDBObject("unique",false));
                    aColl = JacksonDBCollection.wrap(
                            db.getCollection("TermList"), AgentTermList.class,
                            String.class);

                    //aColl.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", true));
                    //aColl.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", false));
                    //aColl.createIndex(new BasicDBObject("owlSameAs",1), new BasicDBObject("unique",false));
                    tColl = JacksonDBCollection.wrap(
                            db.getCollection("TermList"),
                            TimespanTermList.class, String.class);

                    //tColl.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", true));
                    //tColl.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", false));
                    //tColl.createIndex(new BasicDBObject("owlSameAs",1), new BasicDBObject("unique",false));
                    pColl = JacksonDBCollection.wrap(
                            db.getCollection("TermList"), PlaceTermList.class,
                            String.class);

                    //pColl.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", true));
                    //pColl.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", false));
                    //pColl.createIndex(new BasicDBObject("owlSameAs",1), new BasicDBObject("unique",false));

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
        } catch (MongoException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
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

}
