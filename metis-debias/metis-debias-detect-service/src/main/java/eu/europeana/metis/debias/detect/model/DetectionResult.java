package eu.europeana.metis.debias.detect.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * The type Detection result.
 */
public class DetectionResult {

  private Metadata metadata;
  @JsonProperty("results")
  private List<ValueDetection> detections;

  /**
   * Gets metadata.
   *
   * @return the metadata
   */
  public Metadata getMetadata() {
    return metadata;
  }

  /**
   * Sets metadata.
   *
   * @param metadata the metadata
   */
  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }

  /**
   * Gets detections.
   *
   * @return the detections
   */
  public List<ValueDetection> getDetections() {
    return detections;
  }

  /**
   * Sets detections.
   *
   * @param detections the detections
   */
  public void setDetections(List<ValueDetection> detections) {
    this.detections = detections;
  }
}
