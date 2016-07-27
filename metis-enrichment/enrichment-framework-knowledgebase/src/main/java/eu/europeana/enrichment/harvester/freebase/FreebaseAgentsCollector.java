package eu.europeana.enrichment.harvester.freebase;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;
import eu.europeana.enrichment.harvester.api.AgentMap;
import eu.europeana.enrichment.harvester.database.DataManager;
import org.apache.commons.lang.StringUtils;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FreebaseAgentsCollector {

    private static final Logger log = Logger.getLogger(FreebaseAgentsCollector.class.getCanonicalName());
    private static final DataManager dm = new DataManager();
    private static final String AGENTQUERY = "PREFIX ns:<http://rdf.freebase.com/ns/>  SELECT * WHERE "+
    										  "{ ?x ns:type.object.type ns:visual_art.visual_artist } "+
    										  " UNION { ?x ns:type.object.type ns:book.author } "+
    										  "UNION { ?x ns:type.object.type ns:music.composer } "+
    										  "UNION { ?x ns:people.person.profession ns:m.0kyk } "+
    										  "UNION { ?x ns:people.person.profession ns:m.0n1h } "+
    										  "UNION { #support for subclasses of Artist Profession "+
    										  "?x ns:people.person.profession ?role . "+
    										  "?role ns:person.profession.specialization_of_transitive ns:m.0n1h "+
											   "} "+
											   " UNION { ?x ns:people.person.profession ns:m.0mn6 } "+
											   
												"UNION { ?x ns:people.person.profession ns:m.0b7b55p }";
    
    private static final String FREEBASE = "Freebase";
    private static int qLimit = 200;
    private static final int QOFFSET = 0;
    private static final boolean MAXAGENTS = false;  //used for testing purposes, if true qLimit agents are downloaded, use false to download all agents from dbpedia 

    private static Dataset dataset;
    private static Model tdbModel;
    /**
     * @param args
     */
    public static void main(String[] args) {

        FreebaseAgentsCollector dbpc = new FreebaseAgentsCollector();
        if (args != null && args.length > 1) {
            if (StringUtils.isNumeric(args[0])) {
                qLimit = Integer.parseInt(args[0]);
                
            }
        }
        String directory = "/home/cesare/freebase/testdump";
		System.out.println("counting "+directory);
         dataset = TDBFactory.createDataset(directory);
		 tdbModel= dataset.getDefaultModel();
        dbpc.getFreeBaseAgents(false); //get agents from dbpedia and store them locally, (parameter must always have false value, will fix it) ;

    }
    
    private void loadAgentsFromFreebase(){
    	
    }

    private int loadAgentsfromDBPedia(String queryString, boolean harvestData) {
        int i = 0;

        try {
            Date todayDate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            System.out.println(sdf.format(todayDate));
            
            
    		
            
            log.log(Level.INFO, "getting artists from Freebase " + queryString);
            Query query = QueryFactory.create(queryString);
    		QueryExecution qexec= QueryExecutionFactory.create(query, dataset);
    		ResultSet rs= qexec.execSelect();
    		
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();

                String subject = qs.get("subject").toString();
                String id=qs.get("x").toString();
                int langIndex=subject.indexOf('@');
                if(subject.substring(langIndex+1).equalsIgnoreCase("en")) {
                	System.out.println(subject.substring(0, langIndex));
                	System.out.println(qs.get("x").toString());
                	AgentMap agentMap = new AgentMap(subject, new URI(id), FREEBASE, todayDate, null);
                    dm.insertAgentMap(agentMap);
                }
               
                
                i = rs.getRowNumber();

            }

        } catch (Exception e) {

            log.log(Level.SEVERE, e.getMessage());
        }
        return i++;

    }
    /*
     * Harvests agents from DBPedia. Related content can be also harvested if the parameter is true.
     */

    public void getFreeBaseAgents(boolean harvestContent) {
        int resultsize = qLimit;
        int limit = qLimit;
        int offset = QOFFSET;

        while (resultsize == limit) {

            resultsize = loadAgentsfromDBPedia(String.format(AGENTQUERY, limit, offset), harvestContent);
            if (resultsize == limit) {
                offset = offset + limit;
            }
            if (MAXAGENTS) {
                resultsize = 0;
            }
        }

    }

}
