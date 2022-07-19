package eu.europeana.normalization.dates;

import eu.europeana.normalization.dates.edtf.AbstractEDTFDate;

/**
 * a data class that contains a normalised date value. It contains an EDTF date plus a label.
 */
public class EdmTemporalEntity {

  protected String label;
  protected AbstractEDTFDate edtf;

  public EdmTemporalEntity() {
  }

  public EdmTemporalEntity(String label, AbstractEDTFDate edtf) {
    super();
    this.label = label;
    this.edtf = edtf;
  }

  public EdmTemporalEntity(AbstractEDTFDate edtf) {
    this(null, edtf);
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public AbstractEDTFDate getEdtf() {
    return edtf;
  }

  public void setEdtf(AbstractEDTFDate edtf) {
    this.edtf = edtf;
  }

  public EdmTemporalEntity copy() {
    return new EdmTemporalEntity(label, edtf.copy());
  }

}
