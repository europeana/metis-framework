package eu.europeana.enrichment.api.external;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Convenience class for JSON and XML generation
 * 
 * @author Yorgos.Mamakis@ europeana.eu
 * 
 */
@XmlRootElement
@JsonSerialize
public class EntityWrapperList {

	@XmlElement(name = "entities")
	private List<EntityWrapper> wrapperList;

	public EntityWrapperList() {
	}

	public List<EntityWrapper> getWrapperList() {
		return wrapperList;
	}

	public void setWrapperList(List<EntityWrapper> wrapperList) {
		this.wrapperList = wrapperList;
	}

}
