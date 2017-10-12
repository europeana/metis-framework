package eu.europeana.metis.workflow.qa;

import eu.europeana.metis.core.workflow.CloudStatistics;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ExecutionRecordsStatistics;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.workflow.qa.model.QAParams;
import eu.europeana.metis.workflow.qa.model.QAStatistics;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import org.springframework.web.client.RestTemplate;

/**
 * Created by ymamakis on 11/22/16.
 */
public class QAPlugin implements AbstractMetisPlugin {
    private PluginStatus pluginStatus;
    private PluginType pluginType = PluginType.QA;
    private Map<String, List<String>> params;
    private RestTemplate template = new RestTemplate();
    private static Map<String,QAStatistics> statistics = new ConcurrentHashMap<>();
    private static List<String> activeStatistics = new ArrayList<>();

    public QAPlugin(){
        List<String> endpoint = new ArrayList<>();
        endpoint.add("http://144.76.218.178:8080/europeana-qa/batch");
        params = new HashMap<>();
        params.put(QAParams.QA_ENDPOINT,endpoint);

    }

    @PostConstruct
    public void initCache(){
//        if (cache.getAll("qa-datasets")!=null) {
//            activeStatistics.addAll(cache.getAll("qa-datasets"));
//        }
    }

    @Override
    public PluginType getPluginType() {
        return pluginType;
    }

    @Override
    public AbstractMetisPluginMetadata getPluginMetadata() {
        return null;
    }

    @Override
    public void setPluginMetadata(AbstractMetisPluginMetadata abstractMetisPluginMetadata) {

    }

    @Override
    public Date getStartedDate() {
        return null;
    }

    @Override
    public void setStartedDate(Date startedDate) {

    }

    @Override
    public Date getFinishedDate() {
        return null;
    }

    @Override
    public void setFinishedDate(Date finishedDate) {

    }

    @Override
    public Date getUpdatedDate() {
        return null;
    }

    @Override
    public void setUpdatedDate(Date updatedDate) {

    }

    @Override
    public PluginStatus getPluginStatus() {
        return pluginStatus;
    }

    @Override
    public void setPluginStatus(PluginStatus pluginStatus) {
        this.pluginStatus = pluginStatus;
    }

    @Override
    public ExecutionRecordsStatistics getExecutionRecordsStatistics() {
        return null;
    }

    @Override
    public void setExecutionRecordsStatistics(ExecutionRecordsStatistics executionRecordsStatistics) {

    }

    @Override
    public void execute() {
//        QAStatistics qaStatistics = new QAStatistics();
//        MeasuringResponse response = template.getForEntity(URI.create(params.get(QAParams.QA_ENDPOINT).get(0))
//                + "/measuring/start", MeasuringResponse.class).getBody();
//        String sessionId =response.getSessionId();
//        qaStatistics.setSessionId(sessionId);
//        qaStatistics.setStatus(response.getStatus());
//        List<String> records = params.get(QAParams.QA_RECORDS);
//        qaStatistics.setCreated(new Long(records.size()));
//        qaStatistics.setFailedRecords(new CopyOnWriteArrayList<String>());
//        qaStatistics.setFailed(0L);
//        qaStatistics.setProcessed(0L);
//        statistics.put(params.get(WorkflowParameters.DATASET).get(0),qaStatistics);
//        for(String record:records){
//            ResponseEntity<MeasuringResponse> entity = template.getForEntity(URI.create(params.get(QAParams.QA_ENDPOINT).get(0))
//                    +record+"?sessionId="+sessionId, MeasuringResponse.class);
//
//            qaStatistics = statistics.get(params.get(WorkflowParameters.DATASET).get(0));
//            if(entity.getStatusCode()== HttpStatus.OK && StringUtils.equals(entity.getBody().getResult(),"success")){
//                qaStatistics.setProcessed(qaStatistics.getProcessed()+1);
//            } else {
//                qaStatistics.setFailed(qaStatistics.getFailed()+1);
//                List<String> failed = qaStatistics.getFailedRecords();
//                failed.add(record);
//                qaStatistics.setFailedRecords(failed);
//            }
//            statistics.put(params.get(WorkflowParameters.DATASET).get(0),qaStatistics);
//        }
//        template.getForEntity(URI.create(params.get(QAParams.QA_ENDPOINT).get(0))
//                + "/measuring/"+qaStatistics.getSessionId()+"/stop", MeasuringResponse.class);
//        template.getForEntity(URI.create(params.get(QAParams.QA_ENDPOINT).get(0))
//                + "/analyzing/"+qaStatistics.getSessionId()+"/start", MeasuringResponse.class);
//        qaStatistics = statistics.get(params.get(WorkflowParameters.DATASET).get(0));
//        qaStatistics.setStatus("analyzing");
//        statistics.put(WorkflowParameters.DATASET,qaStatistics);
//        activeStatistics.add(params.get(WorkflowParameters.DATASET).get(0));
//        cache.set("qa-datasets", params.get(WorkflowParameters.DATASET).get(0),params.get(WorkflowParameters.DATASET).get(0));
    }

    @Override
    public CloudStatistics monitor(String datasetId) {
//        if(activeStatistics.contains(datasetId)){
//            QAStatistics qaStatistics = statistics.get(datasetId);
//           MeasuringResponse response =  template.getForEntity(URI.create(params.get(QAParams.QA_ENDPOINT).get(0))
//                    + "/analyzing/"+qaStatistics.getSessionId()+"/status", MeasuringResponse.class).getBody();
//            if(StringUtils.equals(response.getResult(),"ready")){
//                activeStatistics.remove(datasetId);
//                cache.remove("qa-datasets",datasetId);
//                qaStatistics.setStatus("finished");
//                statistics.put(datasetId,qaStatistics);
//                return qaStatistics;
//            }
//        }
//
//        return statistics.get(datasetId);
        return null;
    }
}
