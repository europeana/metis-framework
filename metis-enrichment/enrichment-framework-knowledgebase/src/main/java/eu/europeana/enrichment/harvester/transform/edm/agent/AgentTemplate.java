package eu.europeana.enrichment.harvester.transform.edm.agent;

import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.enrichment.harvester.transform.Template;
import eu.europeana.enrichment.harvester.transform.util.CsvUtils;

import java.util.Map;

/**
 * Agent specific implementation of a Template. The template loads the predefined mapping between EDM/XML fields and setter methods and generates a valide 
 * @author gmamakis
 */
public final class AgentTemplate extends Template<AgentImpl>{
	
	private static AgentTemplate instance;
	private static Map<String,String> methodMapping;
	private AgentTemplate(String filePath){
		methodMapping = CsvUtils.readFile(filePath);
		
	}
        
	public AgentImpl transform(String xml, String resourceUri) {
		return parse(new AgentImpl(), resourceUri, xml, methodMapping);
	}

        /**
         * Singleton access to the AgentTemplate 
         * @return 
         */
	public static AgentTemplate getInstance(){
		if (instance == null){
			instance = new AgentTemplate("src/main/resources/agentMapping.csv");
		}
		return instance;
	}
	
	
	
}
