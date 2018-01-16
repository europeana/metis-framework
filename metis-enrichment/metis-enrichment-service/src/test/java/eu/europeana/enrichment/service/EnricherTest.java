package eu.europeana.enrichment.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.utils.InputValue;

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