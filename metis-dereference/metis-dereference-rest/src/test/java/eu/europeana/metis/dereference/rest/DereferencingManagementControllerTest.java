package eu.europeana.metis.dereference.rest;

import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.rest.exceptions.RestResponseExceptionHandler;
import eu.europeana.metis.dereference.service.DereferencingManagementService;
import eu.europeana.metis.dereference.vocimport.exception.VocabularyImportException;
import eu.europeana.metis.utils.RestEndpoints;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Unit tests {@link DereferencingController} class
 */
class DereferencingManagementControllerTest {

  private DereferencingManagementService deRefManagementServiceMock;
  private MockMvc deRefManagementControllerMock;
  private String testEmptyCacheResult = "";

  @BeforeEach
  void setUp() {
    deRefManagementServiceMock = mock(DereferencingManagementService.class);

    DereferencingManagementController dereferencingManagementController = new DereferencingManagementController(
        deRefManagementServiceMock, Set.of("valid.domain.com"));

    deRefManagementControllerMock = MockMvcBuilders.standaloneSetup(dereferencingManagementController)
                                                   .setControllerAdvice(new RestResponseExceptionHandler()).build();
  }

  @Test
  void testGetAllVocabularies() throws Exception {
    Vocabulary dummyVocab1 = new Vocabulary();
    dummyVocab1.setId(new ObjectId());
    dummyVocab1.setName("Dummy1");
    dummyVocab1.setUris(Collections.singleton("https://dummy1.org/path1"));

    Vocabulary dummyVocab2 = new Vocabulary();
    dummyVocab2.setId(new ObjectId());
    dummyVocab2.setName("Dummy2");
    dummyVocab2.setUris(Collections.singleton("https://dummy2.org/path2"));

    ArrayList<Vocabulary> dummyVocabList = new ArrayList<>();
    dummyVocabList.add(dummyVocab1);
    dummyVocabList.add(dummyVocab2);

    when(deRefManagementServiceMock.getAllVocabularies()).thenReturn(dummyVocabList);

    deRefManagementControllerMock.perform(get("/vocabularies"))
                                 .andExpect(jsonPath("$[0].uris[0]", is("https://dummy1.org/path1")))
                                 .andExpect(jsonPath("$[1].uris[0]", is("https://dummy2.org/path2")))
                                 .andExpect(status().is(200));
  }

  @Test
  void testLoadVocabularies_validDomain_expectSuccess() throws Exception {
    doNothing().when(deRefManagementServiceMock).loadVocabularies(any(URL.class));
    deRefManagementControllerMock.perform(post("/load_vocabularies")
                                     .param("directory_url", "https://valid.domain.com/test/call"))
                                 .andExpect(status().is(200));
  }

  @Test
  void testLoadVocabularies_invalidDomain_expectFail() throws Exception {
    deRefManagementControllerMock.perform(post("/load_vocabularies")
                                     .param("directory_url", "https://invalid.domain.com"))
                                 .andExpect(status().is(400));
  }

  @Test
  void testEmptyCache() throws Exception {
    doAnswer((Answer<Void>) invocationOnMock -> {
      testEmptyCacheResult = "OK";
      return null;
    }).when(deRefManagementServiceMock).emptyCache();

    deRefManagementControllerMock.perform(delete(RestEndpoints.CACHE_EMPTY))
                                 .andExpect(status().is(200));

    assertEquals("OK", testEmptyCacheResult);
  }

  @Test
  void testEmptyCacheByEmptyXml() throws Exception {
    doAnswer((Answer<Void>) invocationOnMock -> {
      testEmptyCacheResult = "OK";
      return null;
    }).when(deRefManagementServiceMock).purgeByNullOrEmptyXml();

    deRefManagementControllerMock.perform(delete(RestEndpoints.CACHE_EMPTY_XML))
                                 .andExpect(status().is(200));

    assertEquals("OK", testEmptyCacheResult);
  }

  @Test
  void testEmptyNullOrEmptyXML() throws Exception {
    doAnswer((Answer<Void>) invocationOnMock -> {
      testEmptyCacheResult = "OK";
      return null;
    }).when(deRefManagementServiceMock).purgeByNullOrEmptyXml();

    deRefManagementControllerMock.perform(delete(RestEndpoints.CACHE_EMPTY_XML)).andExpect(status().is(200));

    assertEquals("OK", testEmptyCacheResult);
  }


  @Test
  void testEmptyCacheByResourceId() throws Exception {

    doAnswer((Answer<Void>) invocationOnMock -> {
      testEmptyCacheResult = "OK";
      return null;
    }).when(deRefManagementServiceMock).purgeByResourceId(any(String.class));

    deRefManagementControllerMock.perform(post("/cache/resource")
                                     .param("resourceId", "12345"))
                                 .andExpect(status().is(200));

    assertEquals("OK", testEmptyCacheResult);
  }

  @Test
  void testEmptyCacheByVocabularyId() throws Exception {

    doAnswer((Answer<Void>) invocationOnMock -> {
      testEmptyCacheResult = "OK";
      return null;
    }).when(deRefManagementServiceMock).purgeByVocabularyId(any(String.class));

    deRefManagementControllerMock.perform(post(RestEndpoints.CACHE_EMPTY_VOCABULARY)
                                     .param("vocabularyId", "12345"))
                                 .andExpect(status().is(200));

    assertEquals("OK", testEmptyCacheResult);
  }

  @Test
  void testLoadVocabularies_expectBadRequest() throws Exception {
    deRefManagementControllerMock.perform(post(RestEndpoints.LOAD_VOCABULARIES)
                                     .param("directory_url", "directory"))
                                 .andDo(print())
                                 .andExpect(status().is(400))
                                 .andExpect(content().string("The url of the directory to import is not valid."));
  }

  @Test
  void testLoadVocabularies_expectBadContent() throws Exception {
    doThrow(VocabularyImportException.class).when(deRefManagementServiceMock).loadVocabularies(any(URL.class));
    deRefManagementControllerMock.perform(post(RestEndpoints.LOAD_VOCABULARIES)
                                     .param("directory_url", "\\/tttp://test"))
                                 .andDo(print())
                                 .andExpect(status().is(400))
                                 .andExpect(content().string("Provided directoryUrl '\\/tttp://test', failed to parse."));
    verify(deRefManagementServiceMock, times(0)).loadVocabularies(any(URL.class));
  }

  @Test
  void testLoadVocabularies_expectBadVocabulary() throws Exception {
    doThrow(new VocabularyImportException("Cannot load vocabulary"))
        .when(deRefManagementServiceMock).loadVocabularies(any(URL.class));
    deRefManagementControllerMock.perform(post(RestEndpoints.LOAD_VOCABULARIES)
                                     .param("directory_url", "https://valid.domain.com/test/call"))
                                 .andDo(print())
                                 .andExpect(status().is(502))
                                 .andExpect(content().string("Cannot load vocabulary"));
    verify(deRefManagementServiceMock, times(1)).loadVocabularies(any(URL.class));
  }
}
