package eu.europeana.enrichment.api.external;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * JSON helper class for InputValue class
 * @author Yorgos.Mamakis@ europeana.eu
 *
 */
@JsonSerialize
@XmlRootElement
public class InputValueList {
	private List<InputValue> inputValueList;

	public List<InputValue> getInputValueList() {
		return inputValueList;
	}

	public void setInputValueList(List<InputValue> inputValueList) {
		this.inputValueList = inputValueList;
	}
	
}
