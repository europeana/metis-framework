package eu.europeana.normalization.dates;

import eu.europeana.normalization.dates.edtf.TemporalEntity;

/**
 * a data class that contains a normalised date value. It contains an EDTF date plus a label.
 */
public class EdmTemporalEntity {

  protected String label;
  protected TemporalEntity edtf;

  public EdmTemporalEntity() {
  }

  public EdmTemporalEntity(String label, TemporalEntity edtf) {
    super();
    this.label = label;
    this.edtf = edtf;
  }

  public EdmTemporalEntity(TemporalEntity edtf) {
    this(null, edtf);
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public TemporalEntity getEdtf() {
    return edtf;
  }

  public void setEdtf(TemporalEntity edtf) {
    this.edtf = edtf;
  }

  public EdmTemporalEntity copy() {
    return new EdmTemporalEntity(label, edtf.copy());
  }

}
