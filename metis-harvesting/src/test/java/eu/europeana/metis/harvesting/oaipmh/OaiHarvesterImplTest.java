package eu.europeana.metis.harvesting.oaipmh;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.WireMockServer;
import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.oaipmh.OaiHarvesterImpl.ConnectionClientFactory;
import eu.europeana.metis.network.NetworkUtil;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class OaiHarvesterImplTest {

  private static int portForWireMock = 9999;

  static {
    try {
      portForWireMock = NetworkUtil.getAvailableLocalPort();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  static WireMockServer wireMockServer;
  static String localhostUrl = "http://127.0.0.1:" + portForWireMock;
  private static final String OAI_PMH_ENDPOINT = localhostUrl + "/oai-phm/";

  private static final ConnectionClientFactory CONNECTION_CLIENT_FACTORY = TestHelper.CONNECTION_CLIENT_FACTORY::apply;

  @BeforeAll
  static void prepare() {
    wireMockServer = new WireMockServer(wireMockConfig().port(portForWireMock));
    wireMockServer.start();
  }

  @Test
  public void shouldHarvestRecord() throws IOException, HarvesterException {

    //given
    final String recordId = "mediateka";
    wireMockServer.stubFor(get(urlEqualTo(
        "/oai-phm/?verb=GetRecord&identifier=" + recordId + "&metadataPrefix=oai_dc")).willReturn(
        WiremockHelper
            .response200XmlContent(WiremockHelper.getFileContent("/sampleOaiRecord.xml"))));
    final OaiHarvesterImpl harvester = new OaiHarvesterImpl(CONNECTION_CLIENT_FACTORY);

    //when
    final InputStream result = harvester
        .harvestRecord(new OaiRepository(OAI_PMH_ENDPOINT, "oai_dc"), recordId);

    //then
    final String actual = TestHelper.convertToString(result);
    assertThat(actual,
        TestHelper.isSimilarXml(WiremockHelper.getFileContent("/expectedOaiRecord.xml")));
  }


  @Test
  public void shouldHandleDeletedRecords() throws IOException, HarvesterException {

    //given
    final String recordId = "mediateka";
    wireMockServer.stubFor(get(urlEqualTo(
        "/oai-phm/?verb=GetRecord&identifier=" + recordId + "&metadataPrefix=oai_dc")).willReturn(
        WiremockHelper
            .response200XmlContent(WiremockHelper.getFileContent("/deletedOaiRecord.xml"))));
    final OaiHarvesterImpl harvester = new OaiHarvesterImpl(CONNECTION_CLIENT_FACTORY);

    assertThrows(HarvesterException.class,
        () -> harvester.harvestRecord(new OaiRepository(OAI_PMH_ENDPOINT, "oai_dc"), recordId));
  }

  @Test
  public void shouldThrowExceptionHarvestedRecordNotFound() {

    //given
    final String recordId = "oai:mediateka.centrumzamenhofa.pl:19";
    wireMockServer.stubFor(get(urlEqualTo(
        "/oai-phm/?verb=GetRecord&identifier=" + URLEncoder.encode(recordId, StandardCharsets.UTF_8)
            + "&metadataPrefix=oai_dc")).willReturn(WiremockHelper.response404()));
    final OaiHarvesterImpl harvester = new OaiHarvesterImpl(CONNECTION_CLIENT_FACTORY);

    final HarvesterException exception = assertThrows(HarvesterException.class,
        () -> harvester.harvestRecord(new OaiRepository(OAI_PMH_ENDPOINT, "oai_dc"), recordId));

    assertThat(exception.getMessage(),
        is("Problem with harvesting record " + recordId + " for endpoint " + OAI_PMH_ENDPOINT
            + " because of: Error querying service. Returned HTTP Status Code: 404"));
  }

  @Test
  public void shouldGetCorrectCompleteListSize() throws Exception {

    final String schema1 = "schema1";
    final String schema2 = "schema2";
    final String set1 = "set1";
    final String set2 = "set2";

    wireMockServer.stubFor(
        get(urlEqualTo("/oai-phm/?verb=ListIdentifiers&set=" + set1 + "&metadataPrefix=" + schema1))
            .willReturn(WiremockHelper
                .response200XmlContent(WiremockHelper.getFileContent("/oaiListIdentifiers.xml"))));
    wireMockServer.stubFor(
        get(urlEqualTo("/oai-phm/?verb=ListIdentifiers&set=" + set2 + "&metadataPrefix=" + schema2))
            .willReturn(WiremockHelper
                .response200XmlContent(WiremockHelper.getFileContent("/oaiListIdentifiers2.xml"))));

    final OaiHarvesterImpl harvester = new OaiHarvesterImpl(CONNECTION_CLIENT_FACTORY);
    final OaiHarvest harvest1 = new OaiHarvest(OAI_PMH_ENDPOINT, schema1, set1);
    final OaiHarvest harvest2 = new OaiHarvest(OAI_PMH_ENDPOINT, schema2, set2);

    assertEquals(Integer.valueOf(2932), harvester.countRecords(harvest1));
    assertEquals(Integer.valueOf(2), harvester.countRecords(harvest2));
  }

  @Test
  public void shouldReturnNullWhenEmptyCompleteListSize() throws Exception {
    wireMockServer.stubFor(get(urlEqualTo("/oai-phm/?verb=ListIdentifiers")).willReturn(
        WiremockHelper.response200XmlContent(
            WiremockHelper.getFileContent("/oaiListIdentifiersNoCompleteListSize.xml"))));
    final OaiHarvesterImpl harvester = new OaiHarvesterImpl(CONNECTION_CLIENT_FACTORY);
    final OaiHarvest harvest = new OaiHarvest(OAI_PMH_ENDPOINT, null, null);
    assertNull(harvester.countRecords(harvest));
  }

  @Test
  public void shouldReturnNullWhenIncorrectCompleteListSize() throws Exception {
    wireMockServer.stubFor(get(urlEqualTo("/oai-phm/?verb=ListIdentifiers")).willReturn(
        WiremockHelper.response200XmlContent(
            WiremockHelper.getFileContent("/oaiListIdentifiersIncorrectCompleteListSize.xml"))));
    final OaiHarvesterImpl harvester = new OaiHarvesterImpl(CONNECTION_CLIENT_FACTORY);
    final OaiHarvest harvest = new OaiHarvest(OAI_PMH_ENDPOINT, null, null);
    assertNull(harvester.countRecords(harvest));
  }

  @Test
  public void shouldReturnNullWhen200ReturnedButErrorInResponse() throws Exception {
    wireMockServer.stubFor(get(urlEqualTo("/oai-phm/?verb=ListIdentifiers")).willReturn(
        WiremockHelper.response200XmlContent(
            WiremockHelper.getFileContent("/oaiListIdentifiersIncorrectMetadataPrefix.xml"))));
    final OaiHarvesterImpl harvester = new OaiHarvesterImpl(CONNECTION_CLIENT_FACTORY);
    final OaiHarvest harvest = new OaiHarvest(OAI_PMH_ENDPOINT, null, null);
    assertNull(harvester.countRecords(harvest));
  }

  @Test
  public void shouldReturnNullWhenNoResumptionToken() throws Exception {
    wireMockServer.stubFor(get(urlEqualTo("/oai-phm/?verb=ListIdentifiers")).willReturn(
        WiremockHelper.response200XmlContent(
            WiremockHelper.getFileContent("/oaiListIdentifiersNoResumptionToken.xml"))));
    final OaiHarvesterImpl harvester = new OaiHarvesterImpl(CONNECTION_CLIENT_FACTORY);
    final OaiHarvest harvest = new OaiHarvest(OAI_PMH_ENDPOINT, null, null);
    assertNull(harvester.countRecords(harvest));
  }

  @AfterAll
  static void destroy() {
    wireMockServer.stop();
  }
}
