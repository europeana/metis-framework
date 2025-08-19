package eu.europeana.indexing.common.fullbean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.morphia.Datastore;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.metis.mongo.dao.RecordDao;
import eu.europeana.metis.schema.jibx.AltLabel;
import eu.europeana.metis.schema.jibx.Begin;
import eu.europeana.metis.schema.jibx.End;
import eu.europeana.metis.schema.jibx.HiddenLabel;
import eu.europeana.metis.schema.jibx.IsPartOf;
import eu.europeana.metis.schema.jibx.LiteralType.Datatype;
import eu.europeana.metis.schema.jibx.LiteralType.Lang;
import eu.europeana.metis.schema.jibx.Notation;
import eu.europeana.metis.schema.jibx.Note;
import eu.europeana.metis.schema.jibx.PrefLabel;
import eu.europeana.metis.schema.jibx.TimeSpanType;
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
    TimeSpanType timespan = getTimeSpanType();
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
    assertTimespanFieldInput(timespan, timespanMongo);
  }

  private static void assertTimespanFieldInput(TimeSpanType timespan, TimespanImpl timespanMongo) {
    assertEquals(timespan.getAbout(), timespanMongo.getAbout());
    assertEquals(timespan.getBegin().getString(),
        timespanMongo.getBegin().values().iterator().next().getFirst());
    assertEquals(timespan.getEnd().getString(),
        timespanMongo.getEnd().values().iterator().next().getFirst());
    assertEquals(timespan.getNoteList().getFirst().getString(),
        timespanMongo.getNote().values().iterator().next().getFirst());
    assertTrue(timespanMongo.getAltLabel()
                            .containsKey(timespan.getAltLabelList().getFirst().getLang().getLang()));
    assertTrue(timespanMongo.getPrefLabel()
                            .containsKey(timespan.getPrefLabelList().getFirst().getLang().getLang()));
    assertTrue(timespanMongo.getHiddenLabel()
                            .containsKey(timespan.getHiddenLabelList().getFirst().getLang().getLang()));
    assertEquals(timespan.getAltLabelList().getFirst().getString(),
        timespanMongo.getAltLabel().values().iterator().next().getFirst());
    assertEquals(timespan.getPrefLabelList().getFirst().getString(),
        timespanMongo.getPrefLabel().values().iterator().next().getFirst());
    assertEquals(timespan.getIsPartOfList().getFirst().getResource().getResource(),
        timespanMongo.getIsPartOf().values().iterator().next().getFirst());
    assertEquals(timespan.getNotation().getString(),
        timespanMongo.getSkosNotation().values().iterator().next().getFirst());
  }

  private static TimeSpanType getTimeSpanType() {
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
    Notation notation = new Notation();
    notation.setLang(lang);
    Datatype datatype = new Datatype();
    datatype.setDatatype("http://id.loc.gov/datatypes/edtf/EDTF-level1");
    notation.setDatatype(datatype);
    notation.setString("2022?/2050?");
    timespan.setNotation(notation);
    return timespan;
  }
}
