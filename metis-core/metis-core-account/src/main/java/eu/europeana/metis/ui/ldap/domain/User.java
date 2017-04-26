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

package eu.europeana.metis.ui.ldap.domain;

import javax.naming.ldap.LdapName;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.security.authentication.encoding.LdapShaPasswordEncoder;

/**
 * LDAP Template: dn:
 * cn=Cecile,ou=users,ou=metis_authentication,{dc=europeana,dc=eu} dn:
 * cn={fullName},ou=users,ou=metis_authentication,{base_dn}
 * 
 * @author alena
 *
 */
@Entry(objectClasses = { "person", "inetOrgPerson", "organizationalPerson", "top", "metisUser" })
public class User {
	@Id
	private LdapName dn;

	@Attribute(name = "givenName")
	// @DnAttribute(value = "cn", index = 2)
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
	private byte[] passwordB;

	private String password;

	@Attribute(name = "description")
	private String description;

	@Attribute(name = "mail")
	@DnAttribute(value = "cn", index = 2)
	private String email;

	@Attribute(name = "Active")
	private boolean active;

	@Attribute(name = "Approved")
	private boolean approved;

	public LdapName getDn() {
		return dn;
	}

	public void setDn(LdapName dn) {
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
		return this.passwordB != null ? new String(this.passwordB) : this.password;
	}

	public void setPassword(String password) {
		if (password != null && !password.isEmpty()) {
			LdapShaPasswordEncoder enc = new LdapShaPasswordEncoder();
			String pass = enc.encodePassword(password, null);
			this.passwordB = pass.getBytes();
			this.password = pass;
		}
	}

	public byte[] getPasswordB() {
		this.password = new String(passwordB);
		return this.passwordB;
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

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isApproved() {
		return approved;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}
}
