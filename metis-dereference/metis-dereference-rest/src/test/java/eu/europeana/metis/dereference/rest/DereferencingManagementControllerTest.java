//package eu.europeana.metis.dereference.rest;
//
//import static org.hamcrest.core.Is.is;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.doAnswer;
//import static org.mockito.Mockito.doNothing;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import eu.europeana.metis.dereference.Vocabulary;
//import eu.europeana.metis.dereference.rest.exceptions.RestResponseExceptionHandler;
//import eu.europeana.metis.dereference.service.DereferencingManagementService;
//import java.net.URI;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import org.bson.types.ObjectId;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.stubbing.Answer;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//class DereferencingManagementControllerTest {
//
//  private DereferencingManagementService dereferencingManagementServiceMock;
//  private MockMvc dereferencingManagementControllerMock;
//  private String testEmptyCacheResult = "";
//
//  @BeforeEach
//  void setUp() {
//    dereferencingManagementServiceMock = mock(DereferencingManagementService.class);
//
//    DereferencingManagementController dereferencingManagementController = new DereferencingManagementController(
//        dereferencingManagementServiceMock, List.of("http://correctUrl"));
//
//    dereferencingManagementControllerMock = MockMvcBuilders
//        .standaloneSetup(dereferencingManagementController)
//        .setControllerAdvice(new RestResponseExceptionHandler())
//        .build();
//  }
//
//  @Test
//  void testGetAllVocabularies() throws Exception {
//    Vocabulary dummyVocab1 = new Vocabulary();
//    dummyVocab1.setId(new ObjectId());
//    dummyVocab1.setName("Dummy1");
//    dummyVocab1.setUris(Collections.singleton("http://dummy1.org/path1"));
//
//    Vocabulary dummyVocab2 = new Vocabulary();
//    dummyVocab2.setId(new ObjectId());
//    dummyVocab2.setName("Dummy2");
//    dummyVocab2.setUris(Collections.singleton("http://dummy2.org/path2"));
//
//    ArrayList<Vocabulary> dummyVocabList = new ArrayList<>();
//    dummyVocabList.add(dummyVocab1);
//    dummyVocabList.add(dummyVocab2);
//
//    when(dereferencingManagementServiceMock.getAllVocabularies()).thenReturn(dummyVocabList);
//
//    dereferencingManagementControllerMock.perform(get("/vocabularies"))
//                                         .andExpect(jsonPath("$[0].uris[0]", is("http://dummy1.org/path1")))
//                                         .andExpect(jsonPath("$[1].uris[0]", is("http://dummy2.org/path2")))
//                                         .andExpect(status().is(200));
//  }
//
//  @Test
//  void testLoadVocabularies_validPrefix_expectSuccess() throws Exception {
//    doNothing().when(dereferencingManagementServiceMock).loadVocabularies(any(URI.class));
//    dereferencingManagementControllerMock.perform(post("/load_vocabularies")
//        .param("directory_url", "http://correctUrl/test/call")).andExpect(status().is(200));
//  }
//
//  @Test
//  void testLoadVocabularies_invalidPrefix_expectFail() throws Exception {
//    dereferencingManagementControllerMock.perform(post("/load_vocabularies")
//        .param("directory_url", "http://wrongUrl")).andExpect(status().is(400));
//  }
//
//  @Test
//  void testEmptyCache() throws Exception {
//    doAnswer((Answer<Void>) invocationOnMock -> {
//      testEmptyCacheResult = "OK";
//      return null;
//    }).when(dereferencingManagementServiceMock).emptyCache();
//
//    dereferencingManagementControllerMock.perform(delete("/cache"))
//                                         .andExpect(status().is(200));
//
//    assertEquals("OK", testEmptyCacheResult);
//  }
//}