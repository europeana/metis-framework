package eu.europeana.enrichment.utils;

import java.util.Arrays;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Enrichment input class wrapper. It defines the basics needed for enrichment
 * as the value to be enriched, the Controlled vocabulary to be used and the
 * field (optional) from which the value originated
 * 
 * @author Yorgos.Mamakis@ europeana.eu
 * 
 */
@XmlRootElement
@JsonInclude(Include.ALWAYS)
public class InputValue {

	private String originalField;

	private String value;

	private String language;

	private List<EntityClass> vocabularies;

	public InputValue() {
	}

	public InputValue(String originalField, String value, String language, EntityClass... vocabularies) {
	    this.originalField = originalField;
	    this.value = value;
	    this.language = language;
	    this.vocabularies = Arrays.asList(vocabularies);
	}

	public String getOriginalField() {
		return originalField;
	}

	public void setOriginalField(String originalField) {
		this.originalField = originalField;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public List<EntityClass> getVocabularies() {
		return vocabularies;
	}

	public void setVocabularies(List<EntityClass> vocabularies) {
		this.vocabularies = vocabularies;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
}
