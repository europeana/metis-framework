package eu.europeana.metis.framework.test;

import eu.europeana.cloud.mcs.driver.DataSetServiceClient;
import eu.europeana.metis.framework.dao.DatasetDao;
import eu.europeana.metis.framework.dao.ExecutionDao;
import eu.europeana.metis.framework.dao.FailedRecordsDao;
import eu.europeana.metis.framework.dao.OrganizationDao;
import eu.europeana.metis.framework.dao.ecloud.EcloudDatasetDao;
import eu.europeana.metis.framework.service.DatasetService;
import eu.europeana.metis.framework.service.Orchestrator;
import eu.europeana.metis.framework.workflow.AbstractMetisWorkflow;
import eu.europeana.metis.framework.workflow.Execution;
import eu.europeana.metis.framework.workflow.FailedRecords;
import eu.europeana.metis.framework.workflow.VoidMetisWorkflow;
import eu.europeana.metis.mongo.MongoProvider;
import java.net.UnknownHostException;
import org.mockito.Mockito;
import org.mongodb.morphia.Morphia;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.plugin.core.config.EnablePluginRegistries;

/**
 * Created by ymamakis on 11/17/16.
 */
@Configuration
@EnablePluginRegistries(AbstractMetisWorkflow.class)
public class TestAppConfig {
    @Value("${ecloud.baseMcsUrl}")
    private String ecloudBaseMcsUrl;
    @Value("${ecloud.username}")
    private String ecloudUsername;
    @Value("${ecloud.password}")
    private String ecloudPassword;

    eu.europeana.metis.framework.mongo.MongoProvider provider;
    public TestAppConfig(){
        MongoProvider.start(10000);
    }

    @Bean(name = "mongoProvider")
    eu.europeana.metis.framework.mongo.MongoProvider getMongoProvider() throws UnknownHostException {
        provider = new eu.europeana.metis.framework.mongo.MongoProvider("localhost",10000,"test",null,null);
        return provider;
    }

    @Bean
    @DependsOn(value = "mongoProvider")
    public ExecutionDao getExecutionDao(){
        Morphia morphia = new Morphia();
        morphia.map(Execution.class);
        return new ExecutionDao(provider.getDatastore().getMongo(),morphia,provider.getDatastore().getDB().getName());
    }
    @Bean
    @DependsOn(value = "mongoProvider")
    public FailedRecordsDao getFailedRecordsDao(){
        Morphia morphia = new Morphia();
        morphia.map(FailedRecords.class);
        return new FailedRecordsDao(provider.getDatastore().getMongo(),morphia,provider.getDatastore().getDB().getName());
    }

    @Bean
    public DatasetService service(){
        return Mockito.mock(DatasetService.class);
    }

    @Bean
    DataSetServiceClient dataSetServiceClient() {
        return new DataSetServiceClient(ecloudBaseMcsUrl, ecloudUsername, ecloudPassword);
    }

    @Bean
    @DependsOn(value = "dataSetServiceClient")
    EcloudDatasetDao ecloudDatasetDao(){
        return new EcloudDatasetDao();
    }


    @Bean
    public Orchestrator getOrchestrator(){
        return new Orchestrator();
    }

    @Bean
    public VoidMetisWorkflow getVoidMetisWorkflow(){
        return new VoidMetisWorkflow();
    }

    @Bean
    public DatasetDao dsDao(){
        return Mockito.mock(DatasetDao.class);
    }
    @Bean
    public OrganizationDao orgDao(){
        return Mockito.mock(OrganizationDao.class);
    }
}
