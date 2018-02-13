package eu.europeana.metis.core.workflow;

import org.bson.types.ObjectId;

/**
 * Interface to indicate the support of {@link ObjectId} in a mongo database.
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-06-01
 */
public interface HasMongoObjectId {

  /**
   * Get the {@link ObjectId} related to the object.
   * @return {@link ObjectId}
   */
  ObjectId getId();

  /**
   * Set the {@link ObjectId} related to the object.
   * @param id {@link ObjectId}
   */
  void setId(ObjectId id);
}
