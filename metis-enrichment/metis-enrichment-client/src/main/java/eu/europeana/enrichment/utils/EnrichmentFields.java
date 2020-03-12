package eu.europeana.enrichment.utils;

import eu.europeana.corelib.definitions.jibx.Contributor;
import eu.europeana.corelib.definitions.jibx.Coverage;
import eu.europeana.corelib.definitions.jibx.Created;
import eu.europeana.corelib.definitions.jibx.Creator;
import eu.europeana.corelib.definitions.jibx.Date;
import eu.europeana.corelib.definitions.jibx.EuropeanaType;
import eu.europeana.corelib.definitions.jibx.EuropeanaType.Choice;
import eu.europeana.corelib.definitions.jibx.Format;
import eu.europeana.corelib.definitions.jibx.Issued;
import eu.europeana.corelib.definitions.jibx.Medium;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType.Resource;
import eu.europeana.corelib.definitions.jibx.Spatial;
import eu.europeana.corelib.definitions.jibx.Subject;
import eu.europeana.corelib.definitions.jibx.Temporal;
import eu.europeana.corelib.definitions.jibx.Type;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by gmamakis on 8-3-17.
 */
public enum EnrichmentFields {
  
  DC_CREATOR(Choice::ifCreator, Choice::getCreator, Choice::setCreator, Creator::new,
      EntityClass.AGENT),

  DC_CONTRIBUTOR(Choice::ifContributor, Choice::getContributor, Choice::setContributor,
      Contributor::new, EntityClass.AGENT),

  DC_DATE(Choice::ifDate, Choice::getDate, Choice::setDate, Date::new, EntityClass.TIMESPAN),

  DCTERMS_ISSUED(Choice::ifIssued, Choice::getIssued, Choice::setIssued, Issued::new,
      EntityClass.TIMESPAN),

  DCTERMS_CREATED(Choice::ifCreated, Choice::getCreated, Choice::setCreated, Created::new,
      EntityClass.TIMESPAN),

  DC_COVERAGE(Choice::ifCoverage, Choice::getCoverage, Choice::setCoverage, Coverage::new,
      EntityClass.PLACE),

  DCTERMS_TEMPORAL(Choice::ifTemporal, Choice::getTemporal, Choice::setTemporal, Temporal::new,
      EntityClass.TIMESPAN),

  DC_TYPE(Choice::ifType, Choice::getType, Choice::setType, Type::new, EntityClass.CONCEPT),

  DCTERMS_SPATIAL(Choice::ifSpatial, Choice::getSpatial, Choice::setSpatial, Spatial::new,
      EntityClass.PLACE),

  DC_SUBJECT(Choice::ifSubject, Choice::getSubject, Choice::setSubject, Subject::new,
      EntityClass.CONCEPT),

  DCTERMS_MEDIUM(Choice::ifMedium, Choice::getMedium, Choice::setMedium, Medium::new,
      EntityClass.CONCEPT),

  DC_FORMAT(Choice::ifFormat, Choice::getFormat, Choice::setFormat, Format::new,
      EntityClass.CONCEPT);

  private final ChoiceContentHandler<?> choiceContentHandler;
  private final EntityClass entityClass;

  <T extends ResourceOrLiteralType> EnrichmentFields(Predicate<Choice> choiceChecker,
      Function<Choice, T> contentGetter, BiConsumer<Choice, T> contentSetter,
      Supplier<T> contentCreator, EntityClass entityClass) {
    this.choiceContentHandler =
        new ChoiceContentHandler<>(choiceChecker, contentGetter, contentSetter, contentCreator);
    this.entityClass = entityClass;
  }

  /**
   * Extract fields from a Proxy for enrichment
   *
   * @param proxy The proxy to use for enrichment
   * @return A list of values ready for enrichment
   */
  public final List<InputValue> extractFieldValuesForEnrichment(ProxyType proxy) {
    return extractFields(proxy)
            .filter(content -> StringUtils.isNotEmpty(content.getString()))
            .map(this::convert)
            .collect(Collectors.toList());
  }

  /**
   * Extract resources from a Proxy for enrichment
   *
   * @param proxy The proxy to use for enrichment
   * @return A list of values ready for enrichment
   */
  public final Set<String> extractFieldLinksForEnrichment(ProxyType proxy) {
    return extractFields(proxy)
            .map(ResourceOrLiteralType::getResource)
            .filter(Objects::nonNull)
            .map(Resource::getResource)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toSet());
  }

  private Stream<? extends ResourceOrLiteralType> extractFields(ProxyType proxy) {
    return proxy.getChoiceList().stream()
            .filter(choiceContentHandler.choiceChecker)
            .map(choiceContentHandler.contentGetter)
            .filter(Objects::nonNull);
  }

  private InputValue convert(ResourceOrLiteralType content) {
    final String language = content.getLang() == null ? null : content.getLang().getLang();
    return new InputValue(this.name(), content.getString(), language, entityClass);
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

  /**
   * This method checks whether this choice is of this type and then retrieves the resource value if
   * it is.
   *
   * @param choice The choice of which to retrieve the resource.
   * @return The resource. Can be null if the choice is not of this type or if the choice does not
   * contain a resource.
   */
  public final String getResourceIfRightChoice(Choice choice) {
    return choiceContentHandler.choiceChecker.test(choice) ? Optional
            .of(choiceContentHandler.contentGetter.apply(choice))
            .map(ResourceOrLiteralType::getResource).map(Resource::getResource).orElse(null) : null;
  }

  private static final class ChoiceContentHandler<T extends ResourceOrLiteralType> {
    private final Predicate<Choice> choiceChecker;
    private final Function<Choice, T> contentGetter;
    private final BiConsumer<Choice, T> contentSetter;
    private final Supplier<T> contentCreator;

    private ChoiceContentHandler(Predicate<Choice> choiceChecker, Function<Choice, T> contentGetter,
        BiConsumer<Choice, T> contentSetter, Supplier<T> contentCreator) {
      this.choiceChecker = choiceChecker;
      this.contentGetter = contentGetter;
      this.contentSetter = contentSetter;
      this.contentCreator = contentCreator;
    }

    final EuropeanaType.Choice createChoice(String about) {
      final EuropeanaType.Choice choice = new EuropeanaType.Choice();
      final T content = contentCreator.get();
      final ResourceOrLiteralType.Resource resource = new ResourceOrLiteralType.Resource();
      resource.setResource(about);
      content.setResource(resource);
      content.setString(""); // Required, otherwise jibx (de)serialization fails
      contentSetter.accept(choice, content);
      return choice;
    }
  }
}
