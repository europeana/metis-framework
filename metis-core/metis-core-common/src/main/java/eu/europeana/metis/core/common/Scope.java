package eu.europeana.metis.core.common;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Created by ymamakis on 11/11/16.
 */
public enum Scope {

    OTHER_NONE("other/none"),CROSS("Cross"),SINGLE("Single"),THEMATIC("Thematic"),INDIVIDUAL("Individual");

    private String scope;
    Scope(String scope){
       this.scope =scope;
    }

    public String getScope() {
        return scope;
    }

    @JsonCreator
    public static Scope getScopeFromEnumName(String name){
        for (Scope scope:Scope.values()) {
            if(scope.name().equalsIgnoreCase(name)){
                return scope;
            }
        }
        return null;
    }


}
