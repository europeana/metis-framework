package eu.europeana.metis.preview.service.test;

import static org.mockito.Mockito.when;

import eu.europeana.metis.preview.persistence.RecordDao;
import eu.europeana.metis.preview.service.PreviewService;
import eu.europeana.metis.preview.service.PreviewServiceConfig;
import eu.europeana.metis.preview.service.executor.ValidationTaskFactory;
import java.lang.reflect.InvocationTargetException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.solr.client.solrj.SolrServerException;
import org.jibx.runtime.JiBXException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

//import eu.europeana.metis.preview.service.ExtendedValidationResult;

/**
 * Created by ymamakis on 9/6/16.
 */
public class PreviewServiceTest {

    private PreviewService service;
    private RecordDao mockDao;
    private PreviewServiceConfig mockConfig;
    private ValidationTaskFactory mockValidationTaskFactory;

    @Before
    public void prepare() throws JiBXException {
        mockDao = Mockito.mock(RecordDao.class);
        mockConfig = Mockito.mock(PreviewServiceConfig.class);
        mockValidationTaskFactory = Mockito.mock(ValidationTaskFactory.class);
        when(mockConfig.getPreviewUrl()).thenReturn("test/");
        when(mockConfig.getThreadCount()).thenReturn(10);
        service = new PreviewService(mockConfig, mockDao, mockValidationTaskFactory);
    }

    @Test
    public void test() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, SolrServerException, JiBXException, ParserConfigurationException, InstantiationException, TransformerException {
//        try {
//            String record = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("Item_5791754.xml"));
//            ValidationResult result = new ValidationResult();
//            result.setSuccess(true);
//            when(mockValidationClient.validateRecord("EDM-INTERNAL", record, null)).thenReturn(result);
//            when(mockIdentifierClient.generateIdentifier("12345", "test")).thenReturn("/12345/test");
//            Mockito.doAnswer(new Answer<Void>() {
//                @Override
//                public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
//                    return null;
//                }
//            }).when(mockDao).createRecord(Mockito.anyObject());
//            List<String> records = new ArrayList<>();
//            records.add(record);
//            ExtendedValidationResult extendedValidationResult = service.createRecords(records, "12345", false,"test",false);
//            Assert.assertEquals("test/12345*", extendedValidationResult.getPortalUrl());
//            Assert.assertEquals(0,extendedValidationResult.getResultList().size());
//            Assert.assertEquals(true,extendedValidationResult.isSuccess());
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        } catch (MongoDBException e) {
//            e.printStackTrace();
//        } catch (MongoRuntimeException e) {
//            e.printStackTrace();
//        }

    }
}
