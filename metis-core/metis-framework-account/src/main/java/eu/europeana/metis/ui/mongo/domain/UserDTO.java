package eu.europeana.metis.ui.mongo.domain;

import eu.europeana.metis.ui.ldap.domain.User;

/**
 * Created by ymamakis on 11/24/16.
 */
public class UserDTO {
    private User user;
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
