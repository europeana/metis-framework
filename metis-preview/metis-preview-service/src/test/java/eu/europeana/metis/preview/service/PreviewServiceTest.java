package eu.europeana.metis.preview.service;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.exceptions.MongoRuntimeException;
import eu.europeana.metis.identifier.RestClient;
import eu.europeana.metis.preview.common.exception.PreviewServiceException;
import eu.europeana.metis.preview.common.model.ExtendedValidationResult;
import eu.europeana.metis.preview.persistence.RecordDao;
import eu.europeana.metis.preview.service.PreviewService;
import eu.europeana.metis.preview.service.PreviewServiceConfig;
import eu.europeana.metis.preview.service.executor.ValidationTaskFactory;
import eu.europeana.metis.preview.service.executor.ValidationUtils;
import eu.europeana.validation.client.ValidationClient;
import eu.europeana.validation.model.ValidationResult;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Created by ymamakis on 9/6/16.
 */
public class PreviewServiceTest {

    private PreviewService service;
    private RecordDao mockDao;
    private PreviewServiceConfig mockConfig;
    private ValidationClient mockValidationClient;
    private RestClient mockIdentifierClient;

    @Before
    public void prepare() {
        mockDao = Mockito.mock(RecordDao.class);
        mockConfig = Mockito.mock(PreviewServiceConfig.class);
        mockValidationClient = Mockito.mock(ValidationClient.class);
        mockIdentifierClient = Mockito.mock(RestClient.class);
        final ValidationUtils validationUtils = new ValidationUtils(mockIdentifierClient, mockValidationClient, mockDao, 
            "EDM-EXTERNAL", "EDM-INTERNAL", "EDM_external2internal_v2.xsl");

        ValidationTaskFactory taskFactory = new ValidationTaskFactory(validationUtils);
            Mockito.mock(ValidationTaskFactory.class);
        when(mockConfig.getPreviewUrl()).thenReturn("test/");
        when(mockConfig.getThreadCount()).thenReturn(10);
        service = new PreviewService(mockConfig, mockDao, taskFactory);
    }

    @Test
    public void test()
        throws NoSuchMethodException, MongoRuntimeException, MongoDBException, IllegalAccessException,
        IOException, InvocationTargetException, SolrServerException, PreviewServiceException {
            String record = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("Item_5791754.xml"));
            ValidationResult result = new ValidationResult();
            result.setSuccess(true);
            when(mockValidationClient.validateRecord("EDM-INTERNAL", record)).thenReturn(result);
            when(mockIdentifierClient.generateIdentifier(any(String.class), any(String.class))).thenReturn("/12345/test");
            Mockito.doAnswer(invocationOnMock -> null).when(mockDao).createRecord(Mockito.anyObject());
            List<String> records = new ArrayList<>();
            records.add(record);

            ExtendedValidationResult extendedValidationResult = service.createRecords(records, "12345", false,"test",false);

            Assert.assertEquals("test/12345*", extendedValidationResult.getPortalUrl());
            Assert.assertEquals(0,extendedValidationResult.getResultList().size());
            Assert.assertTrue(extendedValidationResult.isSuccess());
    }
}
