/*
 * Copyright 2007-2012 The Europeana Foundation
 *
 * Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved by the
 * European Commission; You may not use this work except in compliance with the Licence.
 * 
 * You may obtain a copy of the Licence at: http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, without warranties or conditions of any kind, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package eu.europeana.indexing.fullbean;

import eu.europeana.corelib.definitions.jibx.PlaceType;
import eu.europeana.corelib.solr.entity.PlaceImpl;

/**
 * Constructor of a Place EDM Entity
 * 
 * @author Yorgos.Mamakis@ kb.nl
 */
final class PlaceFieldInput {

  /**
   * Create a new Place Mongo Entity from a JiBX palce
   * 
   * @param placeType The JiBX Place Entity
   * @return A new Mongo Place Entity
   */
  PlaceImpl createNewPlace(PlaceType placeType) {
    PlaceImpl place = new PlaceImpl();
    // place.setId(new ObjectId());
    place.setAbout(placeType.getAbout());
    if (placeType.getLat() != null) {
      place.setLatitude(placeType.getLat().getLat());
    }
    if (placeType.getLong() != null) {
      place.setLongitude(placeType.getLong().getLong());
    }
    place.setNote(FieldInputUtils.createLiteralMapFromList(placeType.getNoteList()));
    place.setPrefLabel(FieldInputUtils.createLiteralMapFromList(placeType.getPrefLabelList()));
    place.setAltLabel(FieldInputUtils.createLiteralMapFromList(placeType.getAltLabelList()));

    place.setIsPartOf(
        FieldInputUtils.createResourceOrLiteralMapFromList(placeType.getIsPartOfList()));
    if (placeType.getAlt() != null) {
      place.setAltitude(placeType.getAlt().getAlt());
    }
    place.setDcTermsHasPart(
        FieldInputUtils.createResourceOrLiteralMapFromList(placeType.getHasPartList()));
    place.setOwlSameAs(FieldInputUtils.resourceListToArray(placeType.getSameAList()));
    return place;
  }

}
