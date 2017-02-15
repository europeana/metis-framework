package eu.europeana.metis.preview.service.test;

import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.exceptions.MongoRuntimeException;
import eu.europeana.metis.identifier.RestClient;
import eu.europeana.metis.preview.persistence.RecordDao;
import eu.europeana.metis.preview.service.ExtendedValidationResult;
import eu.europeana.metis.preview.service.PreviewService;
import eu.europeana.validation.client.ValidationClient;
import eu.europeana.validation.model.ValidationResult;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.jibx.runtime.JiBXException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by ymamakis on 9/6/16.
 */
public class PreviewServiceTest {

    private PreviewService service;
    private RecordDao mockDao;
    private ValidationClient mockValidationClient;
    private RestClient mockIdentifierClient;

    @Before
    public void prepare() throws JiBXException {
        mockDao = Mockito.mock(RecordDao.class);
        mockValidationClient = Mockito.mock(ValidationClient.class);
        mockIdentifierClient = Mockito.mock(RestClient.class);
        service = new PreviewService("test/");
        ReflectionTestUtils.setField(service, "dao", mockDao, RecordDao.class);
        ReflectionTestUtils.setField(service, "identifierClient", mockIdentifierClient, RestClient.class);
        ReflectionTestUtils.setField(service, "validationClient", mockValidationClient, ValidationClient.class);
    }

    @Test
    public void test() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, SolrServerException, JiBXException, ParserConfigurationException, InstantiationException, TransformerException {
        try {
            String record = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("Item_5791754.xml"));
            ValidationResult result = new ValidationResult();
            result.setSuccess(true);
            Mockito.when(mockValidationClient.validateRecord("EDM-INTERNAL", record, null)).thenReturn(result);
            Mockito.when(mockIdentifierClient.generateIdentifier("12345", "test")).thenReturn("/12345/test");
            Mockito.doAnswer(new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                    return null;
                }
            }).when(mockDao).createRecord(Mockito.anyObject());
            List<String> records = new ArrayList<>();
            records.add(record);
            ExtendedValidationResult extendedValidationResult = service.createRecords(records, "12345", false,"test");
            Assert.assertEquals("test/12345*", extendedValidationResult.getPortalUrl());
            Assert.assertEquals(0,extendedValidationResult.getResultList().size());
            Assert.assertEquals(true,extendedValidationResult.isSuccess());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (MongoDBException e) {
            e.printStackTrace();
        } catch (MongoRuntimeException e) {
            e.printStackTrace();
        }

    }
}
