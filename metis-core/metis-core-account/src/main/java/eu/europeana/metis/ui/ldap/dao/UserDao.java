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

package eu.europeana.metis.ui.ldap.dao;

import eu.europeana.metis.ui.ldap.domain.Group;
import eu.europeana.metis.ui.ldap.domain.User;
import eu.europeana.metis.ui.mongo.domain.Roles;

import org.springframework.ldap.core.LdapTemplate;

import java.util.List;

public interface UserDao {
   void create(User user);

   void update(User user);

   void delete(User user);

   List<String> getAllUserNames();

   List<User> findAll();

   User findByPrimaryKey(String email);

   LdapTemplate getLdapTemplate();
   
   void addUserRole(User user, Group group);
   
   void removeUserRole(User user, Group group);
   
   void disable(String email);
   
   void approve(String email);
   
   List<String> findUsersByRole(Roles userRole);
}
