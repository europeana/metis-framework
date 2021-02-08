package eu.europeana.metis.harvesting.oaipmh;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.oaipmh.OaiHarvesterImpl.ConnectionClientFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

public class OaiHarvesterImplTest extends WiremockHelper {

    private static final String OAI_PMH_ENDPOINT = "http://localhost:8181/oai-phm/";

    private static final ConnectionClientFactory CONNECTION_CLIENT_FACTORY = CloseableHttpOaiClient::new;

    @Test
    public void shouldHarvestRecord() throws IOException, HarvesterException {

        //given
        final String recordId = "mediateka";
        stubFor(get(urlEqualTo(
                "/oai-phm/?verb=GetRecord&identifier=" + recordId + "&metadataPrefix=oai_dc"))
                .willReturn(response200XmlContent(getFileContent("/sampleOaiRecord.xml"))));
        final OaiHarvesterImpl harvester = new OaiHarvesterImpl(CONNECTION_CLIENT_FACTORY);

        //when
        final InputStream result = harvester
                .harvestRecord(new OaiRepository(OAI_PMH_ENDPOINT, "oai_dc"), recordId);

        //then
        final String actual = TestHelper.convertToString(result);
        assertThat(actual, TestHelper.isSimilarXml(getFileContent("/expectedOaiRecord.xml")));
    }


    @Test(expected = HarvesterException.class)
    public void shouldHandleDeletedRecords() throws IOException, HarvesterException {

        //given
        final String recordId = "mediateka";
        stubFor(get(urlEqualTo(
                "/oai-phm/?verb=GetRecord&identifier=" + recordId + "&metadataPrefix=oai_dc"))
                .willReturn(response200XmlContent(getFileContent("/deletedOaiRecord.xml"))));
        final OaiHarvesterImpl harvester = new OaiHarvesterImpl(CONNECTION_CLIENT_FACTORY);

        //when
        harvester.harvestRecord(new OaiRepository(OAI_PMH_ENDPOINT, "oai_dc"), recordId);

        //then
        //exception expected
    }

    @Test
    public void shouldThrowExceptionHarvestedRecordNotFound() {

        //given
        final String recordId = "oai:mediateka.centrumzamenhofa.pl:19";
        stubFor(get(urlEqualTo("/oai-phm/?verb=GetRecord&identifier=" + URLEncoder
                .encode(recordId, StandardCharsets.UTF_8) + "&metadataPrefix=oai_dc"))
                .willReturn(response404()));
        final OaiHarvesterImpl harvester = new OaiHarvesterImpl(CONNECTION_CLIENT_FACTORY);

        //when
        try {
            harvester.harvestRecord(new OaiRepository(OAI_PMH_ENDPOINT, "oai_dc"),                    recordId);
            fail();
        } catch (HarvesterException e) {
            //then
            assertThat(e.getMessage(), is("Problem with harvesting record " + recordId
                    + " for endpoint http://localhost:8181/oai-phm/ because of: Error querying service. Returned HTTP Status Code: 404"));
        }
    }

    @Test
    public void shouldGetCorrectCompleteListSize() throws Exception {

        final String schema1 = "schema1";
        final String schema2 = "schema2";
        final String set1 = "set1";
        final String set2 = "set2";

        stubFor(get(urlEqualTo("/oai-phm/?verb=ListIdentifiers&set=" + set1 + "&metadataPrefix=" + schema1))
                .willReturn(response200XmlContent(getFileContent("/oaiListIdentifiers.xml"))));
        stubFor(get(urlEqualTo("/oai-phm/?verb=ListIdentifiers&set=" + set2 + "&metadataPrefix=" + schema2))
                .willReturn(response200XmlContent(getFileContent("/oaiListIdentifiers2.xml"))));

        final OaiHarvesterImpl harvester = new OaiHarvesterImpl(CONNECTION_CLIENT_FACTORY);
        final OaiHarvest harvest1 = new OaiHarvest(OAI_PMH_ENDPOINT, schema1, set1);
        final OaiHarvest harvest2 = new OaiHarvest(OAI_PMH_ENDPOINT, schema2, set2);

        assertEquals(Integer.valueOf(2932), harvester.countRecords(harvest1));
        assertEquals(Integer.valueOf(2), harvester.countRecords(harvest2));
    }

    @Test
    public void shouldReturnNullWhenEmptyCompleteListSize() throws Exception {
        stubFor(get(urlEqualTo("/oai-phm/?verb=ListIdentifiers"))
                .willReturn(response200XmlContent(getFileContent("/oaiListIdentifiersNoCompleteListSize.xml"))));
        final OaiHarvesterImpl harvester = new OaiHarvesterImpl(CONNECTION_CLIENT_FACTORY);
        final OaiHarvest harvest = new OaiHarvest(OAI_PMH_ENDPOINT, null, null);
        assertNull(harvester.countRecords(harvest));
    }

    @Test
    public void shouldReturnNullWhenIncorrectCompleteListSize() throws Exception {
        stubFor(get(urlEqualTo("/oai-phm/?verb=ListIdentifiers"))
                .willReturn(response200XmlContent(getFileContent("/oaiListIdentifiersIncorrectCompleteListSize.xml"))));
        final OaiHarvesterImpl harvester = new OaiHarvesterImpl(CONNECTION_CLIENT_FACTORY);
        final OaiHarvest harvest = new OaiHarvest(OAI_PMH_ENDPOINT, null, null);
        assertNull(harvester.countRecords(harvest));
    }

    @Test
    public void shouldReturnNullWhen200ReturnedButErrorInResponse() throws Exception {
        stubFor(get(urlEqualTo("/oai-phm/?verb=ListIdentifiers"))
                .willReturn(response200XmlContent(getFileContent("/oaiListIdentifiersIncorrectMetadataPrefix.xml"))));
        final OaiHarvesterImpl harvester = new OaiHarvesterImpl(CONNECTION_CLIENT_FACTORY);
        final OaiHarvest harvest = new OaiHarvest(OAI_PMH_ENDPOINT, null, null);
        assertNull(harvester.countRecords(harvest));
    }

    @Test
    public void shouldReturnNullWhenNoResumptionToken() throws Exception {
        stubFor(get(urlEqualTo("/oai-phm/?verb=ListIdentifiers"))
                .willReturn(response200XmlContent(getFileContent("/oaiListIdentifiersNoResumptionToken.xml"))));
        final OaiHarvesterImpl harvester = new OaiHarvesterImpl(CONNECTION_CLIENT_FACTORY);
        final OaiHarvest harvest = new OaiHarvest(OAI_PMH_ENDPOINT, null, null);
        assertNull(harvester.countRecords(harvest));
    }

}
