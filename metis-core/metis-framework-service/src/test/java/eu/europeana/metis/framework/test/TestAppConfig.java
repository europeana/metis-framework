package eu.europeana.metis.framework.test;

import eu.europeana.metis.framework.workflow.AbstractMetisWorkflow;
import org.springframework.context.annotation.Configuration;
import org.springframework.plugin.core.config.EnablePluginRegistries;

/**
 * Created by ymamakis on 11/17/16.
 */
@Configuration
@EnablePluginRegistries(AbstractMetisWorkflow.class)
public class TestAppConfig {
//    eu.europeana.metis.framework.mongo.MongoProvider provider;
    public TestAppConfig(){
//        MongoProvider.start(10000);
    }

//    @Bean(name = "mongoProvider")
//    eu.europeana.metis.framework.mongo.MongoProvider getMongoProvider() throws UnknownHostException {
//        provider = new eu.europeana.metis.framework.mongo.MongoProvider("localhost",10000,"test",null,null);
//        return provider;
//    }

//    @Bean(name = "mongoProvider")
//    eu.europeana.metis.framework.mongo.MongoProvider getMongoProvider() throws UnknownHostException {
//        provider = new eu.europeana.metis.framework.mongo.MongoProvider("localhost",10000,"test",null,null);
//        return provider;
//    }


//    @Bean
//    @DependsOn(value = "mongoProvider")
//    public ExecutionDao getExecutionDao(){
//        Morphia morphia = new Morphia();
//        morphia.map(Execution.class);
//        return new ExecutionDao(provider.getDatastore().getMongo(),morphia,provider.getDatastore().getDB().getName());
//    }
//    @Bean
//    @DependsOn(value = "mongoProvider")
//    public FailedRecordsDao getFailedRecordsDao(){
//        Morphia morphia = new Morphia();
//        morphia.map(FailedRecords.class);
//        return new FailedRecordsDao(provider.getDatastore().getMongo(),morphia,provider.getDatastore().getDB().getName());
//    }
//    @Bean
//    public DatasetService service(){
//        return Mockito.mock(DatasetService.class);
//    }
//    @Bean
//    public Orchestrator getOrchestrator(){
//        return new Orchestrator();
//    }
//
//    @Bean
//    public VoidMetisWorkflow getVoidMetisWorkflow(){
//        return new VoidMetisWorkflow();
//    }
//
//    @Bean
//    public DatasetDao dsDao(){
//        return Mockito.mock(DatasetDao.class);
//    }
//    @Bean
//    public OrganizationDao orgDao(){
//        return Mockito.mock(OrganizationDao.class);
//    }
}
