package eu.europeana.indexing.fullbean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.morphia.Datastore;
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filters;
import eu.europeana.metis.schema.jibx.AltLabel;
import eu.europeana.metis.schema.jibx.Concept;
import eu.europeana.metis.schema.jibx.LiteralType.Lang;
import eu.europeana.metis.schema.jibx.Note;
import eu.europeana.metis.schema.jibx.PrefLabel;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Concepts field input creator
 *
 * @author Yorgos.Mamakis@ kb.nl
 */
class ConceptFieldInputTest {

  @Test
  void testConcept() {
    // create concept from jibx bindings
    Concept concept = new Concept();
    concept.setAbout("test about");

    ConceptImpl conceptImpl = new ConceptImpl();
    conceptImpl.setAbout(concept.getAbout());

    EdmMongoServer mongoServerMock = mock(EdmMongoServer.class);
    Datastore datastoreMock = mock(Datastore.class);
    @SuppressWarnings("unchecked")
    Query<ConceptImpl> queryMock = mock(Query.class);

    when(mongoServerMock.getDatastore()).thenReturn(datastoreMock);
    when(mongoServerMock.getDatastore()).thenReturn(datastoreMock);
    when(datastoreMock.find(ConceptImpl.class)).thenReturn(queryMock);
    when(datastoreMock.save(conceptImpl)).thenReturn(conceptImpl);
    when(queryMock.filter(Filters.eq("about", concept.getAbout()))).thenReturn(queryMock);

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
