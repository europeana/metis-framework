package eu.europeana.indexing.fullbean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.Query;
import eu.europeana.corelib.definitions.jibx.AltLabel;
import eu.europeana.corelib.definitions.jibx.Concept;
import eu.europeana.corelib.definitions.jibx.LiteralType.Lang;
import eu.europeana.corelib.definitions.jibx.Note;
import eu.europeana.corelib.definitions.jibx.PrefLabel;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.solr.entity.ConceptImpl;

/**
 * Unit tests for Concepts field input creator
 *
 * @author Yorgos.Mamakis@ kb.nl
 */
public class ConceptFieldInputTest {

  @Test
  public void testConcept() {
    // create concept from jibx bindings
    Concept concept = new Concept();
    concept.setAbout("test about");

    ConceptImpl conceptImpl = new ConceptImpl();
    conceptImpl.setAbout(concept.getAbout());

    EdmMongoServer mongoServerMock = mock(EdmMongoServer.class);
    Datastore datastoreMock = mock(Datastore.class);
    @SuppressWarnings("unchecked")
    Query<ConceptImpl> queryMock = mock(Query.class);
    @SuppressWarnings("unchecked")
    Key<ConceptImpl> keyMock = mock(Key.class);

    when(mongoServerMock.getDatastore()).thenReturn(datastoreMock);
    when(mongoServerMock.getDatastore()).thenReturn(datastoreMock);
    when(datastoreMock.find(ConceptImpl.class)).thenReturn(queryMock);
    when(datastoreMock.save(conceptImpl)).thenReturn(keyMock);
    when(queryMock.filter("about", concept.getAbout())).thenReturn(queryMock);

    Concept.Choice choice = new Concept.Choice();

    AltLabel altLabel = new AltLabel();
    Lang lang = new Lang();
    lang.setLang("en");
    altLabel.setLang(lang);
    altLabel.setString("test alt label");
    assertNotNull(altLabel);
    choice.setAltLabel(altLabel);
    choice.clearChoiceListSelect();
    Note note = new Note();
    note.setString("test note");
    assertNotNull(note);
    choice.setNote(note);
    choice.clearChoiceListSelect();
    PrefLabel prefLabel = new PrefLabel();
    prefLabel.setLang(lang);
    prefLabel.setString("test pred label");
    assertNotNull(prefLabel);
    choice.setPrefLabel(prefLabel);
    choice.clearChoiceListSelect();
    List<Concept.Choice> choiceList = new ArrayList<>();
    choiceList.add(choice);
    concept.setChoiceList(choiceList);
    // store in mongo
    ConceptImpl conceptMongo = new ConceptFieldInput().apply(concept);
    mongoServerMock.getDatastore().save(conceptMongo);
    assertEquals(concept.getAbout(), conceptMongo.getAbout());
    for (Concept.Choice choice2 : concept.getChoiceList()) {
      if (choice2.ifNote()) {
        assertEquals(choice2.getNote().getString(),
            conceptMongo.getNote().values().iterator().next().get(0));
      }
      if (choice2.ifAltLabel()) {
        assertTrue(
            conceptMongo.getAltLabel().containsKey(choice2.getAltLabel().getLang().getLang()));
        assertEquals(choice2.getAltLabel().getString(),
            conceptMongo.getAltLabel().values().iterator().next().get(0));
      }
      if (choice2.ifPrefLabel()) {
        assertTrue(
            conceptMongo.getPrefLabel().containsKey(choice2.getPrefLabel().getLang().getLang()));
        assertEquals(choice2.getPrefLabel().getString(),
            conceptMongo.getPrefLabel().values().iterator().next().get(0));
      }
    }
  }
}
