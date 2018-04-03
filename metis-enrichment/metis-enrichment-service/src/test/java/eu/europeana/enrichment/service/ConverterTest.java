package eu.europeana.enrichment.service;

import static org.junit.Assert.*;

import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.api.external.ObjectIdSerializer;
import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.Concept;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.external.model.Timespan;
import eu.europeana.enrichment.utils.EntityClass;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.types.ObjectId;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.junit.Test;

public class ConverterTest {
  @Test
  public void convertAgent() throws Exception {
    ObjectMapper mapper = createMapper();

    EntityWrapper wrapper = new EntityWrapper();
    AgentImpl impl = new AgentImpl();
    impl.setId(ObjectId.get());
    impl.setAbout("myAbout");
    impl.setEdmWasPresentAt(new String []{"a", "b"});
    impl.setOwlSameAs(new String []{"1", "2"});
    impl.setAltLabel(createAltLabels("en", new String[] {"a_en", "b_en"}));

    wrapper.setEntityClass(EntityClass.AGENT);

    Converter converter = new Converter();
    wrapper.setContextualEntity(mapper.writeValueAsString(impl));
    
    Agent agent = (Agent) converter.convert(wrapper);
    assertEquals("myAbout", agent.getAbout());
    assertEquals( "a", agent.getWasPresentAt().get(0).getResource());
    assertEquals( "b", agent.getWasPresentAt().get(1).getResource());
    assertEquals( "1", agent.getSameAs().get(0).getResource());
    assertEquals( "2", agent.getSameAs().get(1).getResource());
    assertEquals("a_en",agent.getAltLabelList().stream().filter(x->x.getValue().equals("a_en")).findFirst().get().getValue());
    assertEquals("en",agent.getAltLabelList().stream().filter(x->x.getValue().equals("a_en")).findFirst().get().getLang());
    assertEquals("b_en",agent.getAltLabelList().stream().filter(x->x.getValue().equals("b_en")).findFirst().get().getValue());
    assertEquals("en",agent.getAltLabelList().stream().filter(x->x.getValue().equals("b_en")).findFirst().get().getLang());
  }

  private Map<String,List<String>> createAltLabels(String en, String[] strings) {
    Map<String,List<String>> map = new HashMap<>();
    map.put(en, Arrays.asList(strings));
    return map;
  }

  @Test
  public void convertConcept() throws Exception {
    ObjectMapper mapper = createMapper();

    EntityWrapper wrapper = new EntityWrapper();
    ConceptImpl impl = new ConceptImpl();
    impl.setId(ObjectId.get());
    impl.setAbout("myAbout");
    impl.setRelated(new String []{"a", "b"});
    impl.setRelatedMatch(new String []{"1", "2"});

    wrapper.setEntityClass(EntityClass.CONCEPT);

    Converter converter = new Converter();
    wrapper.setContextualEntity(mapper.writeValueAsString(impl));

    Concept agent = (Concept) converter.convert(wrapper);
    assertEquals("myAbout", agent.getAbout());
    assertEquals( "a", agent.getRelated().get(0).getResource());
    assertEquals( "b", agent.getRelated().get(1).getResource());
    assertEquals( "1", agent.getRelatedMatch().get(0).getResource());
    assertEquals( "2", agent.getRelatedMatch().get(1).getResource());

  }

  @Test
  public void convertTimespan() throws Exception {
    ObjectMapper mapper = createMapper();

    EntityWrapper wrapper = new EntityWrapper();
    TimespanImpl impl = new TimespanImpl();
    impl.setId(ObjectId.get());
    impl.setAbout("myAbout");
    impl.setOwlSameAs(new String []{"1", "2"});
    wrapper.setEntityClass(EntityClass.TIMESPAN);

    Converter converter = new Converter();
    wrapper.setContextualEntity(mapper.writeValueAsString(impl));

    Timespan agent = (Timespan) converter.convert(wrapper);
    assertEquals("myAbout", agent.getAbout());
    assertEquals( "1", agent.getSameAs().get(0).getResource());
    assertEquals( "2", agent.getSameAs().get(1).getResource());
  }

  @Test
  public void convertPlace() throws Exception {
    ObjectMapper mapper = createMapper();

    EntityWrapper wrapper = new EntityWrapper();
    PlaceImpl impl = new PlaceImpl();
    impl.setId(ObjectId.get());
    impl.setAbout("myAbout");
    impl.setOwlSameAs(new String []{"1", "2"});
    wrapper.setEntityClass(EntityClass.PLACE);

    Converter converter = new Converter();
    wrapper.setContextualEntity(mapper.writeValueAsString(impl));

    Place agent = (Place) converter.convert(wrapper);
    assertEquals("myAbout", agent.getAbout());
    assertEquals( "1", agent.getSameAs().get(0).getResource());
    assertEquals( "2", agent.getSameAs().get(1).getResource());
  }

  @Test
  public void convertOtherObject_returns_null() throws Exception {
    ObjectMapper mapper = createMapper();

    EntityWrapper wrapper = new EntityWrapper();
    PlaceImpl impl = new PlaceImpl();
    impl.setId(ObjectId.get());
    impl.setAbout("myAbout");
    impl.setOwlSameAs(new String []{"1", "2"});
    wrapper.setEntityClass(EntityClass.ORGANIZATION);

    Converter converter = new Converter();
    wrapper.setContextualEntity(mapper.writeValueAsString(impl));

    Place agent = (Place) converter.convert(wrapper);
    assertNull(agent);
  }

  ObjectMapper createMapper() {
    ObjectMapper mapper = new ObjectMapper();
    SimpleModule sm = new SimpleModule("objId",
        Version.unknownVersion());
    sm.addSerializer(new ObjectIdSerializer());
    mapper.registerModule(sm);
    return mapper;

  }
}