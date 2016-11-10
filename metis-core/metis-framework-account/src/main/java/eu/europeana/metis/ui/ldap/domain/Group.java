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

/**
 * Ldap Group representaion
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
    
    public void addMember(String member) {
    	for (String memb: this.members) {
    		if (memb.equals(member)) {
    			return;
    		}
    	}
    	this.members.add(member);
    }
    
    public void removeMember(String member) {
    	this.members.remove(member);
    }
    
    public void updateMember(String oldMember, String newMember) {
    	if (oldMember == null || newMember == null || oldMember.equals(newMember)) {
    		return;
    	}
    	removeMember(oldMember);
    	addMember(newMember);
    }
    
    public LdapName getDn() {
        return dn;
    }

    public void setDn(LdapName dn) {
        this.dn = dn;
    }
}
