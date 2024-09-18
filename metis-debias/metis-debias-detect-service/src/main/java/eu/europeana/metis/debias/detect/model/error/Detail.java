package eu.europeana.metis.debias.detect.model.error;

import java.util.List;

/**
 * The type Detail.
 */
public class Detail {
  private String type;
  private List<String> loc;
  private String msg;
  private Input input;
  private String url;

  /**
   * Gets type.
   *
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * Sets type.
   *
   * @param type the type
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Gets loc.
   *
   * @return the loc
   */
  public List<String> getLoc() {
    return loc;
  }

  /**
   * Sets loc.
   *
   * @param loc the loc
   */
  public void setLoc(List<String> loc) {
    this.loc = loc;
  }

  /**
   * Gets msg.
   *
   * @return the msg
   */
  public String getMsg() {
    return msg;
  }

  /**
   * Sets msg.
   *
   * @param msg the msg
   */
  public void setMsg(String msg) {
    this.msg = msg;
  }

  /**
   * Gets input.
   *
   * @return the input
   */
  public Input getInput() {
    return input;
  }

  /**
   * Sets input.
   *
   * @param input the input
   */
  public void setInput(Input input) {
    this.input = input;
  }

  /**
   * Gets url.
   *
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * Sets url.
   *
   * @param url the url
   */
  public void setUrl(String url) {
    this.url = url;
  }
}
