package eu.europeana.redirects.rest.config;

import com.mongodb.MongoClientOptions;
import eu.europeana.corelib.lookup.impl.CollectionMongoServerImpl;
import eu.europeana.corelib.lookup.impl.EuropeanaIdMongoServerImpl;
import eu.europeana.corelib.storage.impl.MongoProviderImpl;
import eu.europeana.corelib.tools.lookuptable.CollectionMongoServer;
import eu.europeana.corelib.tools.lookuptable.EuropeanaIdMongoServer;
import eu.europeana.corelib.web.socks.SocksProxy;
import eu.europeana.redirects.service.RedirectService;
import eu.europeana.redirects.service.mongo.MongoRedirectService;
import java.net.MalformedURLException;
import java.util.List;
import javax.annotation.PreDestroy;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@ComponentScan(basePackages = {"eu.europeana.redirects.rest"})
@PropertySource("classpath:redirect.properties")
@EnableWebMvc
@EnableSwagger2
public class Application extends WebMvcConfigurerAdapter implements InitializingBean{
    //Socks proxy
    @Value("${socks.proxy.enabled}")
    private boolean socksProxyEnabled;
    @Value("${socks.proxy.host}")
    private String socksProxyHost;
    @Value("${socks.proxy.port}")
    private String socksProxyPort;
    @Value("${socks.proxy.username}")
    private String socksProxyUsername;
    @Value("${socks.proxy.password}")
    private String socksProxyPassword;

    @Value("${mongo.hosts}")
    private String[] mongoHosts;
    @Value("${mongo.port}")
    private int mongoPort;
    @Value("${mongo.username}")
    private String mongoUsername;
    @Value("${mongo.password}")
    private String mongoPassword;
    @Value("${mongo.db}")
    private String mongoDb;
    @Value("${mongo.collections.db}")
    private String mongoCollectionsDb;

    @Value("${zookeeper.production}")
    private String zookeeperProduction;
    @Value("${solr.production}")
    private String solrProduction;
    @Value("${solr.production.core}")
    private String solrProductionCore;

    private MongoProviderImpl mongoProvider;
    private MongoProviderImpl mongoProviderCollections;

    /**
     * Used for overwriting properties if cloud foundry environment is used
     */
    @Override
    public void afterPropertiesSet() {
        if (socksProxyEnabled) {
            new SocksProxy(socksProxyHost, socksProxyPort, socksProxyUsername, socksProxyPassword).init();
        }

        String[] mongoPorts = new String[mongoHosts.length];
        for (int i = 0; i < mongoHosts.length; i++) {
            mongoPorts[i]= Integer.toString(mongoPort);
        }
        MongoClientOptions.Builder options = MongoClientOptions.builder();
        mongoProvider = new MongoProviderImpl(mongoHosts, mongoPorts, mongoDb, mongoUsername,
            mongoPassword, options);
        mongoProviderCollections = new MongoProviderImpl(mongoHosts, mongoPorts, mongoCollectionsDb, mongoUsername,
            mongoPassword, options);
    }

    @Bean(name = "mongoServer")
    public EuropeanaIdMongoServer getMongoServer() {
        return new EuropeanaIdMongoServerImpl(mongoProvider.getMongo(), mongoDb);
    }

    @Bean(name = "collectionMongoServer")
    public CollectionMongoServer getCollectionMongoServer() {
        return new CollectionMongoServerImpl(mongoProviderCollections.getMongo(), mongoCollectionsDb);
    }

    @Bean(name = "productionSolrServer")
    public CloudSolrServer getProductionSolrServer() throws MalformedURLException {
        LBHttpSolrServer lbTargetProduction = new LBHttpSolrServer(solrProduction);
        CloudSolrServer productionSolrServer;
        productionSolrServer = new CloudSolrServer(zookeeperProduction, lbTargetProduction);
        productionSolrServer.setDefaultCollection(solrProductionCore);
        productionSolrServer.connect();

        return productionSolrServer;
    }

    @Bean
    @DependsOn(value = {"mongoServer", "collectionMongoServer", "productionSolrServer"})
    RedirectService getRedirectService(){
        return new MongoRedirectService();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
    @Override
    public  void configureMessageConverters(List<HttpMessageConverter<?>> converters){
        converters.add(new MappingJackson2HttpMessageConverter());
        super.configureMessageConverters(converters);
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        return propertySourcesPlaceholderConfigurer;
    }

    @Bean
    public Docket api(){
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.regex("/.*"))
                .build()
                .apiInfo(apiInfo());
    }

    @PreDestroy
    public void close()
    {
        if (mongoProvider != null)
            mongoProvider.close();
        if (mongoProviderCollections != null)
            mongoProviderCollections.close();
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
            "Metis redirects REST API",
            "Metis redirects REST API for Europeana",
            "v1",
            "API TOS",
            new Contact("development", "europeana.eu", "development@europeana.eu"),
            "EUPL Licence v1.1",
            ""
        );
    }
}
