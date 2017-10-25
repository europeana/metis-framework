package eu.europeana.metis.core.dao;

import eu.europeana.metis.core.api.MetisKey;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import org.apache.commons.lang.StringUtils;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by gmamakis on 7-2-17.
 */
public class AuthorizationDao implements MetisDao<MetisKey, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationDao.class);
    private static final String API_KEY = "apiKey";

    @Autowired
    private MorphiaDatastoreProvider morphiaDatastoreProvider;
    @Override
    public String create(MetisKey metisKey) {
        Key<MetisKey> metisKeyKey = morphiaDatastoreProvider.getDatastore().save(metisKey);
        LOGGER.info("MetisKey '{}' created in Mongo", metisKey.getApiKey());
        return metisKeyKey.getId().toString();
    }

    @Override
    public String update(MetisKey metisKey) {
        UpdateOperations<MetisKey>ops = morphiaDatastoreProvider.getDatastore().createUpdateOperations(MetisKey.class);
        Query<MetisKey>q = morphiaDatastoreProvider
            .getDatastore().createQuery(MetisKey.class).filter(API_KEY,metisKey.getApiKey());
        ops.set("options",metisKey.getOptions());
        ops.set("profile",metisKey.getProfile());
        morphiaDatastoreProvider.getDatastore().update(q,ops);
        UpdateResults updateResults = morphiaDatastoreProvider.getDatastore().update(q, ops);
        LOGGER.info("MetisKey '{}' updated in Mongo", metisKey.getApiKey());
        return StringUtils.isNotEmpty(updateResults.getNewId().toString()) ? updateResults.getNewId()
            .toString() : metisKey.getObjId().toString();
    }

    @Override
    public MetisKey getById(String apiKey) {
        return morphiaDatastoreProvider.getDatastore().find(MetisKey.class).filter(API_KEY,apiKey).get();
    }

    @Override
    public boolean delete(MetisKey metisKey) {
        morphiaDatastoreProvider.getDatastore().delete(morphiaDatastoreProvider.getDatastore().createQuery(MetisKey.class).filter(API_KEY,metisKey));
        LOGGER.info("MetisKey '{}' deleted from Mongo", metisKey.getApiKey());
        return true;
    }
}
