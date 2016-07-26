package eu.europeana.enrichment.harvester.dbpedia;

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

public class DbPediaLocalAgentsCollector {

    private static final Logger log = Logger.getLogger(DbPediaLocalAgentsCollector.class.getCanonicalName());
    private static final DataManager dm = new DataManager();
    private static final String AGENTQUERY = "SELECT DISTINCT * WHERE {{?subject ?y <http://dbpedia.org/ontology/Artist>.}"+
												" UNION " +
												"{?subject ?y <http://dbpedia.org/ontology/Philosopher>.}" +
												" UNION " +
												"{?subject ?y <http://dbpedia.org/class/yago/Artist109812338>.}} LIMIT %d OFFSET %d";
    
    private static final String SOURCE = "LocalDbPedia";
    private static int qLimit = 200;
    private static final int QOFFSET = 0;
    private static final boolean MAXAGENTS = false;  //used for testing purposes, if true qLimit agents are downloaded, use false to download all agents from dbpedia 

    private static Dataset dataset;
    private static Model tdbModel;
    /**
     * @param args
     */
    public static void main(String[] args) {

        DbPediaLocalAgentsCollector dbpc = new DbPediaLocalAgentsCollector();
        if (args != null && args.length > 1) {
            if (StringUtils.isNumeric(args[0])) {
                qLimit = Integer.parseInt(args[0]);
                
            }
        }
        String directory = "/home/cesare/freebase/DbPediaFullDump";
		System.out.println("counting "+directory);
         dataset = TDBFactory.createDataset(directory);
		 tdbModel= dataset.getDefaultModel();
        dbpc.getLocalDbPediaAgents(false); //get agents from dbpedia and store them locally, (parameter must always have false value, will fix it) ;

    }
    
    private void loadAgentsFromFreebase(){
    	
    }

    private int loadAgentsfromDBPedia(String queryString, boolean harvestData) {
        int i = 0;

        try {
            Date todayDate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            System.out.println(sdf.format(todayDate));
            
            
    		
            
            log.log(Level.INFO, "getting artists from local DbPedia " + queryString);
            Query query = QueryFactory.create(queryString);
    		QueryExecution qexec= QueryExecutionFactory.create(query, dataset);
    		ResultSet rs= qexec.execSelect();
    		
            while (rs.hasNext()) {

                QuerySolution qs = rs.next();

                String subject = qs.get("subject").toString();
                
                AgentMap agentMap = new AgentMap(subject, new URI(subject), SOURCE, todayDate, null);

                dm.insertAgentMap(agentMap);
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

    public void getLocalDbPediaAgents(boolean harvestContent) {
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
