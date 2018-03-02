package eu.europeana.metis.mapping.persistence;

import com.mongodb.MongoClient;
import eu.europeana.metis.mapping.statistics.DatasetStatistics;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;

/**
 * A DatasetStatistics persistence DAO
 * Created by ymamakis on 6/15/16.
 */
public class DatasetStatisticsDao extends BasicDAO<DatasetStatistics, ObjectId> {
    /**
     * Default constructor
     * @param morphia The Morphia wrapper to use
     * @param mongo The Mongo connection settings
     * @param database The database to connect to
     */
    public DatasetStatisticsDao(Morphia morphia, MongoClient mongo, String database){
        super(mongo,morphia,database);
    }
}