package eu.europeana.metis.data.checker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import eu.europeana.metis.data.checker.common.exception.DataCheckerServiceException;
import eu.europeana.metis.data.checker.common.model.DatasetProperties;
import eu.europeana.metis.data.checker.common.model.ExtendedValidationResult;
import eu.europeana.metis.data.checker.service.executor.ValidationTaskFactory;
import eu.europeana.metis.data.checker.service.executor.ValidationUtils;
import eu.europeana.metis.data.checker.service.persistence.RecordDao;
import eu.europeana.validation.client.ValidationClient;
import eu.europeana.validation.model.ValidationResult;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Created by ymamakis on 9/6/16.
 */
public class DataCheckerServiceTest {

  private DataCheckerService service;
  private RecordDao mockDao;
  private DataCheckerServiceConfig mockConfig;
  private ValidationClient mockValidationClient;

  @BeforeEach
  public void prepare() {
    mockDao = Mockito.mock(RecordDao.class);
    mockConfig = Mockito.mock(DataCheckerServiceConfig.class);
    mockValidationClient = Mockito.mock(ValidationClient.class);
    final ValidationUtils validationUtils = new ValidationUtils(mockValidationClient, mockDao,
        "EDM-EXTERNAL", "EDM-INTERNAL", "test_url");
    ValidationTaskFactory taskFactory = new ValidationTaskFactory(validationUtils);
    Mockito.mock(ValidationTaskFactory.class);
    when(mockConfig.getDataCheckerUrl()).thenReturn("test/");
    when(mockConfig.getThreadCount()).thenReturn(10);
    service = new DataCheckerService(mockConfig, mockDao, taskFactory);
  }

  @Test
  public void test() throws DataCheckerServiceException, IOException {
    String record = IOUtils.toString(
        Thread.currentThread().getContextClassLoader().getResourceAsStream("Item_5791754.xml"),
        StandardCharsets.UTF_8);
    ValidationResult result = new ValidationResult();
    result.setSuccess(true);
    when(mockValidationClient.validateRecord("EDM-INTERNAL", record)).thenReturn(result);
    List<String> records = new ArrayList<>();
    records.add(record);

    final DatasetProperties properties = new DatasetProperties("12345", null, null, null);
    ExtendedValidationResult extendedValidationResult =
        service.createRecords(records, properties, false, false);

    assertEquals("test/12345_*", extendedValidationResult.getPortalUrl());
    assertEquals(0, extendedValidationResult.getResultList().size());
    assertTrue(extendedValidationResult.isSuccess());
  }
}
