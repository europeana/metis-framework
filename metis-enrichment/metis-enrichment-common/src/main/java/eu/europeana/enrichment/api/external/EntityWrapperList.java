package eu.europeana.enrichment.api.external;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

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
