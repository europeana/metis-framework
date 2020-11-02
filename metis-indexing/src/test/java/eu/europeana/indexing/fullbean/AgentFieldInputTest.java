package eu.europeana.indexing.fullbean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.morphia.Datastore;
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filters;
import eu.europeana.metis.schema.jibx.AgentType;
import eu.europeana.metis.schema.jibx.AltLabel;
import eu.europeana.metis.schema.jibx.Begin;
import eu.europeana.metis.schema.jibx.End;
import eu.europeana.metis.schema.jibx.LiteralType.Lang;
import eu.europeana.metis.schema.jibx.Note;
import eu.europeana.metis.schema.jibx.PrefLabel;
import eu.europeana.metis.mongo.RecordDao;
import eu.europeana.corelib.solr.entity.AgentImpl;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit test for the Agent field input creator
 * 
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
class AgentFieldInputTest {

  @Test
  void testAgent() {
    AgentType agentType = new AgentType();
    agentType.setAbout("test about");

    AgentImpl agentImpl = new AgentImpl();
    agentImpl.setAbout(agentType.getAbout());

    RecordDao mongoServerMock = mock(RecordDao.class);
    Datastore datastoreMock = mock(Datastore.class);
    @SuppressWarnings("unchecked")
    Query<AgentImpl> queryMock = mock(Query.class);

    when(mongoServerMock.getDatastore()).thenReturn(datastoreMock);
    when(mongoServerMock.getDatastore()).thenReturn(datastoreMock);
    when(datastoreMock.find(AgentImpl.class)).thenReturn(queryMock);
    when(datastoreMock.save(agentImpl)).thenReturn(agentImpl);
    when(queryMock.filter(Filters.eq("about", agentType.getAbout()))).thenReturn(queryMock);

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
