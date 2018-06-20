package eu.europeana.indexing.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import java.util.Collections;
import java.util.Map;
import org.junit.Test;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.exception.IndexingException;

public class FullBeanDaoTest {

  @Test
  public void testGetPersistedAbout() throws IndexingException {

    // Create about strings and prefix.
    final String about1 = "A";
    final String about2 = "B";

    // Create the bean and a dao mocked to return the bean.
    final FullBeanImpl bean = createBean(about1);
    final Map<String, FullBeanImpl> beans = Collections.singletonMap(bean.getAbout(), bean);
    final FullBeanDao dao = spy(new FullBeanDao(null));
    doAnswer(invocation -> beans.get(invocation.getArgument(1))).when(dao)
        .get(eq(FullBeanImpl.class), anyString());

    // Perform the tests.
    assertEquals(bean.getAbout(), dao.getPersistedAbout(createBean(about1)));
    assertNull(dao.getPersistedAbout(createBean(about2)));
  }

  private FullBeanImpl createBean(String about) {
    final FullBeanImpl result = new FullBeanImpl();
    result.setAbout(about);
    return result;
  }

  @Test(expected = IndexingException.class)
  public void testGetPersistedAboutForBadBean() throws IndexingException {
    new FullBeanDao(null).getPersistedAbout(new FullBeanImpl());
  }
}
