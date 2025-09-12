package eu.europeana.indexing.common.fullbean;


import eu.europeana.corelib.solr.entity.PersistentIdentifierImpl;
import eu.europeana.metis.schema.jibx.Created;
import eu.europeana.metis.schema.jibx.Creator1;
import eu.europeana.metis.schema.jibx.InScheme;
import eu.europeana.metis.schema.jibx.LiteralType;
import eu.europeana.metis.schema.jibx.PersistentIdentifierType;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType;
import eu.europeana.metis.schema.jibx.ResourceType;
import eu.europeana.metis.schema.jibx.Value;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    persistentIdentifier.setValue(Optional.of(persistentIdentifierType)
                                          .map(PersistentIdentifierType::getValue)
                                          .map(Value::getString)
                                          .orElse(null));

    persistentIdentifier.setCreator(Map.of(Optional.of(persistentIdentifierType)
                                                   .map(PersistentIdentifierType::getCreator)
                                                   .map(Creator1::getLang)
                                                   .map(ResourceOrLiteralType.Lang::getLang)
                                                   .orElse(""),
                                           Optional.of(persistentIdentifierType)
                                                   .map(PersistentIdentifierType::getCreator)
                                                   .map(Creator1::getString)
                                                   .orElse(""))
                                       .entrySet()
                                       .stream()
                                       .filter(e -> !e.getKey().isEmpty())
                                       .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

    persistentIdentifier.setCreated(Optional.of(persistentIdentifierType)
                                            .map(PersistentIdentifierType::getCreated)
                                            .map(Created::getString)
                                            .orElse(null));

    persistentIdentifier.setNotation(Optional.of(persistentIdentifierType)
                                             .map(PersistentIdentifierType::getNotationList)
                                             .map(notations -> notations
                                                 .stream()
                                                 .map(LiteralType::getString)
                                                 .toList())
                                             .orElse(null));

    persistentIdentifier.setHasURL(Optional.of(persistentIdentifierType)
                                           .map(PersistentIdentifierType::getHasURLList)
                                           .map(hasUrls -> hasUrls
                                               .stream()
                                               .filter(Objects::nonNull)
                                               .map(ResourceType::getResource)
                                               .findAny()
                                               .orElse(""))
                                           .orElse(null));
    persistentIdentifier.setEquivalentPID(Optional.of(persistentIdentifierType)
                                                  .map(PersistentIdentifierType::getEquivalentPIDList)
                                                  .map(equivalentList -> equivalentList
                                                      .stream()
                                                      .filter(Objects::nonNull)
                                                      .map(LiteralType::getString)
                                                      .toList())
                                                  .orElse(null));
    persistentIdentifier.setReplacesPID(Optional.of(persistentIdentifierType)
                                                .map(PersistentIdentifierType::getReplacesPIDList)
                                                .map(replacesPIDList -> replacesPIDList
                                                    .stream()
                                                    .filter(Objects::nonNull)
                                                    .map(LiteralType::getString)
                                                    .toList())
                                                .orElse(null));
    persistentIdentifier.setInScheme(Optional.of(persistentIdentifierType)
                                             .map(PersistentIdentifierType::getInScheme)
                                             .map(InScheme::getResource)
                                             .orElse(null));
    return persistentIdentifier;
  }
}
