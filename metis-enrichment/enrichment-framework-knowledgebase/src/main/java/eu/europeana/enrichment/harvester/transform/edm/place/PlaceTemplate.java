package eu.europeana.enrichment.harvester.transform.edm.place;

import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.enrichment.harvester.transform.Template;
import eu.europeana.enrichment.harvester.transform.util.CsvUtils;

import java.util.Map;

/**
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
