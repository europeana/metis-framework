package eu.europeana.metis.ui.mongo.domain;

import eu.europeana.metis.core.common.Role;

/**
 * Roles for Europeana user management
 * Created by ymamakis on 24-1-17.
 */
public enum Roles {

    DATA_PROVIDER_CONTACT("Data Provider Contact", "hub_contact"){
        @Override
        public boolean isAssignableTo(Role role) {
            return Role.CONTENT_PROVIDER==role;
        }
    },
    PROVIDER_VIEWER("Provider Viewer", "hub_viewer"){
        @Override
        public boolean isAssignableTo(Role role) {
            return Role.DATA_AGGREGATOR==role||Role.DIRECT_PROVIDER==role;
        }
    },
    PROVIDER_DATA_OFFICER("Provider Data Officer", "hub_data_officer"){
        @Override
        public boolean isAssignableTo(Role role) {
            return Role.DATA_AGGREGATOR==role||Role.DIRECT_PROVIDER==role;
        }
    },
    PROVIDER_ADMIN("Provider Admin", "hub_admin"){
        @Override
        public boolean isAssignableTo(Role role) {
            return Role.DATA_AGGREGATOR==role||Role.DIRECT_PROVIDER==role;
        }
    },
    EUROPEANA_VIEWER("Europeana Viewer", "europeana_viewer"){
        @Override
        public boolean isAssignableTo(Role role) {
            return Role.EUROPEANA==role;
        }
    },
    EUROPEANA_DATA_OFFICER("Europeana Data Officer", "europeana_data_officer"){
        @Override
        public boolean isAssignableTo(Role role) {
            return Role.DATA_AGGREGATOR==role||Role.DIRECT_PROVIDER==role;
        }
    },
    EUROPEANA_ADMIN("Europeana Admin", "europeana_admin"){
        @Override
        public boolean isAssignableTo(Role role) {
            return Role.DATA_AGGREGATOR==role||Role.DIRECT_PROVIDER==role;
        }
    },
    LEMMY("Europeana Super Admin", "lemmy"){
        @Override
        public boolean isAssignableTo(Role role) {
            return Role.DATA_AGGREGATOR==role||Role.DIRECT_PROVIDER==role;
        }
    };

    private String name;
    
    private String ldapName;

    Roles(String name, String ldapName) {
        this.name = name;
        this.ldapName = ldapName;
    }

    public String getName(){
        return this.name;
    }

    public abstract boolean isAssignableTo(Role role);

	public String getLdapName() {
		return ldapName;
	}
}
