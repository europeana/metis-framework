package eu.europeana.metis.ui.ldap.dao;

import eu.europeana.metis.ui.ldap.domain.User;
import org.springframework.ldap.core.LdapTemplate;

import java.util.List;

public interface UserDao {
	
   void create(User user);

   void update(User user);

   void delete(User user);

   List<String> getAllUserNames();

   List<User> findAll();

   User findByPrimaryKey(String email, String fullname);

   LdapTemplate getLdapTemplate();
}
