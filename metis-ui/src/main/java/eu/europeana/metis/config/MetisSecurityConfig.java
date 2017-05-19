/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.metis.config;

import eu.europeana.metis.ui.mongo.domain.Roles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * This configuration sets up Metis web pages LDAP authorization.
 *
 * @author alena
 */
@Configuration
@PropertySource("classpath:authentication.properties")
@EnableWebSecurity
public class MetisSecurityConfig extends WebSecurityConfigurerAdapter {

  @Value("${ldap.url}")
  private String url;
  @Value("${ldap.manager.dn}")
  private String managerDN;
  @Value("${ldap.manager.pwd}")
  private String managerPWD;

  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder authenticationMgr) throws Exception {
    authenticationMgr.ldapAuthentication()
        .contextSource()
        .url(url).managerDn(managerDN).managerPassword(managerPWD)
        .and()
        .userSearchBase("ou=users,ou=metis_authentication")
        .userSearchFilter("(uid={0})")
        .groupSearchBase("ou=roles,ou=metis_authentication")
        .groupRoleAttribute("cn")
        .groupSearchFilter("(member={0})");
  }

  @Override
  public void configure(WebSecurity web) throws Exception {
    web.ignoring().antMatchers("/resources/**");
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
        .antMatchers("/", "/register", "/mappings-page").permitAll()
        .antMatchers("/dashboard").authenticated()
        .antMatchers("/profile")
        .hasAnyRole(Roles.EUROPEANA_ADMIN.getLdapName(), Roles.EUROPEANA_VIEWER.getLdapName(),
            Roles.EUROPEANA_DATA_OFFICER.getLdapName(), Roles.PROVIDER_ADMIN.getLdapName(),
            Roles.PROVIDER_VIEWER.getLdapName(), Roles.EUROPEANA_DATA_OFFICER.getLdapName())
        .antMatchers("/requests").hasRole(Roles.EUROPEANA_ADMIN.getLdapName())
        .and()
        .formLogin().loginPage("/login")
        .usernameParameter("email").passwordParameter("password")
        .defaultSuccessUrl("/dashboard")
        .failureUrl("/login?authentication_error=true").permitAll()
        .and()
        .logout()
        .logoutSuccessUrl("/")
        .and()
        .csrf().disable();
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }
}
