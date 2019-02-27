package eu.europeana.indexing.mongo.property;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import eu.europeana.corelib.definitions.edm.entity.WebResource;
import eu.europeana.corelib.solr.entity.WebResourceImpl;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
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
    @SuppressWarnings("unchecked") final ArgumentCaptor<Function<T, String>> arrayCaptor = ArgumentCaptor
        .forClass(Function.class);
    verify(propertyUpdater, times(1)).updateString(eq(fieldName), arrayCaptor.capture());
    assertEquals(value, arrayCaptor.getValue().apply(testEntity));
  }

  void testArrayPropertyUpdate(MongoPropertyUpdater<T> propertyUpdater, String fieldName,
      BiConsumer<T, String[]> setter) {

    // Create a test object with the right value
    final T testEntity = createEmptyMongoEntity();
    final String[] value = new String[]{"testValue"};
    setter.accept(testEntity, value);

    // Check that the updater was called with the field and a getter that returns the correct value.
    @SuppressWarnings("unchecked") final ArgumentCaptor<Function<T, String[]>> arrayCaptor = ArgumentCaptor
        .forClass(Function.class);
    verify(propertyUpdater, times(1)).updateArray(eq(fieldName), arrayCaptor.capture());
    assertArrayEquals(value, arrayCaptor.getValue().apply(testEntity));
  }

  void testMapPropertyUpdate(MongoPropertyUpdater<T> propertyUpdater, String fieldName,
      BiConsumer<T, Map<String, List<String>>> setter) {

    // Create a test object with the right value
    final T testEntity = createEmptyMongoEntity();
    final Map<String, List<String>> value = Collections
        .singletonMap("key", Collections.singletonList("testValue"));
    setter.accept(testEntity, value);

    // Check that the updater was called with the field and a getter that returns the correct value.
    @SuppressWarnings("unchecked") final ArgumentCaptor<Function<T, Map<String, List<String>>>> mapCaptor = ArgumentCaptor
        .forClass(Function.class);
    verify(propertyUpdater, times(1)).updateMap(eq(fieldName), mapCaptor.capture());
    assertEquals(value, mapCaptor.getValue().apply(testEntity));
  }

  <F> void testObjectPropertyUpdate(MongoPropertyUpdater<T> propertyUpdater, String fieldName,
      BiConsumer<T, F> setter, F testValue) {

    // Create a test object with the right value
    final T testEntity = createEmptyMongoEntity();
    setter.accept(testEntity, testValue);

    // Check that the updater was called with the field and a getter that returns the correct value.
    @SuppressWarnings("unchecked") final ArgumentCaptor<Function<T, F>> mapCaptor = ArgumentCaptor
        .forClass(Function.class);
    verify(propertyUpdater, times(1)).updateObject(eq(fieldName), mapCaptor.capture());
    assertEquals(testValue, mapCaptor.getValue().apply(testEntity));
  }

  <F> void testWebResourcesPropertyUpdate(MongoPropertyUpdater<T> propertyUpdater, String fieldName,
      BiConsumer<T, List<WebResource>> setter, RootAboutWrapper rootAbout) {

    // Create a test object with the right value
    final T testEntity = createEmptyMongoEntity();
    final String aboutValue = "about value";
    final WebResource webResourceValue = new WebResourceImpl();
    webResourceValue.setAbout(aboutValue);
    setter.accept(testEntity, Collections.singletonList(webResourceValue));

    // Check that the updater was called with the field and a getter that returns the correct value.
    @SuppressWarnings("unchecked") final ArgumentCaptor<Function<T, List<? extends WebResource>>> mapCaptor = ArgumentCaptor
        .forClass(Function.class);
    verify(propertyUpdater, times(1))
        .updateWebResources(eq(fieldName), mapCaptor.capture(), same(rootAbout));
    assertEquals(Collections.singletonList(aboutValue),
        mapCaptor.getValue().apply(testEntity).stream().map(WebResource::getAbout)
            .collect(Collectors.toList()));
  }
}
