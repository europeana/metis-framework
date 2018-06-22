package eu.europeana.indexing.fullbean;

import java.util.function.Function;
import eu.europeana.corelib.definitions.jibx.PlaceType;
import eu.europeana.corelib.solr.entity.PlaceImpl;

/**
 * Converts a {@link PlaceType} from an {@link eu.europeana.corelib.definitions.jibx.RDF} to a
 * {@link PlaceImpl} for a {@link eu.europeana.corelib.definitions.edm.beans.FullBean}.
 */
final class PlaceFieldInput implements Function<PlaceType, PlaceImpl> {

  @Override
  public PlaceImpl apply(PlaceType placeType) {
    PlaceImpl place = new PlaceImpl();
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
