package eu.europeana.metis.ui.mongo.domain;

import eu.europeana.metis.ui.ldap.domain.User;

/**
 * A Data wrapper object merging an LDAP user with a Mongo user
 * Created by ymamakis on 11/24/16.
 */
public class UserDTO {
    /**
     * The LDAP user
     */
    private User user;

    /**
     * The MongoDB user
     */
    private DBUser dbUser;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public DBUser getDbUser() {
        return dbUser;
    }

    public void setDbUser(DBUser dbUser) {
        this.dbUser = dbUser;
    }
}
