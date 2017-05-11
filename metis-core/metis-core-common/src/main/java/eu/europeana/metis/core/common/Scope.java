package eu.europeana.metis.core.common;

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
}
