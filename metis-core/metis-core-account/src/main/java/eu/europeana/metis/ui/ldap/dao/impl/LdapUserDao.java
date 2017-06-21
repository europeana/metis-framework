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

import eu.europeana.metis.ui.ldap.domain.LdapUser;
import eu.europeana.metis.ui.mongo.domain.Role;
import java.util.ArrayList;
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

public class LdapUserDao implements UserDao {
	
    @Autowired
    private LdapTemplate ldapTemplate;

    /**
     * FIXME Currently for test reasons every newly created user is automatically added to admin group (for testing).
     */
    @Override
    public void create(LdapUser ldapUser) {
        LdapName userDn = buildDn(ldapUser.getEmail());
        ldapUser.setDn(userDn);
        ldapUser.setFirstName(ldapUser.getFirstName());
//        LdapName groupDn = buildRoleDn("EUROPEANA_ADMIN");
//        Group grp = ldapTemplate.findByDn(groupDn, Group.class);
//        List<String> members = grp.getMembers();
//        members.add(user.getDn().toString() + ",dc=europeana,dc=eu");
//        grp.setMembers(members);
//        DirContextOperations context = ldapTemplate.lookupContext(groupDn);
//        updateGroup(grp, context);
//        ldapTemplate.modifyAttributes(context);
        ldapTemplate.bind(userDn, null, buildUser(ldapUser));
    }

    @Override
    public void update(LdapUser ldapUser) {
        LdapName dn = buildDn(ldapUser.getEmail());
        DirContextOperations context = ldapTemplate.lookupContext(dn);
        updateUser(ldapUser, context);
        ldapTemplate.modifyAttributes(context);
    }

    @Override
    public void disable(String email){
        LdapUser ldapUser = findByPrimaryKey(email);
        ldapUser.setActive(false);
        update(ldapUser);
    }

    @Override
    public void approve(String email){
        LdapUser ldapUser = findByPrimaryKey(email);
        ldapUser.setApproved(true);
        ldapUser.setActive(true);
        update(ldapUser);
    }
    @Override
    public void delete(LdapUser ldapUser) {
        ldapTemplate.delete(ldapTemplate.findByDn(buildDn(ldapUser), LdapUser.class));
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
    public List<LdapUser> findAll() {
        return ldapTemplate.findAll(LdapUser.class);
    }

    @Override
    public LdapUser findByPrimaryKey(String email) {
        LdapName dn = buildDn(email);
        LdapUser findByDn;
        try {
        	findByDn = ldapTemplate.findByDn(dn, LdapUser.class);
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
    public void addUserRole(LdapUser ldapUser, Group group) {
      List<String> members = group.getMembers();
      members.add(ldapUser.getDn().toString() + ",dc=europeana,dc=eu");
      group.setMembers(members);
      DirContextOperations context = ldapTemplate.lookupContext(group.getDn());
      updateGroup(group, context);
      ldapTemplate.modifyAttributes(context);
    }
    
    @Override
    public void removeUserRole(LdapUser ldapUser, Group group) {
    	//TODO test this!
    	List<String> members = group.getMembers();
        members.remove(ldapUser.getDn().toString() + ",dc=europeana,dc=eu");
        group.setMembers(members);
        DirContextOperations context = ldapTemplate.lookupContext(group.getDn());
        updateGroup(group, context);
        ldapTemplate.modifyAttributes(context);
    }
    
/**
 *    The user dn in a role group looks as follows:
 *    "cn=email@europeana.eu,ou=users,ou=metis_authentication,dc=europeana,dc=eu"
 */
    @Override
    public List<String> findUsersByRole(Role userRole) {
      List<String> members = ldapTemplate.findByDn(buildRoleDn(userRole.getLdapName()), Group.class).getMembers();
      List<String> emails = new ArrayList<>();
      if (members != null && !members.isEmpty()) {
    	  for (String member : members) {
    		  String cn = member.split(",")[0];
    		  if (cn.length() > 2) {
    			  emails.add(cn.substring(3));    			  
    		  }
    	  }    	  
      }
      return emails;
    }
    
    
    private LdapName buildDn(LdapUser ldapUser) {
        return buildDn(ldapUser.getEmail());
    }

    private LdapName buildDn(String email) {
    	if (email == null) {
    		return null;
    	}
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

    private void updateUser(LdapUser ldapUser, DirContextOperations context) {
        context.setAttributeValue("mail", ldapUser.getEmail());
        context.setAttributeValue("description", ldapUser.getDescription());
        context.setAttributeValue("sn", ldapUser.getLastName());
        context.setAttributeValue("givenName", ldapUser.getFirstName());
        if (ldapUser.getPassword() != null && !ldapUser.getPassword().isEmpty()) {
        	context.setAttributeValue("userPassword", ldapUser.getPassword());
        }
        context.setAttributeValue("Active", (ldapUser.isActive() + "").toUpperCase());
        context.setAttributeValue("Approved", (ldapUser.isApproved() + "").toUpperCase());
    }



    private Attributes buildUser(LdapUser ldapUser){
            Attributes attrs = new BasicAttributes();
            BasicAttribute ocattr = new BasicAttribute("objectclass");
            ocattr.add("top");

            ocattr.add("organizationalPerson");
            ocattr.add("person");
            ocattr.add("inetOrgPerson");
            ocattr.add("metisUser");
            attrs.put(ocattr);
            attrs.put("cn", ldapUser.getEmail());
            attrs.put("sn", ldapUser.getLastName());
            attrs.put("uid", ldapUser.getEmail().toLowerCase());
            attrs.put("givenName", ldapUser.getFirstName());
            if (ldapUser.getPassword() != null && !ldapUser.getPassword().isEmpty()) {
            	attrs.put("userPassword", ldapUser.getPassword());
            }
            attrs.put("mail", ldapUser.getEmail());
            attrs.put("Active", (ldapUser.isActive() + "").toUpperCase());
            attrs.put("Approved", (ldapUser.isApproved() + "").toUpperCase());
            
            if(ldapUser.getDescription() != null) {
                attrs.put("description", ldapUser.getDescription());
            }
            return attrs;
    }

    private void updateGroup(Group group, DirContextOperations context) {
        context.setAttributeValues("member", group.getMembers().toArray(new String[]{}));
    }
}
