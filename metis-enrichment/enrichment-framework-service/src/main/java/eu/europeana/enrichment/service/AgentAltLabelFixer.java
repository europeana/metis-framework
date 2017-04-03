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
package eu.europeana.enrichment.service;

import com.mongodb.DB;
import com.mongodb.Mongo;
import eu.europeana.corelib.definitions.edm.entity.Agent;
import eu.europeana.enrichment.api.internal.AgentTermList;
import eu.europeana.enrichment.api.internal.MongoTerm;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;

/**
 * Created by ymamakis on 8/5/15.
 */
public class AgentAltLabelFixer {

        public static void main (String[] args){
            JacksonDBCollection<AgentTermList, String> cColl;
            Mongo mongo = null;
            mongo = new Mongo("144.76.50.251", 27017);
            DB db = mongo.getDB("annocultor_db");
            cColl = JacksonDBCollection.wrap(
                    db.getCollection("TermList"),
                    AgentTermList.class, String.class);
            JacksonDBCollection<MongoTerm, String> pColl = JacksonDBCollection
                    .wrap(db.getCollection("people"), MongoTerm.class, String.class);
            //pColl.ensureIndex("label");
            DBCursor<MongoTerm> cursor = pColl.find();
            Set<String> uris = new HashSet<>();
            while(cursor.hasNext()){
                MongoTerm term = cursor.next();
                if(StringUtils.contains(term.getCodeUri(),"dbpedia")){
                    uris.add(term.getCodeUri());
                }
            }
            System.out.println("Found uris:" + uris.size());
            for (String uri:uris) {
                DBCursor<AgentTermList> cursorTL = cColl.find().is("codeUri", uri);
                if (cursorTL.hasNext()) {
                    AgentTermList conceptTL =cursorTL.next();

                    Agent concept = conceptTL.getRepresentation();
                    Map<String, List<String>> altLabel = concept.getAltLabel();
                    if(altLabel!=null) {
                        for (Map.Entry<String, List<String>> entry : altLabel.entrySet()) {
                           // DBCursor<MongoTerm> curs = pColl.find().is("codeUri", uri).is("lang", entry.getKey()).is("originalLabel", entry.getValue().get(0));
                           // if (curs.hasNext()) {
                                System.out.println("Removing:" + uri +" altLabel "+ entry.getValue().get(0));
                                pColl.remove(DBQuery.is("codeUri", uri).is("lang", entry.getKey()).is("originalLabel", entry.getValue().get(0)));
                            //}
                        }
                    }
                }
            }


        }
}
