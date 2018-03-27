package eu.europeana.indexing.service.dao;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import eu.europeana.corelib.definitions.edm.entity.AbstractEdmEntity;
import eu.europeana.corelib.definitions.edm.entity.Proxy;
import eu.europeana.corelib.definitions.solr.DocType;
import eu.europeana.corelib.edm.utils.construct.Updater;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;

public class FullBeanDao {

  private final EdmMongoServer mongoServer;
  private static final String ABOUT = "about";

  public FullBeanDao(EdmMongoServer mongoServer) {
    this.mongoServer = mongoServer;
  }

  public <T> T searchByAbout(Class<T> clazz, String about) {
    return mongoServer.getDatastore().find(clazz).filter(ABOUT, about).get();
  }

  public List<FullBeanImpl> getAll() {
    return mongoServer.getDatastore().find(FullBeanImpl.class).asList();
  }

  public FullBeanImpl getFullBean(String id) {
    return mongoServer.getDatastore().find(FullBeanImpl.class).field(ABOUT).equal(id).get();
  }

  public <T> Key<T> save(T data) {
    return mongoServer.getDatastore().save(data);
  }

  public void save(Iterable<?> data) {
    data.forEach(this::save);
  }

  public <T extends AbstractEdmEntity> T update(T data, Class<T> clazz, Updater<T> updater,
      boolean saveNewRecordIfNotFound)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    if (data == null) {
      return null;
    }
    final T existingData = searchByAbout(clazz, data.getAbout());
    final T newData;
    if (existingData != null) {
      newData = updater.update(existingData, data, mongoServer);
    } else if (saveNewRecordIfNotFound) {
      final Key<T> key = save(data);
      newData = mongoServer.getDatastore().getByKey(clazz, key);
    } else {
      newData = null;
    }
    return newData;
  }

  public <T extends AbstractEdmEntity> List<T> update(List<T> dataToAdd, Class<T> clazz,
      Updater<T> updater, boolean saveNewRecordIfNotFound)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    final List<T> result = new ArrayList<>();
    if (dataToAdd == null) {
      return result;
    }
    for (T data : dataToAdd) {
      final T newData = update(data, clazz, updater, saveNewRecordIfNotFound);
      if (newData != null) {
        result.add(newData);
      }
    }
    return result;
  }
  
  // Modified and appended legacy code
  public FullBeanImpl updateFullBean(FullBeanImpl fullBean) {

    Query<FullBeanImpl> updateQuery = mongoServer.getDatastore().createQuery(FullBeanImpl.class)
        .field(ABOUT).equal(fullBean.getAbout().replace("/item", ""));

    UpdateOperations<FullBeanImpl> ops =
        mongoServer.getDatastore().createUpdateOperations(FullBeanImpl.class);

    // To avoid potential index out of bounds
    if (fullBean.getProxies().isEmpty()) {
      ArrayList<Proxy> proxyList = new ArrayList<>();
      ProxyImpl proxy = new ProxyImpl();
      proxyList.add(proxy);
      fullBean.setProxies(proxyList);
    }

    ops.set("title", fullBean.getTitle() != null ? fullBean.getTitle() : new String[] {});
    ops.set("year", fullBean.getYear() != null ? fullBean.getYear() : new String[] {});
    ops.set("provider", fullBean.getProvider() != null ? fullBean.getProvider() : new String[] {});
    ops.set("language", fullBean.getLanguage() != null ? fullBean.getLanguage() : new String[] {});
    ops.set("type", fullBean.getType() != null ? fullBean.getType() : DocType.IMAGE);
    ops.set("europeanaCompleteness", fullBean.getEuropeanaCompleteness());
    ops.set("places",
        fullBean.getPlaces() != null ? fullBean.getPlaces() : new ArrayList<PlaceImpl>());
    ops.set("agents",
        fullBean.getAgents() != null ? fullBean.getAgents() : new ArrayList<AgentImpl>());
    ops.set("timespans",
        fullBean.getTimespans() != null ? fullBean.getTimespans() : new ArrayList<TimespanImpl>());
    ops.set("concepts",
        fullBean.getConcepts() != null ? fullBean.getConcepts() : new ArrayList<ConceptImpl>());
    ops.set("aggregations", fullBean.getAggregations());
    ops.set("providedCHOs", fullBean.getProvidedCHOs());
    ops.set("europeanaAggregation", fullBean.getEuropeanaAggregation());
    ops.set("proxies", fullBean.getProxies());
    ops.set("country", fullBean.getCountry() != null ? fullBean.getCountry() : new String[] {});
    ops.set("services", fullBean.getServices());
    ops.set("europeanaCollectionName", fullBean.getEuropeanaCollectionName());

    mongoServer.getDatastore().update(updateQuery, ops);

    return getFullBean(fullBean.getAbout());
  }
}
