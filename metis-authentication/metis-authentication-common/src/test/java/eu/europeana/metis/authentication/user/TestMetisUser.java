package eu.europeana.metis.authentication.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.europeana.metis.authentication.exceptions.BadContentException;
import java.io.File;
import java.util.Iterator;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-11-02
 */
public class TestMetisUser {

  private static final String DATA_JSON_NODE_ZOHO_USER_EXAMPLE = "data/jsonNodeZohoUserExample";
  private static final String DATA_JSON_NODE_ZOHO_ORGANIZATION_EXAMPLE = "data/jsonNodeZohoOrganizationExample";

  @Test
  public void metisUserConstructor() throws Exception {
    File jsonNodeZohoUserExampleFile = new File(getClass().getClassLoader().getResource(
        DATA_JSON_NODE_ZOHO_USER_EXAMPLE).getFile());
    ObjectMapper mapper = new ObjectMapper();
    JsonNode jsonNodeZohoUserExample = mapper
        .readTree(FileUtils.readFileToString(jsonNodeZohoUserExampleFile));

    File jsonNodeZohoOrganizationExampleFile = new File(getClass().getClassLoader().getResource(
        DATA_JSON_NODE_ZOHO_ORGANIZATION_EXAMPLE).getFile());
    JsonNode jsonNodeZohoOrganizationExample = mapper
        .readTree(FileUtils.readFileToString(jsonNodeZohoOrganizationExampleFile));

    MetisUser metisUser = new MetisUser(jsonNodeZohoUserExample.get("FL"));
    Assert.assertEquals(true, metisUser.isMetisUserFlag());
    Assert.assertEquals(AccountRole.EUROPEANA_DATA_OFFICER, metisUser.getAccountRole());
    Assert.assertEquals("simon.metis@europeana.eu", metisUser.getEmail());
    Assert.assertEquals(true, metisUser.isNetworkMember());
    Assert.assertNotNull(metisUser.getUserId());
    Assert.assertEquals("Europeana Foundation", metisUser.getOrganizationName());

    metisUser.setOrganizationIdFromJsonNode(jsonNodeZohoOrganizationExample.get("FL"));
    Assert.assertNotNull(metisUser.getOrganizationId());
  }

  @Test(expected = BadContentException.class)
  public void metisUserConstructorWithAdminRoleFromZohoFails() throws Exception {
    File jsonNodeZohoUserExampleFile = new File(getClass().getClassLoader().getResource(
        DATA_JSON_NODE_ZOHO_USER_EXAMPLE).getFile());
    ObjectMapper mapper = new ObjectMapper();
    JsonNode jsonNodeZohoUserExample = mapper
        .readTree(FileUtils.readFileToString(jsonNodeZohoUserExampleFile));
    Iterator<JsonNode> elements = jsonNodeZohoUserExample.get("FL").elements();
    while (elements.hasNext()) {
      JsonNode next = elements.next();
      if (next.get("val").textValue().equals("Account Role")) {
        ObjectNode objectNode = (ObjectNode) next;
        objectNode.remove("val");
        objectNode.remove("content");
        objectNode.put("val", "Account Role");
        objectNode.put("content", "METIS_ADMIN");
      }
    }
    new MetisUser(jsonNodeZohoUserExample.get("FL"));
  }

  @Test(expected = BadContentException.class)
  public void metisUserSetOrganizationWithEmptyRoleFails() throws Exception {
    File jsonNodeZohoOrganizationExampleFile = new File(getClass().getClassLoader().getResource(
        DATA_JSON_NODE_ZOHO_ORGANIZATION_EXAMPLE).getFile());
    ObjectMapper mapper = new ObjectMapper();
    JsonNode jsonNodeZohoOrganizationExample = mapper
        .readTree(FileUtils.readFileToString(jsonNodeZohoOrganizationExampleFile));
    Iterator<JsonNode> elements = jsonNodeZohoOrganizationExample.get("FL").elements();
    while (elements.hasNext()) {
      JsonNode next = elements.next();
      if (next.get("val").textValue().equals("Organisation Role")) {
        ObjectNode objectNode = (ObjectNode) next;
        objectNode.remove("content");
        objectNode.put("content", "WRONG_ROLE");
      }
    }
    MetisUser metisUser = new MetisUser();
    metisUser.setOrganizationIdFromJsonNode(jsonNodeZohoOrganizationExample.get("FL"));
  }


}
