package eu.europeana.normalization.common.cleaning;

import eu.europeana.normalization.common.cleaning.MarkupTagsCleaning.Mode;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class MarkupTagsCleaningTest {

  String html = "<div\n\tid=\"blah\" alt=\" man\n"
      + "\tthis is ugly html \"\n"
      + "\t>"
      + "fire this <made-up-tag>guy</made-up-tag>…\n"
      + "</div>";

  @Test
  public void testHtmlMarkup() throws Exception {
    MarkupTagsCleaning cleaner = new MarkupTagsCleaning(Mode.HTML_ONLY);
    List<String> cleaned = cleaner.normalize(html);
    System.out.println(html);
    System.out.println(cleaned);
    Assert.assertEquals(1, cleaned.size());
    Assert.assertTrue(cleaned.get(0).contains("ire this"));
    Assert.assertTrue(cleaned.get(0).contains("<made-up-tag>guy</made-up-tag>"));
    Assert.assertFalse(cleaned.get(0).contains("this is ugly html"));
    Assert.assertFalse(cleaned.get(0).contains("div"));
    Assert.assertFalse(cleaned.get(0).contains("alt"));
    Assert.assertFalse(cleaned.get(0).contains("</div>"));
  }

  @Test
  public void testAllMarkup() throws Exception {
    MarkupTagsCleaning cleaner = new MarkupTagsCleaning(Mode.ALL_MARKUP);
    List<String> cleaned = cleaner.normalize(html);

    System.out.println(cleaned);
    Assert.assertEquals(1, cleaned.size());
    Assert.assertTrue(cleaned.get(0).contains("ire this guy"));
    Assert.assertFalse(cleaned.get(0).contains("this is ugly html"));
    Assert.assertFalse(cleaned.get(0).contains("div"));
    Assert.assertFalse(cleaned.get(0).contains("alt"));
    Assert.assertFalse(cleaned.get(0).contains("</div>"));
  }
}
