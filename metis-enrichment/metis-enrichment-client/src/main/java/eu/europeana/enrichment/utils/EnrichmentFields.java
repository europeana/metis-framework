package eu.europeana.enrichment.utils;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import eu.europeana.corelib.definitions.jibx.Contributor;
import eu.europeana.corelib.definitions.jibx.Coverage;
import eu.europeana.corelib.definitions.jibx.Created;
import eu.europeana.corelib.definitions.jibx.Creator;
import eu.europeana.corelib.definitions.jibx.Date;
import eu.europeana.corelib.definitions.jibx.EuropeanaType;
import eu.europeana.corelib.definitions.jibx.EuropeanaType.Choice;
import eu.europeana.corelib.definitions.jibx.Issued;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
import eu.europeana.corelib.definitions.jibx.Spatial;
import eu.europeana.corelib.definitions.jibx.Subject;
import eu.europeana.corelib.definitions.jibx.Temporal;
import eu.europeana.corelib.definitions.jibx.Type;

/**
 * Created by gmamakis on 8-3-17.
 */
public enum EnrichmentFields {
  
  DC_CREATOR(Choice::ifCreator, Choice::getCreator, (choice, content) -> choice.setCreator(content),
      Creator::new, EntityClass.AGENT),

  DC_CONTRIBUTOR(Choice::ifContributor, Choice::getContributor,
      (choice, content) -> choice.setContributor(content), Contributor::new, EntityClass.AGENT),

  DC_DATE(Choice::ifDate, Choice::getDate, (choice, content) -> choice.setDate(content), Date::new,
      EntityClass.TIMESPAN),

  DCTERMS_ISSUED(Choice::ifIssued, Choice::getIssued,
      (choice, content) -> choice.setIssued(content), Issued::new, EntityClass.TIMESPAN),

  DCTERMS_CREATED(Choice::ifCreated, Choice::getCreated,
      (choice, content) -> choice.setCreated(content), Created::new, EntityClass.TIMESPAN),

  DC_COVERAGE(Choice::ifCoverage, Choice::getCoverage,
      (choice, content) -> choice.setCoverage(content), Coverage::new, EntityClass.PLACE),

  DCTERMS_TEMPORAL(Choice::ifTemporal, Choice::getTemporal,
      (choice, content) -> choice.setTemporal(content), Temporal::new, EntityClass.TIMESPAN),

  DC_TYPE(Choice::ifType, Choice::getType, (choice, content) -> choice.setType(content), Type::new,
      EntityClass.CONCEPT),

  DCTERMS_SPATIAL(Choice::ifSpatial, Choice::getSpatial,
      (choice, content) -> choice.setSpatial(content), Spatial::new, EntityClass.PLACE),

  DC_SUBJECT(Choice::ifSubject, Choice::getSubject, (choice, content) -> choice.setSubject(content),
      Subject::new, EntityClass.CONCEPT);

  private final ChoiceContentHandler<?> choiceContentHandler;
  private final EntityClass[] entityClasses;

  private <T extends ResourceOrLiteralType> EnrichmentFields(Predicate<Choice> choiceChecker,
      Function<Choice, T> contentGetter, BiConsumer<Choice, T> contentSetter,
      Supplier<T> contentCreator, EntityClass... entityClasses) {
    this.choiceContentHandler =
        new ChoiceContentHandler<>(choiceChecker, contentGetter, contentSetter, contentCreator);
    this.entityClasses = entityClasses;
  }

  /**
   * Extract fields from a Proxy for enrichment
   * 
   * @param proxy The proxy to use for enrichment
   * @return A list of values ready for enrichment
   */
  public final List<InputValue> extractFieldValuesForEnrichment(ProxyType proxy) {
    return proxy.getChoiceList().stream()
        .filter(choiceContentHandler.choiceChecker)
        .map(choiceContentHandler.contentGetter)
        .filter(Objects::nonNull)
        .filter(content -> content.getString() != null)
        .map(this::convert)
        .collect(Collectors.toList());
  }
  
  private InputValue convert(ResourceOrLiteralType content) {
    final String language = content.getLang() != null ? content.getLang().getLang() : null;
    return new InputValue(this.name(), content.getString(), language, entityClasses);
  }

  /**
   * Create a field appendable on a Europeana Proxy during enrichment for semantic linking
   * 
   * @param about The rdf:about of the Class to append on the specified field
   * @return The full field to append
   */
  public final EuropeanaType.Choice createChoice(String about) {
    return choiceContentHandler.createChoice(about);
  }

  private static final class ChoiceContentHandler<T extends ResourceOrLiteralType> {
    public final Predicate<Choice> choiceChecker;
    public final Function<Choice, T> contentGetter;
    public final BiConsumer<Choice, T> contentSetter;
    public final Supplier<T> contentCreator;

    private ChoiceContentHandler(Predicate<Choice> choiceChecker, Function<Choice, T> contentGetter,
        BiConsumer<Choice, T> contentSetter, Supplier<T> contentCreator) {
      this.choiceChecker = choiceChecker;
      this.contentGetter = contentGetter;
      this.contentSetter = contentSetter;
      this.contentCreator = contentCreator;
    }

    public final EuropeanaType.Choice createChoice(String about) {
      final EuropeanaType.Choice choice = new EuropeanaType.Choice();
      final T content = contentCreator.get();
      final ResourceOrLiteralType.Resource resource = new ResourceOrLiteralType.Resource();
      resource.setResource(about);
      content.setResource(resource);
      contentSetter.accept(choice, content);
      return choice;
    }
  }
}
