package eu.europeana.metis.harvesting.oaipmh;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.metis.harvesting.HarvesterException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

public class OaiRecordParserTest {

  private static final Charset ENCODING = StandardCharsets.UTF_8;

  @Test
  public void shouldFilterOaiDcResponse() throws IOException, HarvesterException {

    //given
    final String fileContent = WiremockHelper.getFileContent("/sampleOaiRecord.xml");
    final InputStream inputStream = IOUtils.toInputStream(fileContent, ENCODING);
    String content = IOUtils.toString(inputStream, ENCODING);

    //when
    final InputStream result = new OaiRecordParser(content).getRdfRecord();

    //then
    final String actual = TestHelper.convertToString(result);
    assertThat(actual,
            TestHelper.isSimilarXml(WiremockHelper.getFileContent("/expectedOaiRecord.xml")));
  }

  @Test
  public void shouldReturnRecordIsDeleted() throws IOException, HarvesterException {
    //given
    final String fileContent = WiremockHelper.getFileContent("/deletedOaiRecord.xml");
    final InputStream inputStream = IOUtils.toInputStream(fileContent, ENCODING);
    String content = IOUtils.toString(inputStream, ENCODING);
    assertTrue(new OaiRecordParser(content).recordIsDeleted());
  }

  @Test
  public void shouldReturnRecordIsNotDeleted() throws IOException, HarvesterException {
    //given
    final String fileContent = WiremockHelper.getFileContent("/sampleOaiRecord.xml");
    final InputStream inputStream = IOUtils.toInputStream(fileContent, ENCODING);
    String content = IOUtils.toString(inputStream, ENCODING);
    assertFalse(new OaiRecordParser(content).recordIsDeleted());
  }

  @Test
  public void shouldThrowExceptionOnEmpty() throws IOException {
    //given
    final String fileContent = "";
    final InputStream inputStream = IOUtils.toInputStream(fileContent, ENCODING);
    String content = IOUtils.toString(inputStream, ENCODING);

    final HarvesterException exception = assertThrows(HarvesterException.class,
        () -> new OaiRecordParser(content).getRdfRecord());
    assertThat(exception.getMessage(), is("Cannot xpath XML!"));
  }
}
