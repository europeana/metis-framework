package eu.europeana.enrichment.api.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.europeana.enrichment.utils.InputValue;
import io.swagger.annotations.ApiModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * JSON helper class for InputValue class
 * 
 * @author Yorgos.Mamakis@ europeana.eu
 *
 */
@JsonSerialize
@JsonRootName(value = InputValueList.API_NAME)
@XmlRootElement(name = InputValueList.API_NAME)
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel(value = InputValueList.API_NAME)
public class InputValueList {

  static final String API_NAME = "inputValueList";

  @XmlElement(name = "inputValue")
  @JsonProperty("inputValue")
  private List<InputValue> inputValues;

  public InputValueList() {
    // Required for XML mapping.
  }

  public List<InputValue> getInputValues() {
    return inputValues == null ? null : Collections.unmodifiableList(inputValues);
  }

  public void setInputValues(List<InputValue> inputValues) {
    this.inputValues = inputValues == null ? null : new ArrayList<>(inputValues);
  }
}
