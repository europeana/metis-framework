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

package eu.europeana.metis.ui.ldap.dao.impl;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.ldap.LdapName;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;

import eu.europeana.metis.ui.ldap.dao.UserDao;
import eu.europeana.metis.ui.ldap.domain.Group;
import eu.europeana.metis.ui.ldap.domain.User;

public class UserDaoImpl implements UserDao {

    @Autowired
    private LdapTemplate ldapTemplate;

    /**
     * FIXME Currently for test reasons every newly created user is automatically added to admin group (for testing).
     */
    @Override
    public void create(User user) {
        LdapName userDn = buildDn(user.getEmail());
        user.setDn(userDn);
        LdapName groupDn = buildRoleDn("europeana_admin");
        Group grp = ldapTemplate.findByDn(groupDn, Group.class);
        List<String> members = grp.getMembers();
        members.add(user.getDn().toString() + ",dc=europeana,dc=eu");
        grp.setMembers(members);
        DirContextOperations context = ldapTemplate.lookupContext(groupDn);
        updateGroup(grp, context);
        ldapTemplate.modifyAttributes(context);
        ldapTemplate.bind(userDn,null,buildUser(user));
    }

    @Override
    public void update(User user) {
        LdapName dn = buildDn(user.getEmail());
        DirContextOperations context = ldapTemplate.lookupContext(dn);
        updateUser(user, context);
        ldapTemplate.modifyAttributes(context);
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
    public User findByPrimaryKey(String email) {
        LdapName dn = buildDn(email);
        User findByDn;
        try {
        	findByDn = ldapTemplate.findByDn(dn, User.class);			
		} catch (Exception e) {
			findByDn = null;
		}
		return findByDn;
    }
    
    @Override
    public LdapTemplate getLdapTemplate() {
    	return ldapTemplate;
    	
    }

    @Override
    public void addUserRole(User user, Group group) {
    	// TODO Auto-generated method stub
    }
    
    @Override
    public void removeUserRole(User user, Group group) {
    	// TODO Auto-generated method stub
    }
    
    
    private LdapName buildDn(User user) {
        return buildDn(user.getEmail());
    }

    private LdapName buildDn(String email) {
        return LdapNameBuilder.newInstance()
                .add("ou", "metis_authentication")
                .add("ou", "users")
                .add("cn", email)
                .build();
    }

    private LdapName buildRoleDn(String groupName) {
        return LdapNameBuilder.newInstance()
                .add("ou", "metis_authentication")
                .add("ou", "roles")
                .add("cn", groupName)
                .build();
    }

    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    private void updateUser(User user, DirContextOperations context) {
        context.setAttributeValue("mail", user.getEmail());
        context.setAttributeValue("description", user.getDescription());
        context.setAttributeValue("sn", user.getLastName());
        context.setAttributeValue("userPassword", user.getPasswordB());
    }

    private Attributes buildUser(User user){
            Attributes attrs = new BasicAttributes();
            BasicAttribute ocattr = new BasicAttribute("objectclass");
            ocattr.add("top");

            ocattr.add("organizationalPerson");
            ocattr.add("person");
            ocattr.add("inetOrgPerson");
            attrs.put(ocattr);
            attrs.put("cn", user.getEmail());
            attrs.put("sn", user.getLastName());
            attrs.put("uid",user.getEmail().toLowerCase());
            attrs.put("givenName",user.getFullName());
            attrs.put("userPassword",user.getPasswordB());
            attrs.put("mail",user.getEmail());
            if(user.getDescription()!=null) {
                attrs.put("description", user.getDescription());
            }
            return attrs;
    }

    private void updateGroup(Group group, DirContextOperations context) {
        context.setAttributeValues("member", group.getMembers().toArray(new String[]{}));
    }
}
