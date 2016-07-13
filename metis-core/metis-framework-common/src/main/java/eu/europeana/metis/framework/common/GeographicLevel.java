package eu.europeana.metis.framework.common;

/**
 * Created by ymamakis on 4/4/16.
 */
public enum GeographicLevel {
    REGIONAL("Regional"),MUNICIPAL("Municipal"),NATIONAL("National"),EUROPEAN("European"),WORLDWIDE("Worldwide");

    private String name;
    GeographicLevel(String name){
        this.name =name;
    }

    public String getName(){
        return this.name;
    }

    public static GeographicLevel getGeographicLevelFromName(String name){
        for (GeographicLevel gl:GeographicLevel.values()) {
            if(gl.getName().equals(name)){
                return gl;
            }
        }
        return null;
    }
}
