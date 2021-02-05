package eu.europeana.metis.harvesting.oaipmh;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import eu.europeana.metis.harvesting.HarvesterException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

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
        assertThat(actual, TestHelper.isSimilarXml(WiremockHelper.getFileContent("/expectedOaiRecord.xml")));
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

        try {
            //when
            new OaiRecordParser(content).getRdfRecord();
            fail();
        } catch (HarvesterException e) {
            //then
            assertThat(e.getMessage(), is("Cannot xpath XML!"));
        }
    }
}
