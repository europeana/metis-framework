package eu.europeana.metis.ui.ldap.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;

import eu.europeana.metis.ui.ldap.dao.UserDao;
import eu.europeana.metis.ui.ldap.domain.User;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapName;
import java.util.List;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

public class UserDaoImpl implements UserDao {

	@Autowired
	private LdapTemplate ldapTemplate;

    @Override
	public void create(User user) {
    	user.setDn(buildDn(user.getFullName()));
		ldapTemplate.create(user);
	}

    @Override
	public void update(User user) {
		ldapTemplate.update(user);
	}

    @Override
	public void delete(User user) {
		ldapTemplate.delete(ldapTemplate.findByDn(buildDn(user), User.class));
	}

    @Override
	public List<String> getAllUserNames() {
        return ldapTemplate.search(query()
                .attributes("cn")
                .where("objectclass").is("person"),
                new AttributesMapper<String>() {
                    public String mapFromAttributes(Attributes attrs) throws NamingException {
                        return attrs.get("cn").get().toString();
                    }
                });
    }

    @Override
	public List<User> findAll() {
        return ldapTemplate.findAll(User.class);
	}

    @Override
	public User findByPrimaryKey(String email, String fullname) {
		LdapName dn = buildDn(fullname);
        return ldapTemplate.findByDn(dn, User.class);
	}

	private LdapName buildDn(User user) {
		return buildDn(user.getFullName());
	}

	private LdapName buildDn(String fullname) {
        return LdapNameBuilder.newInstance()
                .add("ou", "metis_authentication")
                .add("ou", "users")
                .add("cn", fullname)
                .build();
	}

	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}
}
