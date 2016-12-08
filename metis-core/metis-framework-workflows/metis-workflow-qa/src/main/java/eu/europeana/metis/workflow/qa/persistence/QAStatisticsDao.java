package eu.europeana.metis.workflow.qa.persistence;

import com.mongodb.MongoClient;
import eu.europeana.metis.workflow.qa.model.QAStatistics;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;

/**
 * Created by ymamakis on 12/7/16.
 */
public class QAStatisticsDao extends BasicDAO<QAStatistics,String> {
    public QAStatisticsDao(MongoClient mongoClient, Morphia morphia, String dbName) {
        super(mongoClient, morphia, dbName);
    }
}
