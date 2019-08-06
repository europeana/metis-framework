package eu.europeana.indexing.solr.facet.value;

/**
 * This interface is implemented by all facet values with an encoding.
 */
public interface FacetValue {

  /**
   * Codify the facet value.
   *
   * @return The (non-shifted) value.
   */
  int getCode();

}
