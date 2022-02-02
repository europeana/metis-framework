package eu.europeana.metis.zoho;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link ZohoAccessClient}
 *
 * @author Jorge Ortiz
 * @since 02-02-2022
 */
class ZohoAccessClientTest {

  @Mock
  private ZohoAccessClient accessClient;

  @Test
  void getZohoRecordContactByEmail() {
    accessClient.getZohoRecordContactByEmail()
  }

  @Test
  void getZohoRecordOrganizationByName() {
  }

  @Test
  void getZohoRecordOrganizationById() {
  }

  @Test
  void getZohoDeletedRecordOrganizations() {
  }

  @Test
  void getZcrmRecordOrganizations() {
  }

  @Test
  void testGetZcrmRecordOrganizations() {
  }
}