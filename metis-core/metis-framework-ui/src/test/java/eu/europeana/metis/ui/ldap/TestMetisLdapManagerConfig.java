package eu.europeana.metis.ui.ldap;

import eu.europeana.metis.ui.ldap.dao.UserDao;
import eu.europeana.metis.ui.ldap.dao.impl.UserDaoImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.test.EmbeddedLdapServerFactoryBean;
import org.springframework.ldap.test.LdifPopulator;
import org.springframework.security.ldap.DefaultLdapUsernameToDnMapper;
import org.springframework.security.ldap.userdetails.InetOrgPersonContextMapper;
import org.springframework.security.ldap.userdetails.LdapUserDetailsManager;

@Configuration
@PropertySource("classpath:/authentication.properties")
public class TestMetisLdapManagerConfig {

	@Value("${ldif.url}")
	private String url;
	
	@Value("${ldif.dn}")
	private String dn;
	
	@Value("${ldif.pwd}")
	private String pwd;
	
	@Value("${ldif.base}")
	private String base;
	
	@Value("${ldif.clean}")
	private String clean;

	@Value("${ldap.url}")
	private String ldapUrl;

	@Value("${ldap.manager.dn}")
	private String ldapManagerDn;

	@Value("${ldap.manager.pwd}")
	private String ldapPwd;

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
		return new PropertySourcesPlaceholderConfigurer();
	}



	@Bean
	public LdapContextSource contextSource() {
	    LdapContextSource ldapContextSource = new LdapContextSource();
	    ldapContextSource.setUrl(url);
	    ldapContextSource.setBase(base);
	    ldapContextSource.setUserDn(dn);
	    ldapContextSource.setPassword(pwd);
	    return ldapContextSource;
	}



/*

	@Bean
	public LdapContextSource contextSource() {
		LdapContextSource ldapContextSource = new LdapContextSource();
		ldapContextSource.setUrl(ldapUrl);
		ldapContextSource.setUserDn(ldapManagerDn);
		ldapContextSource.setPassword(ldapPwd);
		return ldapContextSource;
	}
	*/
	@Bean
	public LdapTemplate ldapTemplate() {
	    return new LdapTemplate(contextSource());
	}

	@Bean
	public InetOrgPersonContextMapper inetOrgPersonContextMapper() {
	    return new InetOrgPersonContextMapper();
	}
	
	@Bean
	public DefaultLdapUsernameToDnMapper defaultLdapUsernameToDnMapper() {
	    return new DefaultLdapUsernameToDnMapper("ou=users", "uid");// "uid"
	}

	@Bean
	public LdapUserDetailsManager ldapUserDetailManager() {
	    LdapUserDetailsManager userManager = new LdapUserDetailsManager(contextSource());
	    userManager.setGroupSearchBase("ou=roles,ou=metis_authentication");
	    userManager.setUserDetailsMapper(inetOrgPersonContextMapper());
	    userManager.setUsernameMapper(defaultLdapUsernameToDnMapper());
	    userManager.setGroupRoleAttributeName("cn");
	    userManager.setGroupMemberAttributeName("member");
	    return userManager;
	}


	@Bean
	public EmbeddedLdapServerFactoryBean embeddedLdapServer() {
		EmbeddedLdapServerFactoryBean embeddedLdapServerFactoryBean = new EmbeddedLdapServerFactoryBean();
		embeddedLdapServerFactoryBean.setPartitionName("example");
		embeddedLdapServerFactoryBean.setPartitionSuffix(base);
		embeddedLdapServerFactoryBean.setPort(18880);		
		return embeddedLdapServerFactoryBean;
	}
	
	@Bean
	@DependsOn("embeddedLdapServer")
	public LdifPopulator ldifPopulator() {
		LdifPopulator ldifPopulator = new LdifPopulator();
		ldifPopulator.setContextSource(contextSource());
		ldifPopulator.setResource(new ClassPathResource("/metis-ldap.ldif"));
		ldifPopulator.setBase(base);
		ldifPopulator.setDefaultBase("dc=europeana,dc=eu");
		ldifPopulator.setClean(Boolean.valueOf(clean));
		return ldifPopulator;
	}


	
	@Bean
	public UserDao userDao() {
		return new UserDaoImpl();
	}
}
