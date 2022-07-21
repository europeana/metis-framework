package eu.europeana.normalization.dates.edtf;

/**
 * Contains a normalised date value.
 * <p>
 * It contains an EDTF date plus a label if applicable.
 * </p>
 */
public class EdtfDateWithLabel {

  protected String label;
  protected AbstractEdtfDate edtfDate;

  public EdtfDateWithLabel() {
  }

  public EdtfDateWithLabel(String label, AbstractEdtfDate edtfDate) {
    super();
    this.label = label;
    this.edtfDate = edtfDate;
  }

  public EdtfDateWithLabel(AbstractEdtfDate edtfDate) {
    this(null, edtfDate);
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public AbstractEdtfDate getEdtfDate() {
    return edtfDate;
  }

  public void setEdtfDate(AbstractEdtfDate edtfDate) {
    this.edtfDate = edtfDate;
  }
}
