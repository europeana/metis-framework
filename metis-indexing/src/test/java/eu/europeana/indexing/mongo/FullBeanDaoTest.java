package eu.europeana.indexing.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.exception.IndexingException;

public class FullBeanDaoTest {

  @Test
  public void testGetPersistedAbout() throws IndexingException {

    // Create about strings and prefix.
    final String prefix = "/item";
    final String about1 = "A";
    final String about2 = "B";
    final String about3 = "C";

    // Support two beans: one with prefix, the other without.
    final FullBeanImpl bean1 = createBean(about1);
    final FullBeanImpl bean2 = createBean(prefix + about2);
    final Map<String, FullBeanImpl> beans = new HashMap<>();
    beans.put(bean1.getAbout(), bean1);
    beans.put(bean2.getAbout(), bean2);

    // Create the dao and mock it to return the two prefixes.
    final FullBeanDao dao = spy(new FullBeanDao(null));
    doAnswer(invocation -> beans.get(invocation.getArgument(1))).when(dao)
        .get(eq(FullBeanImpl.class), anyString());

    // Perform the tests: all four combinations should be supported.
    assertEquals(bean1.getAbout(), dao.getPersistedAbout(createBean(about1)));
    assertEquals(bean1.getAbout(), dao.getPersistedAbout(createBean(prefix + about1)));
    assertEquals(bean2.getAbout(), dao.getPersistedAbout(createBean(about2)));
    assertEquals(bean2.getAbout(), dao.getPersistedAbout(createBean(prefix + about2)));

    // Perform the tests: when an unknown about is passed, null should be returned.
    assertNull(dao.getPersistedAbout(createBean(about3)));
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
