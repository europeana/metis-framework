package eu.europeana.enrichment.harvester.freebase;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import eu.europeana.enrichment.harvester.database.DataManager;
import eu.europeana.enrichment.harvester.transform.edm.agent.AgentTransformer;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FreeBaseCollector {

	private static final Logger log = Logger.getLogger(FreeBaseCollector.class.getName());

	private final DataManager dm = new DataManager();
	private String agentKey = "";

	private int gloffset = 0;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		FreeBaseCollector dbpc = new FreeBaseCollector();

		

		// dbpc.collectAndMapControlledData("fava");
		//dbpc.testHarvesting();
		dbpc.getAgents();
		
	}

	public void getAgents() {

		int resultsize = 1000;
		int limit = 1000;
		int offset = 0;
		//dm.extractFreebaseAgentsFromLocalStorage(limit, offset);
		while (resultsize >0) {

			List<String> agents = dm.extractFreebaseAgentsFromLocalStorage(limit, offset);
			
			resultsize = agents.size();
			for (String am : agents) {
				//collectAndMapControlledData(am);
			}
			if (agents.size() >0) {
				offset = offset + limit;
				gloffset=offset;
			}
		}

	}

	private void testHarvesting() {


		//collectAndMapControlledData("http://rdf.freebase.com/ns/m.0fp_5cj");
		//http://rdf.freebase.com/ns/m.07xs92
		dm.addSameAs("http://dbpedia.org/resource/Giovanni_Fattori", "http://de.dbpedia.org/resource/Giovanni_Fattori");


	}

	private void collectAndMapControlledData(String key) {


    	if (key.equalsIgnoreCase("http://rdf.freebase.com/ns/m.0dsryz"))
    		return;
    	if (key.equalsIgnoreCase("http://rdf.freebase.com/ns/m.07y4pk"))
    		return;
    	
    	if (key.equalsIgnoreCase("http://rdf.freebase.com/ns/m.0d31bv"))
    		return;
    	if (key.equalsIgnoreCase("http://rdf.freebase.com/ns/m.0gh62v6"))
    		return;
    	if (key.equalsIgnoreCase("http://rdf.freebase.com/ns/m.04zxtgs"))
    		return;
    	if (key.equalsIgnoreCase("http://rdf.freebase.com/ns/m.04q6hyw"))
    		return;
    	if (key.equalsIgnoreCase("http://rdf.freebase.com/ns/m.03m4w0t"))
    		return;
    	
    	if (key.equalsIgnoreCase("http://rdf.freebase.com/ns/m.0g56rkv"))
    		return;
    	
    	
        QueryEngineHTTP endpoint = new QueryEngineHTTP("http://localhost:3030/tdb/query", "describe <" + key + ">");
        log.log(Level.INFO, "describing " + key+" offset: "+gloffset);
        agentKey = key;
        
        Model model=ModelFactory.createDefaultModel();
        
       
        
       
        try{
        	model = endpoint.execDescribe();
        }
        catch (Exception he){
        	System.out.println (he);
        	return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        RDFWriter writer = model.getWriter("RDF/XML");
        writer.setProperty("allowBadURIs", "true");

        writer.write(model, baos, null);
        Source inputDoc = new StreamSource(new ByteArrayInputStream((baos.toByteArray())));
        dm.insertAgent(new AgentTransformer().transform("src/main/resources/freebase.xsl", key, inputDoc));
        //new AgentTransformer().transform("src/main/resources/freebase.xsl", key, inputDoc);
		System.out.println("done");

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
	
	
	private void inferFromDumpDB(){



		String directory = "/home/cesare/freebase/testdump";
		System.out.println("dump dir "+directory);
		Dataset dataset = TDBFactory.createDataset(directory);
		Model tdbModel= dataset.getDefaultModel();
		String ns="http://rdf.freebase.com/ns/";
		Property p= tdbModel.createProperty(ns, "person.profession.specialization_of");
		Property q= tdbModel.createProperty(ns, "person.profession.specialization_of_transitive");
		//tdbModel.add(p, RDFS.subPropertyOf, q);
		//tdbModel.add(q, RDF.type, OWL.TransitiveProperty);
		
		//System.out.println (tdbModel.getGraph().toString());
		//InfModel infTDB= ModelFactory.createRDFSModel(tdbModel);
		//Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
		Reasoner reasoner = ReasonerRegistry.getOWLMicroReasoner();
		
		//GenericRuleReasoner reasoner = (GenericRuleReasoner) ReasonerRegistry.getRDFSReasoner();
		 //InfModel infTDB= ModelFactory.createRDFSModel(tdbModel);
		InfModel infTDB= ModelFactory.createInfModel(reasoner, tdbModel);
		infTDB.add(p, RDFS.subPropertyOf, q);
		infTDB.add(q, RDF.type, OWL.TransitiveProperty);
		
		infTDB.setDerivationLogging(true);
		Model test =infTDB.getDeductionsModel();
		
		System.out.println ("created... ");
		/*
		System.out.println ("getting resource... ");
		Resource a= infTDB.getResource(ns+"person.profession.specialization_of");
		System.out.println ("done: "+a.getProperty(q));
		ValidityReport validity = infTDB.validate();
		if (validity.isValid()) {
		    System.out.println("OK");
		} else {
		    System.out.println("Conflicts");
		    for (Iterator i = validity.getReports(); i.hasNext(); ) {
		        System.out.println(" - " + i.next());
		    }
		}*/
		
		String queryString="PREFIX ns:<http://rdf.freebase.com/ns/> "+
					
		" SELECT (count (distinct ?x) as ?count) WHERE {{?x ns:type.object.type ns:visual_art.visual_artist}"+
		" UNION { ?x ns:type.object.type ns:book.author }"+
		" UNION { ?x ns:type.object.type ns:music.composer }" +
		" UNION { ?x ns:people.person.profession ns:m.0kyk } UNION { ?x ns:people.person.profession ns:m.0n1h }" +
		" UNION { ?x ns:people.person.profession ?role . ?role ns:person.profession.specialization_of_transitive ns:m.0n1h}"+
		" UNION { ?x ns:people.person.profession ns:m.0mn6 } UNION { ?x ns:people.person.profession ns:m.0b7b55p }} LIMIT 5";
		
		
		System.out.println ("query... ");
		Query query = QueryFactory.create(queryString);
		//QueryExecution qexec= QueryExecutionFactory.create(query, dataset);
		QueryExecution qexec= QueryExecutionFactory.create(query, infTDB);
		ResultSet results= qexec.execSelect();
		ResultSetFormatter.out(results);

	}
	
	
	
	private void checkDumpDB(){



		String directory = "/home/cesare/freebase/testdump";
		System.out.println("dir: "+directory);
		Dataset dataset = TDBFactory.createDataset(directory);
		Model tdbModel= dataset.getDefaultModel();

		//String queryString="SELECT (count(*) AS ?count) {?s ?o ?p }";
		String queryString="PREFIX ns:<http://rdf.freebase.com/ns/> "+
				//"SELECT (COUNT (DISTINCT ?x) AS ?COUNT) WHERE {?x ns:people.person.profession ns:m.0b7b55p}";
				/*"SELECT (COUNT (DISTINCT ?x) AS ?COUNT) WHERE {"+
				
				"{ ?x ns:type.object.type ns:visual_art.visual_artist }"+
				"UNION { ?x ns:type.object.type ns:book.author }"+
				"UNION { ?x ns:type.object.type ns:music.composer }"+
				"UNION { ?x ns:people.person.profession ns:m.0kyk }"+
				"UNION { ?x ns:people.person.profession ns:m.0n1h }"+
				"UNION { ?x ns:people.person.profession ns:m.02nxqmh }"+
				"UNION { ?x ns:people.person.profession ns:m.0mn6 }"+
				"UNION { ?x ns:people.person.profession ns:m.0b7b55p }"+
				"}";*/
				
					/*"{ ?x ns:type.object.type ns:visual_art.visual_artist } "+
					"UNION { ?x ns:type.object.type ns:book.author } "+
					"UNION { ?x ns:type.object.type ns:music.composer } "+
					 "} ";*/
					
		" SELECT * WHERE {?x ns:people.person.profession ns:m.0b7b55p . ?x ns:type.object.name ?subject .}";

		Query query = QueryFactory.create(queryString);
		QueryExecution qexec= QueryExecutionFactory.create(query, dataset);
		ResultSet results= qexec.execSelect();
		ResultSetFormatter.out(results);

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

}
