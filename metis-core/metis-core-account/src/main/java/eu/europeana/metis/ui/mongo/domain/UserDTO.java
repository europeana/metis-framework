package eu.europeana.metis.ui.mongo.domain;

import eu.europeana.metis.ui.ldap.domain.LdapUser;

/**
 * A Data wrapper object merging an LDAP user with a Mongo user
 * Created by ymamakis on 11/24/16.
 */
public class UserDTO {
    /**
     * The LDAP user
     */
    private LdapUser ldapUser;

    /**
     * The MongoDB user
     */
    private User user;

    public boolean notNullUser()
    {
        return ldapUser != null && user != null;
    }

    public LdapUser getLdapUser() {
        return ldapUser;
    }

    public void setLdapUser(LdapUser ldapUser) {
        this.ldapUser = ldapUser;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
