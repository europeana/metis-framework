package eu.europeana.metis.framework.dao;

import com.mongodb.MongoClient;
import eu.europeana.metis.framework.workflow.Execution;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;

/**
 * Execution Mongo DAO
 * Created by ymamakis on 11/15/16.
 */
public class ExecutionDao extends BasicDAO<Execution,String> {
    public ExecutionDao(MongoClient mongoClient, Morphia morphia, String dbName) {
        super(mongoClient, morphia, dbName);
    }
}
