package eu.europeana.metis.dereference.vocimport.model;

/**
 * This class represents an entry in the vocabulary directory. It is meant to reflect the structure
 * of the source of the data and changes should therefore not be made to this class without also
 * changing the format of the source.
 */
public class VocabularyDirectoryEntry {

  private String metadata;
  private String mapping;

  public String getMetadata() {
    return metadata;
  }

  public String getMapping() {
    return mapping;
  }

  void setMetadata(String metadata) {
    this.metadata = metadata;
  }

  void setMapping(String mapping) {
    this.mapping = mapping;
  }
}
