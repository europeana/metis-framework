package eu.europeana.normalization.dates.edtf;

/**
 * Contains a normalised date value.
 * <p>
 * It contains an EDTF date plus a label if applicable.
 * </p>
 */
public class EdtfDateWithLabel {

  protected String label;
  protected AbstractEdtfDate edtf;

  public EdtfDateWithLabel() {
  }

  public EdtfDateWithLabel(String label, AbstractEdtfDate edtf) {
    super();
    this.label = label;
    this.edtf = edtf;
  }

  public EdtfDateWithLabel(AbstractEdtfDate edtf) {
    this(null, edtf);
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public AbstractEdtfDate getEdtf() {
    return edtf;
  }

  public void setEdtf(AbstractEdtfDate edtf) {
    this.edtf = edtf;
  }

  public EdtfDateWithLabel copy() {
    return new EdtfDateWithLabel(label, edtf.copy());
  }

}
