package eu.europeana.enrichment.harvester.util;

import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import org.apache.commons.validator.routines.UrlValidator;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.IOException;

public class MongoDataSerializer {

	public MongoDataSerializer() {
		// TODO Auto-generated constructor stub
	}


	public Node serializeAgent(Document doc, AgentImpl agent){

		

		Element agentElement = doc.createElement("edm:Agent");
		agentElement.setAttribute("rdf:about", agent.getAbout());
		if(agent.getPrefLabel()!=null){
			for (String attrVal:agent.getPrefLabel().keySet())
				agentElement.appendChild(getElements(doc, agentElement, "skos:prefLabel", "xml:lang", attrVal, agent.getPrefLabel().get(attrVal) ));
		}
		
		if(agent.getAltLabel()!=null){
			for (String attrVal:agent.getAltLabel().keySet())
				agentElement.appendChild(getElements(doc, agentElement, "skos:altLabel", "xml:lang", attrVal, agent.getAltLabel().get(attrVal) ));
		}

		if(agent.getHiddenLabel()!=null){
			for (String attrVal:agent.getHiddenLabel().keySet())
				agentElement.appendChild(getElements(doc, agentElement, "skos:hiddenLabel", "xml:lang", attrVal, agent.getHiddenLabel().get(attrVal) ));
		}
		if(agent.getFoafName()!=null){
			for (String attrVal:agent.getFoafName().keySet())
				agentElement.appendChild(getElements(doc, agentElement, "foaf:name", "xml:lang", attrVal, agent.getFoafName().get(attrVal) ));
		}
		if(agent.getNote()!=null){
			for (String attrVal:agent.getNote().keySet())
				agentElement.appendChild(getElements(doc, agentElement, "skos:note", "xml:lang", attrVal, agent.getNote().get(attrVal) ));
		}

		if(agent.getBegin()!=null){
			for (String attrVal:agent.getBegin().keySet())
				agentElement.appendChild(getElements(doc, agentElement, "edm:begin", "xml:lang", attrVal, agent.getBegin().get(attrVal) ));
		}
		if(agent.getEnd()!=null){
			for (String attrVal:agent.getEnd().keySet())
				agentElement.appendChild(getElements(doc, agentElement, "edm:end", "xml:lang", attrVal, agent.getEnd().get(attrVal) ));
		}
		
		if(agent.getDcIdentifier()!=null){
			for (String attrVal:agent.getDcIdentifier().keySet())
				agentElement.appendChild(getElements(doc, agentElement, "dc:identifier", "xml:lang", attrVal, agent.getDcIdentifier().get(attrVal) ));
		}
		
		if(agent.getEdmHasMet()!=null){
			for (String attrVal:agent.getEdmHasMet().keySet())
				agentElement.appendChild(getElements(doc, agentElement, "edm:hasMet", "xml:lang", attrVal, agent.getEdmHasMet().get(attrVal) ));
		}
		
		if(agent.getRdaGr2BiographicalInformation()!=null){
			for (String attrVal:agent.getRdaGr2BiographicalInformation().keySet())
				agentElement.appendChild(getElements(doc, agentElement, "rdaGr2:biographicalInformation", "xml:lang", attrVal, agent.getRdaGr2BiographicalInformation().get(attrVal) ));
		}
		if(agent.getRdaGr2DateOfBirth()!=null){
			for (String attrVal:agent.getRdaGr2DateOfBirth().keySet())
				agentElement.appendChild(getElements(doc, agentElement, "rdaGr2:dateOfBirth", "xml:lang", attrVal, agent.getRdaGr2DateOfBirth().get(attrVal) ));
		}
		
		if(agent.getRdaGr2DateOfDeath()!=null){
			for (String attrVal:agent.getRdaGr2DateOfDeath().keySet())
				agentElement.appendChild(getElements(doc, agentElement, "rdaGr2:dateOfDeath", "xml:lang", attrVal, agent.getRdaGr2DateOfDeath().get(attrVal) ));
		}
		if(agent.getRdaGr2DateOfEstablishment()!=null){
			for (String attrVal:agent.getRdaGr2DateOfEstablishment().keySet())
				agentElement.appendChild(getElements(doc, agentElement, "rdaGr2:dateOfEstablishment", "xml:lang", attrVal, agent.getRdaGr2DateOfEstablishment().get(attrVal) ));
		}
		
		if(agent.getRdaGr2DateOfTermination()!=null){
			for (String attrVal:agent.getRdaGr2DateOfTermination().keySet())
				agentElement.appendChild(getElements(doc, agentElement, "rdaGr2:dateOfTermination", "xml:lang", attrVal, agent.getRdaGr2DateOfTermination().get(attrVal) ));
		}
		
		if(agent.getRdaGr2Gender()!=null){
			for (String attrVal:agent.getRdaGr2Gender().keySet())
				agentElement.appendChild(getElements(doc, agentElement, "rdaGr2:gender", "xml:lang", attrVal, agent.getRdaGr2Gender().get(attrVal) ));
		}
		
		if(agent.getEdmWasPresentAt()!=null){
			for (String attrVal:agent.getEdmWasPresentAt())
				agentElement.appendChild(getElements(doc, agentElement, "edm:wasPresentAt", "rdf:resource", attrVal, attrVal ));
		}
		
		if(agent.getOwlSameAs()!=null){
			for (String attrVal:agent.getOwlSameAs())
				agentElement.appendChild(getElements(doc, agentElement, "owl:sameAs", "rdf:resource", attrVal, attrVal ));
		}
		
		if (agent.getEdmIsRelatedTo()!=null){
			for (String attrVal:agent.getEdmIsRelatedTo().keySet()){
				agentElement.appendChild(getElements(doc, agentElement, "edm:isRelatedTo", attrVal, agent.getEdmIsRelatedTo().get(attrVal) ));
			}
			
		}
		
		if (agent.getDcDate()!=null){
			for (String attrVal:agent.getDcDate().keySet()){
				agentElement.appendChild(getElements(doc, agentElement, "dc:date", attrVal, agent.getDcDate().get(attrVal) ));
			}
			
		}
		
		if (agent.getRdaGr2ProfessionOrOccupation()!=null){
			for (String attrVal:agent.getRdaGr2ProfessionOrOccupation().keySet()){
				agentElement.appendChild(getElements(doc, agentElement, "rdaGr2:professionOrOccupation", attrVal, agent.getRdaGr2ProfessionOrOccupation().get(attrVal) ));
			}
			
		}
		/*try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource source = new DOMSource(doc);
			DOMSource source1 = new DOMSource(conceptElement);
			//StreamResult result = new StreamResult(new File("C:\\file.xml"));

			// Output to console for testing
			StreamResult result = new StreamResult(System.out);

			transformer.transform(source1, result);
			
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		return agentElement;
	
	}
	
	public Node serializeAgentsAsStored(Document doc, AgentImpl agent){

		

		Element agentElement = doc.createElement("edm:Agent");
		
		agentElement.setAttribute("rdf:about", agent.getAbout());
		if(agent.getPrefLabel()!=null){
			for (String attrVal:agent.getPrefLabel().keySet()){
				for (String val:agent.getPrefLabel().get(attrVal))
					agentElement.appendChild(getElements(doc, "skos:prefLabel", "xml:lang", attrVal, val));
			}
		}
		
		
		if(agent.getAltLabel()!=null){
			for (String attrVal:agent.getAltLabel().keySet()){
				for (String val:agent.getAltLabel().get(attrVal))
					agentElement.appendChild(getElements(doc, "skos:altLabel", "xml:lang", attrVal, val));
			}
		}

		if(agent.getHiddenLabel()!=null){
			for (String attrVal:agent.getHiddenLabel().keySet())
				for (String val:agent.getHiddenLabel().get(attrVal))
					agentElement.appendChild(getElements(doc, "skos:hiddenLabel", "xml:lang", attrVal, val));
		}
		if(agent.getFoafName()!=null){
			for (String attrVal:agent.getFoafName().keySet())
				for (String val:agent.getFoafName().get(attrVal))
					agentElement.appendChild(getElements(doc, "foaf:name", "xml:lang", attrVal, val));
		}
		if(agent.getNote()!=null){
			for (String attrVal:agent.getNote().keySet())
				for (String val:agent.getNote().get(attrVal))
					agentElement.appendChild(getElements(doc, "skos:note", "xml:lang", attrVal, val));
		}

		if(agent.getBegin()!=null){
			for (String attrVal:agent.getBegin().keySet())
				for (String val:agent.getBegin().get(attrVal))
					agentElement.appendChild(getElements(doc, "edm:begin", "xml:lang", attrVal, val));
		}
		if(agent.getEnd()!=null){
			for (String attrVal:agent.getEnd().keySet())
				for (String val:agent.getEnd().get(attrVal))
					agentElement.appendChild(getElements(doc, "edm:end", "xml:lang", attrVal, val));
		}
		
		if(agent.getDcIdentifier()!=null){
			for (String attrVal:agent.getDcIdentifier().keySet())
				for (String val:agent.getDcIdentifier().get(attrVal))
					agentElement.appendChild(getElements(doc, "dc:identifier", "xml:lang", attrVal, val));

		}
		
		if(agent.getEdmHasMet()!=null){
			for (String attrVal:agent.getEdmHasMet().keySet())
				for (String val:agent.getEdmHasMet().get(attrVal))
					agentElement.appendChild(getElements(doc, "edm:hasMet", "xml:lang", attrVal, val));
		}
		
		if(agent.getRdaGr2BiographicalInformation()!=null){
			for (String attrVal:agent.getRdaGr2BiographicalInformation().keySet())
				//for (String val:agent.getRdaGr2BiographicalInformation().get(attrVal))
				//	agentElement.appendChild(getElements(doc, "rdaGr2:biographicalInformation", "xml:lang", attrVal, val));
				agentElement.appendChild(getElements(doc, agentElement, "rdaGr2:biographicalInformation", attrVal, agent.getRdaGr2BiographicalInformation().get(attrVal) ));

			if(agent.getRdaGr2PlaceOfBirth()!=null){
				for (String attrVal:agent.getRdaGr2PlaceOfBirth().keySet())
					for (String val:agent.getRdaGr2PlaceOfBirth().get(attrVal))
						agentElement.appendChild(getElements(doc, "rdaGr2:placeOfBirth", "xml:lang", attrVal, val));
					//agentElement.appendChild(getElements(doc, agentElement, "rdaGr2:placeOfBirth", attrVal, agent.getRdaGr2PlaceOfBirth().get(attrVal) ));

		
		}
			if(agent.getRdaGr2PlaceOfDeath()!=null){
				for (String attrVal:agent.getRdaGr2PlaceOfDeath().keySet())
					for (String val:agent.getRdaGr2PlaceOfDeath().get(attrVal))
						agentElement.appendChild(getElements(doc, "rdaGr2:placeOfDeath", "xml:lang", attrVal, val));
					//agentElement.appendChild(getElements(doc, agentElement, "rdaGr2:placeOfDeath", attrVal, agent.getRdaGr2PlaceOfDeath().get(attrVal) ));
			}
		
		
		
		}
		if(agent.getRdaGr2DateOfBirth()!=null){
			for (String attrVal:agent.getRdaGr2DateOfBirth().keySet())
				for (String val:agent.getRdaGr2DateOfBirth().get(attrVal))
					agentElement.appendChild(getElements(doc, "rdaGr2:dateOfBirth", "xml:lang", attrVal, val));
			
		}
		
		if(agent.getRdaGr2DateOfDeath()!=null){
			for (String attrVal:agent.getRdaGr2DateOfDeath().keySet())
				for (String val:agent.getRdaGr2DateOfDeath().get(attrVal))
					agentElement.appendChild(getElements(doc, "rdaGr2:dateOfDeath", "xml:lang", attrVal, val));
			

		}
		if(agent.getRdaGr2DateOfEstablishment()!=null){
			for (String attrVal:agent.getRdaGr2DateOfEstablishment().keySet())
				for (String val:agent.getRdaGr2DateOfEstablishment().get(attrVal))
					agentElement.appendChild(getElements(doc, "rdaGr2:dateOfEstablishment", "xml:lang", attrVal, val));
	

		}
		
		if(agent.getRdaGr2DateOfTermination()!=null){
			for (String attrVal:agent.getRdaGr2DateOfTermination().keySet())
				for (String val:agent.getRdaGr2DateOfTermination().get(attrVal))
					agentElement.appendChild(getElements(doc, "rdaGr2:dateOfTermination", "xml:lang", attrVal, val));
				
		}
		
		if(agent.getRdaGr2Gender()!=null){
			for (String attrVal:agent.getRdaGr2Gender().keySet())
				for (String val:agent.getRdaGr2Gender().get(attrVal))
					agentElement.appendChild(getElements(doc, "rdaGr2:gender", "xml:lang", attrVal, val));
;
		}
		
		if(agent.getEdmWasPresentAt()!=null){
			for (String attrVal:agent.getEdmWasPresentAt())
				agentElement.appendChild(getElements(doc, agentElement, "edm:wasPresentAt", "rdf:resource", attrVal, attrVal ));
		}
		
		if(agent.getOwlSameAs()!=null){
			for (String attrVal:agent.getOwlSameAs())
				agentElement.appendChild(getElements(doc, agentElement, "owl:sameAs", "rdf:resource", attrVal, attrVal ));
		}
		
		if (agent.getEdmIsRelatedTo()!=null){
			for (String attrVal:agent.getEdmIsRelatedTo().keySet()){
				for (String val:agent.getEdmIsRelatedTo().get(attrVal))
				//	agentElement.appendChild(getElements(doc, "edm:isRelatedTo", "xml:lang", attrVal, val));
				agentElement.appendChild(getElement(doc, agentElement, "edm:isRelatedTo", attrVal, val ));
			}
			
		}
		
		if (agent.getDcDate()!=null){
			for (String attrVal:agent.getDcDate().keySet()){
				//for (String val:agent.getDcDate().get(attrVal))
				//	agentElement.appendChild(getElements(doc, "dc:date", "xml:lang", attrVal, val));
				agentElement.appendChild(getElements(doc, agentElement, "dc:date", attrVal, agent.getDcDate().get(attrVal) ));

			}
			
		}
		
		if (agent.getRdaGr2ProfessionOrOccupation()!=null){
			for (String attrVal:agent.getRdaGr2ProfessionOrOccupation().keySet()){
				for (String val:agent.getRdaGr2ProfessionOrOccupation().get(attrVal))
				//	agentElement.appendChild(getElements(doc, "rdaGr2:professionOrOccupation", "xml:lang", attrVal, val));
					agentElement.appendChild(getElement(doc, agentElement, "rdaGr2:professionOrOccupation", attrVal, val ));

				

			}
			
		}
		
		return agentElement;
	
	}

	public Node serializeConcept(Document doc, ConceptImpl concept)
			throws JsonParseException, JsonMappingException, IOException {

		Element conceptElement = doc.createElement("skos:Concept");
		conceptElement.setAttribute("rdf:about", concept.getAbout());
		if(concept.getPrefLabel()!=null){
			for (String attrVal:concept.getPrefLabel().keySet())
				conceptElement.appendChild(getElements(doc, conceptElement, "skos:prefLabel", "xml:lang", attrVal, concept.getPrefLabel().get(attrVal) ));
		}
		if(concept.getAltLabel()!=null){
			for (String attrVal:concept.getAltLabel().keySet())
				conceptElement.appendChild(getElements(doc, conceptElement, "skos:altLabel", "xml:lang", attrVal, concept.getAltLabel().get(attrVal) ));
		}

		if(concept.getHiddenLabel()!=null){
			for (String attrVal:concept.getHiddenLabel().keySet())
				conceptElement.appendChild(getElements(doc, conceptElement, "skos:hiddenLabel", "xml:lang", attrVal, concept.getHiddenLabel().get(attrVal) ));
		}
		if(concept.getNotation()!=null){
			for (String attrVal:concept.getNotation().keySet())
				conceptElement.appendChild(getElements(doc, conceptElement, "skos:notation", "xml:lang", attrVal, concept.getNotation().get(attrVal) ));
		}
		if(concept.getNote()!=null){
			for (String attrVal:concept.getNote().keySet())
				conceptElement.appendChild(getElements(doc, conceptElement, "skos:note", "xml:lang", attrVal, concept.getNote().get(attrVal) ));
		}

		if(concept.getBroader()!=null){
			for (String attrVal:concept.getBroader())
				conceptElement.appendChild(getElements(doc, conceptElement, "skos:broader", "rdf:resource", attrVal, attrVal ));
		}
		
		if(concept.getBroadMatch()!=null){
			for (String attrVal:concept.getBroadMatch())
				conceptElement.appendChild(getElements(doc, conceptElement, "skos:broadMatch", "rdf:resource", attrVal, attrVal ));
		}
		
		if(concept.getCloseMatch()!=null){
			for (String attrVal:concept.getCloseMatch())
				conceptElement.appendChild(getElements(doc, conceptElement, "skos:closeMatch", "rdf:resource", attrVal, attrVal));
		}
		if(concept.getExactMatch()!=null){
			for (String attrVal:concept.getExactMatch())
				conceptElement.appendChild(getElements(doc, conceptElement, "skos:exactMatch", "rdf:resource", attrVal, attrVal));
		}
		if(concept.getInScheme()!=null){
			for (String attrVal:concept.getInScheme())
				conceptElement.appendChild(getElements(doc, conceptElement, "skos:inScheme", "rdf:resource", attrVal, attrVal));
		}
		
		if(concept.getNarrower()!=null){
			for (String attrVal:concept.getNarrower())
				conceptElement.appendChild(getElements(doc, conceptElement, "skos:narrower", "rdf:resource", attrVal, attrVal));
		}
		
		if(concept.getNarrowMatch()!=null){
			for (String attrVal:concept.getNarrowMatch())
				conceptElement.appendChild(getElements(doc, conceptElement, "skos:narrowMatch", "rdf:resource", attrVal, attrVal));
		}
		if(concept.getRelated()!=null){
			for (String attrVal:concept.getRelated())
				conceptElement.appendChild(getElements(doc, conceptElement, "skos:related", "rdf:resource", attrVal, attrVal));
		}
		if(concept.getRelatedMatch()!=null){
			for (String attrVal:concept.getRelatedMatch())
				conceptElement.appendChild(getElements(doc, conceptElement, "skos:relatedMatch", "rdf:resource", attrVal, attrVal));
		}
		/*try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource source = new DOMSource(doc);
			DOMSource source1 = new DOMSource(conceptElement);
			//StreamResult result = new StreamResult(new File("C:\\file.xml"));

			// Output to console for testing
			StreamResult result = new StreamResult(System.out);

			transformer.transform(source1, result);
			
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		return conceptElement;
	}
    //1
	private Node getElements(Document doc, Element conceptElement,
			String name, String attribute, String attributeValue, java.util.List<String> textValue) {
		String temp;
		Element node = doc.createElement(name);

		if (attribute.equals("xml:lang") && attributeValue.equals("def"))
			//System.out.println("a def");
			temp ="";
		else
			node.setAttribute(attribute, attributeValue);
		for (String value:textValue)
			node.appendChild(doc.createTextNode(value));

		return node;
	}

	//2
	private Node getElements(Document doc, Element conceptElement,
			String name, String attribute, String attributeValue, String textValue) {

		Element node = doc.createElement(name);
        node.setAttribute(attribute, textValue);

		return node;
	}
	 //3
		private Node getElements(Document doc, String name, String attribute, String attributeValue, String textValue) {
			String temp;
			Element node = doc.createElement(name);

			String value= textValue.replace("\n", " ");
			textValue=value.replace("\t", "");
			textValue=textValue.trim().replaceAll(" +", " ");
			if (attribute.equals("xml:lang") && attributeValue.equals("def"))
				//System.out.println("a def");
				temp="";
			else
				node.setAttribute(attribute, attributeValue);
			
			node.appendChild(doc.createTextNode(textValue));

			return node;
		}

		

	private Node getElements(Document doc, Element conceptElement, String name,  String attributeValue, java.util.List<String> textValue) {

		Element node = doc.createElement(name);
		UrlValidator urlValidator = new UrlValidator();
		
		for (String value:textValue){
			String myValue= value.replace("\t", "");
			value= myValue.replace("\n", "");
			value=value.trim().replaceAll(" +", " ");
			if (urlValidator.isValid(value)){
				node.setAttribute("rdf:resource", value);
			}
			else{
				if (!attributeValue.trim().equalsIgnoreCase("def"))
						node.setAttribute("xml:lang", attributeValue);
				node.appendChild(doc.createTextNode(value));
			}
		}

		return node;
	}
	private Node getElement(Document doc, Element conceptElement, String name,  String attributeValue, String value) {

		Element node = doc.createElement(name);
		UrlValidator urlValidator = new UrlValidator();
		String myValue= value.replace("\t", "");
		value= myValue.replace("\n", "");
		
		value=value.trim().replaceAll(" +", " ");
			if (urlValidator.isValid(value)){
				node.setAttribute("rdf:resource", value);
			}
			else{
				if (!attributeValue.trim().equalsIgnoreCase("def"))
						node.setAttribute("xml:lang", attributeValue);
				node.appendChild(doc.createTextNode(value));
			}
		

		return node;
	}
}
