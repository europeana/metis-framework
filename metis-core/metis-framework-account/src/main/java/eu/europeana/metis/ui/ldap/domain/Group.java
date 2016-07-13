package eu.europeana.metis.ui.ldap.domain;

/**
 * Created by ymamakis on 7/11/16.
 */

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.ldap.LdapName;
import java.util.List;

@Entry(objectClasses = {"groupOfNames", "top"})
public class Group {

    @Id
    private LdapName dn;

    @Attribute(name = "cn")
    @DnAttribute(value = "cn", index = 2)
    private String cn;

    @Attribute(name = "ou")
    @DnAttribute(value = "ou", index = 1)
    private String roles;

    @Attribute(name = "ou")
    @DnAttribute(value = "ou", index = 0)
    private String metisAuthenticationDn;

    @Attribute(name="member")
    private List<String> members;

    public String getCn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getMetisAuthenticationDn() {
        return metisAuthenticationDn;
    }

    public void setMetisAuthenticationDn(String metisAuthenticationDn) {
        this.metisAuthenticationDn = metisAuthenticationDn;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public LdapName getDn() {
        return dn;
    }

    public void setDn(LdapName dn) {
        this.dn = dn;
    }
}
