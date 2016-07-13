package eu.europeana.metis.framework.common;

/**
 * Created by ymamakis on 4/4/16.
 */
public enum Role {

    CONTENT_PROVIDER("Content provider"),DATA_AGGREGATOR("Data aggregator"),
    FINANCIAL_PARTNER("Financial partner / sponsor"),POLICY_MAKER("Policy maker"),
    THINK_TANK("Think tank / Knowledge organisation / Creative");

    private String name;

    Role(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public static Role getRoleFromName(String name){
        for (Role role:Role.values()) {
            if(role.getName().equals(name)){
                return role;
            }
        }
        return null;
    }
}
