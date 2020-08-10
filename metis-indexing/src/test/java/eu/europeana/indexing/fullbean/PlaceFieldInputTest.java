package eu.europeana.indexing.fullbean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.morphia.Datastore;
import dev.morphia.Key;
import dev.morphia.query.Query;
import eu.europeana.corelib.definitions.jibx.AltLabel;
import eu.europeana.corelib.definitions.jibx.IsPartOf;
import eu.europeana.corelib.definitions.jibx.Lat;
import eu.europeana.corelib.definitions.jibx.LiteralType.Lang;
import eu.europeana.corelib.definitions.jibx.Note;
import eu.europeana.corelib.definitions.jibx.PlaceType;
import eu.europeana.corelib.definitions.jibx.PrefLabel;
import eu.europeana.corelib.definitions.jibx._Long;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Place Field Input Field creator
 *
 * @author Yorgos.Mamakis@ kb.nl
 */
class PlaceFieldInputTest {

  @Test
  void testPlace() {
    PlaceImpl placeImpl = new PlaceImpl();
    placeImpl.setAbout("test about");

    EdmMongoServer mongoServerMock = mock(EdmMongoServer.class);
    Datastore datastoreMock = mock(Datastore.class);
    @SuppressWarnings("unchecked")
    Query<PlaceImpl> queryMock = mock(Query.class);
    @SuppressWarnings("unchecked")
    Key<PlaceImpl> keyMock = mock(Key.class);

    when(mongoServerMock.getDatastore()).thenReturn(datastoreMock);
    when(datastoreMock.find(PlaceImpl.class)).thenReturn(queryMock);
    when(datastoreMock.save(placeImpl)).thenReturn(keyMock);
    when(queryMock.filter("about", placeImpl.getAbout())).thenReturn(queryMock);

    PlaceType place = new PlaceType();
    place.setAbout("test about");
    List<AltLabel> altLabelList = new ArrayList<>();
    AltLabel altLabel = new AltLabel();
    Lang lang = new Lang();
    lang.setLang("en");
    altLabel.setLang(lang);
    altLabel.setString("test alt label");
    assertNotNull(altLabel);
    altLabelList.add(altLabel);
    place.setAltLabelList(altLabelList);
    List<Note> noteList = new ArrayList<>();
    Note note = new Note();
    note.setString("test note");
    assertNotNull(note);
    noteList.add(note);
    place.setNoteList(noteList);
    List<PrefLabel> prefLabelList = new ArrayList<>();
    PrefLabel prefLabel = new PrefLabel();
    prefLabel.setLang(lang);
    prefLabel.setString("test pred label");
    assertNotNull(prefLabel);
    prefLabelList.add(prefLabel);
    place.setPrefLabelList(prefLabelList);
    List<IsPartOf> isPartOfList = new ArrayList<>();
    IsPartOf isPartOf = new IsPartOf();
    isPartOf.setString("test resource");
    isPartOfList.add(isPartOf);
    place.setIsPartOfList(isPartOfList);
    Lat posLat = new Lat();
    posLat.setLat(new Float("1.0"));
    place.setLat(posLat);
    _Long posLong = new _Long();
    posLong.setLong(new Float("1.0"));
    place.setLong(posLong);
    // create mongo place
    PlaceImpl placeMongo = new PlaceFieldInput().apply(place);
    mongoServerMock.getDatastore().save(placeMongo);
    assertEquals(place.getAbout(), placeMongo.getAbout());
    assertEquals(place.getNoteList().get(0).getString(),
        placeMongo.getNote().values().iterator().next().get(0));
    assertTrue(
        placeMongo.getAltLabel().containsKey(place.getAltLabelList().get(0).getLang().getLang()));
    assertEquals(place.getAltLabelList().get(0).getString(),
        placeMongo.getAltLabel().values().iterator().next().get(0));

    assertEquals(place.getPrefLabelList().get(0).getString(),
        placeMongo.getPrefLabel().values().iterator().next().get(0));
    assertEquals(place.getIsPartOfList().get(0).getString(),
        placeMongo.getIsPartOf().values().iterator().next().get(0));
    assertEquals(Float.toString(place.getLat().getLat()), Float.toString(placeMongo.getLatitude()));
    assertEquals(Float.toString(place.getLong().getLong()),
        Float.toString(placeMongo.getLongitude()));
  }
}
