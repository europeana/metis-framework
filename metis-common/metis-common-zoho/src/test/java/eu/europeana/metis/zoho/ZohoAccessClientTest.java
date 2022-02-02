package eu.europeana.metis.zoho;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zoho.crm.api.record.DeletedRecord;
import com.zoho.crm.api.record.Record;
import com.zoho.crm.api.users.User;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

/**
 * Unit test for {@link ZohoAccessClient}
 * very simple testing, only tests signatures of methods
 *
 * @author Jorge Ortiz
 * @since 02-02-2022
 */
class ZohoAccessClientTest {

  @Mock
  private ZohoAccessClient accessClient;

  @BeforeEach
  void setup() {
    accessClient = mock(ZohoAccessClient.class);
  }

  @Test
  void getZohoRecordContactByEmail() throws ZohoException {
    final Record testRecord = new Record();
    testRecord.addKeyValue(ZohoConstants.EMAIL_FIELD, "test@gmail.com");
    final Optional<Record> optionalRecord = Optional.of(testRecord);
    when(accessClient.getZohoRecordContactByEmail(anyString())).thenReturn(optionalRecord);

    final Optional<Record> actualEmail = accessClient.getZohoRecordContactByEmail("test@gmail.com");

    assertEquals("test@gmail.com", actualEmail.get().getKeyValue(ZohoConstants.EMAIL_FIELD));
  }

  @Test
  void getZohoRecordOrganizationByName() throws ZohoException {
    final Record testRecord = new Record();
    testRecord.addKeyValue(ZohoConstants.ACCOUNT_NAME_FIELD, "europeana");
    final Optional<Record> optionalRecord = Optional.of(testRecord);
    when(accessClient.getZohoRecordOrganizationByName(anyString())).thenReturn(optionalRecord);

    final Optional<Record> actualName = accessClient.getZohoRecordOrganizationByName("europeana");

    assertEquals("europeana", actualName.get().getKeyValue(ZohoConstants.ACCOUNT_NAME_FIELD));
  }

  @Test
  void getZohoRecordOrganizationById() throws ZohoException {
    final Record testRecord = new Record();
    testRecord.addKeyValue(ZohoConstants.ID_FIELD, "1a192382-844b-11ec-a8a3-0242ac120002");
    final Optional<Record> optionalRecord = Optional.of(testRecord);
    when(accessClient.getZohoRecordOrganizationById(anyString())).thenReturn(optionalRecord);

    final Optional<Record> actualId = accessClient.getZohoRecordOrganizationById("1a192382-844b-11ec-a8a3-0242ac120002");

    assertEquals("1a192382-844b-11ec-a8a3-0242ac120002", actualId.get().getKeyValue(ZohoConstants.ID_FIELD));
  }

  @Test
  void getZohoDeletedRecordOrganizations() throws ZohoException {
    final List<DeletedRecord> optionalRecord = getDeletedRecords();

    when(accessClient.getZohoDeletedRecordOrganizations(anyInt())).thenReturn(optionalRecord);

    final List<DeletedRecord> deletedRecords = accessClient.getZohoDeletedRecordOrganizations(1);

    assertEquals(2, deletedRecords.size());
    assertEquals(410888000000099071L, deletedRecords.get(0).getId());
    assertEquals(410888000000094004L, deletedRecords.get(1).getId());
  }

  @Test
  void getZcrmRecordOrganizations() throws ZohoException {
    final List<Record> recordList = getOrganizationRecords();
    when(accessClient.getZcrmRecordOrganizations(anyInt(), anyInt(), any(OffsetDateTime.class))).thenReturn(recordList);

    final List<Record> actualList = accessClient.getZcrmRecordOrganizations(1, 10,
        OffsetDateTime.parse("2022-02-02T02:20:22+02:00"));

    assertEquals(2, actualList.size());
    assertEquals("europeana", actualList.get(0).getKeyValue(ZohoConstants.ACCOUNT_NAME_FIELD));
    assertEquals("europeana pro", actualList.get(1).getKeyValue(ZohoConstants.ACCOUNT_NAME_FIELD));
  }

  @Test
  void testGetZcrmRecordOrganizations() throws ZohoException {
    final List<Record> recordList = getOrganizationRecords();
    when(accessClient.getZcrmRecordOrganizations(anyInt(), anyInt(), any(OffsetDateTime.class), any(Map.class),
        anyString())).thenReturn(recordList);

    final List<Record> actualList = accessClient.getZcrmRecordOrganizations(1, 10,
        OffsetDateTime.parse("2022-02-02T02:20:22+02:00"), new HashMap<>() {{
          put("key1", "value1");
          put("key2", "value2");
        }}, "(Last_Name:equals:Burns%5C%2CB)and(First_Name:starts_with:M)");

    assertEquals(2, actualList.size());
    assertEquals("europeana", actualList.get(0).getKeyValue(ZohoConstants.ACCOUNT_NAME_FIELD));
    assertEquals("europeana pro", actualList.get(1).getKeyValue(ZohoConstants.ACCOUNT_NAME_FIELD));
  }

  private List<DeletedRecord> getDeletedRecords() {
    final DeletedRecord testRecord = new DeletedRecord();
    final User userRecord = new User();
    testRecord.setDeletedBy(userRecord);
    userRecord.setName("Patricia Boyle");
    testRecord.setId(410888000000099071L);
    testRecord.setDisplayName("Patricia");
    testRecord.setType("recycle");
    testRecord.setCreatedBy(userRecord);
    testRecord.setDeletedTime(OffsetDateTime.parse("2022-02-02T02:20:22+02:00"));

    final DeletedRecord testRecord2 = new DeletedRecord();
    testRecord2.setDeletedBy(userRecord);
    testRecord2.setId(410888000000094004L);
    testRecord2.setDeletedBy(userRecord);
    testRecord2.setDisplayName("Patricia");
    testRecord2.setType("recycle");
    testRecord2.setCreatedBy(userRecord);
    testRecord2.setDeletedTime(OffsetDateTime.parse("2022-02-02T02:22:22+02:00"));

    final List<DeletedRecord> optionalRecord = List.of(testRecord, testRecord2);
    return optionalRecord;
  }

  private List<Record> getOrganizationRecords() {
    final Record testRecord = new Record();
    testRecord.addKeyValue(ZohoConstants.ACCOUNT_NAME_FIELD, "europeana");
    final Record testRecord2 = new Record();
    testRecord2.addKeyValue(ZohoConstants.ACCOUNT_NAME_FIELD, "europeana pro");
    return List.of(testRecord, testRecord2);
  }
}