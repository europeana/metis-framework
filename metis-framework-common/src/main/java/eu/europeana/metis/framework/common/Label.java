package eu.europeana.metis.framework.common;

/**
 * Created by ymamakis on 4/4/16.
 */
public class Label<T> {
    private String lang;
    private T label;

    public String getLang() {
        return lang;
    }

    public T getLabel() {
        return label;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public void setLabel(T label) {
        this.label = label;
    }
}
