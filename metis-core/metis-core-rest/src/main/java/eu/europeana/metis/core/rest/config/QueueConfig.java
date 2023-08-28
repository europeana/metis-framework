package eu.europeana.metis.core.rest.config;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.impl.ForgivingExceptionHandler;
import eu.europeana.metis.core.execution.QueueConsumer;
import eu.europeana.metis.core.execution.WorkflowExecutionMonitor;
import eu.europeana.metis.core.execution.WorkflowExecutorManager;
import eu.europeana.metis.exception.GenericMetisException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import metis.common.config.properties.TruststoreConfigurationProperties;
import metis.common.config.properties.rabbitmq.RabbitmqConfigurationProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties({RabbitmqConfigurationProperties.class, TruststoreConfigurationProperties.class})
@ComponentScan(basePackages = {"eu.europeana.metis.core.rest.controller"})
@EnableScheduling
public class QueueConfig implements WebMvcConfigurer {

  private static final Logger LOGGER = LoggerFactory.getLogger(QueueConfig.class);
  private QueueConsumer queueConsumer;

  private Connection connection;
  private Channel publisherChannel;
  private Channel consumerChannel;

  @Bean
  Connection getConnection(RabbitmqConfigurationProperties rabbitmqConfigurationProperties,
      TruststoreConfigurationProperties truststoreConfigurationProperties)
      throws KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException, KeyStoreException, CertificateException {
    ConnectionFactory connectionFactory = new ConnectionFactory();
    connectionFactory.setHost(rabbitmqConfigurationProperties.getHost());
    connectionFactory.setPort(rabbitmqConfigurationProperties.getPort());
    connectionFactory.setVirtualHost(
        StringUtils.isNotBlank(rabbitmqConfigurationProperties.getVirtualHost()) ? rabbitmqConfigurationProperties
            .getVirtualHost() : "/");
    connectionFactory.setUsername(rabbitmqConfigurationProperties.getUsername());
    connectionFactory.setPassword(rabbitmqConfigurationProperties.getPassword());
    connectionFactory.setAutomaticRecoveryEnabled(true);
    if (rabbitmqConfigurationProperties.isEnableSsl()) {
      if (rabbitmqConfigurationProperties.isEnableCustomTruststore()) {
        // Load the ssl context with the provided truststore
        final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        // This file is determined in the config files, it does not pose a risk.
        @SuppressWarnings("findsecbugs:PATH_TRAVERSAL_IN")
        final Path trustStoreFile = Paths.get(
            truststoreConfigurationProperties.getPath());
        try (final InputStream inputStream = Files.newInputStream(trustStoreFile)) {
          keyStore.load(inputStream, truststoreConfigurationProperties.getPassword().toCharArray());
        }
        TrustManagerFactory trustManagerFactory = TrustManagerFactory
            .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
        connectionFactory.useSslProtocol(sslContext);
        LOGGER.info("RabbitMQ enabled SSL WITH certificate verification using custom Truststore");
      } else {
        connectionFactory.useSslProtocol();
        LOGGER.info("RabbitMQ enabled SSL WITHOUT certificate verification");
      }
    }
    //Does not close the channel if an unhandled exception occurred
    //Can happen in QueueConsumer and it's safe to not handle the execution, it will be picked up
    //again from the failsafe Executor.
    connectionFactory.setExceptionHandler(new ForgivingExceptionHandler());
    connection = connectionFactory.newConnection();
    return connection;
  }

  @Bean(name = "rabbitmqPublisherChannel")
  Channel getRabbitmqPublisherChannel(Connection connection, RabbitmqConfigurationProperties rabbitmqConfigurationProperties)
      throws IOException {
    publisherChannel = connection.createChannel();
    setupChannelProperties(publisherChannel, rabbitmqConfigurationProperties);
    return publisherChannel;
  }

  @Bean(name = "rabbitmqConsumerChannel")
  Channel getRabbitmqConsumerChannel(Connection connection, RabbitmqConfigurationProperties rabbitmqConfigurationProperties)
      throws IOException {
    consumerChannel = connection.createChannel();
    setupChannelProperties(consumerChannel, rabbitmqConfigurationProperties);
    return consumerChannel;
  }

  private void setupChannelProperties(Channel channel, RabbitmqConfigurationProperties rabbitmqConfigurationProperties)
      throws IOException {
    Map<String, Object> args = new ConcurrentHashMap<>();
    args.put("x-max-priority",
        rabbitmqConfigurationProperties.getHighestPriority());//Higher number means higher priority
    //Second boolean durable to false
    channel.queueDeclare(rabbitmqConfigurationProperties.getQueueName(), false, false, false, args);
  }

  @Bean
  public QueueConsumer getQueueConsumer(
      RabbitmqConfigurationProperties rabbitmqConfigurationProperties,
      WorkflowExecutorManager workflowExecutionManager,
      WorkflowExecutionMonitor workflowExecutionMonitor,
      @Qualifier("rabbitmqConsumerChannel") Channel rabbitmqConsumerChannel) throws IOException {
    queueConsumer = new QueueConsumer(rabbitmqConsumerChannel,
        rabbitmqConfigurationProperties.getQueueName(), workflowExecutionManager, workflowExecutionManager,
        workflowExecutionMonitor);
    return queueConsumer;
  }

  // TODO: 24/08/2023 Is there a better way to load the configuration here?
  @Scheduled(
      fixedDelayString = "${metis-core.pollingTimeoutForCleaningCompletionServiceInMilliseconds}",
      initialDelayString = "${metis-core.pollingTimeoutForCleaningCompletionServiceInMilliseconds}")
  public void runQueueConsumerCleanup() throws InterruptedException {
    this.queueConsumer.checkAndCleanCompletionService();
    LOGGER.debug("Queue consumer cleanup finished.");
  }

  /**
   * Close resources.
   *
   * @throws GenericMetisException if a resource failed to close
   */
  @PreDestroy
  public void close() throws GenericMetisException {
    try {
      // Shut down RabbitMQ
      if (publisherChannel != null && publisherChannel.isOpen()) {
        publisherChannel.close();
      }
      if (consumerChannel != null && consumerChannel.isOpen()) {
        consumerChannel.close();
      }
      if (connection != null && connection.isOpen()) {
        connection.close();
      }
      // Shutdown the queue consumer
      if (queueConsumer != null) {
        queueConsumer.close();
      }
    } catch (IOException | TimeoutException e) {
      throw new GenericMetisException("Could not shutdown resources properly.", e);
    }
  }

}
