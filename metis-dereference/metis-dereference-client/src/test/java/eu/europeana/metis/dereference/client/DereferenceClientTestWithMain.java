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

import eu.europeana.metis.utils.DereferenceUtils;
import eu.europeana.corelib.definitions.jibx.IsPartOf;
import eu.europeana.corelib.definitions.jibx.PlaceType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by gmamakis on 1-3-16.
 */
public class DereferenceClientTestWithMain {

    public static void main (String[] args) {
        try {
            DereferenceClient client = new DereferenceClient();
            
            String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?><rdf:RDF xmlns:edm=\"http://www.europeana.eu/schemas/edm/\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">" +
            "<edm:ProvidedCHO rdf:about=\"/00903/1008362\"/>" +
            "<edm:WebResource rdf:about=\"/00903/1008362\">" +
            "<edm:rights rdf:resource=\"http://www.europeana.eu/rights/rr-f/\"/>" +
             "</edm:WebResource>" +
            	"<ore:Aggregation xmlns:ore=\"http://www.openarchives.org/ore/terms/\" rdf:about=\"1008362\">" +
              "<edm:aggregatedCHO rdf:resource=\"/00903/1008362\"/>" +
              "<edm:dataProvider>Österreichische Mediathek</edm:dataProvider>" +
              "<edm:isShownAt rdf:resource=\"http://www.mediathek.at/virtuelles-museum/Schifter/Der_Sammler/Nachkriegszeit/Seite_24_24.htm/zone_doc_id=1000867\"/>" +
              "<edm:provider>Österreichische Mediathek</edm:provider>" +
              "<dc:rights xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Österreichische Mediathek</dc:rights>" +
              "<edm:rights rdf:resource=\"http://www.europeana.eu/rights/rr-f/\"/>" +
            "</ore:Aggregation>" +
            "<ore:Proxy xmlns:ore=\"http://www.openarchives.org/ore/terms/\" rdf:about=\"/00903/1008362\">" +
              "<dc:contributor xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Österreichische Mediathek</dc:contributor>" +
              "<dc:coverage xmlns:dc=\"http://purl.org/dc/elements/1.1/\">20. Jahrhundert</dc:coverage>" +
              "<dc:creator xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Schifter, Günther</dc:creator>" +
              "<dc:date xmlns:dc=\"http://purl.org/dc/elements/1.1/\">1998-12-04T00:00:00+00:00</dc:date>" +
              "<dc:date xmlns:dc=\"http://purl.org/dc/elements/1.1/\">1998 12 04</dc:date>" +
              "<dc:format xmlns:dc=\"http://purl.org/dc/elements/1.1/\">flv</dc:format>" +
              "<dc:identifier xmlns:dc=\"http://purl.org/dc/elements/1.1/\">1008362</dc:identifier>" +
              "<dc:language xmlns:dc=\"http://purl.org/dc/elements/1.1/\">DE</dc:language>" +
              "<dc:publisher xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Österreichische Mediathek</dc:publisher>" +
              "<dc:source xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Österreichische Mediathek, V-09157</dc:source>" +
              "<dc:subject xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Erinnerung - Rückblicke - 1946 bis 1970</dc:subject>" +
              "<dc:title xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Anfänge bei Rot-Weiß-Rot</dc:title>" +
              "<dc:type xmlns:dc=\"http://purl.org/dc/elements/1.1/\">video</dc:type>" +
              "<edm:type>VIDEO</edm:type>" +
            "</ore:Proxy>" +
            "<edm:EuropeanaAggregation rdf:about=\"1008362\">" +
              "<edm:aggregatedCHO rdf:resource=\"1008362\"/>" +
              "<edm:country>Austria</edm:country>" +
              "<edm:language>de</edm:language>" +
              "<edm:rights rdf:resource=\"http://www.europeana.eu/rights/rr-f/\"/>" +
            "</edm:EuropeanaAggregation>" +
          "</rdf:RDF>";
            
            RDF rdf = DereferenceUtils.toRDF(xml);
            
            PlaceType place = new PlaceType();
            place.setAbout("http://dummy1.dum");
            
            IsPartOf ipo = new IsPartOf();
            
            ResourceOrLiteralType.Resource resource = new ResourceOrLiteralType.Resource();
            resource.setResource("http://dummy2.dum");
            ipo.setResource(resource);
            
            ArrayList<IsPartOf> ipoList = new ArrayList<IsPartOf>();
            ipoList.add(ipo);
            
            place.setIsPartOfList(ipoList);
            
            ArrayList<PlaceType> placeList = new ArrayList<PlaceType>();
            placeList.add(place);
            
            rdf.setPlaceList(placeList);
            
            //Set<String> result = DereferenceUtils.extractValuesForDereferencing(xml);
            
            Set<String> result = DereferenceUtils.extractValuesForDereferencing(rdf);
            
            Iterator<String> i = result.iterator();
            int count = 0;
            System.out.println("Processing results:");
            while (i.hasNext()) {
            	count++;
            	System.out.println(count + ". " + i.next());
            }
            
            //Vocabulary geonames = new Vocabulary();
            //geonames.setURI("http://sws.geonames.org/");
            //geonames.setXslt(IOUtils.toString(DereferenceClientMain.class.getClassLoader().getResourceAsStream("geonames.xsl")));
            //geonames.setRules("*");
            //geonames.setName("Geonames");
            //geonames.setIterations(0);
           
            //Vocabulary geonames = client.getVocabularyByName("Geonames");
            //geonames.setType(ContextualClass.PLACE);
            //client.updateVocabulary(geonames);
            //client.deleteEntity("http://sws.geonames.org/3020251");
            System.out.println(client.dereference("http://sws.geonames.org/3020251"));
            //client.deleteVocabulary("string");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
