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
package eu.europeana.enrichment.harvester.geonames;

import eu.europeana.enrichment.harvester.transform.edm.place.PlaceTransformer;
import eu.europeana.metis.json.CustomObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Main class for creating geonames
 * Created by ymamakis on 3/17/16.
 */
public class Main {

    public static void main(String[] args){

        try {
            File[] files = new File("/home/ymamakis/git/tools/annocultor_solr3.5/converters/geonames/input_source/SA").listFiles();
            for(File file:files) {
                if(file.getName().endsWith(".rdf")) {
                    System.out.println("Processing " + file.getName());
                    long start = new Date().getTime();
                    String test = IOUtils.toString(new FileInputStream
                            (file));
                    String prefix = StringUtils.substringBefore(test, "<rdf:D");
                    String suffix = "</rdf:RDF>";
                    test = "<rdf:D" + StringUtils.substringAfter(test, "<rdf:D");
                    test = StringUtils.substringBeforeLast(test, "tion>") + "tion>";
                    String[] countries = test.split("\n\n");
                    PlaceTransformer transformer = new PlaceTransformer();
                    List<String> places = new ArrayList<>();
                    CustomObjectMapper mapper = new CustomObjectMapper();
                    for (String country : countries) {
                        Source doc = new StreamSource(new ByteArrayInputStream((prefix + country + suffix).getBytes()));
                        String uri = StringUtils.substringBetween(country, "rdf:about=\"", "\">");
                        places.add(mapper.writeValueAsString
                                (transformer.transform("/home/ymamakis/git/tools/europeana-enrichment-framework/enrichment/enrichment-framework-knowledgebase/src/main/resources/mapped_geonames_places.xsl",
                                        uri, doc)));
                    }
                    FileUtils.writeLines(new File(file.getParentFile() + "/" + StringUtils.replace(file.getName(), ".rdf", ".edm")), places);
                    System.out.println("Processed " + file.getName() + " in " + (new Date().getTime() - start) + " ms");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
