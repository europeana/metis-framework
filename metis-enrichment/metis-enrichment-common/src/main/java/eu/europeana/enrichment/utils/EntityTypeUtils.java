package eu.europeana.enrichment.utils;

import eu.europeana.corelib.solr.entity.AbstractEdmEntityImpl;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.enrichment.api.internal.AgentTermList;
import eu.europeana.enrichment.api.internal.ConceptTermList;
import eu.europeana.enrichment.api.internal.MongoTermList;
import eu.europeana.enrichment.api.internal.OrganizationTermList;
import eu.europeana.enrichment.api.internal.PlaceTermList;
import eu.europeana.enrichment.api.internal.TimespanTermList;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Simon Tzanakis
 * @since 2020-07-17
 */
public class EntityTypeUtils {

  private static final List<EntityInfo<?, ?>> ENTITY_INFOS = createEntityTypeList();

  private EntityTypeUtils() {
  }

  private static List<EntityInfo<?, ?>> createEntityTypeList() {
    final ArrayList<EntityInfo<?, ?>> entityInfos = new ArrayList<>();
    entityInfos.add(new EntityInfo<>(EntityType.AGENT, AgentTermList.class, AgentImpl.class));
    entityInfos.add(new EntityInfo<>(EntityType.CONCEPT, ConceptTermList.class, ConceptImpl.class));
    entityInfos.add(new EntityInfo<>(EntityType.PLACE, PlaceTermList.class, PlaceImpl.class));
    entityInfos
        .add(new EntityInfo<>(EntityType.TIMESPAN, TimespanTermList.class, TimespanImpl.class));
    entityInfos.add(new EntityInfo<>(EntityType.ORGANIZATION, OrganizationTermList.class,
        OrganizationImpl.class));
    return entityInfos;
  }

  public static EntityInfo<?, ?> getEntityMongoTermListClass(EntityType entityType) {
    return ENTITY_INFOS.stream().filter(entityInfo -> entityType == entityInfo.entityType)
        .findFirst().orElse(null);

  }

  public static EntityType getEntityTypeFromClassImpl(String entityClassImpl) {
    return ENTITY_INFOS.stream()
        .filter(entityInfo -> entityInfo.getEntityClassImpl().getSimpleName()
            .equalsIgnoreCase(entityClassImpl)).findFirst().map(EntityInfo::getEntityType)
        .orElse(null);
  }

  public static class EntityInfo<T extends MongoTermList<S>, S extends AbstractEdmEntityImpl> {

    private final EntityType entityType;
    private final Class<T> mongoTermListClass;
    private final Class<S> entityClassImpl;

    EntityInfo(EntityType entityType, Class<T> mongoTermListClass,
        Class<S> entityClassImpl) {
      this.entityType = entityType;
      this.mongoTermListClass = mongoTermListClass;
      this.entityClassImpl = entityClassImpl;
    }

    public EntityType getEntityType() {
      return entityType;
    }

    public Class<T> getMongoTermListClass() {
      return mongoTermListClass;
    }

    public Class<S> getEntityClassImpl() {
      return entityClassImpl;
    }
  }

}
