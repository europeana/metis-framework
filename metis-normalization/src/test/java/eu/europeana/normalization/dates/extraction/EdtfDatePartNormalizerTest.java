package eu.europeana.normalization.dates.extraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.normalizers.DatesNormalizer;
import java.util.HashMap;
import org.junit.jupiter.api.Test;

public class EdtfDatePartNormalizerTest {

  HashMap<String, String> testCases = new HashMap<String, String>();

  public EdtfDatePartNormalizerTest() {
    testCases.put("XIV", "13XX");
    testCases.put("1905 09 01", "1905-09-01");
    testCases.put("1851-01-01  - 1851-12-31", "1851-01-01/1851-12-31");
    testCases.put("1952-02-25T00:00:00Z-1952-02-25T23:59:59Z", null);
    testCases.put(" 1820/1820", "1820/1820");
    testCases.put("1910/05/31 | 1910/05/01", "1910-05-01/1910-05-31");
    testCases.put("1650? - 1700?", "1650?/1700?");
    testCases.put("1916-09-26 1916-09-28", "1916-09-26/1916-09-28");
    testCases.put("1937--1938", null);
    testCases.put("18..", "18XX");
    testCases.put("2013-09-07 09:31:51 UTC", "2013-09-07");
    testCases.put("1918 / 1919", "1918/1919");
    testCases.put("1205/1215 [Herstellung]", "1205/1215");
    testCases.put("1997-07-18T00:00:00 [Create]", "1997-07-18");
    testCases.put("1924 ca.", null);
    testCases.put(" 1757/1757", "1757/1757");
    testCases.put("ca 1757/1757", "1757~/1757~");
    testCases.put("2000 vC - 2002 nC", "-2000/2002");
    testCases.put("0114 aC - 0113 aC", "-0114/-0113");
    testCases.put("0390 AD - 0425 AD", "0390/0425");
    testCases.put("337 BC - 283 BC", "-0337/-0283");
    testCases.put("100 vC - 150 nC", "-0100/0150");
    testCases.put("400 BC - 400 AD", "-0400/0400");
    testCases.put("235 AD – 236 AD", "0235/0236");
    testCases.put("S. XVI-XX", "15XX/19XX");
    testCases.put("19??", "19XX");
    testCases.put("192?", null);// ambiguous
    testCases.put("[1712?]", "1712?");
    testCases.put("circa 1712", "1712~");
    testCases.put("[171-]", null); // ambiguous
    testCases.put("[ca. 1946]", "1946~");
    testCases.put("[ca. 193-]", null);// ambiguous
    testCases.put("1651?]", "1651?");
    testCases.put("19--?]", "19XX?");
    testCases.put(". 1885", null);
    testCases.put("- 1885", null);
    testCases.put("192?-1958", "0192?/1958"); // this is an incorrect normalisation, but a few mistakes must happen
    testCases.put("1749 (Herstellung (Werk))", "1749");
    testCases.put("1939; 1954; 1955; 1978; 1939-1945", null); // multiple dates no suported
    testCases.put("[ca. 1920-1930]", "1920~/1930~");
    testCases.put("[17__]", null);// this pattern is not supported (this pattern was never tested
    testCases.put("19--]", "19XX");
    testCases.put("1939 [1942?]", "1939/1942?"); // this may not be 100% correct, maybe it is not a range but two dates
    testCases.put("S.VIII-XV", "07XX/14XX");
    testCases.put("S. XVIII-", null); // open ended period? this is not supported
    testCases.put("S. XVI-XVIII", "15XX/17XX");
    testCases.put("[XVI-XIX]", null);// this is missing 'S.'
    testCases.put("1972/10/31 | 1972/10/01", "1972-10-01/1972-10-31");
    testCases.put("19xx", "19XX");
    testCases.put("Sat Jan 01 01:00:00 CET 1701", "1701-01-01");
    testCases.put("2013-03-21 18:45:36 UTC", "2013-03-21");
    testCases.put("15.02.1985 (identification)", "1985-02-15");
    testCases.put("-500000", "Y-500000");
    testCases.put("091090", null);
    testCases.put("-0043-12-07", "-0043-12-07");
    testCases.put("1651 [ca. 1656]",
        "1651~/1656~"); // this may not be a 100% correct normalisation, maybe it is not a range but two dates
    testCases.put("imp. 1901", null);
    testCases.put("-701950/-251950", "Y-701950/Y-251950");
    testCases.put("18720601/18720630", null);
    testCases.put("1872-06-01/1872-06-30", "1872-06-01/1872-06-30");
    testCases.put("19471950/19501953", null);
    testCases.put("1947-19-50/1950-19-53", null);
    testCases.put("u.1707-1739", null);// what does 'u.' mean?
    testCases.put("29-10-2009 29-10-2009", "2009-10-29/2009-10-29");
    testCases.put("MDCLXX", null);
    testCases.put("MDCVII", null);
    testCases.put("[10th century]", "09XX"); // not supported
    testCases.put("12th century BC", null); // not supported
    testCases.put("1990-", null); // open ended period not supported
    testCases.put("22.07.1971 (identification)", "1971-07-22");
    testCases.put("-2100/-1550", "-2100/-1550");
    testCases.put("187-?]", null); // ambiguous pattern
    testCases.put("18. September 1914", "1914-09-18");
    testCases.put("21.1.1921", "1921-01-21");
    testCases.put("2014/15", "2014/2015");
    testCases.put("12.10.1690", "1690-10-12");
    testCases.put("26.4.1828", "1828-04-26");
    testCases.put("28.05.1969", "1969-05-28");
    testCases.put("28. 1. 1240", null);
    testCases.put("01?-1905", null);
    testCases.put("15/21-8-1918", null);
    testCases.put("199--09-28", null);
    testCases.put("19960216-19960619", null);
    testCases.put("-0549-01-01T00:00:00Z", "-0549-01-01");
    testCases.put("Byzantine Period; start=0395; end=0641", "0395/0641");
    testCases.put("Modern era; start=1975;", "1975/..");
    testCases.put("1918-20", "1918/1920");
    testCases.put("1942-1943 c.", null);
    testCases.put("[1942-1943]", "1942/1943");
    testCases.put("(1942-1943)", "1942/1943");
    testCases.put("(1942)", "1942");
    testCases.put("-3.6982", null);
    testCases.put("[ca. 16??]", "16XX~");
    testCases.put("[19--?]", "19XX?");
    testCases.put("19th century", "18XX");
    testCases.put("2nd century", "01XX");
    testCases.put("ISO9126", null);
    testCases.put("SVV", null);
    testCases.put("1985-10-xx", "1985-10");
    testCases.put("14:27", null);
    testCases.put("11.11.1947", "1947-11-11");
    testCases.put("c.6 Nov 1902", "1902-11-06~");
    testCases.put("1941-22-06", "1941-06-22");
    testCases.put("-1234", "-1234");
    testCases.put("(17--?)", "17XX?");
    testCases.put("20/09/18XX", "18XX-09-20");
    testCases.put("-123456/-12345", "Y-123456/Y-12345");
    testCases.put("23.02.[18--]", "18XX-02-23");
    testCases.put("0 2 1980", "1980-02");
    testCases.put("168 B.C.-135 A.D.", "-0168/0135");
    testCases.put("02?-1915", null);
    testCases.put("1.1848/49[?]", null);
    testCases.put("1889/98? (Herstellung)", "1889?/1898?");
    testCases.put("?/1807", "../1807");
    testCases.put("1937-10-??", "1937-10");
    testCases.put("09.1972 (gathering)", "1972-09");
    testCases.put("1871 - 191-", "1871/191X");
    testCases.put("name=Prehistoric Period; end=-5300", "../-5300");
  }

  @Test
  void extractorsTest() {
    DatesNormalizer normaliser = new DatesNormalizer();
    DateNormalizationResult dateNormalizationResult;

    for (String testCase : testCases.keySet()) {
      dateNormalizationResult = normaliser.normalizeDateProperty(testCase);
      if (dateNormalizationResult.getDateNormalizationExtractorMatchId() == DateNormalizationExtractorMatchId.NO_MATCH
          || dateNormalizationResult.getDateNormalizationExtractorMatchId() == DateNormalizationExtractorMatchId.INVALID) {
        assertNull(testCases.get(testCase), "Test case '" + testCase
            + "' was a no-match but should be normalised to '" + testCases.get(testCase) + "'");
      } else {
        String edtfStr = dateNormalizationResult.getEdtfDate().toString();
        assertEquals(testCases.get(testCase), edtfStr, "Test case '" + testCase + "'");
        if (dateNormalizationResult.getDateNormalizationExtractorMatchId() == DateNormalizationExtractorMatchId.DCMI_PERIOD) {
          assertTrue(
              testCase.startsWith(dateNormalizationResult.getEdtfDate().getLabel()) || testCase.startsWith(
                  "name=" + dateNormalizationResult.getEdtfDate().getLabel()),
              "Test case '" + testCase + "' period name not extracted");
        }
      }
    }

  }

}
