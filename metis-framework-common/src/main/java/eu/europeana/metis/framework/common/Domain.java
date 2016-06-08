package eu.europeana.metis.framework.common;

/**
 * Created by ymamakis on 4/4/16.
 */
public enum Domain {
    GALLERY("Gallery"), LIBRARY("Library"), ARCHIVE("Archive"), MUSEUM("Museum"), AV_ARCHIVE("AV/Sound archive"),
    PUBLISHER("Publisher"), RESEARCH("Research (eg university)"), EDUCATION("Education (eg school)"),
    CREATIVE_INDUSTRY("Creative Industry"), CROSS_DOMAIN("Cross domain"), PERFORMING_ARTS("Performing Arts"),
    CONSULTANT("Consultant"), OTHER("Other");

    private String name;
    Domain(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public static Domain getDomainFromName(String name){
        for (Domain domain:Domain.values()) {
            if(domain.getName().equals(name)){
                return domain;
            }
        }
        return null;
    }
}
