package eu.europeana.metis.ui.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class MetisSecurityConfig extends WebSecurityConfigurerAdapter {

	@Configuration
	@PropertySource("classpath:authentication.properties")
	protected static class AuthenticationConfiguration extends GlobalAuthenticationConfigurerAdapter {
		
		@Value("${ldap.url}")
		private String url;
		
		@Value("${ldap.manager.dn}")
		private String managerDN;
		
		@Value("${ldap.manager.pwd}")
		private String managerPWD;
		
		@Override
		public void init(AuthenticationManagerBuilder auth) throws Exception {		
			auth.ldapAuthentication().contextSource()
			//ldif("classpath:metis-ldap.ldif").root("dc=europeana,dc=eu")	//TODO   comment this line when we get LDAP server!
			.url(url).managerDn(managerDN).managerPassword(managerPWD) 	//TODO uncomment this line when we get LDAP server!
			.and()
            .userSearchBase("ou=users,ou=metis_authentication")
            .userSearchFilter("(uid={0})")
            .groupSearchBase("ou=roles,ou=metis_authentication")
            .groupRoleAttribute("cn")
            .groupSearchFilter("(member={0})");
		}
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/resources/**");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// @formatter:off
		http.authorizeRequests()
				.antMatchers("/metis/**", "/result/**")
				.hasAnyRole("EUROPEANA_ADMIN","EUROPEANA_VIEWER", "EUROPEANA_DATA_OFFICER", "HUB_ADMIN", "HUB_VIEWER", "HUB_DATA_OFFICER")
				.anyRequest().permitAll()
				.and()
				.logout().logoutSuccessUrl("/login").permitAll()
				.and().formLogin().loginProcessingUrl("/login")
				.loginPage("/login").defaultSuccessUrl("/").failureUrl("/login?authentication_error=true").permitAll()
				.and()
				.csrf().disable();
		// @formatter:on
	}
}
