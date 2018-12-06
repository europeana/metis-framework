package eu.europeana.metis.authentication.dao;

import com.zoho.crm.library.api.response.BulkAPIResponse;
import com.zoho.crm.library.crud.ZCRMModule;
import com.zoho.crm.library.crud.ZCRMRecord;
import com.zoho.crm.library.exception.ZCRMException;
import com.zoho.crm.library.setup.restclient.ZCRMRestClient;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.NoUserFoundException;
import java.util.List;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-12-06
 */
public class ZohoAccessClient {

  private final ZCRMModule zcrmModule;
  private final ZCRMModule zcrmModuleAccounts;

  public ZohoAccessClient() throws Exception {
    ZCRMRestClient.initialize();
    zcrmModule = ZCRMModule.getInstance("Contacts");
    zcrmModuleAccounts = ZCRMModule.getInstance("Accounts");
  }

  public ZCRMRecord getZcrmRecordContactByEmail(String email)
      throws NoUserFoundException, BadContentException {
    final BulkAPIResponse bulkAPIResponseContacts;
    try {
      bulkAPIResponseContacts = zcrmModule.searchByEmail(email);
    } catch (ZCRMException e) {
      throw new BadContentException("Zoho search by email threw an exception", e);
    }
    final List<ZCRMRecord> zcrmRecords = (List<ZCRMRecord>) bulkAPIResponseContacts.getData();
    if (zcrmRecords.isEmpty()) {
      throw new NoUserFoundException("User was not found in Zoho");
    }

    return zcrmRecords.get(0);
  }


  public ZCRMRecord getZcrmRecordOrganizationByName(String organizationName)
      throws BadContentException {
    final BulkAPIResponse bulkAPIResponseAccounts;
    try {
      bulkAPIResponseAccounts = zcrmModuleAccounts
          .searchByCriteria(
              String.format("(Account_Name:equals:%s)", organizationName));
    } catch (ZCRMException e) {
      throw new BadContentException("Zoho search by email threw an exception", e);
    }
    final List<ZCRMRecord> zcrmRecords = (List<ZCRMRecord>) bulkAPIResponseAccounts.getData();
    if (zcrmRecords.isEmpty()) {
      throw new BadContentException("Organization Role from Zoho is empty");
    }

    return zcrmRecords.get(0);
  }
}
