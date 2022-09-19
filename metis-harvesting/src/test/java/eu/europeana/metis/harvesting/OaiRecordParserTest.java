package eu.europeana.metis.harvesting;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.metis.harvesting.oaipmh.OaiRecord;
import eu.europeana.metis.harvesting.oaipmh.OaiRecordParser;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

class OaiRecordParserTest {

  private static final Charset ENCODING = StandardCharsets.UTF_8;

  @Test
  void happyFlowExtantRecords() throws IOException, HarvesterException {

    //given
    final String fileContent = WiremockHelper.getFileContent("/sampleOaiRecord.xml");
    final InputStream inputStream = IOUtils.toInputStream(fileContent, ENCODING);
    byte[] content = IOUtils.toByteArray(inputStream);

    //when
    final OaiRecord result = new OaiRecordParser().parseOaiRecord(content);

    //then
    assertEquals("oai:mediateka.centrumzamenhofa.pl:19", result.getHeader().getOaiIdentifier());
    assertEquals(LocalDateTime.of(1981, 7, 1, 0, 0).toInstant(ZoneOffset.UTC),
        result.getHeader().getDatestamp());
    assertFalse(result.getHeader().isDeleted());
    final String actual = TestHelper.convertToString(result.getRecord());
    assertThat(actual,
        TestHelper.isSimilarXml(WiremockHelper.getFileContent("/expectedOaiRecord.xml")));
  }

  @Test
  void happyFlowDeletedRecords() throws IOException, HarvesterException {

    //given
    final String fileContent = WiremockHelper.getFileContent("/deletedOaiRecord.xml");
    final InputStream inputStream = IOUtils.toInputStream(fileContent, ENCODING);
    byte[] content = IOUtils.toByteArray(inputStream);

    //when
    final OaiRecord result = new OaiRecordParser().parseOaiRecord(content);

    //then
    assertTrue(result.getHeader().isDeleted());
    assertEquals("oai:mediateka.centrumzamenhofa.pl:20", result.getHeader().getOaiIdentifier());
    assertEquals(LocalDateTime.of(2020, 2, 2, 12, 21).toInstant(ZoneOffset.UTC),
        result.getHeader().getDatestamp());
    assertThrows(HarvesterException.class, result::getRecord);
  }

  @Test
  void shouldThrowExceptionOnEmpty() throws Exception {
    //given
    final String fileContent = "";
    final InputStream inputStream = IOUtils.toInputStream(fileContent, ENCODING);
    byte[] content = IOUtils.toByteArray(inputStream);

    final OaiRecordParser oaiRecordParser = new OaiRecordParser();
    final HarvesterException exception = assertThrows(HarvesterException.class,
        () -> oaiRecordParser.parseOaiRecord(content));
    assertThat(exception.getMessage(), is("Cannot xpath XML!"));
  }
}
