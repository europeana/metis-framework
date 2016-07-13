/*
 * Copyright 2005-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.europeana.metis.ui.ldap;

import eu.europeana.metis.ui.config.MetisLdapManagerConfig;
import eu.europeana.metis.ui.ldap.dao.UserDao;
import eu.europeana.metis.ui.ldap.domain.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MetisLdapManagerConfig.class, loader = AnnotationConfigContextLoader.class)
public class LdifTest extends AbstractJUnit4SpringContextTests {

	protected User user;

	@Autowired
	private UserDao userDao;

	@Before
	public void prepareUser() throws Exception {
		user = new User();
		user.setFullName("John");
		user.setLastName("Doe");

		user.setPassword("test_pass");
		user.setEmail("john.doe@europeana.eu");
		user.setDescription("test description");
	}

	@Test
	public void testCreateUpdateAuthenticateDelete() {
		try {
			LdapTemplate tmp = userDao.getLdapTemplate();
			userDao.create(user);
			User newUser = userDao.findByPrimaryKey("john.doe@europeana.eu", "John");
			List<User> users = userDao.findAll();
			assertEquals(5,users.size());
			assertEquals("john.doe@europeana.eu", newUser.getEmail());
			Assert.assertTrue(tmp.authenticate("cn=John,ou=users,ou=metis_authentication", "(&(objectclass=person)(objectclass=inetOrgPerson)(objectclass=organizationalPerson)(objectclass=top))"
					,"test_pass"));
			// creation succeeded
			newUser.setLastName("Doe2");
			newUser.setDescription("Added new description");
			userDao.update(newUser);
			List<User> usersNew = userDao.findAll();
			assertEquals(5,usersNew.size());
			User result = userDao.findByPrimaryKey("john.doe@europeana.eu", "John");
			assertEquals("Doe2", result.getLastName());
			assertEquals("Added new description", result.getDescription());
			Assert.assertTrue(tmp.authenticate("cn=John,ou=users,ou=metis_authentication", "(&(objectclass=person)(objectclass=inetOrgPerson)(objectclass=organizationalPerson)(objectclass=top))"
					,"test_pass"));

		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			userDao.delete(user);
			try {
				Assert.assertNull(userDao.findByPrimaryKey("john.doe@europeana.eu", "John"));
				fail("NameNotFoundException for embedded LDAP or RuntimeException for normal LDAP");
			} catch (NameNotFoundException e) {
				//e.printStackTrace();
			} catch (RuntimeException e) {
				//e.printStackTrace();
			}
		}
	}

	@Test
	public void testGetAllUserNames() {
		List<String> result = userDao.getAllUserNames();
		assertEquals(4, result.size());
		String first = result.get(0);
		assertEquals("Alena", first);
	}

	@Test
	public void testFindAll() {
		List<User> result = userDao.findAll();
		assertEquals(4, result.size());
		User first = result.get(0);
		assertEquals("Alena", first.getFullName());
	}

	@Test
	public void testFindByPrimaryKey() {
		User result = userDao.findByPrimaryKey("alena.fedasenka@europeana.eu", "Alena");
		assertEquals("Fedasenka", result.getLastName());
		assertEquals("alena.fedasenka@europeana.eu", result.getEmail());
	}
}
