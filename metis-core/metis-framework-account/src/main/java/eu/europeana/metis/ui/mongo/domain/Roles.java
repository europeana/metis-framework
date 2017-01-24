package eu.europeana.metis.ui.mongo.domain;

import eu.europeana.metis.framework.common.Role;

/**
 * Roles for Europeana user management
 * Created by ymamakis on 24-1-17.
 */
public enum Roles {

    DATA_PROVIDER_CONTACT("Data Provider Contact"){
        @Override
        public boolean isAssignableTo(Role role) {
            return Role.CONTENT_PROVIDER==role;
        }
    },
    PROVIDER_VIEWER("Provider Viewer"){
        @Override
        public boolean isAssignableTo(Role role) {
            return Role.DATA_AGGREGATOR==role||Role.DIRECT_PROVIDER==role;
        }
    },
    PROVIDER_DATA_OFFICER("Provider Data Officer"){
        @Override
        public boolean isAssignableTo(Role role) {
            return Role.DATA_AGGREGATOR==role||Role.DIRECT_PROVIDER==role;
        }
    },
    PROVIDER_ADMIN("Provider Admin"){
        @Override
        public boolean isAssignableTo(Role role) {
            return Role.DATA_AGGREGATOR==role||Role.DIRECT_PROVIDER==role;
        }
    },
    EUROPEANA_VIEWER("Europeana Viewer"){
        @Override
        public boolean isAssignableTo(Role role) {
            return Role.EUROPEANA==role;
        }
    },
    EUROPEANA_DATA_OFFICER("Europeana Data Officer"){
        @Override
        public boolean isAssignableTo(Role role) {
            return Role.DATA_AGGREGATOR==role||Role.DIRECT_PROVIDER==role;
        }
    },
    EUROPEANA_ADMIN("Europeana Admin"){
        @Override
        public boolean isAssignableTo(Role role) {
            return Role.DATA_AGGREGATOR==role||Role.DIRECT_PROVIDER==role;
        }
    },
    LEMMY("Europeana Super Admin"){
        @Override
        public boolean isAssignableTo(Role role) {
            return Role.DATA_AGGREGATOR==role||Role.DIRECT_PROVIDER==role;
        }
    };

    private String name;

    Roles(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public abstract boolean isAssignableTo(Role role);
}
