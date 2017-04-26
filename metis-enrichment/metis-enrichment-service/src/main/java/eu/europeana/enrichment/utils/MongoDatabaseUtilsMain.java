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

import eu.europeana.enrichment.api.internal.MongoTermList;

import java.net.MalformedURLException;

/**
 * Created by ymamakis on 3/3/16.
 */
public class MongoDatabaseUtilsMain {

    public static void main (String[] args){
        MongoDatabaseUtils.dbExists("136.243.103.29",27017);
        /**List<String> str = new ArrayList<>();
        str.add("http://sws.geonames.org/3094802/");
        MongoDatabaseUtils.delete(str);*/

        try {
           MongoTermList termList =  MongoDatabaseUtils.findByLabel("Victor Hugo","people");
            System.out.println(termList.getCodeUri());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
