package eu.europeana.metis.framework.ui.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.ldap.DefaultLdapUsernameToDnMapper;
import org.springframework.security.ldap.userdetails.InetOrgPersonContextMapper;
import org.springframework.security.ldap.userdetails.LdapUserDetailsManager;

@Configuration
public class LDAPManagerConfig {

	@Bean
	public LdapContextSource contextSourceTarget() {
	    LdapContextSource ldapContextSource = new LdapContextSource();
	    ldapContextSource.setUrl("ldap://Alenas-iMac:1389");
	    ldapContextSource.setBase("ou=metis_authentication,dc=europeana,dc=eu");
	    ldapContextSource.setUserDn("cn=Metis Authentication");
	    ldapContextSource.setPassword("secret");
	    return ldapContextSource;

	}

	@Bean
	public LdapTemplate ldapTemplate() {
	    return new LdapTemplate(contextSourceTarget());
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
	    LdapUserDetailsManager userManager = new LdapUserDetailsManager(contextSourceTarget());
	    userManager.setGroupSearchBase("ou=roles,ou=metis_authentication");
	    userManager.setUserDetailsMapper(inetOrgPersonContextMapper());
	    userManager.setUsernameMapper(defaultLdapUsernameToDnMapper());
	    userManager.setGroupRoleAttributeName("cn");
	    userManager.setGroupMemberAttributeName("member");
	    return userManager;
	}
}
