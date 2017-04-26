package eu.europeana.metis.mail.config;

import eu.europeana.metis.mail.MailService;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;

import java.util.HashMap;
import java.util.Map;

/**
 * Mail configuration
 * Created by ymamakis on 6-1-17.
 */
@Configuration
public class MailConfig {
    @Value("${mail.host}")
    private String host;
    @Value("${mail.port}")
    private int port;
    @Value("${mail.username}")
    private String username;
    @Value("${mail.password}")
    private String password;

    @Bean
    public MailService mailService() {
        return new MailService("metis@europeana.eu");
    }

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password);
        return sender;
    }

    @Bean
    public VelocityEngine engine() {
        VelocityEngineFactoryBean bean = new VelocityEngineFactoryBean();
        Map<String, Object> properties = new HashMap<>();
        properties.put("resource.loader", "class");
        properties.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        bean.setVelocityPropertiesMap(properties);
        return bean.getObject();
    }
}
