package eu.europeana.enrichment.gui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Enrichment wrapper data transformation object
 * @author Yorgos.Mamakis@ europeana.eu
 *
 */
public class EntityWrapperDTO implements IsSerializable {
	private String originalField;
	private String className;
	private String contextualEntity;
	/**
	 * The original field that generated the enrichment
	 * @return
	 */
	public String getOriginalField() {
		return originalField;
	}
	public void setOriginalField(String originalField) {
		this.originalField = originalField;
	}
	
	/**
	 * The class type of the enrichment
	 * @return
	 */
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	
	/**
	 * The actual value of the enrichment
	 * @return
	 */
	public String getContextualEntity() {
		return contextualEntity;
	}
	public void setContextualEntity(String contextualEntity) {
		this.contextualEntity = contextualEntity;
	}
}
