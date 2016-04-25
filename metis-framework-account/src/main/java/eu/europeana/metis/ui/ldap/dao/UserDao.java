package eu.europeana.metis.ui.ldap.dao;

import java.util.List;

import eu.europeana.metis.ui.ldap.domain.User;

public interface UserDao {
	
   void create(User user);

   void update(User user);

   void delete(User user);

   List<String> getAllUserNames();

   List<User> findAll();

   User findByPrimaryKey(String email, String fullname);
}
