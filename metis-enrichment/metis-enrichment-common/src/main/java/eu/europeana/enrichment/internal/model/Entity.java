package eu.europeana.enrichment.internal.model;

import java.util.List;
import java.util.Map;

/**
 * Interface representing entity classes (Agent, Place, Timespan, Concept)
 *
 * @author Simon Tzanakis
 * @since 2020-08-31
 */
public interface Entity {

  /**
   * Retrieves the Preferable Label for an entity (language,value) format
   *
   * @return A Map<String,List<List<String>>> for the Preferable Labels of a contextual class (one
   * per language)
   */
  Map<String, List<String>> getPrefLabel();

  /**
   * Retrieves the Alternative Label for an entity (language,value) format
   *
   * @return A Map<String,List<List<String>>> for the Alternative Labels of a contextual class (one
   * per language)
   */
  Map<String, List<String>> getAltLabel();

  /**
   * Retrieves the skos:note fields of an entity
   *
   * @return A string array with notes for the entity
   */
  Map<String, List<String>> getNote();

  /**
   * Set the altLabel for an entity
   *
   * @param altLabel A Map<String,List<List<String>>> for the Alternative Labels of an entity (one
   * per language)
   */
  void setAltLabel(Map<String, List<String>> altLabel);

  /**
   * Set the notes for an entity
   *
   * @param note A String array with notes for the entity
   */
  void setNote(Map<String, List<String>> note);

  /**
   * Set the prefLabel for an entity
   *
   * @param prefLabel A Map<String,List<List<String>>> for the Preferable Labels of an entity (one
   * per language)
   */
  void setPrefLabel(Map<String, List<String>> prefLabel);

  /**
   * sets the skos:hiddenLabel for an entity
   */
  void setHiddenLabel(Map<String, List<String>> hiddenLabel);

  /**
   * @return the skos:hiddenLabel for the entity
   */
  Map<String, List<String>> getHiddenLabel();

  /**
   * sets the foaf:depiction for entity
   */
  void setFoafDepiction(String foafDepiction);


  /**
   * @return the owl:sameAs for the entity
   */
  List<String> getOwlSameAs();

  /**
   * sets the owl:sameAs for entity
   * @param owlSameAs the owl:sameAs
   */
  void setOwlSameAs(List<String> owlSameAs);


  /**
   * @return the dcterms:isPartOf
   */
  String getIsPartOf();

  /**
   * Sets the dcterms:isPartOf
   * @param isPartOf the dcterms:isPartOf
   */
  void setIsPartOf(String isPartOf);

  /**
   * @return the foaf:depiction
   */
  String getFoafDepiction();

  /**
   * @return the identifier part (last part) of the getAbout url
   */
  String getEntityIdentifier();

}
