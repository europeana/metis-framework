/* LanguageName.java - created on 16/03/2016, Copyright (c) 2011 The European Library, all rights reserved */
package eu.europeana.normalization.language.nal;

/**
 * A label assigned to a language represented in NAL
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 16/03/2016
 */
public class Label {
    /** String label */
    protected String label;
    /** String language */
    protected String language;
    /** String script */
    protected String script;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    /**
     * Creates a new instance of this class.
     * 
     * @param label
     */
    public Label(String label) {
        super();
        this.label = label;
    }

    /**
     * Creates a new instance of this class.
     * 
     * @param label
     * @param language
     */
    public Label(String label, String language) {
        super();
        this.label = label;
        this.language = language;
    }

    /**
     * Creates a new instance of this class.
     * 
     * @param label
     * @param language
     * @param script
     */
    public Label(String label, String language, String script) {
        super();
        this.label = label;
        this.language = language;
        this.script = script;
    }

    @Override
    public String toString() {
        return "Label [label=" + label + ", language=" + language + ", script=" + script + "]";
    }

}
