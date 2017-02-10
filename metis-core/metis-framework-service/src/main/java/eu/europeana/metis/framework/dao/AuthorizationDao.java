package eu.europeana.metis.framework.dao;

import eu.europeana.metis.framework.api.MetisKey;
import eu.europeana.metis.framework.mongo.MongoProvider;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by gmamakis on 7-2-17.
 */
public class AuthorizationDao implements MetisDao<MetisKey> {
    @Autowired
    private MongoProvider provider;
    @Override
    public void create(MetisKey metisKey) {
        provider.getDatastore().save(metisKey);
    }

    @Override
    public void update(MetisKey metisKey) {
        UpdateOperations<MetisKey>ops = provider.getDatastore().createUpdateOperations(MetisKey.class);
        Query<MetisKey>q = provider.getDatastore().createQuery(MetisKey.class).filter("apiKey",metisKey.getApiKey());
        ops.set("options",metisKey.getOptions());
        ops.set("profile",metisKey.getProfile());
        provider.getDatastore().update(q,ops);

    }

    @Override
    public MetisKey getById(String id) {
        return provider.getDatastore().find(MetisKey.class).filter("apiKey",id).get();
    }

    @Override
    public void delete(MetisKey metisKey) {
        provider.getDatastore().delete(provider.getDatastore().createQuery(MetisKey.class).filter("apiKey",metisKey));
    }
}
