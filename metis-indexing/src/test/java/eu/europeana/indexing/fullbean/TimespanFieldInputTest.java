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
import eu.europeana.metis.schema.jibx.Begin;
import eu.europeana.metis.schema.jibx.End;
import eu.europeana.metis.schema.jibx.HiddenLabel;
import eu.europeana.metis.schema.jibx.IsPartOf;
import eu.europeana.metis.schema.jibx.LiteralType.Lang;
import eu.europeana.metis.schema.jibx.Note;
import eu.europeana.metis.schema.jibx.PrefLabel;
import eu.europeana.metis.schema.jibx.TimeSpanType;
import eu.europeana.metis.mongo.RecordDao;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Timespan Field Input Creator
 *
 * @author Yorgos.Mamakis@ kb.nl
 */
class TimespanFieldInputTest {

  @Test
  void testTimespan() {
    TimeSpanType timespan = new TimeSpanType();
    timespan.setAbout("test about");
    List<AltLabel> altLabelList = new ArrayList<>();
    AltLabel altLabel = new AltLabel();
    Lang lang = new Lang();
    lang.setLang("en");
    altLabel.setLang(lang);
    altLabel.setString("test alt label");
    assertNotNull(altLabel);
    altLabelList.add(altLabel);
    timespan.setAltLabelList(altLabelList);
    Begin begin = new Begin();
    begin.setString("test begin");
    timespan.setBegin(begin);
    End end = new End();
    end.setString("test end");
    timespan.setEnd(end);
    List<Note> noteList = new ArrayList<>();
    Note note = new Note();
    note.setString("test note");
    assertNotNull(note);
    noteList.add(note);
    timespan.setNoteList(noteList);
    List<PrefLabel> prefLabelList = new ArrayList<>();
    PrefLabel prefLabel = new PrefLabel();
    prefLabel.setLang(lang);
    prefLabel.setString("test pred label");
    assertNotNull(prefLabel);
    prefLabelList.add(prefLabel);
    timespan.setPrefLabelList(prefLabelList);
    List<HiddenLabel> hiddelLabelList = new ArrayList<>();
    HiddenLabel hiddenLabel = new HiddenLabel();
    hiddenLabel.setLang(lang);
    hiddenLabel.setString("test hidden label");
    assertNotNull(hiddenLabel);
    hiddelLabelList.add(hiddenLabel);
    timespan.setHiddenLabelList(hiddelLabelList);
    List<IsPartOf> isPartOfList = new ArrayList<>();
    IsPartOf isPartOf = new IsPartOf();
    eu.europeana.metis.schema.jibx.ResourceOrLiteralType.Resource isPartOfResource = new eu.europeana.metis.schema.jibx.ResourceOrLiteralType.Resource();
    isPartOfResource.setResource("test resource");
    isPartOf.setResource(isPartOfResource);
    isPartOfList.add(isPartOf);
    timespan.setIsPartOfList(isPartOfList);

    TimespanImpl timespanImpl = new TimespanImpl();
    timespanImpl.setAbout(timespan.getAbout());

    // create mongo
    RecordDao mongoServerMock = mock(RecordDao.class);
    Datastore datastoreMock = mock(Datastore.class);
    @SuppressWarnings("unchecked") Query<TimespanImpl> queryMock = mock(Query.class);

    when(mongoServerMock.getDatastore()).thenReturn(datastoreMock);
    when(datastoreMock.find(TimespanImpl.class)).thenReturn(queryMock);
    when(datastoreMock.save(timespanImpl)).thenReturn(timespanImpl);
    when(queryMock.filter(Filters.eq("about", timespan.getAbout()))).thenReturn(queryMock);

    TimespanImpl timespanMongo = new TimespanFieldInput().apply(timespan);
    mongoServerMock.getDatastore().save(timespanMongo);
    assertEquals(timespan.getAbout(), timespanMongo.getAbout());
    assertEquals(timespan.getBegin().getString(),
        timespanMongo.getBegin().values().iterator().next().get(0));
    assertEquals(timespan.getEnd().getString(),
        timespanMongo.getEnd().values().iterator().next().get(0));
    assertEquals(timespan.getNoteList().get(0).getString(),
        timespanMongo.getNote().values().iterator().next().get(0));
    assertTrue(timespanMongo.getAltLabel()
        .containsKey(timespan.getAltLabelList().get(0).getLang().getLang()));
    assertTrue(timespanMongo.getPrefLabel()
        .containsKey(timespan.getPrefLabelList().get(0).getLang().getLang()));
    assertTrue(timespanMongo.getHiddenLabel()
        .containsKey(timespan.getHiddenLabelList().get(0).getLang().getLang()));
    assertEquals(timespan.getAltLabelList().get(0).getString(),
        timespanMongo.getAltLabel().values().iterator().next().get(0));
    assertEquals(timespan.getPrefLabelList().get(0).getString(),
        timespanMongo.getPrefLabel().values().iterator().next().get(0));
    assertEquals(timespan.getIsPartOfList().get(0).getResource().getResource(),
        timespanMongo.getIsPartOf().values().iterator().next().get(0));
  }
}
