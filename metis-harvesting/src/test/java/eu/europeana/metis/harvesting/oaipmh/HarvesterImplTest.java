package eu.europeana.metis.harvesting.oaipmh;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.oaipmh.OaiHarvesterImpl.ConnectionClientFactory;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;

public class HarvesterImplTest extends WiremockHelper {

    private static final String OAI_PMH_ENDPOINT = "http://localhost:8181/oai-phm/";

    protected static final int TEST_RETRIES = 1;
    protected static final int TEST_SLEEP_TIME = 2_000; /* = 2sec */
    private static final int TEST_SOCKET_TIMEOUT = 5_000; /* = 5sec */

    private static final ConnectionClientFactory CONNECTION_CLIENT_FACTORY = baseUrl ->
            new CloseableHttpOaiClient(baseUrl, null, TEST_RETRIES, TEST_SLEEP_TIME,
                    TEST_SOCKET_TIMEOUT, TEST_SOCKET_TIMEOUT, TEST_SOCKET_TIMEOUT);

    @Test
    public void shouldHarvestRecord() throws IOException, HarvesterException {

        //given
        stubFor(get(urlEqualTo("/oai-phm/?verb=GetRecord&identifier=mediateka" +
                "&metadataPrefix=oai_dc"))
                .willReturn(response200XmlContent(getFileContent("/sampleOaiRecord.xml"))
                ));
        final OaiHarvesterImpl harvester = new OaiHarvesterImpl(CONNECTION_CLIENT_FACTORY);

        //when
        final InputStream result = harvester
                .harvestRecord(new OaiRepository(OAI_PMH_ENDPOINT, "oai_dc"), "mediateka");

        //then
        final String actual = TestHelper.convertToString(result);
        assertThat(actual, TestHelper.isSimilarXml(getFileContent("/expectedOaiRecord.xml")));
    }


    @Test(expected = HarvesterException.class)
    public void shouldHandleDeletedRecords() throws IOException, HarvesterException {

        //given
        stubFor(get(urlEqualTo("/oai-phm/?verb=GetRecord&identifier=mediateka" +
                "&metadataPrefix=oai_dc"))
                .willReturn(response200XmlContent(getFileContent("/deletedOaiRecord.xml"))
                ));
        final OaiHarvesterImpl harvester = new OaiHarvesterImpl(CONNECTION_CLIENT_FACTORY);

        //when
        harvester.harvestRecord(new OaiRepository(OAI_PMH_ENDPOINT, "oai_dc"), "mediateka");

        //then
        //exception expected
    }

    @Test
    public void shouldThrowExceptionHarvestedRecordNotFound() {

        //given
        stubFor(get(urlEqualTo("/oai-phm/?verb=GetRecord&identifier=oai%3Amediateka.centrumzamenhofa" +
                ".pl%3A19&metadataPrefix=oai_dc"))
                .willReturn(response404()));
        final OaiHarvesterImpl harvester = new OaiHarvesterImpl(CONNECTION_CLIENT_FACTORY);

        //when
        try {
            harvester.harvestRecord(new OaiRepository(OAI_PMH_ENDPOINT, "oai_dc"),
                    "oai:mediateka.centrumzamenhofa.pl:19");
            fail();
        } catch (HarvesterException e) {
            //then
            assertThat(e.getMessage(),
                    is("Problem with harvesting record oai:mediateka.centrumzamenhofa.pl:19 for endpoint http://localhost:8181/oai-phm/ because of: Error querying service. Returned HTTP Status Code: 404"));
        }
    }

    @Test(expected = HarvesterException.class)
    public void shouldHandleTimeout() throws IOException, HarvesterException {

        //given
        stubFor(get(urlEqualTo("/oai-phm/?verb=GetRecord&identifier=mediateka" +
                "&metadataPrefix=oai_dc"))
                .willReturn(responsTimeoutGreaterThanSocketTimeout(getFileContent("/sampleOaiRecord.xml"), TEST_SOCKET_TIMEOUT)
                ));
        final OaiHarvesterImpl harvester = new OaiHarvesterImpl(CONNECTION_CLIENT_FACTORY);

        //when
        harvester.harvestRecord(new OaiRepository(OAI_PMH_ENDPOINT, "oai_dc"), "mediateka");

        //then
        //exception expected
    }
}
