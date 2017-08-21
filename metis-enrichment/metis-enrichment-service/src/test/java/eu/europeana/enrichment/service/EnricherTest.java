package eu.europeana.enrichment.service;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.metis.utils.InputValue;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EnricherTest {

  private RedisInternalEnricher internalEnricher;
  private Enricher enricher;

  @Before
  public void setUp() throws Exception {
    internalEnricher = mock(RedisInternalEnricher.class);
    enricher = new Enricher(internalEnricher);
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void tagExternal() throws Exception {

    List<InputValue> inputValues = new ArrayList<>();

    List<EntityWrapper> x = new ArrayList<>();
    x.add(new EntityWrapper());
    doReturn(x).when(internalEnricher).tag(inputValues);
    assertEquals(enricher.tagExternal(inputValues), x);
  }

  @Test
  public void getByUri() throws Exception {
    EntityWrapper entityWrapper = new EntityWrapper();
    when(internalEnricher.getByUri(any(String.class))).thenReturn(entityWrapper);
    assertSame(enricher.getByUri("uri"), entityWrapper);
  }

}