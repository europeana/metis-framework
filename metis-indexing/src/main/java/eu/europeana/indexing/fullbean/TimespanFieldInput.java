package eu.europeana.indexing.fullbean;

import java.util.function.Function;
import eu.europeana.corelib.definitions.jibx.TimeSpanType;
import eu.europeana.corelib.solr.entity.TimespanImpl;

/**
 * Converts a {@link TimeSpanType} from an {@link eu.europeana.corelib.definitions.jibx.RDF} to a
 * {@link TimespanImpl} for a {@link eu.europeana.corelib.definitions.edm.beans.FullBean}.
 */
final class TimespanFieldInput implements Function<TimeSpanType, TimespanImpl> {

  @Override
  public TimespanImpl apply(TimeSpanType timeSpan) {
    TimespanImpl mongoTimespan = new TimespanImpl();
    mongoTimespan.setAbout(timeSpan.getAbout());
    mongoTimespan.setNote(FieldInputUtils.createLiteralMapFromList(timeSpan.getNoteList()));
    mongoTimespan
        .setPrefLabel(FieldInputUtils.createLiteralMapFromList(timeSpan.getPrefLabelList()));
    mongoTimespan.setAltLabel(FieldInputUtils.createLiteralMapFromList(timeSpan.getAltLabelList()));
    mongoTimespan.setIsPartOf(
        FieldInputUtils.createResourceOrLiteralMapFromList(timeSpan.getIsPartOfList()));
    mongoTimespan.setDctermsHasPart(
        FieldInputUtils.createResourceOrLiteralMapFromList(timeSpan.getHasPartList()));
    mongoTimespan.setOwlSameAs(FieldInputUtils.resourceListToArray(timeSpan.getSameAList()));
    mongoTimespan.setBegin(FieldInputUtils.createLiteralMapFromString(timeSpan.getBegin()));
    mongoTimespan.setEnd(FieldInputUtils.createLiteralMapFromString(timeSpan.getEnd()));
    return mongoTimespan;
  }
}
