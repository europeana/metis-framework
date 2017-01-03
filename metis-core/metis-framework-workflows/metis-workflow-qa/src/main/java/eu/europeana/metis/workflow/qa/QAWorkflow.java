package eu.europeana.metis.workflow.qa;

import eu.europeana.metis.framework.dao.ExecutionDao;
import eu.europeana.metis.framework.workflow.AbstractMetisWorkflow;
import eu.europeana.metis.framework.workflow.CloudStatistics;
import eu.europeana.metis.framework.workflow.Execution;
import eu.europeana.metis.framework.workflow.WorkflowParameters;
import eu.europeana.metis.workflow.qa.model.MeasuringResponse;
import eu.europeana.metis.workflow.qa.model.QAParams;
import eu.europeana.metis.workflow.qa.model.QAStatistics;
import eu.europeana.metis.workflow.qa.persistence.QAStatisticsDao;
import org.apache.commons.lang.StringUtils;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * Created by ymamakis on 11/22/16.
 */
public class QAWorkflow implements AbstractMetisWorkflow {
    private String name = "qa";
    private Map<String, List<String>> params;
    private RestTemplate template = new RestTemplate();
    private static Map<String,QAStatistics> statistics = new ConcurrentHashMap<>();
    private static List<String> activeStatistics = new ArrayList<>();

    @Autowired
    private ExecutionDao dao;

    @Autowired
    private QAStatisticsDao qaDao;

    @PostConstruct
    public void loadActive(){
        Query<Execution> q = dao.createQuery();
        q.filter("workflow","qa");
        q.filter("active",true);
        List<Execution> active = dao.find(q).asList();
        for(Execution execution:active){
            QAStatistics stats = qaDao.findOne("executionId",execution.getId());
            statistics.put(execution.getDatasetId(),stats);
            activeStatistics.add(execution.getDatasetId());
        }
    }

    public QAWorkflow(){
        List<String> endpoint = new ArrayList<>();
        endpoint.add("http://144.76.218.178:8080/europeana-qa/batch");
        params = new HashMap<>();
        params.put(QAParams.QA_ENDPOINT,endpoint);
    }
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setParameters(Map<String, List<String>> parameters) {
        this.params = parameters;
        List<String> endpoint = new ArrayList<>();
        endpoint.add("http://144.76.218.178:8080/europeana-qa/batch");
        params.put(QAParams.QA_ENDPOINT,endpoint);
    }

    @Override
    public Map<String, List<String>> getParameters() {
        return params;
    }

    @Override
    public void execute() {
        QAStatistics qaStatistics = new QAStatistics();
        MeasuringResponse response = template.getForEntity(URI.create(params.get(QAParams.QA_ENDPOINT).get(0))
                + "/measuring/start", MeasuringResponse.class).getBody();
        String sessionId =response.getSessionId();
        qaStatistics.setStatus(response.getStatus());
        List<String> records = params.get(QAParams.QA_RECORDS);
        qaStatistics.setCreated(new Long(records.size()));
        qaStatistics.setFailedRecords(new CopyOnWriteArrayList<String>());
        qaStatistics.setFailed(0L);
        qaStatistics.setProcessed(0L);
        qaStatistics.setDeleted(0L);
        qaStatistics.setFailedRecords(Collections.<String>emptyList());
        qaStatistics.setStatus("");
        qaStatistics.setUpdated(0L);
        qaStatistics.setExecutionId(params.get(WorkflowParameters.EXECUTION).get(0));
        statistics.put(params.get(WorkflowParameters.DATASET).get(0),qaStatistics);
        for(String record:records){
            ResponseEntity<MeasuringResponse> entity = template.getForEntity(URI.create(params.get(QAParams.QA_ENDPOINT).get(0))
                    +record+"?sessionId="+sessionId, MeasuringResponse.class);

            qaStatistics = statistics.get(params.get(WorkflowParameters.DATASET).get(0));
            if(entity.getStatusCode()== HttpStatus.OK && StringUtils.equals(entity.getBody().getResult(),"success")){
                qaStatistics.setProcessed(qaStatistics.getProcessed()+1);
            } else {
                qaStatistics.setFailed(qaStatistics.getFailed()+1);
                List<String> failed = qaStatistics.getFailedRecords();
                failed.add(record);
                qaStatistics.setFailedRecords(failed);
            }
            statistics.put(params.get(WorkflowParameters.DATASET).get(0),qaStatistics);
        }

        template.getForEntity(URI.create(params.get(QAParams.QA_ENDPOINT).get(0))
                + "/measuring/"+qaStatistics+"/stop", MeasuringResponse.class);
        template.getForEntity(URI.create(params.get(QAParams.QA_ENDPOINT).get(0))
                + "/analyzing/"+qaStatistics+"/start", MeasuringResponse.class);
        qaStatistics = statistics.get(params.get(WorkflowParameters.DATASET).get(0));
        qaStatistics.setStatus("analyzing");
        System.out.println(qaStatistics);
        statistics.put(params.get(WorkflowParameters.DATASET).get(0),qaStatistics);
        System.out.println(statistics);
        activeStatistics.add(params.get(WorkflowParameters.DATASET).get(0));
        System.out.println(activeStatistics);
        qaDao.save(qaStatistics);
    }

    @Override
    public CloudStatistics monitor(String datasetId) {

        if(activeStatistics.contains(datasetId)){
            Logger.getLogger(this.getName()).info("Updating: "+ datasetId);
            QAStatistics qaStatistics = statistics.get(datasetId);
           MeasuringResponse response =  template.getForEntity(URI.create(params.get(QAParams.QA_ENDPOINT).get(0))
                    + "/analyzing/"+qaStatistics.getSessionId()+"/status", MeasuringResponse.class).getBody();
            Logger.getLogger(this.getName()).info("Updating: " +URI.create(params.get(QAParams.QA_ENDPOINT).get(0))
                    + "/analyzing/"+qaStatistics.getSessionId()+"/status");
            if(StringUtils.equals(response.getResult(),"ready")){
                activeStatistics.remove(datasetId);
                UpdateOperations<QAStatistics> ops = qaDao.createUpdateOperations();
                ops.set("status","finished");
                Query<QAStatistics> q = qaDao.createQuery();
                q.filter("executionId",qaStatistics.getExecutionId());
                qaDao.update(q,ops);
                qaStatistics.setStatus("finished");
                statistics.put(datasetId,qaStatistics);
                return qaStatistics;
            }
        }

        return statistics.get(datasetId);
    }

    @Override
    public boolean supports(String s) {
        return s.equalsIgnoreCase(name);
    }
}
