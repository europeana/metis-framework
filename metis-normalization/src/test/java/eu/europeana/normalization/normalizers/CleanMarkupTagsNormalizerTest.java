package eu.europeana.normalization.normalizers;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import eu.europeana.normalization.normalizers.CleanMarkupTagsNormalizer;
import eu.europeana.normalization.settings.CleanMarkupTagsMode;

public class CleanMarkupTagsNormalizerTest {

  private String html = "<div\n\tid=\"blah\" alt=\" man\n"
      + "\tthis is ugly html \"\n"
      + "\t>"
      + "fire this <made-up-tag>guy</made-up-tag>…\n"
      + "</div>";

  @Test
  public void testHtmlMarkup() throws Exception {
    CleanMarkupTagsNormalizer cleaner = new CleanMarkupTagsNormalizer(CleanMarkupTagsMode.HTML_ONLY);
    List<String> cleaned = cleaner.normalizeValue(html).stream()
        .map(NormalizedValueWithConfidence::getNormalizedValue).collect(Collectors.toList());
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
    CleanMarkupTagsNormalizer cleaner = new CleanMarkupTagsNormalizer(CleanMarkupTagsMode.ALL_MARKUP);
    List<String> cleaned = cleaner.normalizeValue(html).stream()
        .map(NormalizedValueWithConfidence::getNormalizedValue).collect(Collectors.toList());
    System.out.println(cleaned);
    Assert.assertEquals(1, cleaned.size());
    Assert.assertTrue(cleaned.get(0).contains("ire this guy"));
    Assert.assertFalse(cleaned.get(0).contains("this is ugly html"));
    Assert.assertFalse(cleaned.get(0).contains("div"));
    Assert.assertFalse(cleaned.get(0).contains("alt"));
    Assert.assertFalse(cleaned.get(0).contains("</div>"));
  }
}
