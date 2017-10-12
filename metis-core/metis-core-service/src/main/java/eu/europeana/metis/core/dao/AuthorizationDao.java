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

    @Autowired
    private MorphiaDatastoreProvider provider;
    @Override
    public String create(MetisKey metisKey) {
        Key<MetisKey> metisKeyKey = provider.getDatastore().save(metisKey);
        LOGGER.info("MetisKey '{}' created in Mongo", metisKey.getApiKey());
        return metisKeyKey.getId().toString();
    }

    @Override
    public String update(MetisKey metisKey) {
        UpdateOperations<MetisKey>ops = provider.getDatastore().createUpdateOperations(MetisKey.class);
        Query<MetisKey>q = provider.getDatastore().createQuery(MetisKey.class).filter("apiKey",metisKey.getApiKey());
        ops.set("options",metisKey.getOptions());
        ops.set("profile",metisKey.getProfile());
        provider.getDatastore().update(q,ops);
        UpdateResults updateResults = provider.getDatastore().update(q, ops);
        LOGGER.info("MetisKey '{}' updated in Mongo", metisKey.getApiKey());
        return StringUtils.isNotEmpty(updateResults.getNewId().toString()) ? updateResults.getNewId()
            .toString() : metisKey.getObjId().toString();
    }

    @Override
    public MetisKey getById(String apiKey) {
        return provider.getDatastore().find(MetisKey.class).filter("apiKey",apiKey).get();
    }

    @Override
    public boolean delete(MetisKey metisKey) {
        provider.getDatastore().delete(provider.getDatastore().createQuery(MetisKey.class).filter("apiKey",metisKey));
        LOGGER.info("MetisKey '{}' deleted from Mongo", metisKey.getApiKey());
        return true;
    }
}
