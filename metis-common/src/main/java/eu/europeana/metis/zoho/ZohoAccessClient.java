package eu.europeana.metis.zoho;

import com.zoho.crm.library.api.response.BulkAPIResponse;
import com.zoho.crm.library.crud.ZCRMModule;
import com.zoho.crm.library.crud.ZCRMRecord;
import com.zoho.crm.library.exception.ZCRMException;
import com.zoho.crm.library.setup.restclient.ZCRMRestClient;
import com.zoho.oauth.client.ZohoOAuthClient;
import com.zoho.oauth.common.ZohoOAuthException;
import eu.europeana.metis.exception.BadContentException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-12-06
 */
public class ZohoAccessClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZohoAccessClient.class);
  private final ZCRMModule zcrmModule;
  private final ZCRMModule zcrmModuleAccounts;

  public ZohoAccessClient(String grantToken) throws Exception {
    ZCRMRestClient.initialize();
    ZohoOAuthClient cli = ZohoOAuthClient.getInstance();
    try {
      cli.generateAccessToken(grantToken);
    } catch (ZohoOAuthException ex) {
      LOGGER
          .warn("Exception when generating access token. Grant tokens can be used only once, "
              + "so when the access and refresh tokens are generated, the grant token becomes obsolete on subsequent deployments");
    }

    zcrmModule = ZCRMModule.getInstance(ZohoConstants.CONTACTS_MODULE);
    zcrmModuleAccounts = ZCRMModule.getInstance(ZohoConstants.ACCOUNTS_MODULE);
  }

  public ZCRMRecord getZcrmRecordContactByEmail(String email) throws BadContentException {
    final BulkAPIResponse bulkAPIResponseContacts;
    try {
      bulkAPIResponseContacts = zcrmModule.searchByEmail(email);
    } catch (ZCRMException e) {
      throw new BadContentException("Zoho search by email threw an exception", e);
    }
    final List<ZCRMRecord> zcrmRecords = (List<ZCRMRecord>) bulkAPIResponseContacts.getData();
    if (zcrmRecords.isEmpty()) {
      return null;
    }

    return zcrmRecords.get(0);
  }


  public ZCRMRecord getZcrmRecordOrganizationByName(String organizationName)
      throws BadContentException {
    final BulkAPIResponse bulkAPIResponseAccounts;
    try {
      bulkAPIResponseAccounts = zcrmModuleAccounts
          .searchByCriteria(
              String.format("(%s:%s:%s)", ZohoConstants.ACCOUNT_NAME_FIELD, ZohoConstants.EQUALS_OPERATION, organizationName));
    } catch (ZCRMException e) {
      throw new BadContentException("Zoho search organization by organization name threw an exception", e);
    }
    final List<ZCRMRecord> zcrmRecords = (List<ZCRMRecord>) bulkAPIResponseAccounts.getData();
    if (zcrmRecords.isEmpty()) {
      return null;
    }

    return zcrmRecords.get(0);
  }
}
