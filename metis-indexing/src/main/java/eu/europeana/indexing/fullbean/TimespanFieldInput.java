package eu.europeana.indexing.fullbean;

import eu.europeana.metis.schema.jibx.IsNextInSequence;
import java.util.Optional;
import java.util.function.Function;
import eu.europeana.metis.schema.jibx.TimeSpanType;
import eu.europeana.corelib.solr.entity.TimespanImpl;

/**
 * Converts a {@link TimeSpanType} from an {@link eu.europeana.metis.schema.jibx.RDF} to a
 * {@link TimespanImpl} for a {@link eu.europeana.metis.schema.edm.beans.FullBean}.
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
    mongoTimespan
        .setHiddenLabel(FieldInputUtils.createLiteralMapFromList(timeSpan.getHiddenLabelList()));
    mongoTimespan.setIsNextInSequence(
        Optional.ofNullable(timeSpan.getIsNextInSequence()).map(IsNextInSequence::getResource)
            .orElse(null));
    return mongoTimespan;
  }
}
