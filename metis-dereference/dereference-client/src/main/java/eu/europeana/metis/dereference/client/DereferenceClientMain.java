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
package eu.europeana.metis.dereference.client;

import eu.europeana.metis.dereference.ContextualClass;
import eu.europeana.metis.dereference.Vocabulary;

/**
 * Created by gmamakis on 1-3-16.
 */
public class DereferenceClientMain {

    public static void main (String[] args){
        try {
            DereferenceClient client = new DereferenceClient();
            //Vocabulary geonames = new Vocabulary();
            //geonames.setURI("http://sws.geonames.org/");
            //geonames.setXslt(IOUtils.toString(DereferenceClientMain.class.getClassLoader().getResourceAsStream("geonames.xsl")));
            //geonames.setRules("*");
            //geonames.setName("Geonames");
            //geonames.setIterations(0);
            Vocabulary geonames = client.getVocabularyByName("Geonames");
            geonames.setType(ContextualClass.PLACE);
            client.updateVocabulary(geonames);
            client.deleteEntity("http://sws.geonames.org/3020251");
            System.out.println(client.dereference("http://sws.geonames.org/3020251"));
            client.deleteVocabulary("string");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
