package eu.europeana.metis.ui.ldap.dao.impl;

import eu.europeana.metis.ui.ldap.dao.UserDao;
import eu.europeana.metis.ui.ldap.domain.Group;
import eu.europeana.metis.ui.ldap.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.ldap.LdapName;
import java.util.List;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

public class UserDaoImpl implements UserDao {

    @Autowired
    private LdapTemplate ldapTemplate;

    @Override
    public void create(User user) {
        LdapName userDn = buildDn(user.getFullName());
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
        LdapName dn = buildDn(user.getFullName());
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
            attrs.put("cn", user.getFullName());
            attrs.put("sn", user.getLastName());
            attrs.put("uid",user.getFullName().toLowerCase());
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


    @Override
    public LdapTemplate getLdapTemplate() {
        return ldapTemplate;

    }
}
