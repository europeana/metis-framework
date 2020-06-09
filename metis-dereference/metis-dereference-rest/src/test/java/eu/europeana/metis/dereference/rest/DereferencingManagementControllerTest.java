package eu.europeana.metis.dereference.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.rest.exceptions.RestResponseExceptionHandler;
import eu.europeana.metis.dereference.service.DereferencingManagementService;
import java.util.ArrayList;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DereferencingManagementControllerTest {

  private DereferencingManagementService dereferencingManagementServiceMock;
  private MockMvc dereferencingManagementControllerMock;
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
  void testGetAllVocabularies() throws Exception {
    Vocabulary dummyVocab1 = new Vocabulary();
    dummyVocab1.setId("Dummy1");
    dummyVocab1.setName("Dummy1");
    dummyVocab1.setUris(Collections.singleton("http://dummy1.org/path1"));

    Vocabulary dummyVocab2 = new Vocabulary();
    dummyVocab2.setId("Dummy2");
    dummyVocab2.setName("Dummy2");
    dummyVocab2.setUris(Collections.singleton("http://dummy2.org/path2"));

    ArrayList<Vocabulary> dummyVocabList = new ArrayList<>();
    dummyVocabList.add(dummyVocab1);
    dummyVocabList.add(dummyVocab2);

    when(dereferencingManagementServiceMock.getAllVocabularies()).thenReturn(dummyVocabList);

    dereferencingManagementControllerMock.perform(get("/vocabularies"))
        .andExpect(content().string(
            "[{\"id\":\"Dummy1\",\"uris\":[\"http://dummy1.org/path1\"],\"suffix\":null,\"xslt\":null,\"iterations\":0,\"name\":\"Dummy1\"},{\"id\":\"Dummy2\",\"uris\":[\"http://dummy2.org/path2\"],\"suffix\":null,\"xslt\":null,\"iterations\":0,\"name\":\"Dummy2\"}]"))
        .andExpect(status().is(200));
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
}