package eu.europeana.metis.framework.dao;

import com.mongodb.MongoClient;
import eu.europeana.metis.framework.workflow.FailedRecords;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;

/**
 * A FailedRecords DAO
 * Created by ymamakis on 11/17/16.
 */
public class FailedRecordsDao extends BasicDAO<FailedRecords,String> {
    public FailedRecordsDao(MongoClient mongoClient, Morphia morphia, String dbName) {
        super(mongoClient, morphia, dbName);
    }
}
