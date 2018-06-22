package eu.europeana.indexing.fullbean;

import java.util.function.Function;
import eu.europeana.corelib.definitions.jibx.Concept;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.utils.StringArrayUtils;

/**
 * Converts a {@link Concept} from an {@link eu.europeana.corelib.definitions.jibx.RDF} to a
 * {@link ConceptImpl} for a {@link eu.europeana.corelib.definitions.edm.beans.FullBean}.
 */
final class ConceptFieldInput implements Function<Concept, ConceptImpl> {

  @Override
  public ConceptImpl apply(Concept concept) {
    ConceptImpl conceptMongo = new ConceptImpl();
    conceptMongo.setAbout(concept.getAbout());
    if (concept.getChoiceList() != null) {
      for (Concept.Choice choice : concept.getChoiceList()) {
        applyChoice(choice, conceptMongo);
      }
    }
    return conceptMongo;
  }

  private static void applyChoice(Concept.Choice choice, ConceptImpl conceptMongo) {
    if (choice.ifNote()) {
      conceptMongo.setNote(FieldInputUtils.mergeMaps(conceptMongo.getNote(),
          FieldInputUtils.createLiteralMapFromString(choice.getNote())));
    }
    if (choice.ifBroader()) {
      conceptMongo.setBroader(StringArrayUtils.addToArray(conceptMongo.getBroader(),
          FieldInputUtils.getResourceString(choice.getBroader())));
    }
    if (choice.ifBroadMatch()) {
      conceptMongo.setBroadMatch(StringArrayUtils.addToArray(conceptMongo.getBroadMatch(),
          FieldInputUtils.getResourceString(choice.getBroadMatch())));
    }
    if (choice.ifCloseMatch()) {
      conceptMongo.setCloseMatch(StringArrayUtils.addToArray(conceptMongo.getCloseMatch(),
          FieldInputUtils.getResourceString(choice.getCloseMatch())));
    }
    if (choice.ifExactMatch()) {
      conceptMongo.setExactMatch(StringArrayUtils.addToArray(conceptMongo.getExactMatch(),
          FieldInputUtils.getResourceString(choice.getExactMatch())));
    }
    if (choice.ifNarrower()) {
      conceptMongo.setNarrower(StringArrayUtils.addToArray(conceptMongo.getNarrower(),
          FieldInputUtils.getResourceString(choice.getNarrower())));
    }
    if (choice.ifNarrowMatch()) {
      conceptMongo.setNarrowMatch(StringArrayUtils.addToArray(conceptMongo.getNarrowMatch(),
          FieldInputUtils.getResourceString(choice.getNarrowMatch())));
    }
    if (choice.ifNotation()) {
      conceptMongo.setNotation(FieldInputUtils.mergeMaps(conceptMongo.getNotation(),
          FieldInputUtils.createLiteralMapFromString(choice.getNotation())));
    }
    if (choice.ifRelated()) {
      conceptMongo.setRelated(StringArrayUtils.addToArray(conceptMongo.getRelated(),
          FieldInputUtils.getResourceString(choice.getRelated())));
    }
    if (choice.ifRelatedMatch()) {
      conceptMongo.setCloseMatch(StringArrayUtils.addToArray(conceptMongo.getRelatedMatch(),
          FieldInputUtils.getResourceString(choice.getRelatedMatch())));
    }
    if (choice.ifPrefLabel()) {
      conceptMongo.setPrefLabel(FieldInputUtils.mergeMaps(conceptMongo.getPrefLabel(),
          FieldInputUtils.createLiteralMapFromString(choice.getPrefLabel())));
    }
    if (choice.ifAltLabel()) {
      conceptMongo.setAltLabel(FieldInputUtils.mergeMaps(conceptMongo.getAltLabel(),
          FieldInputUtils.createLiteralMapFromString(choice.getAltLabel())));
    }
  }
}
