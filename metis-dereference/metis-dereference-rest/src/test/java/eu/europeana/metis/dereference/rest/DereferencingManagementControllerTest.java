package eu.europeana.metis.dereference.rest;

import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.rest.exceptions.RestResponseExceptionHandler;
import eu.europeana.metis.dereference.service.DereferencingManagementService;
import eu.europeana.metis.utils.TestUtil;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DereferencingManagementControllerTest {
	  private DereferencingManagementService dereferencingManagementServiceMock;
	  private MockMvc dereferencingManagementControllerMock;

	  @Before
	  public void setUp() throws Exception {
	    dereferencingManagementServiceMock = mock(DereferencingManagementService.class);
	    
	    DereferencingManagementController dereferencingManagementController = new DereferencingManagementController(dereferencingManagementServiceMock);
	    
	    dereferencingManagementControllerMock = MockMvcBuilders.standaloneSetup(dereferencingManagementController)
	        .setControllerAdvice(new RestResponseExceptionHandler())
	        .build();
	  }
	  	    
	  String testSaveVocabularyResult = "";	  
	  @Test
	  public void testSaveVocabulary() throws Exception {
		  Vocabulary dummyVocab = new Vocabulary();
		  dummyVocab.setId("Dummy");
		  dummyVocab.setName("Dummy");
		  
		  doAnswer(new Answer<Void>() {
		        @Override
		        public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
		        	testSaveVocabularyResult = "OK";
		        	return null;
		        }
		    }).when(dereferencingManagementServiceMock).saveVocabulary(any(Vocabulary.class));
		  
		   dereferencingManagementControllerMock.perform(post("/vocabulary")
				   .contentType(TestUtil.APPLICATION_JSON_UTF8)
				   .content(TestUtil.convertObjectToJsonBytes(dummyVocab)))
		   .andExpect(status().is(200));
		  
		  Assert.assertEquals("OK", testSaveVocabularyResult);
	  }
	 
	  String testUpdateVocabularyResult = "";	  
	  @Test
	  public void testUpdateVocabulary() throws Exception {
		  Vocabulary dummyVocab = new Vocabulary();
		  dummyVocab.setId("Dummy");
		  dummyVocab.setName("Dummy");
		  
		  doAnswer(new Answer<Void>() {
		        @Override
		        public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
		        	testUpdateVocabularyResult = "OK";
		        	return null;
		        }
		    }).when(dereferencingManagementServiceMock).updateVocabulary(any(Vocabulary.class));
		  
		   dereferencingManagementControllerMock.perform(put("/vocabulary")
				   .contentType(TestUtil.APPLICATION_JSON_UTF8)
				   .content(TestUtil.convertObjectToJsonBytes(dummyVocab)))
		   .andExpect(status().is(200));
		  
		  Assert.assertEquals("OK", testUpdateVocabularyResult);
	  }
	  
	  String testDeleteVocabularyResult = "";	  
	  @Test
	  public void testDeleteVocabulary() throws Exception {
		  doAnswer(new Answer<Void>() {
		        @Override
		        public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
		        	testDeleteVocabularyResult = "OK";
		        	return null;
		        }
		    }).when(dereferencingManagementServiceMock).deleteVocabulary(any(String.class));
		  
		   dereferencingManagementControllerMock.perform(delete("/vocabulary/dummy"))
		   			.andExpect(status().is(200));
		  
		  Assert.assertEquals("OK", testDeleteVocabularyResult);
	  }

	  @Test
	  public void testGetVocabulary() throws Exception {
		Vocabulary dummyVocab = new Vocabulary();
		dummyVocab.setId("Dummy");
		dummyVocab.setName("Dummy");
		  
	    when(dereferencingManagementServiceMock.findByName("dummy")).thenReturn(dummyVocab);

	    dereferencingManagementControllerMock.perform(get("/vocabulary/dummy"))
	    		.andExpect(status().is(200))
	        	.andExpect(content().string("{\"id\":\"Dummy\",\"typeRules\":null,\"rules\":null,\"xslt\":null,\"iterations\":0,\"name\":\"Dummy\",\"type\":null,\"uri\":null}"));
	  }	  
	  
	  @Test
	  public void testGetAllVocabularies() throws Exception {
		Vocabulary dummyVocab1 = new Vocabulary();
		dummyVocab1.setId("Dummy1");
		dummyVocab1.setName("Dummy1");
		
		Vocabulary dummyVocab2 = new Vocabulary();
		dummyVocab2.setId("Dummy2");
		dummyVocab2.setName("Dummy2");
		
		ArrayList<Vocabulary> dummyVocabList = new ArrayList<Vocabulary>();
		dummyVocabList.add(dummyVocab1);
		dummyVocabList.add(dummyVocab2);

	    when(dereferencingManagementServiceMock.getAllVocabularies()).thenReturn(dummyVocabList);

	    dereferencingManagementControllerMock.perform(get("/vocabularies"))	
	        	.andExpect(status().is(200))	        	
	        	.andExpect(content().string("[{\"id\":\"Dummy1\",\"typeRules\":null,\"rules\":null,\"xslt\":null,\"iterations\":0,\"name\":\"Dummy1\",\"type\":null,\"uri\":null},{\"id\":\"Dummy2\",\"typeRules\":null,\"rules\":null,\"xslt\":null,\"iterations\":0,\"name\":\"Dummy2\",\"type\":null,\"uri\":null}]"));
	  }
	  
	  String testDeleteEntityResult = "";	  
	  @Test
	  public void testDeleteEntity() throws Exception {
		  doAnswer(new Answer<Void>() {
		        @Override
		        public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
		        	testDeleteEntityResult = "OK";
		        	return null;
		        }
		    }).when(dereferencingManagementServiceMock).removeEntity(any(String.class));
		  
		   dereferencingManagementControllerMock.perform(delete("/entity/dummy"))
		   			.andExpect(status().is(200));
		  
		  Assert.assertEquals("OK", testDeleteEntityResult);
	  }
	  
	  String testUpdateEntityResult = "";	  
	  @Test
	  public void testUpdateEntity() throws Exception {
		  doAnswer(new Answer<Void>() {
		        @Override
		        public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
		        	testUpdateEntityResult = "OK";
		        	return null;
		        }
		    }).when(dereferencingManagementServiceMock).updateEntity(any(String.class), any(String.class));
		  
		   dereferencingManagementControllerMock.perform(put("/entity")
				   .contentType(TestUtil.APPLICATION_JSON_UTF8)
				   .param("uri", "dummy1")
				   .param("xml", "dummy2"))
		   .andExpect(status().is(200));
		  
		  Assert.assertEquals("OK", testUpdateEntityResult);
	  }
	  
	  String testEmptyCacheResult = "";	  
	  @Test
	  public void testEmptyCache() throws Exception {
		  doAnswer(new Answer<Void>() {
		        @Override
		        public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
		        	testEmptyCacheResult = "OK";
		        	return null;
		        }
		    }).when(dereferencingManagementServiceMock).emptyCache();
		  
		   dereferencingManagementControllerMock.perform(delete("/cache"))
		   			.andExpect(status().is(200));
		  
		  Assert.assertEquals("OK", testEmptyCacheResult);
	  }
}