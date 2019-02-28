package eu.europeana.indexing.mongo.property;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import eu.europeana.corelib.definitions.edm.entity.AbstractEdmEntity;
import eu.europeana.corelib.definitions.edm.entity.WebResource;
import eu.europeana.corelib.solr.entity.WebResourceImpl;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import org.mockito.ArgumentCaptor;

abstract class MongoEntityUpdaterTest<T> {

  abstract T createEmptyMongoEntity();

  void testStringPropertyUpdate(MongoPropertyUpdater<T> propertyUpdater, String fieldName,
      BiConsumer<T, String> setter) {

    // Create a test object with the right value
    final T testEntity = createEmptyMongoEntity();
    final String value = "testValue";
    setter.accept(testEntity, value);

    // Check that the updater was called with the field and a getter that returns the correct value.
    @SuppressWarnings("unchecked") final ArgumentCaptor<Function<T, String>> getterCaptor = ArgumentCaptor
        .forClass(Function.class);
    verify(propertyUpdater, times(1)).updateString(eq(fieldName), getterCaptor.capture());
    assertEquals(value, getterCaptor.getValue().apply(testEntity));
  }

  void testArrayPropertyUpdate(MongoPropertyUpdater<T> propertyUpdater, String fieldName,
      BiConsumer<T, String[]> setter) {

    // Create a test object with the right value
    final T testEntity = createEmptyMongoEntity();
    final String[] value = new String[]{"testValue"};
    setter.accept(testEntity, value);

    // Check that the updater was called with the field and a getter that returns the correct value.
    @SuppressWarnings("unchecked") final ArgumentCaptor<Function<T, String[]>> getterCaptor = ArgumentCaptor
        .forClass(Function.class);
    verify(propertyUpdater, times(1)).updateArray(eq(fieldName), getterCaptor.capture());
    assertArrayEquals(value, getterCaptor.getValue().apply(testEntity));
  }

  void testMapPropertyUpdate(MongoPropertyUpdater<T> propertyUpdater, String fieldName,
      BiConsumer<T, Map<String, List<String>>> setter) {

    // Create a test object with the right value
    final T testEntity = createEmptyMongoEntity();
    final Map<String, List<String>> value = Collections
        .singletonMap("key", Collections.singletonList("testValue"));
    setter.accept(testEntity, value);

    // Check that the updater was called with the field and a getter that returns the correct value.
    @SuppressWarnings("unchecked") final ArgumentCaptor<Function<T, Map<String, List<String>>>> getterCaptor = ArgumentCaptor
        .forClass(Function.class);
    verify(propertyUpdater, times(1)).updateMap(eq(fieldName), getterCaptor.capture());
    assertEquals(value, getterCaptor.getValue().apply(testEntity));
  }

  <F> void testObjectPropertyUpdate(MongoPropertyUpdater<T> propertyUpdater, String fieldName,
      BiConsumer<T, F> setter, F testValue) {
    testObjectPropertyUpdate(propertyUpdater, fieldName, setter, testValue, null);
  }

  <F> void testObjectPropertyUpdate(MongoPropertyUpdater<T> propertyUpdater, String fieldName,
        BiConsumer<T, F> setter, F testValue, UnaryOperator<F> preprocessing) {

      // Create a test object with the right value
    final T testEntity = createEmptyMongoEntity();
    setter.accept(testEntity, testValue);

    // Check that the updater was called with the field and a getter that returns the correct value.
    @SuppressWarnings("unchecked") final ArgumentCaptor<Function<T, F>> getterCaptor = ArgumentCaptor
        .forClass(Function.class);
    if (preprocessing == null) {
      verify(propertyUpdater, times(1)).updateObject(eq(fieldName), getterCaptor.capture());
    } else {
      verify(propertyUpdater, times(1))
          .updateObject(eq(fieldName), getterCaptor.capture(), same(preprocessing));
    }
    assertEquals(testValue, getterCaptor.getValue().apply(testEntity));
  }

  void testWebResourcesPropertyUpdate(MongoPropertyUpdater<T> propertyUpdater, String fieldName,
      BiConsumer<T, List<WebResource>> setter, RootAboutWrapper rootAbout) {

    // Create a test object with the right value
    final T testEntity = createEmptyMongoEntity();
    final String aboutValue = "about value";
    final WebResource webResourceValue = new WebResourceImpl();
    webResourceValue.setAbout(aboutValue);
    setter.accept(testEntity, Collections.singletonList(webResourceValue));

    // Check that the updater was called with valid values.
    @SuppressWarnings("unchecked") final ArgumentCaptor<Function<T, List<? extends WebResource>>> getterCaptor = ArgumentCaptor
        .forClass(Function.class);
    verify(propertyUpdater, times(1))
        .updateWebResources(eq(fieldName), getterCaptor.capture(), same(rootAbout));
    assertEquals(Collections.singletonList(aboutValue),
        getterCaptor.getValue().apply(testEntity).stream().map(WebResource::getAbout)
            .collect(Collectors.toList()));
  }

  <F extends AbstractEdmEntity, A> void testReferencedEntitiesPropertyUpdate(
      MongoPropertyUpdater<T> propertyUpdater, String fieldName,
      BiConsumer<T, List<? extends F>> setter, Class<A> ancestorInfoType,
      Supplier<F> emptyEntityCreator) {

    // Create a test object with the right value
    final T testEntity = createEmptyMongoEntity();
    final String aboutValue = "about value";
    final F testValue = emptyEntityCreator.get();
    testValue.setAbout(aboutValue);
    setter.accept(testEntity, Collections.singletonList(testValue));

    // Check that the updater was called with valid values.
    @SuppressWarnings("unchecked") final ArgumentCaptor<Function<T, List<F>>> getterCaptor = ArgumentCaptor
        .forClass(Function.class);
    @SuppressWarnings("unchecked") final ArgumentCaptor<Function<T, A>> ancestorInfoCreatorCaptor = ArgumentCaptor
        .forClass(Function.class);
    @SuppressWarnings("unchecked") final ArgumentCaptor<AbstractEdmEntityUpdater<F, A>> updaterCaptor = ArgumentCaptor
        .forClass(AbstractEdmEntityUpdater.class);
    verify(propertyUpdater, times(1))
        .updateReferencedEntities(eq(fieldName), getterCaptor.capture(),
            ancestorInfoCreatorCaptor.capture(), updaterCaptor.capture());
    assertEquals(Collections.singletonList(aboutValue),
        getterCaptor.getValue().apply(testEntity).stream().map(AbstractEdmEntity::getAbout)
            .collect(Collectors.toList()));
    if (ancestorInfoType == null) {
      assertNull(ancestorInfoCreatorCaptor.getValue().apply(testEntity));
    } else {
      assertTrue(ancestorInfoType
          .isAssignableFrom(ancestorInfoCreatorCaptor.getValue().apply(testEntity).getClass()));
    }
    assertEquals(testValue.getClass(), updaterCaptor.getValue().getObjectClass());
  }

  <F extends AbstractEdmEntity, A> void testReferencedEntityPropertyUpdate(
      MongoPropertyUpdater<T> propertyUpdater, String fieldName,
      BiConsumer<T, F> setter, Class<A> ancestorInfoType, Supplier<F> emptyEntityCreator) {

    // Create a test object with the right value
    final T testEntity = createEmptyMongoEntity();
    final String aboutValue = "about value";
    final F testValue = emptyEntityCreator.get();
    testValue.setAbout(aboutValue);
    setter.accept(testEntity, testValue);

    // Check that the updater was called with valid values.
    @SuppressWarnings("unchecked") final ArgumentCaptor<Function<T, F>> getterCaptor = ArgumentCaptor
        .forClass(Function.class);
    @SuppressWarnings("unchecked") final ArgumentCaptor<Function<T, A>> ancestorInfoCreatorCaptor = ArgumentCaptor
        .forClass(Function.class);
    @SuppressWarnings("unchecked") final ArgumentCaptor<AbstractEdmEntityUpdater<F, A>> updaterCaptor = ArgumentCaptor
        .forClass(AbstractEdmEntityUpdater.class);
    verify(propertyUpdater, times(1))
        .updateReferencedEntity(eq(fieldName), getterCaptor.capture(),
            ancestorInfoCreatorCaptor.capture(), updaterCaptor.capture());
    assertEquals(aboutValue, getterCaptor.getValue().apply(testEntity).getAbout());
    if (ancestorInfoType == null) {
      assertNull(ancestorInfoCreatorCaptor.getValue().apply(testEntity));
    } else {
      assertTrue(ancestorInfoType
          .isAssignableFrom(ancestorInfoCreatorCaptor.getValue().apply(testEntity).getClass()));
    }
    assertEquals(testValue.getClass(), updaterCaptor.getValue().getObjectClass());
  }
}
