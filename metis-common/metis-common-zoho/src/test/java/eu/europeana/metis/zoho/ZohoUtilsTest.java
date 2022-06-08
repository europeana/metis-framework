package eu.europeana.metis.zoho;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zoho.crm.api.record.Record;
import com.zoho.crm.api.util.Choice;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
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
    final List expectedChoiceList = List.of("Organization1Role", "Organization2Role");
    recordOrganization.addKeyValue(ZohoConstants.ORGANIZATION_ROLE_FIELD,
        expectedChoiceList.stream().map(Choice::new).collect(Collectors.toList()));

    final List<String> organizationRoleStringList = ZohoUtils.stringListSupplier(
        recordOrganization.getKeyValue(ZohoConstants.ORGANIZATION_ROLE_FIELD));

    assertEquals(2, organizationRoleStringList.size());
    assertTrue(CollectionUtils.isEqualCollection(expectedChoiceList, organizationRoleStringList));
  }
}