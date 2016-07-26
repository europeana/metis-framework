package eu.europeana.enrichment.gui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The Input value data transformation object
 * @author Yorgos.Mamakis@ europeana.eu
 *
 */
public class InputValueDTO implements IsSerializable{

	private String originalField;
	
	private String vocabulary;
	
	private String value;

	private String language;
	
	/**
	 * The (optional) metadata field name that generated the enrichment
	 * @return
	 */
	public String getOriginalField() {
		return originalField;
	}

	public void setOriginalField(String originalField) {
		this.originalField = originalField;
	}

	/**
	 * The vocabulary to use to generate the enrichment
	 * @return
	 */
	public String getVocabulary() {
		return vocabulary;
	}

	public void setVocabulary(String vocabulary) {
		this.vocabulary = vocabulary;
	}

	/**
	 * The value to enrich
	 * @return
	 */
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}


	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
}
