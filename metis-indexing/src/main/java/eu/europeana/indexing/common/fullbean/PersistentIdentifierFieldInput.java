package eu.europeana.indexing.common.fullbean;


import static eu.europeana.indexing.common.fullbean.FieldInputUtils.createResourceOrLiteralMapSingleFromString;
import static eu.europeana.indexing.common.fullbean.FieldInputUtils.getLiteralValueString;
import static eu.europeana.indexing.common.fullbean.FieldInputUtils.getResourceOrLiteralValueString;
import static eu.europeana.indexing.common.fullbean.FieldInputUtils.getResourceString;
import static eu.europeana.indexing.common.fullbean.FieldInputUtils.literalListToArray;
import static eu.europeana.indexing.common.fullbean.FieldInputUtils.resourceListToArray;

import eu.europeana.corelib.solr.entity.PersistentIdentifierImpl;
import eu.europeana.metis.schema.jibx.PersistentIdentifierType;
import java.util.List;
import java.util.function.Function;

/**
 * The type Persistent identifier field input.
 */
public class PersistentIdentifierFieldInput implements Function<PersistentIdentifierType, PersistentIdentifierImpl> {

  /**
   * Applies this function to the given argument.
   *
   * @param persistentIdentifierType the function argument
   * @return the function result
   */
  @Override
  public PersistentIdentifierImpl apply(PersistentIdentifierType persistentIdentifierType) {
    PersistentIdentifierImpl persistentIdentifier = new PersistentIdentifierImpl();
    persistentIdentifier.setAbout(persistentIdentifierType.getAbout());
    persistentIdentifier.setValue(getLiteralValueString(persistentIdentifierType.getValue()));
    persistentIdentifier.setCreator(createResourceOrLiteralMapSingleFromString(persistentIdentifierType.getCreator()));
    persistentIdentifier.setCreated(getResourceOrLiteralValueString(persistentIdentifierType.getCreated()));
    persistentIdentifier.setNotation(List.of(literalListToArray(persistentIdentifierType.getNotationList())));
    persistentIdentifier.setHasPolicy(getResourceString(persistentIdentifierType.getHasPolicy()));
    persistentIdentifier.setHasURL(List.of(resourceListToArray(persistentIdentifierType.getHasURLList())));
    persistentIdentifier.setEquivalentPID(List.of(literalListToArray(persistentIdentifierType.getEquivalentPIDList())));
    persistentIdentifier.setReplacesPID(List.of(literalListToArray(persistentIdentifierType.getReplacesPIDList())));
    persistentIdentifier.setInScheme(getResourceString(persistentIdentifierType.getInScheme()));
    return persistentIdentifier;
  }
}
