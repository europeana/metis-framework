package eu.europeana.metis.zoho;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zoho.crm.api.record.Record;
import com.zoho.crm.api.util.Choice;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit Test for {@link ZohoUtils}
 *
 * @author Jorge Ortiz
 * @since 02-02-2022
 */
class ZohoUtilsTest {

  @Test
  void stringFieldSupplier() {
    final Record record = new Record();
    record.addKeyValue(ZohoConstants.FIRST_NAME_FIELD, "First Name");

    final String actualKey = ZohoUtils.stringFieldSupplier(record.getKeyValue(ZohoConstants.FIRST_NAME_FIELD));

    assertEquals("First Name", actualKey);
  }

  @Test
  void stringListSupplier() {
    final Record recordOrganization = new Record();
    recordOrganization.addKeyValue(ZohoConstants.ORGANIZATION_ROLE_FIELD,
        List.of(new Choice<>("Organization1Role"),
            new Choice<>("Organization2Role")));

    final List<String> organizationRoleStringList = ZohoUtils.stringListSupplier(
        recordOrganization.getKeyValue(ZohoConstants.ORGANIZATION_ROLE_FIELD));

    assertEquals(2, organizationRoleStringList.size());
    assertEquals("Organization1Role", organizationRoleStringList.get(0));
    assertEquals("Organization2Role", organizationRoleStringList.get(1));
  }
}