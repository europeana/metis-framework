package eu.europeana.normalization.dates.extraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.normalizers.DatesNormalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class EdtfDatePartNormalizerTest {

  final HashMap<String, String> datePropertyTestCases = new HashMap<>();
  final HashMap<String, String> genericPropertyTestCases = new HashMap<>();

  public EdtfDatePartNormalizerTest() {
    //DATE PROPERTY
    //DCMI
    datePropertyTestCases.put("name=Prehistoric Period; end=-5300", "../-5300");
    datePropertyTestCases.put("Byzantine Period; start=0395; end=0641", "0395/0641");
    datePropertyTestCases.put("Modern era; start=1975;", "1975/..");

    //Centuries numeric
    datePropertyTestCases.put("18..", "18XX");
    datePropertyTestCases.put("19??", "19XX");
    datePropertyTestCases.put("192?", null);// ambiguous
    datePropertyTestCases.put("[171-]", null); // ambiguous
    datePropertyTestCases.put("19th century", "18XX");
    datePropertyTestCases.put("2nd century", "01XX");
    datePropertyTestCases.put("[10th century]", "09XX"); // not supported
    datePropertyTestCases.put("12th century BC", null); // not supported

    //Centuries roman
    datePropertyTestCases.put("XIV", "13XX");
    datePropertyTestCases.put("MDCLXX", null);
    datePropertyTestCases.put("MDCVII", null);
    datePropertyTestCases.put("S. XVI-XX", "15XX/19XX");
    datePropertyTestCases.put("S.VIII-XV", "07XX/14XX");
    datePropertyTestCases.put("S. XVI-XVIII", "15XX/17XX");
    datePropertyTestCases.put("S. XVIII-", null); // open-ended period

    //Unknown/Unspecified start or end of range
    datePropertyTestCases.put("1907/?", "1907/..");
    datePropertyTestCases.put("?/1907", "../1907");
    datePropertyTestCases.put("1907/", "1907/..");
    datePropertyTestCases.put("/1907", "../1907");

    datePropertyTestCases.put("1905 09 01", "1905-09-01");
    datePropertyTestCases.put("1851-01-01  - 1851-12-31", "1851-01-01/1851-12-31");
    datePropertyTestCases.put("1952-02-25T00:00:00Z-1952-02-25T23:59:59Z", null);
    datePropertyTestCases.put(" 1820/1820", "1820/1820");
    datePropertyTestCases.put("1910/05/31 | 1910/05/01", "1910-05-01/1910-05-31");
    datePropertyTestCases.put("1650? - 1700?", "1650?/1700?");
    datePropertyTestCases.put("1916-09-26 1916-09-28", "1916-09-26/1916-09-28");
    datePropertyTestCases.put("1937--1938", null);
    datePropertyTestCases.put("2013-09-07 09:31:51 UTC", "2013-09-07");
    datePropertyTestCases.put("1918 / 1919", "1918/1919");
    datePropertyTestCases.put("1205/1215 [Herstellung]", "1205/1215");
    datePropertyTestCases.put("1997-07-18T00:00:00 [Create]", "1997-07-18");
    datePropertyTestCases.put("1924 ca.", null);
    datePropertyTestCases.put(" 1757/1757", "1757/1757");
    datePropertyTestCases.put("ca 1757/1757", "1757~/1757~");
    datePropertyTestCases.put("2000 vC - 2002 nC", "-2000/2002");
    datePropertyTestCases.put("0114 aC - 0113 aC", "-0114/-0113");
    datePropertyTestCases.put("0390 AD - 0425 AD", "0390/0425");
    datePropertyTestCases.put("337 BC - 283 BC", "-0337/-0283");
    datePropertyTestCases.put("100 vC - 150 nC", "-0100/0150");
    datePropertyTestCases.put("400 BC - 400 AD", "-0400/0400");
    datePropertyTestCases.put("235 AD – 236 AD", "0235/0236");
    datePropertyTestCases.put("[1712?]", "1712?");
    datePropertyTestCases.put("circa 1712", "1712~");
    datePropertyTestCases.put("[ca. 1946]", "1946~");
    datePropertyTestCases.put("[ca. 193-]", null);// ambiguous
    datePropertyTestCases.put("1651?]", "1651?");
    datePropertyTestCases.put("19--?]", "19XX?");
    datePropertyTestCases.put(". 1885", null);
    datePropertyTestCases.put("- 1885", null);
    datePropertyTestCases.put("192?-1958", "0192?/1958"); // this is an incorrect normalisation, but a few mistakes must happen
    datePropertyTestCases.put("1749 (Herstellung (Werk))", "1749");
    datePropertyTestCases.put("1939; 1954; 1955; 1978; 1939-1945", null); // multiple dates no suported
    datePropertyTestCases.put("[ca. 1920-1930]", "1920~/1930~");
    datePropertyTestCases.put("[17__]", null);// this pattern is not supported (this pattern was never tested
    datePropertyTestCases.put("19--]", "19XX");
    datePropertyTestCases.put("1939 [1942?]",
        "1939/1942?"); // this may not be 100% correct, maybe it is not a range but two dates
    datePropertyTestCases.put("S.VIII-XV", "07XX/14XX");
    datePropertyTestCases.put("S. XVIII-", null); // open ended period? this is not supported
    datePropertyTestCases.put("S. XVI-XVIII", "15XX/17XX");
    datePropertyTestCases.put("[XVI-XIX]", "15XX/18XX");
    datePropertyTestCases.put("1972/10/31 | 1972/10/01", "1972-10-01/1972-10-31");
    datePropertyTestCases.put("19xx", "19XX");
    datePropertyTestCases.put("Sat Jan 01 01:00:00 CET 1701", "1701-01-01");
    datePropertyTestCases.put("2013-03-21 18:45:36 UTC", "2013-03-21");
    datePropertyTestCases.put("15.02.1985 (identification)", "1985-02-15");
    datePropertyTestCases.put("-500000", "Y-500000");
    datePropertyTestCases.put("091090", null);
    datePropertyTestCases.put("-0043-12-07", "-0043-12-07");
    datePropertyTestCases.put("1651 [ca. 1656]",
        "1651~/1656~"); // this may not be a 100% correct normalisation, maybe it is not a range but two dates
    datePropertyTestCases.put("imp. 1901", null);
    datePropertyTestCases.put("-701950/-251950", "Y-701950/Y-251950");
    datePropertyTestCases.put("18720601/18720630", null);
    datePropertyTestCases.put("1872-06-01/1872-06-30", "1872-06-01/1872-06-30");
    datePropertyTestCases.put("19471950/19501953", null);
    datePropertyTestCases.put("1947-19-50/1950-19-53", null);
    datePropertyTestCases.put("u.1707-1739", null);// what does 'u.' mean?
    datePropertyTestCases.put("29-10-2009 29-10-2009", "2009-10-29/2009-10-29");
    datePropertyTestCases.put("1990-", null); // open ended period not supported
    datePropertyTestCases.put("22.07.1971 (identification)", "1971-07-22");
    datePropertyTestCases.put("-2100/-1550", "-2100/-1550");
    datePropertyTestCases.put("187-?]", null); // ambiguous pattern
    datePropertyTestCases.put("18. September 1914", "1914-09-18");
    datePropertyTestCases.put("21.1.1921", "1921-01-21");
    datePropertyTestCases.put("2014/15", "2014/2015");
    datePropertyTestCases.put("12.10.1690", "1690-10-12");
    datePropertyTestCases.put("26.4.1828", "1828-04-26");
    datePropertyTestCases.put("28.05.1969", "1969-05-28");
    datePropertyTestCases.put("28. 1. 1240", null);
    datePropertyTestCases.put("01?-1905", null);
    datePropertyTestCases.put("15/21-8-1918", null);
    datePropertyTestCases.put("199--09-28", null);
    datePropertyTestCases.put("19960216-19960619", null);
    datePropertyTestCases.put("-0549-01-01T00:00:00Z", "-0549-01-01");
    datePropertyTestCases.put("1918-20", "1918/1920");
    datePropertyTestCases.put("1942-1943 c.", null);
    datePropertyTestCases.put("[1942-1943]", "1942/1943");
    datePropertyTestCases.put("(1942-1943)", "1942/1943");
    datePropertyTestCases.put("(1942)", "1942");
    datePropertyTestCases.put("-3.6982", null);
    datePropertyTestCases.put("[ca. 16??]", "16XX~");
    datePropertyTestCases.put("[19--?]", "19XX?");
    datePropertyTestCases.put("ISO9126", null);
    datePropertyTestCases.put("SVV", null);
    datePropertyTestCases.put("1985-10-xx", "1985-10");
    datePropertyTestCases.put("14:27", null);
    datePropertyTestCases.put("11.11.1947", "1947-11-11");
    datePropertyTestCases.put("c.6 Nov 1902", "1902-11-06~");
    datePropertyTestCases.put("1941-22-06", "1941-06-22");
    datePropertyTestCases.put("-1234", "-1234");
    datePropertyTestCases.put("(17--?)", "17XX?");
    datePropertyTestCases.put("20/09/18XX", "18XX-09-20");
    datePropertyTestCases.put("-123456/-12345", "Y-123456/Y-12345");
    datePropertyTestCases.put("23.02.[18--]", "18XX-02-23");
    datePropertyTestCases.put("0 2 1980", "1980-02");
    datePropertyTestCases.put("168 B.C.-135 A.D.", "-0168/0135");
    datePropertyTestCases.put("02?-1915", null);
    datePropertyTestCases.put("1.1848/49[?]", null);
    datePropertyTestCases.put("1889/98? (Herstellung)", "1889?/1898?");
    datePropertyTestCases.put("?/1807", "../1807");
    datePropertyTestCases.put("1937-10-??", "1937-10");
    datePropertyTestCases.put("09.1972 (gathering)", "1972-09");
    datePropertyTestCases.put("1871 - 191-", "1871/191X");

    //GENERIC PROPERTY
    genericPropertyTestCases.put("XIV", null);
    genericPropertyTestCases.put("1905 09 01", "1905-09-01");
    genericPropertyTestCases.put("1851-01-01  - 1851-12-31", "1851-01-01/1851-12-31");
    genericPropertyTestCases.put("18..", null);
    genericPropertyTestCases.put("2013-09-07 09:31:51 UTC", "2013-09-07");
    genericPropertyTestCases.put("1918 / 1919", "1918/1919");
    genericPropertyTestCases.put("1205/1215 [Herstellung]", null);
    genericPropertyTestCases.put("1997-07", null);
    genericPropertyTestCases.put("19??", null);
    genericPropertyTestCases.put("1871 - 191-", null);
  }

  @Test
  void extractorsTest() {
    DatesNormalizer normaliser = new DatesNormalizer();
    verifyNormalizations(datePropertyTestCases, normaliser::normalizeDateProperty);
    verifyNormalizations(genericPropertyTestCases, normaliser::normalizeGenericProperty);
  }

  private void verifyNormalizations(Map<String, String> testCases, Function<String, DateNormalizationResult> normaliserFunction) {
    DateNormalizationResult dateNormalizationResult;
    for (String testCase : testCases.keySet()) {
      dateNormalizationResult = normaliserFunction.apply(testCase);
      if (dateNormalizationResult.getDateNormalizationExtractorMatchId() == DateNormalizationExtractorMatchId.NO_MATCH
          || dateNormalizationResult.getDateNormalizationExtractorMatchId() == DateNormalizationExtractorMatchId.INVALID) {
        assertNull(testCases.get(testCase), "Test case '" + testCase
            + "' was a no-match but should be normalised to '" + testCases.get(testCase) + "'");
      } else {
        String edtfStr = dateNormalizationResult.getEdtfDate().toString();
        assertEquals(testCases.get(testCase), edtfStr, "Test case '" + testCase + "'");
        if (dateNormalizationResult.getDateNormalizationExtractorMatchId() == DateNormalizationExtractorMatchId.DCMI_PERIOD) {
          if (dateNormalizationResult.getEdtfDate().getLabel() != null) {
            assertTrue(testCase.contains(dateNormalizationResult.getEdtfDate().getLabel()));
          }
        }
      }
    }
  }

}
