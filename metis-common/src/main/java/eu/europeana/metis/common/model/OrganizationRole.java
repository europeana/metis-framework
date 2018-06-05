package eu.europeana.metis.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * A role of a supplying institution to Europeana
 * Created by ymamakis on 4/4/16.
 */
public enum OrganizationRole {

    CONTENT_PROVIDER("Content Provider"),DIRECT_PROVIDER("Direct Provider"), AGGREGATOR("Aggregator"),
    FINANCIAL_PARTNER("Financial Partner"),TECHNOLOGY_PARTNER("Technology Partner"),POLICY_MAKER("Policy Maker"),
    CONSULTANT("Consultant"),OTHER("Other"),EUROPEANA("Europeana");

    private String name;

    OrganizationRole(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    /**
     * Do a lookup of {@link OrganizationRole} enum field based on the {@link #name} parameter
     * @param name the name parameter inside the enum
     * @return the corresponding {@link OrganizationRole}
     */
    public static OrganizationRole getRoleFromName(String name){
        for (OrganizationRole role: OrganizationRole.values()) {
            if(role.getName().equalsIgnoreCase(name)){
                return role;
            }
        }
        return null;
    }

    /**
     * Do a lookup of {@link OrganizationRole} enum field based on the {@link #name()} function.
     * <p>{@link #name()} is the string representation of the enum field name</p>
     * @param name the string representation of the enum field name
     * @return the corresponding {@link OrganizationRole}
     */
    @JsonCreator
    public static OrganizationRole getRoleFromEnumName(String name){
        for (OrganizationRole role: OrganizationRole.values()) {
            if(role.name().equalsIgnoreCase(name)){
                return role;
            }
        }
        return null;
    }
}
