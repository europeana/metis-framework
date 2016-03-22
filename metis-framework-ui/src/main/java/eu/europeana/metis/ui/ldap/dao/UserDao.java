package eu.europeana.metis.ui.ldap.dao;

import java.util.List;

import eu.europeana.metis.ui.ldap.domain.User;

public interface UserDao {
   void create(User person);

   void update(User person);

   void delete(User person);

   List<String> getAllPersonNames();

   List<User> findAll();

   User findByPrimaryKey(String country, String company, String fullname);
}
