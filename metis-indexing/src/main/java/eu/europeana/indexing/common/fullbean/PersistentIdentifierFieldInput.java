package eu.europeana.indexing.common.fullbean;


import static eu.europeana.indexing.common.fullbean.FieldInputUtils.createResourceOrLiteralMapSingleFromString;
import static eu.europeana.indexing.common.fullbean.FieldInputUtils.createStringListFromTypeList;
import static eu.europeana.indexing.common.fullbean.FieldInputUtils.getLiteralValueString;
import static eu.europeana.indexing.common.fullbean.FieldInputUtils.getResourceOrLiteralValueString;
import static eu.europeana.indexing.common.fullbean.FieldInputUtils.getResourceString;

import eu.europeana.corelib.solr.entity.PersistentIdentifierImpl;
import eu.europeana.metis.schema.jibx.LiteralType;
import eu.europeana.metis.schema.jibx.PersistentIdentifierType;
import eu.europeana.metis.schema.jibx.ResourceType;
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
    persistentIdentifier.setNotation(createStringListFromTypeList(persistentIdentifierType.getNotationList(), LiteralType::getString));
    persistentIdentifier.setHasPolicy(getResourceString(persistentIdentifierType.getHasPolicy()));
    persistentIdentifier.setHasURL(createStringListFromTypeList(persistentIdentifierType.getHasURLList(), ResourceType::getResource));
    persistentIdentifier.setEquivalentPID(createStringListFromTypeList(persistentIdentifierType.getEquivalentPIDList(), LiteralType::getString));
    persistentIdentifier.setReplacesPID(createStringListFromTypeList(persistentIdentifierType.getReplacesPIDList(), LiteralType::getString));
    persistentIdentifier.setInScheme(getResourceString(persistentIdentifierType.getInScheme()));
    return persistentIdentifier;
  }
}
