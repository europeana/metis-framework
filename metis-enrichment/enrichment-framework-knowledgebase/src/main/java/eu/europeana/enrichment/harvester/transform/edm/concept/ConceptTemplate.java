package eu.europeana.enrichment.harvester.transform.edm.concept;

import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.enrichment.harvester.transform.Template;
import eu.europeana.enrichment.harvester.transform.util.CsvUtils;

import java.util.Map;

/**
 * Concept specific implementation of a Template. The template loads the predefined mapping between EDM/XML fields and setter methods and generates a valide 
 * @author gmamakis
 */
public final class ConceptTemplate extends Template<ConceptImpl>{
	
	private static ConceptTemplate instance;
	private static Map<String,String> methodMapping;
	private ConceptTemplate(String filePath){
		methodMapping = CsvUtils.readFile(filePath);
		
	}
        
	public ConceptImpl transform(String xml, String resourceUri) {
		return parse(new ConceptImpl(), resourceUri, xml, methodMapping);
	}

        /**
         * Singleton access to the ConceptTemplate 
         * @return 
         */
	public static ConceptTemplate getInstance(){
		if (instance == null){
			instance = new ConceptTemplate("src/main/resources/conceptMapping.csv");
		}
		return instance;
	}
	
	
	
}
