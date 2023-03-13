package eu.europeana.normalization.normalizers;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.BC_AD;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.BRIEF_DATE_RANGE;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.CENTURY_NUMERIC;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.CENTURY_RANGE_ROMAN;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.CENTURY_ROMAN;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.DCMI_PERIOD;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.EDTF;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.FORMATTED_FULL_DATE;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.MONTH_NAME;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_ALL_VARIANTS;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_ALL_VARIANTS_XX;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_RANGE_ALL_VARIANTS;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.YYYY_MM_DD_SPACES;
import static eu.europeana.normalization.dates.edtf.IntervalEdtfDate.DATE_INTERVAL_SEPARATOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.of;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.DateNormalizationResultStatus;
import eu.europeana.normalization.dates.edtf.AbstractEdtfDate;
import eu.europeana.normalization.dates.edtf.DateBoundaryType;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DatesNormalizerTest {

  private final static DatesNormalizer NORMALIZER = new DatesNormalizer();

  void assertExtract(String input, String expected, DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId,
      String label) {
    final DateNormalizationResult dateNormalizationResult = NORMALIZER.normalizeDateProperty(input);
    if (expected != null) {
      assertEquals(dateNormalizationExtractorMatchId, dateNormalizationResult.getDateNormalizationExtractorMatchId());
      assertEquals(label, dateNormalizationResult.getEdtfDate().getLabel());
      AbstractEdtfDate edtfDate = dateNormalizationResult.getEdtfDate();
      if (edtfDate instanceof IntervalEdtfDate) {
        String startPart = expected.substring(0, expected.indexOf(DATE_INTERVAL_SEPARATOR));
        String endPart = expected.substring(expected.indexOf(DATE_INTERVAL_SEPARATOR) + 1);
        InstantEdtfDate start = ((IntervalEdtfDate) edtfDate).getStart();
        InstantEdtfDate end = ((IntervalEdtfDate) edtfDate).getEnd();
        assertEdtfDate(startPart, start);
        assertEdtfDate(endPart, end);
      } else {
        assertEdtfDate(expected, (InstantEdtfDate) dateNormalizationResult.getEdtfDate());
      }
      assertEquals(expected, edtfDate.toString());
    } else {
      assertEquals(DateNormalizationResultStatus.NO_MATCH, dateNormalizationResult.getDateNormalizationResultStatus());
    }

  }

  private static void assertEdtfDate(String expected, InstantEdtfDate instantEdtfDate) {
    assertEquals(expected.contains("?"), instantEdtfDate.getDateQualification() == DateQualification.UNCERTAIN);
    assertEquals(expected.contains("~"), instantEdtfDate.getDateQualification() == DateQualification.APPROXIMATE);
    assertEquals(expected.contains("%"), instantEdtfDate.getDateQualification() == DateQualification.UNCERTAIN_APPROXIMATE);
    assertEquals(expected.equals(DateBoundaryType.OPEN.getSerializedRepresentation()),
        instantEdtfDate.getDateBoundaryType() == DateBoundaryType.OPEN
            || instantEdtfDate.getDateBoundaryType() == DateBoundaryType.UNKNOWN);
  }

  @ParameterizedTest
  @MethodSource
  void extractDateProperties(String input, String expected, DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId,
      String label) {
    assertExtract(input, expected, dateNormalizationExtractorMatchId, label);
  }

  private static Stream<Arguments> extractDateProperties() {
    Stream<Arguments> argumentsWithoutLabel = Stream.of(
        extractDatePropertiesWithoutLabel()
    ).flatMap(Function.identity()).map(arguments ->
    {
      Object[] argumentsWithLabel = Arrays.copyOf(arguments.get(), arguments.get().length + 1);
      argumentsWithLabel[argumentsWithLabel.length - 1] = null;
      return of(argumentsWithLabel);
    });
    return Stream.concat(extractDatePropertiesWithLabel(), argumentsWithoutLabel);
  }

  private static Stream<Arguments> extractDatePropertiesWithLabel() {
    return Stream.of(
        //DCMI
        of("name=Prehistoric Period; end=-5300", "../-5300", DCMI_PERIOD, "Prehistoric Period"),
        of("Byzantine Period; start=0395; end=0641", "0395/0641", DCMI_PERIOD, null),
        of("Modern era; start=1975;", "1975/..", DCMI_PERIOD, null)
    );
  }

  private static Stream<Arguments> extractDatePropertiesWithoutLabel() {
    return Stream.of(
        //Brief dates. Those are similar to EDFT but should match first.
        of("2014/15", "2014/2015", BRIEF_DATE_RANGE),
        of("1889/98? (Herstellung)", "1889?/1898?", BRIEF_DATE_RANGE),
        of("1918-20", "1918/1920", BRIEF_DATE_RANGE),

        //Centuries numeric
        of("18..", "18XX", CENTURY_NUMERIC),
        of("19??", "19XX", NUMERIC_ALL_VARIANTS_XX),
        of("192?", null, null),// ambiguous
        of("[171-]", null, null), // ambiguous
        of("19th century", "18XX", CENTURY_NUMERIC),
        of("2nd century", "01XX", CENTURY_NUMERIC),
        of("[10th century]", "09XX", CENTURY_NUMERIC), // not supported
        of("12th century BC", null, null), // not supported

        //Centuries roman
        of("XIV", "13XX", CENTURY_ROMAN),
        of("MDCLXX", null, null),
        of("MDCVII", null, null),
        of("S. XVI-XX", "15XX/19XX", CENTURY_RANGE_ROMAN),
        of("S.VIII-XV", "07XX/14XX", CENTURY_RANGE_ROMAN),
        of("S. XVI-XVIII", "15XX/17XX", CENTURY_RANGE_ROMAN),
        of("S. XVIII-", null, null), // open-ended period
        of("[XVI-XIX]", "15XX/18XX", CENTURY_RANGE_ROMAN),
        of("SVV", null, null),

        //Unknown/Unspecified start or end of range
        of("1907/?", "1907/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?/1907", "../1907", NUMERIC_RANGE_ALL_VARIANTS),
        of("1907/", "1907/..", EDTF),
        of("/1907", "../1907", EDTF),

        //Numeric range '/'
        of("1872-06-01/1872-06-30", "1872-06-01/1872-06-30", EDTF),
        of(" 1820/1820", "1820/1820", NUMERIC_RANGE_ALL_VARIANTS),
        of("1918 / 1919", "1918/1919", NUMERIC_RANGE_ALL_VARIANTS),
        of("1205/1215 [Herstellung]", "1205/1215", EDTF),
        of(" 1757/1757", "1757/1757", NUMERIC_RANGE_ALL_VARIANTS),
        of("ca 1757/1757", "1757~/1757~", EDTF),
        of("2000 vC - 2002 nC", "-2000/2002", BC_AD),
        of("0114 aC - 0113 aC", "-0114/-0113", BC_AD),
        of("0390 AD - 0425 AD", "0390/0425", BC_AD),
        of("337 BC - 283 BC", "-0337/-0283", BC_AD),
        of("100 vC - 150 nC", "-0100/0150", BC_AD),
        of("400 BC - 400 AD", "-0400/0400", BC_AD),
        of("235 AD – 236 AD", "0235/0236", BC_AD),
        of("168 B.C.-135 A.D.", "-0168/0135", BC_AD),
        of("20/09/18XX", "18XX-09-20", NUMERIC_ALL_VARIANTS_XX),
        of("?/1807", "../1807", NUMERIC_RANGE_ALL_VARIANTS),
        //Incorrect day values
        of("1947-19-50/1950-19-53", null, null),
        of("15/21-8-1918", null, null),
        of("1.1848/49[?]", null, null),

        //Numeric range ' - '(spaces around hyphen)
        of("1851-01-01  - 1851-12-31", "1851-01-01/1851-12-31", NUMERIC_RANGE_ALL_VARIANTS),
        of("1650? - 1700?", "1650?/1700?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1871 - 191-", null, null),

        //Numeric range '-'
        of("[1942-1943]", "1942/1943", NUMERIC_RANGE_ALL_VARIANTS),
        of("(1942-1943)", "1942/1943", NUMERIC_RANGE_ALL_VARIANTS),
        of("192?-1958", null, null),
        of("[ca. 1920-1930]", "1920~/1930~", NUMERIC_RANGE_ALL_VARIANTS),
        of("1937--1938", null, null),
        of("[ca. 193-]", null, null),// ambiguous
        of("1990-", null, null), // open-ended period not supported

        //Numeric range '|'
        of("1910/05/31 | 1910/05/01", "1910-05-01/1910-05-31", NUMERIC_RANGE_ALL_VARIANTS),

        //Numeric range ' '(space)
        of("1916-09-26 1916-09-28", "1916-09-26/1916-09-28", NUMERIC_RANGE_ALL_VARIANTS),
        of("29-10-2009 29-10-2009", "2009-10-29/2009-10-29", NUMERIC_RANGE_ALL_VARIANTS),
        // this may not be 100% correct, maybe it is not a range but two dates
        of("1939 [1942?]", "1939/1942?", NUMERIC_RANGE_ALL_VARIANTS),
        // this may not be a 100% correct normalisation, maybe it is not a range but two dates
        of("1651 [ca. 1656]", "1651~/1656~", NUMERIC_RANGE_ALL_VARIANTS),

        //Numeric year
        of("(17--?)", "17XX?", NUMERIC_ALL_VARIANTS_XX),
        of("[19--?]", "19XX?", NUMERIC_ALL_VARIANTS_XX),

        //Numeric date with dot "."
        of("21.1.1921", "1921-01-21", NUMERIC_ALL_VARIANTS),
        of("12.10.1690", "1690-10-12", NUMERIC_ALL_VARIANTS),
        of("26.4.1828", "1828-04-26", NUMERIC_ALL_VARIANTS),
        of("28.05.1969", "1969-05-28", NUMERIC_ALL_VARIANTS),
        of("11.11.1947", "1947-11-11", NUMERIC_ALL_VARIANTS),
        of("23.02.[18--]", "18XX-02-23", NUMERIC_ALL_VARIANTS_XX),
        of("28. 1. 1240", null, null),

        //Numeric date with dash "-"
        of("1941-22-06", "1941-06-22", NUMERIC_ALL_VARIANTS),
        of("1937-10-??", "1937-10", NUMERIC_ALL_VARIANTS_XX),
        of("199--09-28", null, null),
        of("01?-1905", null, null),
        of("02?-1915", null, null),

        //Numeric date with space " "
        of("1905 09 01", "1905-09-01", YYYY_MM_DD_SPACES),
        of("0 2 1980", "1980-02", YYYY_MM_DD_SPACES),

        //More than 4 digits year
        of("18720601/18720630", null, null),
        of("19471950/19501953", null, null),

        of("-2100/-1550", "-2100/-1550", EDTF),
        // TODO: 21/12/2022 Check the below, expected null but returns 1952-02-25 instead
        //    of("1952-02-25T00:00:00Z-1952-02-25T23:59:59Z", null),
        of("2013-09-07 09:31:51 UTC", "2013-09-07", FORMATTED_FULL_DATE),
        of("1997-07-18T00:00:00 [Create]", "1997-07-18", EDTF),
        of("1924 ca.", null, null),
        of("[1712?]", "1712?", EDTF),
        of("circa 1712", "1712~", EDTF),
        of("[ca. 1946]", "1946~", EDTF),
        of("1651?]", "1651?", EDTF),
        of("19--?]", "19XX?", NUMERIC_ALL_VARIANTS_XX),
        of(". 1885", null, null),
        of("- 1885", null, null),
        of("1749 (Herstellung (Werk))", "1749", EDTF),
        of("1939; 1954; 1955; 1978; 1939-1945", null, null), // multiple dates no suported
        of("[17__]", null, null),// this pattern is not supported (this pattern was never tested
        of("19--]", "19XX", NUMERIC_ALL_VARIANTS_XX),
        of("19xx", "19XX", NUMERIC_ALL_VARIANTS_XX),
        of("Sat Jan 01 01:00:00 CET 1701", "1701-01-01", FORMATTED_FULL_DATE),
        of("2013-03-21 18:45:36 UTC", "2013-03-21", FORMATTED_FULL_DATE),
        of("15.02.1985 (identification)", "1985-02-15", NUMERIC_ALL_VARIANTS),
        of("091090", null, null),
        of("-0043-12-07", "-0043-12-07", EDTF),
        of("imp. 1901", null, null),
        of("u.1707-1739", null, null),// what does 'u.' mean?
        of("22.07.1971 (identification)", "1971-07-22", NUMERIC_ALL_VARIANTS),

        //Ambiguous pattern
        of("187-?]", null, null),

        of("18. September 1914", "1914-09-18", MONTH_NAME),
        of("19960216-19960619", null, null),
        of("-0549-01-01T00:00:00Z", "-0549-01-01", EDTF),
        of("1942-1943 c.", null, null),
        of("(1942)", "1942", EDTF),
        of("-3.6982", null, null),
        of("[ca. 16??]", "16XX~", NUMERIC_ALL_VARIANTS_XX),
        of("ISO9126", null, null),
        of("1985-10-xx", "1985-10", NUMERIC_ALL_VARIANTS_XX),
        of("14:27", null, null),
        of("c.6 Nov 1902", "1902-11-06~", MONTH_NAME),
        of("-1234", "-1234", EDTF),
        of("09.1972 (gathering)", "1972-09", NUMERIC_ALL_VARIANTS)
    );

  }

  // TODO: 10/03/2023 Don't forget to add specific to generic properties normalization
  //    //GENERIC PROPERTY
  //    genericPropertyTestCases.put("XIV", null);
  //    genericPropertyTestCases.put("1905 09 01", "1905-09-01");
  //    genericPropertyTestCases.put("1851-01-01  - 1851-12-31", "1851-01-01/1851-12-31");
  //    genericPropertyTestCases.put("18..", null);
  //    genericPropertyTestCases.put("2013-09-07 09:31:51 UTC", "2013-09-07");
  //    genericPropertyTestCases.put("1918 / 1919", "1918/1919");
  //    genericPropertyTestCases.put("1205/1215 [Herstellung]", null);
  //    genericPropertyTestCases.put("1997-07", null);
  //    genericPropertyTestCases.put("19??", null);
  //    genericPropertyTestCases.put("1871 - 191-", null);

}