package eu.europeana.metis.core.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.europeana.metis.core.exceptions.UserWorkflowAlreadyExistsException;
import eu.europeana.metis.core.rest.exception.RestResponseExceptionHandler;
import eu.europeana.metis.core.service.OrchestratorService;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.UserWorkflow;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-06
 */
public class TestOrchestratorController {
  private static OrchestratorService orchestratorService;
  private static MockMvc orchestratorControllerMock;

  @BeforeClass
  public static void setUp() throws Exception {
    orchestratorService = mock(OrchestratorService.class);
    OrchestratorController orchestratorController = new OrchestratorController(orchestratorService);
    orchestratorControllerMock = MockMvcBuilders
        .standaloneSetup(orchestratorController)
        .setControllerAdvice(new RestResponseExceptionHandler())
        .build();
  }

  @Test
  public void createUserWorkflow() throws Exception
  {
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    orchestratorControllerMock.perform(post("/orchestrator/user_workflows")
        .contentType(TestUtil.APPLICATION_JSON_UTF8)
        .content(TestUtil.convertObjectToJsonBytes(userWorkflow)))
        .andExpect(status().is(201))
        .andExpect(content().string(""));

    verify(orchestratorService, times(1)).createUserWorkflow(any(UserWorkflow.class));
  }

  @Test
  public void createUserWorkflow_UserWorkflowAlreadyExistsException() throws Exception
  {
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    doThrow(UserWorkflowAlreadyExistsException.class).when(orchestratorService).createUserWorkflow(any(UserWorkflow.class));
    orchestratorControllerMock.perform(post("/orchestrator/user_workflows")
        .contentType(TestUtil.APPLICATION_JSON_UTF8)
        .content(TestUtil.convertObjectToJsonBytes(userWorkflow)))
        .andExpect(status().is(409))
        .andExpect(content().string("{\"errorMessage\":null}"));

    verify(orchestratorService, times(1)).createUserWorkflow(any(UserWorkflow.class));
  }


}
