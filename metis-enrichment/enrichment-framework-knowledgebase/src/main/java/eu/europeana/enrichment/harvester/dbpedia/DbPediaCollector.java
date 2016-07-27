package eu.europeana.enrichment.harvester.dbpedia;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.enrichment.converters.ContextualEntityToXmlConverter;
import eu.europeana.enrichment.harvester.api.AgentMap;
import eu.europeana.enrichment.harvester.database.DataManager;
import eu.europeana.enrichment.harvester.transform.edm.agent.AgentTransformer;
import eu.europeana.enrichment.harvester.transform.edm.concept.ConceptTransformer;
import eu.europeana.enrichment.harvester.util.MongoDataSerializer;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DbPediaCollector {

    private static final Logger log = Logger.getLogger(DbPediaCollector.class.getName());
    private static final String AGENT = "Agent";
	private static final String CONCEPT = "Concept";
	private static String CONCEPT_LIST_PATH="src/main/resources/dbpedia_concepts_list_15072015.txt";
    private final DataManager dm = new DataManager();
    private String agentKey = "";
    private Model model=ModelFactory.createDefaultModel();
    private QueryEngineHTTP endpoint;
    MongoDataSerializer ds= new MongoDataSerializer();
	DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder icBuilder;

    private int gloffset = 0;

    /**
     * @param args
     */
    public static void main(String[] args) {

        DbPediaCollector dbpc = new DbPediaCollector();

       //dbpc.harvestDBPedia(); //fetch agents from local storage and harvests rdf description
      //dbpc.deleteDBPediaConcepts();
       //dbpc.harvestDBPediaConcepts();
        //dbpc.printDBPediaConcepts();
        dbpc.printDbPediaAgents();
       // dbpc.printLocalDbPediaAgents("de");
       
       // dbpc.testHarvesting();
       // dbpc.getLocalAgents("it", false);
        
    }

    public void harvestDBPedia() {

        int resultsize = 1000;
        int limit = 1000;
        int offset =178900;
        while (resultsize == limit) {

            List<AgentMap> agents = dm.extractAllAgentsFromLocalStorage(limit, offset);
            resultsize = agents.size();
            for (AgentMap am : agents) {
            	if (am.getAgentUri().toASCIIString().contains("dbpedia.org"))
					try {
						collectAndMapControlledData(java.net.URLDecoder.decode(am.getAgentUri().toASCIIString(), "UTF-8"), AGENT);
					} catch (UnsupportedEncodingException e) {
						
						System.out.println ("Error "+e);
					}
            }
            if (agents.size() == limit) {
                offset = offset + limit;
                gloffset=offset;
                System.out.println ("Offset "+offset);
            }
        }

    }
    
    
    public void harvestDBPediaConcepts() {
    	File file = new File(CONCEPT_LIST_PATH);
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
    	    for(String line; (line = br.readLine()) != null; ) {
    	        if (line.startsWith("http://dbpedia"))
    	        	collectAndMapControlledData(line, CONCEPT);
    	    }
    	    
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
}
    public void deleteDBPediaConcepts() {
    	File file = new File(CONCEPT_LIST_PATH);
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
    	    for(String line; (line = br.readLine()) != null; ) {
    	        if (line.startsWith("http://dbpedia"))
    	        	dm.deleteConcept(line);
    	    }
    	    
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
}
    
    public void getDBPediaConcepts() {
    	File file = new File(CONCEPT_LIST_PATH);
    	String id="";
    	ContextualEntityToXmlConverter myXMLConverter= new ContextualEntityToXmlConverter();
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
    	    for(String line; (line = br.readLine()) != null; ) {
    	        if (line.startsWith("http://dbpedia")){
    	        	id=line;
    	        	if (line!=null && ! line.isEmpty()){
    	        		
    	        		
    	        		//if (dm.getConcept(line)!=null)
    	        			//System.out.println(myXMLConverter.convertConcept(dm.getConcept(line)));
    	        		//dm.getConcept(line);
    	        	}
    	        }
    	        	
    	    }
    	    
    	} catch (Exception e) {
			System.out.println (id);
			e.printStackTrace();
		}
}
    public void printLocalDbPediaAgents(String locale){

		int resultsize = 1000;
		int limit = 1000;
		int offset = 0;
		Writer writer = null;
		try {
			icBuilder = icFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		try{
			 writer = new BufferedWriter(new OutputStreamWriter(
			          new FileOutputStream("not_in_live_dbpedia.txt"), "utf-8"));
			    
		}
		catch (IOException ioe){
			ioe.printStackTrace();
		}
        Document doc = icBuilder.newDocument();
        Element mainRootElement = createMainRootElement(doc);
		
		while (resultsize >0) {

			List<String> agents = dm.ecxtractLocalizedDbPediaAgentsFromLocalStorage(locale, limit, offset);
			
			for (String aid:agents){
				AgentImpl agent=dm.getAgent(aid);
				if (agent!=null && agent.getAbout()!=null){
				Node agentElement=ds.serializeAgentsAsStored(doc, agent);
				if (agentElement!=null){
					mainRootElement.appendChild(agentElement);
					System.out.println(">>>>>>>>>>>>ADDED!!!");
				}
				}
				else{
					 try {
						writer.write(aid+"\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					log.warning("Not in the db "+aid);
				}
		
			}
				
			resultsize = agents.size();
			if (agents.size() >0) {
				offset = offset + limit;
				gloffset=offset;
			}
		}
		printXmlDocument( doc, "live_"+locale+"_storedagents.xml");

	}
    public void printDbPediaAgents(){

    	
    	String id="";

    	
        
        int resultsize = 1000;
		int limit = 1000;
		int offset = 0;
		try{
		 icBuilder = icFactory.newDocumentBuilder();
         Document doc = icBuilder.newDocument();
         //rootNode.createElementNS("http://example/namespace", "PREFIX:aNodeName");
         
         Element mainRootElement = doc.createElementNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:RDF");
        
         
         mainRootElement.setAttribute("xmlns:skos", "http://www.w3.org/2004/02/skos/core#");
         
         mainRootElement.setAttribute("xmlns:rdaGr2", "http://RDVocab.info/ElementsGr2/");
         
         mainRootElement.setAttribute("xmlns:foaf", "http://xmlns.com/foaf/0.1/");
         
         mainRootElement.setAttribute("xmlns:owl", "http://www.w3.org/2002/07/owl#");
         
         mainRootElement.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
         mainRootElement.setAttribute("xmlns:edm", "http://www.europeana.eu/schemas/edm/");
         
         
         doc.appendChild(mainRootElement);
		while (resultsize >0) {

			List<AgentMap> agents =dm.extractAllAgentsFromLocalStorage(limit, offset);
			for (AgentMap auri:agents){
				if (java.net.URLDecoder.decode(auri.getAgentUri().toASCIIString(), "UTF-8").startsWith("http://dbpedia.org/resource")){
					//String aid=auri.getAgentUri().toASCIIString();
					String aid=java.net.URLDecoder.decode(auri.getAgentUri().toASCIIString(), "UTF-8");
					if (aid.equals("http://dbpedia.org/resource/Ørjan_Nilsen"))
						System.out.println("eccolo");
						
					if (dm.getAgent(aid)!=null){
						Node conceptElement=ds.serializeAgentsAsStored(doc, dm.getAgent(aid));
						if (conceptElement!=null)
							mainRootElement.appendChild(conceptElement);
					}
				}
			}
			resultsize = agents.size();
			if (agents.size() >0) {
				offset = offset + limit;
				gloffset=offset;
			}
		}
           
 
		try {
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
				DOMSource source = new DOMSource(doc);
				//DOMSource source1 = new DOMSource(conceptElement);
				StreamResult result = new StreamResult(new File("localstoredagents.xml"));

				// Output to console for testing
				//StreamResult result = new StreamResult(System.out);

				transformer.transform(source, result);
				
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

    	    
    	} catch (Exception e) {
			System.out.println (id);
			e.printStackTrace();
		}

    }
    public void printDBPediaConcepts() {
    	File file = new File(CONCEPT_LIST_PATH);
    	String id="";
    	ContextualEntityToXmlConverter myXMLConverter= new ContextualEntityToXmlConverter();
    	MongoDataSerializer ds= new MongoDataSerializer();
    	DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder icBuilder;
       
           
 
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
			 icBuilder = icFactory.newDocumentBuilder();
	            Document doc = icBuilder.newDocument();
	            //Element mainRootElement = doc.createElementNS("http://europeana.eu/concepts", "Concepts");
	            Element mainRootElement = doc.createElementNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:RDF");
	            //mainRootElement.setAttributeNS("xmlns:skos", "skos", "http://www.w3.org/2002/02/skos/core#");
	            mainRootElement.setAttribute("xmlns:skos", "http://www.w3.org/2004/02/skos/core#");
	            
	            
	            
	            doc.appendChild(mainRootElement);
	            Set<String> conceptsSet = new HashSet<String>();

	            for(String line; (line = br.readLine()) != null; ) {
	    	        if (line!=null && line.trim().startsWith("http://dbpedia")){
	    	        	conceptsSet.add(line);
	    	        }
	    	        	
	    	    }
	            java.util.Iterator<String> iterator = conceptsSet.iterator();
	            while(iterator.hasNext()){
	              String concept = (String) iterator.next();
	              if (dm.getConcept(concept)!=null){
	        			//String myxml=myXMLConverter.convertConcept(dm.getConcept(concept));
	        			Node conceptElement=ds.serializeConcept(doc, dm.getConcept(concept));
	        			if (conceptElement!=null)
	        				mainRootElement.appendChild(conceptElement);
	        			
	        		}
	            }


    	    /*for(String line; (line = br.readLine()) != null; ) {
    	        if (line.startsWith("http://dbpedia")){
    	        	id=line;
    	        	if (line!=null && ! line.isEmpty()){
    	        		
    	        		
    	        		if (dm.getConcept(line)!=null){
    	        			String myxml=myXMLConverter.convertConcept(dm.getConcept(line));
    	        			Node conceptElement=ds.serializeConcept(doc, dm.getConcept(line));
    	        			if (conceptElement!=null)
    	        				mainRootElement.appendChild(conceptElement);
    	        			
    	        		}
    	        		
    	        	}
    	        }
    	        	
    	    }*/
    	    try {
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
				DOMSource source = new DOMSource(doc);
				//DOMSource source1 = new DOMSource(conceptElement);
				StreamResult result = new StreamResult(new File("storeddbpediaconcepts.xml"));

				// Output to console for testing
				//StreamResult result = new StreamResult(System.out);

				transformer.transform(source, result);
				
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

    	    
    	} catch (Exception e) {
			System.out.println (id);
			e.printStackTrace();
		}
}
    private String cleanString(String str){
    	
    	str= str.replace("&lt;", "");
    	str= str.replace("/&gt;", "");
    	str= str.replace("&gt;", "");
    	return str;
    }
    private void testHarvesting() {

                
                collectAndMapControlledData("http://dbpedia.org/resource/Ørjan_Nilsen", AGENT);
    	//collectAndMapLocalControlledData("de", "http://de.dbpedia.org/resource/Ian_Siegal", AGENT);
           

    }

    
    public void getLocalAgents(String locale, boolean live) {

		int resultsize = 1000;
		int limit = 1000;
		int offset = 0;
		if (live)
			locale="live."+locale;
		while (resultsize >0) {

			List<String> agents = dm.ecxtractLocalizedDbPediaAgentsFromLocalStorage(locale, limit, offset);
			
			for (String am:agents){
				
				collectAndMapLocalControlledData(locale, am, AGENT);
			}
				
			resultsize = agents.size();
			if (agents.size() >0) {
				offset = offset + limit;
				gloffset=offset;
			}
		}

	}

    private void collectAndMapControlledData(String key, String entity) {
    	
    	if (key.contains("\""))
    		return;
    	if (key.endsWith("Charles_Hamilton_(rapper)")|| key.endsWith("MétisArtists")|| key.endsWith("MétisPerformanceArtists"))
    		return;
    	if (key.endsWith("Johannes_Liechtenauer")|| key.endsWith("Hasan_Cihat_Örter")|| key.endsWith("A _(rapper)"))
    		return;
    	if (key.endsWith("Thomas_Negovan") || key.endsWith("Szibilla_Margó_Bakó") || key.endsWith("VarèseSarabandeArtists"))
    		return;
    	if (key.endsWith("Paulo_Henrique_(choreographer)")|| key.endsWith("Boško_Radišić_(RNA)") || key.endsWith("Inti Quila"))
    		return;
    	if (key.endsWith("Roman_Dragoun") || key.endsWith("Wu_Ching_(吳卿)")|| key.endsWith("Steve,_Righ?"))
    		return;
    	if (key.endsWith("Glen_Burtnik")|| key.endsWith("Paulus_Schäfer") || key.endsWith("MétisInstallationArtists"))
    		return;
    	
    	if (key.endsWith("Michael_J._Carrasquillo") || key.endsWith("Paul_Young_(Mike_ _The_Mechanics)")|| key.endsWith("DJ_Clue?"))
    		return;
    	if (key.endsWith("Paulus_Sch%C3%A4fer") || key.endsWith("Luciano_Azevedo")|| key.endsWith("NouveauRéalismeArtists"))
    		return;	
    	if (key.endsWith("Dave_Kellett") || key.endsWith("Walter_Bublé") || key.endsWith("Dalziel_ _Scullion"))
    		return;	
    	if (key.endsWith("Steffen_Thomas") || key.endsWith("Richard_Evans_(artist)") || key.endsWith("Abdul_Vaheed_`Kamal'"))
    		return;
    	
    	if (key.endsWith("Kaya_Jones") || key.endsWith("K-the-I???") || key.endsWith("Béla_Nagy_Abodi"))
    		
    		return;
    	if (key.endsWith("Daniel_Kerr_(fighter)")|| key.endsWith("Peter_Connelly")|| key.endsWith("C C_Music_Factory"))
    		
    		return;
    	if (key.endsWith("Yevgeny_Zamyatin")|| key.endsWith("Richard_Evans")|| key.endsWith("Abu_Nu`aym"))
    		
    		return;
    	
    	if (key.endsWith("Hasan_Cihat_%C3%96rter") || key.endsWith("Laure_Conan")|| key.endsWith("PrikosnovénieArtists"))
    		return;
    	if (key.endsWith("Paul_Poovathingal") || key.endsWith("Zafer_Aracagök") || key.endsWith("Imakuni?"))
    		return;
    	
    	if (key.endsWith("Murray_Leinster") || key.endsWith("Jards_Macal%C3%A9"))
    		return;
    	if (key.endsWith("Oliver_Onions")|| key.endsWith("J%C3%BCrgen_Schmitt__Germany-composer-stub__1"))
    		return;
    	
    	if (key.endsWith("Jonathan_Emile") || key.endsWith("Anita_%C3%81lvarez_de_Williams__US-photographer-stub__1"))
    		return;
    	
    	if (key.endsWith("Jah_Paul_Jo") || key.endsWith("Kate_Lambert"))
    		return;
    	if (key.endsWith("Kevontay_Jackson") || key.endsWith("Judy_Carter"))
    		return;
    	if (key.endsWith("Kryz_Reid")|| key.endsWith("Sara_Mayhew"))
    		return;
    	if (key.endsWith("Adam_Deitch") || key.endsWith("Jim_Klein"))
    		return;
    	if (key.endsWith("Davide_Carbone") || key.endsWith("B%C3%A9la_Nagy_Abodi"))
    		return;
    	if (key.endsWith("Marci_Geller") || key.endsWith("Kirk_Reeves"))
    		return;
    	if (key.endsWith("Lars_Lilholt")|| key.endsWith("James_Mylne_(artist)"))
    		return;
    	if (key.endsWith("Gurdas_Maan") || key.endsWith("Redfoo"))
    		return;
    	if (key.endsWith("Greg_Williamson_(drummer)") || key.endsWith("Chris_Ryall"))
    		return;
    	if (key.endsWith("Mr._Fastfinger") || key.endsWith("Richard_Blanco"))
    		return;
    	if (key.endsWith("Casey_Dunmore") || key.endsWith("Muyiwa_Olarewaju"))
    		return;
    	if (key.endsWith("Thandiswa_Mazwai") || key.endsWith("Olivier_Chastan"))
    		return;
    	if (key.endsWith("Carolus-Duran")|| key.endsWith("Sam_Young_(DJ)"))
    		return;
    	if (key.endsWith("Ian_Maclaren") || key.endsWith("Chris_Kuzneski"))
    		return;
    	if (key.endsWith("David_Bazan") || key.endsWith("Jonathan_Leong"))
    		return;
    	if (key.endsWith("John_Strange_Winter") || key.endsWith("Christine_Tobin"))
    		return;
    	
    	
    	 QueryEngineHTTP endpoint = new QueryEngineHTTP("http://localhost:3031/tdbdb/query", "describe <" + key.trim() + ">");
       // QueryEngineHTTP endpoint = new QueryEngineHTTP("http://dbpedia.org/sparql", "describe <" + key + ">");
      // log.log(Level.INFO, "describing " + key+" offset: "+gloffset);
    	//System.out.println(key+" "+gloffset);
        agentKey = key;
        
       // Model model=ModelFactory.createDefaultModel();
        
       
        model = endpoint.execDescribe();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        RDFWriter writer = model.getWriter("RDF/XML");
        writer.setProperty("allowBadURIs", "true");

        writer.write(model, baos, null);
        Source inputDoc = new StreamSource(new ByteArrayInputStream((baos.toByteArray())));
       // System.out.println (baos.toString());
        if (entity.equals(AGENT))
        	dm.insertAgent(new AgentTransformer().transform("src/main/resources/dbpedia2agent.xsl", key, inputDoc));

        if (entity.equals(CONCEPT))
        	dm.insertConcept(new ConceptTransformer().transform("src/main/resources/dbpedia_skos_concepts.xsl", key, inputDoc));
        	//new ConceptTransformer().transform("src/main/resources/dbpedia_skos_concepts_new.xsl", key, inputDoc);
       // dm.updateAgent(new AgentTransformer().transform("src/main/resources/dbpedia.xsl", key, inputDoc));
    }
    
    private void collectAndMapLocalControlledData(String localPrefix, String key, String entity){

    	
    	if (key.endsWith("Charles_Hamilton_(rapper)"))
    		return;
    	if (key.endsWith("Johannes_Liechtenauer"))
    		return;
    	if (key.endsWith("resource/Francesco_Racanelli"))
    		return;
    	String sparqlEndPoint="http://"+localPrefix+".dbpedia.org/sparql";
        endpoint = new QueryEngineHTTP(sparqlEndPoint, "describe <" + key + ">");
        log.log(Level.INFO, "describing " + key+" offset: "+gloffset);
        agentKey = key;
        
       // Model model=ModelFactory.createDefaultModel();
        
       
        model = endpoint.execDescribe();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        RDFWriter writer = model.getWriter("RDF/XML");
        writer.setProperty("allowBadURIs", "true");

        writer.write(model, baos, null);
        Source inputDoc = new StreamSource(new ByteArrayInputStream((baos.toByteArray())));
      
        if (entity.equals(AGENT))
        	dm.insertAgent(new AgentTransformer().transform("src/main/resources/dbpedia.xsl", key, inputDoc));

        if (entity.equals(CONCEPT))
        	dm.insertConcept(new ConceptTransformer().transform("src/main/resources/dbpedia_skos_concepts.xsl", key, inputDoc));
       // dm.updateAgent(new AgentTransformer().transform("src/main/resources/dbpedia.xsl", key, inputDoc));
    
    }

    private HashMap<String, List<String>> getAgentProperty(String tag, String alternativeTag, Document doc) {
        HashMap<String, List<String>> myM = new HashMap<>();
        String logTag = tag;
        NodeList nodeList = doc.getElementsByTagName(tag);
        if (nodeList.getLength() == 0 && alternativeTag != null) {
            nodeList = doc.getElementsByTagName(alternativeTag);
            logTag = alternativeTag;
        }
        String lang = "def";
        if (nodeList.getLength() > 0) {
            log.info(logTag + " (" + nodeList.getLength() + ")");
        }
        for (int temp = 0; temp < nodeList.getLength(); temp++) {

            Node nNode = nodeList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE && nNode.hasChildNodes()) {
                NamedNodeMap nnm = nNode.getAttributes();
                Node langAtt = nnm.getNamedItem("xml:lang");

                if (langAtt != null && langAtt.hasChildNodes()) {
                    lang = langAtt.getFirstChild().getNodeValue();
                }

                if (!myM.containsKey(lang)) {
                    List<String> date = new ArrayList<>();
                    date.add(nNode.getFirstChild().getNodeValue());
                    myM.put(lang, date);
                } else {
                    myM.get(lang).add(nNode.getFirstChild().getNodeValue());
                }

                log.log(Level.SEVERE, "  " + lang + ", " + nNode.getFirstChild().getNodeValue());

            } else {
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    List<String> attrName = new ArrayList<>();
                    attrName.add("rdf:resource");
                    List<String> attrValues = getElementResourceAttribute(nNode, attrName);
                    if (attrValues.size() > 0) {
                        log.log(Level.INFO, lang + ", " + attrValues.toString());

                        if (!myM.containsKey(lang)) {
                            myM.put(lang, attrValues);
                        } else {
                            myM.get(lang).addAll(attrValues);
                        }
                    }

                }
            }
        }
        return myM;
    }

    private String[] getAgentResource(String tag, String alternativeTag, List<String> attributes, Document doc) {

        NodeList nodeList = doc.getElementsByTagName(tag);
        List<String> result = new ArrayList<>();
        if (nodeList.getLength() == 0 && alternativeTag != null) {
            nodeList = doc.getElementsByTagName(alternativeTag);
        }
        log.log(Level.INFO, tag + "  (" + nodeList.getLength() + ", duplicates will be removed)");

        for (int temp = 0; temp < nodeList.getLength(); temp++) {

            Node nNode = nodeList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                NamedNodeMap nnm = nNode.getAttributes();
                for (String atts : attributes) {
                    Node attValue = nnm.getNamedItem(atts);
                    if (attValue != null && attValue.hasChildNodes()) {
                        if (!result.contains(attValue.getFirstChild().getNodeValue())) {
                            result.add(attValue.getFirstChild().getNodeValue());
                            log.log(Level.INFO, attValue.getFirstChild().getNodeValue());

                        }
                    }

                }

            }
        }
        return result.toArray(new String[result.size()]);
    }

    private List<String> getElementResourceAttribute(Node nNode, List<String> attributes) {

        List<String> result = new ArrayList<>();

        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
            NamedNodeMap nnm = nNode.getAttributes();

            for (String atts : attributes) {
                Node attValue = nnm.getNamedItem(atts);
                if (attValue != null && attValue.hasChildNodes()) {
                    String attribStr = attValue.getFirstChild().getNodeValue();
                    if (!attribStr.trim().equalsIgnoreCase(agentKey)) {
                        result.add(attValue.getFirstChild().getNodeValue());
                    } else {//check if the value is in the parent node
                        Node tmpNode = nNode.getParentNode();
                        nnm = tmpNode.getAttributes();
                        Node parentAttValue = nnm.getNamedItem("rdf:about"); //change this
                        if (parentAttValue != null && attValue.hasChildNodes()) {
                            result.add(parentAttValue.getFirstChild().getNodeValue());
                        }
                    }
                }

            }
        }
        return result;
    }
    private Element createMainRootElement(Document doc){
    	
        //rootNode.createElementNS("http://example/namespace", "PREFIX:aNodeName");
        
        Element mainRootElement = doc.createElementNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:RDF");
       
        
        mainRootElement.setAttribute("xmlns:skos", "http://www.w3.org/2002/02/skos/core#");
        
        mainRootElement.setAttribute("xmlns:rdaGr2", "http://RDVocab.info/ElementsGr2/");
        
        mainRootElement.setAttribute("xmlns:foaf", "http://xmlns.com/foaf/0.1/");
        
        mainRootElement.setAttribute("xmlns:owl", "http://www.w3.org/2002/07/owl#");
        
        mainRootElement.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
        mainRootElement.setAttribute("xmlns:edm", "http://www.europeana.eu/schemas/edm/");
        
        
        doc.appendChild(mainRootElement);
        return mainRootElement;
    }
    
    private void printXmlDocument(Document doc, String name){
    	try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource source = new DOMSource(doc);
			//DOMSource source1 = new DOMSource(conceptElement);
			StreamResult result = new StreamResult(new File(name));

			// Output to console for testing
			//StreamResult result = new StreamResult(System.out);

			transformer.transform(source, result);
			
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    
	
    }

}
