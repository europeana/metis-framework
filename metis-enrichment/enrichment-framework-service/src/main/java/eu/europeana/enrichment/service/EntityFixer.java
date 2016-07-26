package eu.europeana.enrichment.service;

import com.google.code.morphia.Morphia;
import com.mongodb.DB;
import com.mongodb.Mongo;
import eu.europeana.corelib.definitions.edm.entity.Concept;
import eu.europeana.enrichment.api.internal.ConceptTermList;
import eu.europeana.enrichment.api.internal.MongoTerm;
import org.apache.commons.lang.StringUtils;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;

import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by ymamakis on 8/5/15.
 */
public class EntityFixer {

        public static void main (String[] args){
            JacksonDBCollection<ConceptTermList, String> cColl;
            Mongo mongo = null;
            try {
                mongo = new Mongo("144.76.50.251", 27017);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            DB db = mongo.getDB("annocultor_db");
            cColl = JacksonDBCollection.wrap(
                    db.getCollection("TermList"),
                    ConceptTermList.class, String.class);
            JacksonDBCollection<MongoTerm, String> pColl = JacksonDBCollection
                    .wrap(db.getCollection("concept"), MongoTerm.class, String.class);
            pColl.ensureIndex("label");
            DBCursor<MongoTerm> cursor = pColl.find();
            Set<String> uris = new HashSet<>();
            while(cursor.hasNext()){
                MongoTerm term = cursor.next();
                if(StringUtils.contains(term.getCodeUri(),"dbpedia")){
                    uris.add(term.getCodeUri());
                }
            }
            List<MongoTerm> termsToInsert = new ArrayList<>();
            for (String uri:uris) {
                DBCursor<ConceptTermList> cursorTL = cColl.find().is("codeUri", uri);
//                if (cursorTL.hasNext()) {
//                    ConceptTermList conceptTL =cursorTL.next();
//
//                    Concept concept = conceptTL.getRepresentation();
//                    Map<String, List<String>> prefLabel = concept.getPrefLabel();
//                    for (Map.Entry<String, List<String>> entry : prefLabel.entrySet()) {
//                        DBCursor<MongoTerm> curs = pColl.find().is("codeUri", uri).is("lang", entry.getKey()).is("originalLabel", entry.getValue().get(0));
//                        if (!curs.hasNext()) {
//                            MongoTerm term = new MongoTerm();
//                            term.setCodeUri(uri);
//                            term.setLang(entry.getKey());
//                            term.setOriginalLabel(entry.getValue().get(0));
//                            term.setLabel(StringUtils.lowerCase(entry.getValue().get(0)));
//                            termsToInsert.add(term);
//
//                        }
//                    }
//                } else {
//                    System.out.println(uri);
//                }
                if(!cursorTL.hasNext()){

                    pColl.remove(DBQuery.is("codeUri",uri));
                }
            }
           // pColl.insert(termsToInsert);


        }
}
