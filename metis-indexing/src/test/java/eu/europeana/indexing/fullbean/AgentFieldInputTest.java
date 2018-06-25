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
import eu.europeana.corelib.definitions.jibx.AgentType;
import eu.europeana.corelib.definitions.jibx.AltLabel;
import eu.europeana.corelib.definitions.jibx.Begin;
import eu.europeana.corelib.definitions.jibx.End;
import eu.europeana.corelib.definitions.jibx.LiteralType.Lang;
import eu.europeana.corelib.definitions.jibx.Note;
import eu.europeana.corelib.definitions.jibx.PrefLabel;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.solr.entity.AgentImpl;

/**
 * Unit test for the Agent field input creator
 * 
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
public class AgentFieldInputTest {

  @Test
  public void testAgent() {
    AgentType agentType = new AgentType();
    agentType.setAbout("test about");

    AgentImpl agentImpl = new AgentImpl();
    agentImpl.setAbout(agentType.getAbout());

    EdmMongoServer mongoServerMock = mock(EdmMongoServer.class);
    Datastore datastoreMock = mock(Datastore.class);
    @SuppressWarnings("unchecked")
    Query<AgentImpl> queryMock = mock(Query.class);
    @SuppressWarnings("unchecked")
    Key<AgentImpl> keyMock = mock(Key.class);

    when(mongoServerMock.getDatastore()).thenReturn(datastoreMock);
    when(mongoServerMock.getDatastore()).thenReturn(datastoreMock);
    when(datastoreMock.find(AgentImpl.class)).thenReturn(queryMock);
    when(datastoreMock.save(agentImpl)).thenReturn(keyMock);
    when(queryMock.filter("about", agentType.getAbout())).thenReturn(queryMock);

    List<AltLabel> altLabelList = new ArrayList<>();
    AltLabel altLabel = new AltLabel();
    Lang lang = new Lang();
    lang.setLang("en");
    altLabel.setLang(lang);
    altLabel.setString("test alt label");
    assertNotNull(altLabel);
    altLabelList.add(altLabel);
    agentType.setAltLabelList(altLabelList);
    Begin begin = new Begin();
    begin.setString("test begin");
    agentType.setBegin(begin);
    End end = new End();
    end.setString("test end");
    agentType.setEnd(end);
    List<Note> noteList = new ArrayList<>();
    Note note = new Note();
    note.setString("test note");
    assertNotNull(note);
    noteList.add(note);
    agentType.setNoteList(noteList);
    List<PrefLabel> prefLabelList = new ArrayList<>();
    PrefLabel prefLabel = new PrefLabel();
    prefLabel.setLang(lang);
    prefLabel.setString("test pred label");
    assertNotNull(prefLabel);
    prefLabelList.add(prefLabel);
    agentType.setPrefLabelList(prefLabelList);

    // store in mongo
    AgentImpl agent = new AgentFieldInput().apply(agentType);
    mongoServerMock.getDatastore().save(agent);
    assertEquals(agentType.getAbout(), agent.getAbout());
    assertEquals(agentType.getBegin().getString(),
        agent.getBegin().values().iterator().next().get(0));
    assertEquals(agentType.getEnd().getString(), agent.getEnd().values().iterator().next().get(0));
    assertEquals(agentType.getNoteList().get(0).getString(),
        agent.getNote().values().iterator().next().get(0));
    assertTrue(
        agent.getAltLabel().containsKey(agentType.getAltLabelList().get(0).getLang().getLang()));
    assertTrue(
        agent.getPrefLabel().containsKey(agentType.getPrefLabelList().get(0).getLang().getLang()));
    assertEquals(agentType.getAltLabelList().get(0).getString(),
        agent.getAltLabel().values().iterator().next().get(0));
    assertEquals(agentType.getPrefLabelList().get(0).getString(),
        agent.getPrefLabel().values().iterator().next().get(0));
  }
}
