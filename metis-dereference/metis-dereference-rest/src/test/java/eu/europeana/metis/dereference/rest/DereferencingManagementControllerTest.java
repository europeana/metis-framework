package eu.europeana.metis.dereference.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.rest.exceptions.RestResponseExceptionHandler;
import eu.europeana.metis.dereference.service.DereferencingManagementService;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DereferencingManagementControllerTest {

  private static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
      MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

  private DereferencingManagementService dereferencingManagementServiceMock;
  private MockMvc dereferencingManagementControllerMock;
  private String testSaveVocabularyResult = "";
  private String testUpdateVocabularyResult = "";
  private String testUpdateEntityResult = "";
  private String testDeleteVocabularyResult = "";
  private String testDeleteEntityResult = "";
  private String testEmptyCacheResult = "";

  @BeforeEach
  void setUp() {
    dereferencingManagementServiceMock = mock(DereferencingManagementService.class);

    DereferencingManagementController dereferencingManagementController = new DereferencingManagementController(
        dereferencingManagementServiceMock);

    dereferencingManagementControllerMock = MockMvcBuilders
        .standaloneSetup(dereferencingManagementController)
        .setControllerAdvice(new RestResponseExceptionHandler())
        .build();
  }

  @Test
  void testSaveVocabulary() throws Exception {
    Vocabulary dummyVocab = new Vocabulary();
    dummyVocab.setId("Dummy");
    dummyVocab.setName("Dummy");

    doAnswer((Answer<Void>) invocationOnMock -> {
      testSaveVocabularyResult = "OK";
      return null;
    }).when(dereferencingManagementServiceMock).saveVocabulary(any(Vocabulary.class));

    dereferencingManagementControllerMock.perform(post("/vocabulary")
        .contentType(APPLICATION_JSON_UTF8)
        .content(convertObjectToJsonBytes(dummyVocab)))
        .andExpect(status().is(200));

    assertEquals("OK", testSaveVocabularyResult);
  }

  @Test
  void testUpdateVocabulary() throws Exception {
    Vocabulary dummyVocab = new Vocabulary();
    dummyVocab.setId("Dummy");
    dummyVocab.setName("Dummy");

    doAnswer((Answer<Void>) invocationOnMock -> {
      testUpdateVocabularyResult = "OK";
      return null;
    }).when(dereferencingManagementServiceMock).updateVocabulary(any(Vocabulary.class));

    dereferencingManagementControllerMock.perform(put("/vocabulary")
        .contentType(APPLICATION_JSON_UTF8)
        .content(convertObjectToJsonBytes(dummyVocab)))
        .andExpect(status().is(200));

    assertEquals("OK", testUpdateVocabularyResult);
  }

  @Test
  void testDeleteVocabulary() throws Exception {
    doAnswer((Answer<Void>) invocationOnMock -> {
      testDeleteVocabularyResult = "OK";
      return null;
    }).when(dereferencingManagementServiceMock).deleteVocabulary(any(String.class));

    dereferencingManagementControllerMock.perform(delete("/vocabulary/dummy"))
        .andExpect(status().is(200));

    assertEquals("OK", testDeleteVocabularyResult);
  }

  @Test
  void testGetVocabulary() throws Exception {
    Vocabulary dummyVocab = new Vocabulary();
    dummyVocab.setId("Dummy");
    dummyVocab.setName("Dummy");

    when(dereferencingManagementServiceMock.findByName("dummy")).thenReturn(dummyVocab);

    dereferencingManagementControllerMock.perform(get("/vocabulary/dummy"))
        .andExpect(status().is(200))
        .andExpect(content().string(
            "{\"id\":\"Dummy\",\"uri\":null,\"suffix\":null,\"typeRules\":null,\"rules\":null,\"xslt\":null,\"iterations\":0,\"name\":\"Dummy\",\"type\":null}"));
  }

  @Test
  void testGetAllVocabularies() throws Exception {
    Vocabulary dummyVocab1 = new Vocabulary();
    dummyVocab1.setId("Dummy1");
    dummyVocab1.setName("Dummy1");

    Vocabulary dummyVocab2 = new Vocabulary();
    dummyVocab2.setId("Dummy2");
    dummyVocab2.setName("Dummy2");

    ArrayList<Vocabulary> dummyVocabList = new ArrayList<>();
    dummyVocabList.add(dummyVocab1);
    dummyVocabList.add(dummyVocab2);

    when(dereferencingManagementServiceMock.getAllVocabularies()).thenReturn(dummyVocabList);

    dereferencingManagementControllerMock.perform(get("/vocabularies"))
        .andExpect(status().is(200))
        .andExpect(content().string(
            "[{\"id\":\"Dummy1\",\"uri\":null,\"suffix\":null,\"typeRules\":null,\"rules\":null,\"xslt\":null,\"iterations\":0,\"name\":\"Dummy1\",\"type\":null},{\"id\":\"Dummy2\",\"uri\":null,\"suffix\":null,\"typeRules\":null,\"rules\":null,\"xslt\":null,\"iterations\":0,\"name\":\"Dummy2\",\"type\":null}]"));
  }

  @Test
  void testDeleteEntity() throws Exception {
    doAnswer((Answer<Void>) invocationOnMock -> {
      testDeleteEntityResult = "OK";
      return null;
    }).when(dereferencingManagementServiceMock).removeEntity(any(String.class));

    dereferencingManagementControllerMock.perform(delete("/entity/dummy"))
        .andExpect(status().is(200));

    assertEquals("OK", testDeleteEntityResult);
  }

  @Test
  void testUpdateEntity() throws Exception {
    doAnswer((Answer<Void>) invocationOnMock -> {
      testUpdateEntityResult = "OK";
      return null;
    }).when(dereferencingManagementServiceMock).updateEntity(any(String.class), any(String.class));

    dereferencingManagementControllerMock.perform(put("/entity")
        .contentType(APPLICATION_JSON_UTF8)
        .param("uri", "dummy1")
        .param("xml", "dummy2"))
        .andExpect(status().is(200));

    assertEquals("OK", testUpdateEntityResult);
  }

  @Test
  void testEmptyCache() throws Exception {
    doAnswer((Answer<Void>) invocationOnMock -> {
      testEmptyCacheResult = "OK";
      return null;
    }).when(dereferencingManagementServiceMock).emptyCache();

    dereferencingManagementControllerMock.perform(delete("/cache"))
        .andExpect(status().is(200));

    assertEquals("OK", testEmptyCacheResult);
  }

  private static byte[] convertObjectToJsonBytes(Object object) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    return mapper.writeValueAsBytes(object);
  }
}