package eu.europeana.metis.ui.mongo.domain;

import eu.europeana.metis.core.common.OrganizationRole;

/**
 * Roles for Europeana user management
 * Created by ymamakis on 24-1-17.
 */
public enum Role {

    DATA_PROVIDER_CONTACT("Data Provider Contact", "hub_contact"){
        @Override
        public boolean isAssignableTo(OrganizationRole role) {
            return OrganizationRole.CONTENT_PROVIDER==role;
        }
    },
    PROVIDER_VIEWER("Provider Viewer", "hub_viewer"){
        @Override
        public boolean isAssignableTo(OrganizationRole role) {
            return OrganizationRole.DATA_AGGREGATOR==role|| OrganizationRole.DIRECT_PROVIDER==role;
        }
    },
    PROVIDER_DATA_OFFICER("Provider Data Officer", "hub_data_officer"){
        @Override
        public boolean isAssignableTo(OrganizationRole role) {
            return OrganizationRole.DATA_AGGREGATOR==role|| OrganizationRole.DIRECT_PROVIDER==role;
        }
    },
    PROVIDER_ADMIN("Provider Admin", "hub_admin"){
        @Override
        public boolean isAssignableTo(OrganizationRole role) {
            return OrganizationRole.DATA_AGGREGATOR==role|| OrganizationRole.DIRECT_PROVIDER==role;
        }
    },
    EUROPEANA_VIEWER("Europeana Viewer", "europeana_viewer"){
        @Override
        public boolean isAssignableTo(OrganizationRole role) {
            return OrganizationRole.EUROPEANA==role;
        }
    },
    EUROPEANA_DATA_OFFICER("Europeana Data Officer", "europeana_data_officer"){
        @Override
        public boolean isAssignableTo(OrganizationRole role) {
            return OrganizationRole.DATA_AGGREGATOR==role||
                OrganizationRole.DIRECT_PROVIDER==role;
        }
    },
    EUROPEANA_ADMIN("Europeana Admin", "europeana_admin"){
        @Override
        public boolean isAssignableTo(OrganizationRole role) {
            return OrganizationRole.DATA_AGGREGATOR==role|| OrganizationRole.DIRECT_PROVIDER==role;
        }
    },
    LEMMY("Europeana Super Admin", "lemmy"){
        @Override
        public boolean isAssignableTo(OrganizationRole role) {
            return OrganizationRole.DATA_AGGREGATOR==role|| OrganizationRole.DIRECT_PROVIDER==role;
        }
    };

    private String name;
    
    private String ldapName;

    Role(String name, String ldapName) {
        this.name = name;
        this.ldapName = ldapName;
    }

    public String getName(){
        return this.name;
    }

    public abstract boolean isAssignableTo(OrganizationRole role);

	public String getLdapName() {
		return ldapName;
	}
}
