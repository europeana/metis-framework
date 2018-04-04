package eu.europeana.indexing.solr;

import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.Place;

/**
 * Generate Place SOLR fields from Mongo
 *
 * @author Yorgos.Mamakis@ europeana.eu
 *
 */
public class PlaceSolrCreator extends PropertySolrCreator<Place> {

  @Override
  public void addToDocument(SolrInputDocument doc, Place place) {

    SolrUtils.addValue(doc, EdmLabel.EDM_PLACE, place.getAbout());
    SolrUtils.addValues(doc, EdmLabel.PL_SKOS_PREF_LABEL, place.getPrefLabel());
    SolrUtils.addValues(doc, EdmLabel.PL_SKOS_ALT_LABEL, place.getAltLabel());
    SolrUtils.addValues(doc, EdmLabel.PL_SKOS_NOTE, place.getNote());
    SolrUtils.addValues(doc, EdmLabel.PL_SKOS_HIDDENLABEL, place.getHiddenLabel());
    SolrUtils.addValues(doc, EdmLabel.PL_OWL_SAMEAS, place.getOwlSameAs());
    SolrUtils.addValues(doc, EdmLabel.PL_DCTERMS_HASPART, place.getDcTermsHasPart());
    SolrUtils.addValues(doc, EdmLabel.PL_DCTERMS_ISPART_OF, place.getIsPartOf());

    if (place.getLatitude() != null && place.getLatitude() != 0) {
      SolrUtils.addValue(doc, EdmLabel.PL_WGS84_POS_LAT, place.getLatitude());
    }
    if (place.getLongitude() != null && place.getLongitude() != 0) {
      SolrUtils.addValue(doc, EdmLabel.PL_WGS84_POS_LONG, place.getLongitude());
    }
    if (place.getAltitude() != null && place.getAltitude() != 0) {
      SolrUtils.addValue(doc, EdmLabel.PL_WGS84_POS_ALT, place.getAltitude());
    }
  }
}
