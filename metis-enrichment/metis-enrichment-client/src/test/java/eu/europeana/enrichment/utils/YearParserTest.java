package eu.europeana.enrichment.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.Test;

public class YearParserTest {

  @Test
  public void parseTest() {

    // Mock parser
    final YearParser yearParser = spy(new YearParser());
    final String bcPattern1 = "(?<" + YearParser.REGEX_YEAR_GROUP_NAME + ">\\d+) BC";
    final String bcPattern2 = "(?<" + YearParser.REGEX_YEAR_GROUP_NAME + ">\\d+) b.c.";
    final String adPattern = "(?<" + YearParser.REGEX_YEAR_GROUP_NAME + ">\\d+) AD";
    doReturn(Arrays.asList(Pattern.compile(bcPattern1), Pattern.compile(bcPattern2)))
        .when(yearParser).getBcPatterns();
    doReturn(Collections.singletonList(Pattern.compile(adPattern))).when(yearParser)
        .getAdPatterns();

    // Try parser for single values
    assertEquals(Integer.valueOf(1234), yearParser.parse("1234"));
    assertEquals(Integer.valueOf(-1), yearParser.parse(" -1 "));
    assertEquals(Integer.valueOf(2018), yearParser.parse("2018 AD"));
    assertEquals(Integer.valueOf(-12), yearParser.parse("12 BC"));
    assertEquals(Integer.valueOf(-111), yearParser.parse("111 b.c."));
    assertNull(yearParser.parse("102 a.d."));

    // Try parser for multiple values
    final Set<Integer> result = yearParser.parse(Arrays.asList("15", "18 AD", "19 BC", "20 a.d."));
    assertEquals(3, result.size());
    assertTrue(result.contains(15));
    assertTrue(result.contains(18));
    assertTrue(result.contains(-19));
    assertTrue(yearParser.parse(Collections.emptyList()).isEmpty());
    assertTrue(yearParser.parse(Collections.singletonList("")).isEmpty());
  }
}
