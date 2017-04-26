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
package eu.europeana.enrichment.harvester.transform.edm.place;

import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.enrichment.harvester.transform.Template;
import eu.europeana.enrichment.harvester.transform.util.CsvUtils;

import java.util.Map;

/**
 * Place specific template
 * Created by ymamakis on 3/17/16.
 */
public class PlaceTemplate extends Template<PlaceImpl> {
    private static PlaceTemplate instance;
    private static Map<String,String> methodMapping;
    private PlaceTemplate(String filePath){
        methodMapping = CsvUtils.readFile(filePath);

    }

    public PlaceImpl transform(String xml, String resourceUri) {
        return parse(new PlaceImpl(), resourceUri, xml, methodMapping);
    }

    /**
     * Singleton access to the ConceptTemplate
     * @return
     */
    public static PlaceTemplate getInstance(){
        if (instance == null){
            instance = new PlaceTemplate("/home/ymamakis/git/tools/europeana-enrichment-framework/enrichment/enrichment-framework-knowledgebase/src/main/resources/placeMapping.csv");
        }
        return instance;
    }
}
