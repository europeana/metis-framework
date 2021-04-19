package eu.europeana.indexing.mongo.property;

import eu.europeana.corelib.definitions.edm.entity.AbstractEdmEntity;
import eu.europeana.corelib.definitions.edm.entity.WebResource;
import eu.europeana.corelib.definitions.edm.model.metainfo.WebResourceMetaInfo;
import eu.europeana.corelib.edm.model.metainfo.WebResourceMetaInfoImpl;
import eu.europeana.corelib.solr.entity.WebResourceImpl;
import eu.europeana.indexing.mongo.AbstractEdmEntityUpdater;
import eu.europeana.indexing.mongo.WebResourceInformation;
import eu.europeana.indexing.mongo.WebResourceMetaInfoUpdater;
import eu.europeana.indexing.mongo.WebResourceUpdater;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Objects implementing this interface  provide functionality to update the properties of a given
 * object. It keeps track of the operations that are required to perform the update. There is a
 * factory class to create these objects: {@link MongoPropertyUpdaterFactory}.
 *
 * @param <T> The type of the object to update.
 */
public interface MongoPropertyUpdater<T> {

  /**
   * <p>
   * This method updates a map property. It does not pre-process the map before updating.
   * </p>
   * <p>
   * This method tests if there is anything to update. If there is, after this method is called,
   * {@link #applyOperations()} will include the update.
   * </p>
   *
   * @param updateField The name of the field to update. This is the name under which they will be
   * stored in the operations list (see {@link #applyOperations()}).
   * @param getter The getter that obtains the property value from the object.
   */
  void updateMap(String updateField, Function<T, Map<String, List<String>>> getter);

  /**
   * <p>
   * This method updates a string array property. Before doing so, it will remove null or empty
   * values from the array. It will not however remove an empty array or set an empty array to
   * null.
   * </p>
   * <p>
   * This method tests if there is anything to update. If there is, after this method is called,
   * {@link #applyOperations()} will include the update.
   * </p>
   *
   * @param updateField The name of the field to update. This is the name under which they will be
   * stored in the operations list (see {@link #applyOperations()}).
   * @param getter The getter that obtains the property value from the object.
   */
  void updateArray(String updateField, Function<T, String[]> getter);

  /**
   * <p>
   * This method updates a string array property. Before doing so, it will remove null or empty
   * values from the array.
   * </p>
   * <p>
   * This method tests if there is anything to update. If there is, after this method is called,
   * {@link #applyOperations()} will include the update.
   * </p>
   *
   * @param updateField The name of the field to update. This is the name under which they will be
   * stored in the operations list (see {@link #applyOperations()}).
   * @param getter The getter that obtains the property value from the object.
   * @param makeEmptyArrayNull Whether to remove an empty array (i.e. set an empty array to null).
   */
  void updateArray(String updateField, Function<T, String[]> getter, boolean makeEmptyArrayNull);

  /**
   * <p>
   * This method updates an object list property.
   * </p>
   * <p>
   * This method tests if there is anything to update. If there is, after this method is called,
   * {@link #applyOperations()} will include the update.
   * </p>
   *
   * @param updateField The name of the field to update. This is the name under which they will be
   * stored in the operations list (see {@link #applyOperations()}).
   * @param getter The getter that obtains the property value from the object.
   */
  <P> void updateObjectList(String updateField, Function<T, List<P>> getter);

  /**
   * <p>
   * This method updates a String property. Before doing so, it will trim the string.
   * </p>
   * <p>
   * This method tests if there is anything to update. If there is, after this method is called,
   * {@link #applyOperations()} will include the update.
   * </p>
   *
   * @param updateField The name of the field to update. This is the name under which they will be
   * stored in the operations list (see {@link #applyOperations()}).
   * @param getter The getter that obtains the property value from the object.
   */
  void updateString(String updateField, Function<T, String> getter);

  /**
   * <p>
   * This method updates a generic property without pre-processing.
   * </p>
   * <p>
   * This method tests if there is anything to update. If there is, after this method is called,
   * {@link #applyOperations()} will include the update.
   * </p>
   *
   * @param updateField The name of the field to update. This is the name under which they will be
   * stored in the operations list (see {@link #applyOperations()}).
   * @param getter The getter that obtains the property value from the object.
   */
  <P> void updateObject(String updateField, Function<T, P> getter);

  /**
   * <p>
   * This method updates a generic property with pre-processing.
   * </p>
   * <p>
   * This method tests if there is anything to update. If there is, after this method is called,
   * {@link #applyOperations()} will include the update.
   * </p>
   *
   * @param updateField The name of the field to update. This is the name under which they will be
   * stored in the operations list (see {@link #applyOperations()}).
   * @param getter The getter that obtains the property value from the object.
   * @param preprocessing The pre-processing to be applied to the update property value before
   * comparing and storing.
   */
  <P> void updateObject(String updateField, Function<T, P> getter, UnaryOperator<P> preprocessing);

  /**
   * <p>
   * This method updates a list of web resources. It additionally triggers an update for each web
   * resource (using {@link WebResourceUpdater}).
   * </p>
   * <p>
   * This method tests if there is anything to update. If there is, after this method is called,
   * {@link #applyOperations()} will include the update.
   * </p>
   *
   * @param updateField The name of the field to update. This is the name under which they will be
   * stored in the operations list (see {@link #applyOperations()}).
   * @param getter The getter that obtains the property value from the object.
   * @param ancestorInformation The parent entity's info to be used for updating the web resources.
   * @param webResourceUpdater The updater that may be used to update the web resources.
   */
  void updateWebResources(String updateField, Function<T, List<? extends WebResource>> getter,
      RootAboutWrapper ancestorInformation,
      AbstractEdmEntityUpdater<WebResourceImpl, RootAboutWrapper> webResourceUpdater);

  /**
   * <p>
   * This method updates a referenced entity (i.e. entity also stored in the database). It
   * additionally triggers an update for the entity (using the supplied {@link
   * AbstractEdmEntityUpdater}).
   * </p>
   * <p>
   * This method tests if there is anything to update. If there is, after this method is called,
   * {@link #applyOperations()} will include the update.
   * </p>
   *
   * @param updateField The name of the field to update. This is the name under which they will be
   * stored in the operations list (see {@link #applyOperations()}).
   * @param getter The getter that obtains the property value from the object.
   * @param ancestorInfoGetter The parent entity's info to be used for updating the property.
   * @param objectUpdater The updater that may be used to update the referenced objects.
   */
  <P extends AbstractEdmEntity, A> void updateReferencedEntity(String updateField,
      Function<T, P> getter, Function<T, A> ancestorInfoGetter,
      MongoObjectUpdater<P, A> objectUpdater);

  /**
   * <p>
   * This method updates a list of referenced entities (i.e. entities also stored in the database).
   * It additionally triggers an update for each entities (using the supplied {@link
   * AbstractEdmEntityUpdater}).
   * </p>
   * <p>
   * This method tests if there is anything to update. If there is, after this method is called,
   * {@link #applyOperations()} will include the update.
   * </p>
   *
   * @param updateField The name of the field to update. This is the name under which they will be
   * stored in the operations list (see {@link #applyOperations()}).
   * @param getter The getter that obtains the property value from the object.
   * @param ancestorInfoGetter The parent entity's info to be used for updating the properties.
   * @param objectUpdater The updater that may be used to update the referenced objects.
   */
  <P extends AbstractEdmEntity, A> void updateReferencedEntities(String updateField,
      Function<T, List<P>> getter, Function<T, A> ancestorInfoGetter,
      MongoObjectUpdater<P, A> objectUpdater);

  /**
   * <p>
   * This method removes an object if it is no longer needed. It does not trigger an update for the
   * object or its contents. This is useful to check for embedded entities (for the properties of
   * which separate update calls can then be made).
   * </p>
   * <p>
   * This method tests if there is anything to update. If there is, after this method is called,
   * {@link #applyOperations()} will include the update.
   * </p>
   *
   * @param updateField The name of the field to update. This is the name under which they will be
   * stored in the operations list (see {@link #applyOperations()}).
   * @param getter The getter that obtains the property value from the object.
   * @return Whether or not this field will be marked for deletion.
   */
  <P> boolean removeObjectIfNecessary(String updateField, Function<T, P> getter);

  /**
   * <p>
   * This method triggers an update for the meta info itself (using {@link
   * WebResourceMetaInfoUpdater}). It doesn't add any operations.
   * </p>
   *
   * @param getter The getter that obtains the property value from the object.
   * @param ancestorInfoGetter The parent entity's info to be used for updating the meta info.
   * @param updaterSupplier A supplier for the manager that may be used to manage the referenced
   * objects.
   */
  void updateWebResourceMetaInfo(Function<T, WebResourceMetaInfo> getter,
      Function<T, WebResourceInformation> ancestorInfoGetter,
      Supplier<MongoObjectManager<WebResourceMetaInfoImpl, WebResourceInformation>> updaterSupplier);

  /**
   * <p>
   * This method applies the operations to the database. After calling this method, the instance
   * should no longer be used.
   * </p>
   * <p>
   * Note that this method attempts the upsert operation twice. This is due to the problem that if
   * separate threads attempt the same upsert simultaneously one of them may fail. For a description
   * of this behavior see the following links:
   * <ul>
   * <li><a href=
   * "https://docs.mongodb.com/manual/reference/method/db.collection.update/#use-unique-indexes">The
   * Mongo documentation</a>, which documents this behavior but is not very clear on the
   * subject.</li>
   * <li><a href="https://jira.mongodb.org/browse/SERVER-14322">This suggested Mongo
   * improvement</a>, which explains this problem a bit better and provides hope that some time in
   * the future this workaround will no longer be necessary.</li>
   * </ul>
   * </p>
   *
   * @return The updated version of the mongo entity (this is the current entity supplied during
   * construction, but with the required changes made).
   */
  T applyOperations();
}
