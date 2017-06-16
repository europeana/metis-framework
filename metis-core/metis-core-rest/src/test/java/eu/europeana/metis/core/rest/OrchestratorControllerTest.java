package eu.europeana.metis.core.rest;

import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.core.exceptions.UserWorkflowAlreadyExistsException;
import eu.europeana.metis.core.service.OrchestratorService;
import eu.europeana.metis.core.workflow.UserWorkflow;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class OrchestratorControllerTest {

    private MockMvc orchestratorControllerMock;
    private OrchestratorService orchestratorServiceMock;

    @Before
    public void setUp() throws Exception {

        orchestratorServiceMock = mock(OrchestratorService.class);
        OrchestratorController orchestratorController = new OrchestratorController(orchestratorServiceMock);
        orchestratorControllerMock = MockMvcBuilders.standaloneSetup(orchestratorController).build();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void create_new_UserWorkflow_returns_201() throws Exception {

        UserWorkflow flow = new UserWorkflow();
        orchestratorControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_USERWORKFLOWS)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(flow)))
                .andExpect(status().is(201))
                .andExpect(content().string(""));

        verify(orchestratorServiceMock, times(1)).createUserWorkflow(any(UserWorkflow.class));
        verifyNoMoreInteractions(orchestratorServiceMock);
    }

    @Test
    public void create_new_UserWorkflow_returns_409() throws Exception {

        UserWorkflow flow = new UserWorkflow();
        doThrow(new UserWorkflowAlreadyExistsException("Exception")).when(orchestratorServiceMock).createUserWorkflow(any(UserWorkflow.class));

        orchestratorControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_USERWORKFLOWS)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(flow)))
                .andExpect(status().is(409))
                .andExpect(content().string(""));

        verify(orchestratorServiceMock, times(1)).createUserWorkflow(any(UserWorkflow.class));
        verifyNoMoreInteractions(orchestratorServiceMock);
    }


    @Test
    public void updateUserWorkflow() throws Exception {

    }

    @Test
    public void deleteUserWorkflow() throws Exception {

    }

    @Test
    public void getUserWorkflow() throws Exception {

    }

    @Test
    public void getAllUserWorkflows() throws Exception {

    }

    @Test
    public void addUserWorkflowInQueueOfUserWorkflowExecutions() throws Exception {

    }

    @Test
    public void cancelUserWorkflowExecution() throws Exception {

    }

    @Test
    public void getRunningUserWorkflowExecution() throws Exception {

    }

    @Test
    public void addUserWorkflowInQueueOfUserWorkflowExecutions1() throws Exception {

    }

    @Test
    public void getAllUserWorkflowExecutions() throws Exception {

    }

    @Test
    public void getAllUserWorkflowExecutions1() throws Exception {

    }

}