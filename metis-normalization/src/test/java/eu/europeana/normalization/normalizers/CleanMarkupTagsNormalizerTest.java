package eu.europeana.normalization.normalizers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.normalization.settings.CleanMarkupTagsMode;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

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
    assertEquals(1, cleaned.size());
    assertTrue(cleaned.get(0).contains("ire this"));
    assertTrue(cleaned.get(0).contains("<made-up-tag>guy</made-up-tag>"));
    assertFalse(cleaned.get(0).contains("this is ugly html"));
    assertFalse(cleaned.get(0).contains("div"));
    assertFalse(cleaned.get(0).contains("alt"));
    assertFalse(cleaned.get(0).contains("</div>"));
  }

  @Test
  public void testAllMarkup() throws Exception {
    CleanMarkupTagsNormalizer cleaner = new CleanMarkupTagsNormalizer(CleanMarkupTagsMode.ALL_MARKUP);
    List<String> cleaned = cleaner.normalizeValue(html).stream()
        .map(NormalizedValueWithConfidence::getNormalizedValue).collect(Collectors.toList());
    System.out.println(cleaned);
    assertEquals(1, cleaned.size());
    assertTrue(cleaned.get(0).contains("ire this guy"));
    assertFalse(cleaned.get(0).contains("this is ugly html"));
    assertFalse(cleaned.get(0).contains("div"));
    assertFalse(cleaned.get(0).contains("alt"));
    assertFalse(cleaned.get(0).contains("</div>"));
  }
}
