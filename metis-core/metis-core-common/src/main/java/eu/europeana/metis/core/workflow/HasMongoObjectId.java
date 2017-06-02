package eu.europeana.metis.core.workflow;

import org.bson.types.ObjectId;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-06-01
 */
public interface HasMongoObjectId {
  ObjectId getId();
  void setId(ObjectId id);
}
