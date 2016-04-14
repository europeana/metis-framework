package eu.europeana.metis.ui.ldap.domain;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
//import org.springframework.ldap.odm.annotations.Transient;

import javax.naming.Name;

/**
 * LDAP Template:
 * dn: cn=Cecile,ou=users,ou=metis_authentication,{dc=europeana,dc=eu}
 * dn: cn={fullName},ou=users,ou=metis_authentication,{base_dn}
 * @author alena
 *
 */
@Entry(objectClasses = {"person", "inetOrgPerson", "organizationalPerson", "top"})
public class User {
    @Id
    private Name dn;

    @Attribute(name = "cn")
    @DnAttribute(value = "cn", index = 2)
    private String fullName;
    
    @Attribute(name = "ou")
    @DnAttribute(value = "ou", index = 1)
    private String usersDn;

	@Attribute(name = "ou")
    @DnAttribute(value = "ou", index = 0)
    private String metisAuthenticationDn;

    @Attribute(name = "sn")
    private String lastName;
    
    @Attribute(name = "userPassword")
    private String password;

    @Attribute(name = "description")
    private String description;  

    @Attribute(name = "mail")
    private String email;

    public Name getDn() {
        return dn;
    }

    public void setDn(Name dn) {
        this.dn = dn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
	public String getUsersDn() {
		return usersDn;
	}
	
	public void setUsersDn(String usersDn) {
		this.usersDn = usersDn;
	}
	
	public String getMetisAuthenticationDn() {
		return metisAuthenticationDn;
	}
	
	public void setMetisAuthenticationDn(String metisAuthenticationDn) {
		this.metisAuthenticationDn = metisAuthenticationDn;
	}

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getEmail() {
    	return email;
    }
    
    public void setEmail(String email) {
    	this.email = email;
    }
    
    public String getPassword() {
    	return password;
    }
    
    public void setPassword(String password) {
    	this.password = password;
    }

    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
