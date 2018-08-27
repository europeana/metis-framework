package eu.europeana.indexing.solr.property;

import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.Place;
import eu.europeana.indexing.solr.EdmLabel;

/**
 * Property Solr Creator for 'edm:Place' tags.
 *
 * @author Yorgos.Mamakis@ europeana.eu
 *
 */
public class PlaceSolrCreator implements PropertySolrCreator<Place> {

  @Override
  public void addToDocument(SolrInputDocument doc, Place place) {

    SolrPropertyUtils.addValue(doc, EdmLabel.EDM_PLACE, place.getAbout());
    SolrPropertyUtils.addValues(doc, EdmLabel.PL_SKOS_PREF_LABEL, place.getPrefLabel());
    SolrPropertyUtils.addValues(doc, EdmLabel.PL_SKOS_ALT_LABEL, place.getAltLabel());

    if (place.getLatitude() != null && place.getLatitude() != 0) {
      SolrPropertyUtils.addValue(doc, EdmLabel.PL_WGS84_POS_LAT, place.getLatitude());
    }
    if (place.getLongitude() != null && place.getLongitude() != 0) {
      SolrPropertyUtils.addValue(doc, EdmLabel.PL_WGS84_POS_LONG, place.getLongitude());
    }
    if (place.getAltitude() != null && place.getAltitude() != 0) {
      SolrPropertyUtils.addValue(doc, EdmLabel.PL_WGS84_POS_ALT, place.getAltitude());
    }
  }
}
