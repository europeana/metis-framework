package eu.europeana.metis.authentication.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.metis.exception.BadContentException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-11-02
 */
public class TestMetisUser {

  private static final String DATA_JSON_NODE_ZOHO_USER_EXAMPLE = "data/jsonNodeZohoUserExample";
  private static final String DATA_JSON_NODE_ZOHO_USER_IS_ADMIN_EXAMPLE = "data/jsonNodeZohoUserIsAdminExample";

  @Test
  public void metisUserConstructor() throws Exception {
    MetisUser metisUser = new MetisUser(getZohoJsonNodeExample(DATA_JSON_NODE_ZOHO_USER_EXAMPLE));
    Assert.assertTrue(metisUser.isMetisUserFlag());
    Assert.assertEquals(AccountRole.EUROPEANA_DATA_OFFICER, metisUser.getAccountRole());
    Assert.assertEquals("simon.metis@europeana.eu", metisUser.getEmail());
    Assert.assertTrue(metisUser.isNetworkMember());
    Assert.assertNotNull(metisUser.getUserId());
    Assert.assertEquals("Europeana Foundation", metisUser.getOrganizationName());
  }

  @Test(expected = BadContentException.class)
  public void metisUserConstructorWithAdminRoleFromZohoFails() throws Exception {
    new MetisUser(getZohoJsonNodeExample(DATA_JSON_NODE_ZOHO_USER_IS_ADMIN_EXAMPLE));
  }

  private JsonNode getZohoJsonNodeExample(String filePath) throws IOException, URISyntaxException {
    URL resource = getClass().getClassLoader().getResource(filePath);
    if (resource != null) {
      File jsonNodeZohoUserExampleFile = new File(resource.toURI());
      ObjectMapper mapper = new ObjectMapper();
      JsonNode jsonNodeZohoUserExample = mapper
          .readTree(FileUtils.readFileToString(jsonNodeZohoUserExampleFile));

      return jsonNodeZohoUserExample.get("FL");
    }
    throw new FileNotFoundException();
  }


}
