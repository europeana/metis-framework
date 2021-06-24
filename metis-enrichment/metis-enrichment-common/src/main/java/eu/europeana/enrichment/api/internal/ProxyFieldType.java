package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.metis.schema.jibx.Contributor;
import eu.europeana.metis.schema.jibx.Coverage;
import eu.europeana.metis.schema.jibx.Created;
import eu.europeana.metis.schema.jibx.Creator;
import eu.europeana.metis.schema.jibx.Date;
import eu.europeana.metis.schema.jibx.EuropeanaType;
import eu.europeana.metis.schema.jibx.EuropeanaType.Choice;
import eu.europeana.metis.schema.jibx.Format;
import eu.europeana.metis.schema.jibx.Issued;
import eu.europeana.metis.schema.jibx.Medium;
import eu.europeana.metis.schema.jibx.ProxyType;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType.Resource;
import eu.europeana.metis.schema.jibx.Spatial;
import eu.europeana.metis.schema.jibx.Subject;
import eu.europeana.metis.schema.jibx.Temporal;
import eu.europeana.metis.schema.jibx.Type;
import java.util.Collection;
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

public enum ProxyFieldType implements FieldType<ProxyType> {

  DC_CREATOR(Choice::ifCreator, Choice::getCreator, Choice::setCreator, Creator::new,
      EntityType.AGENT),

  DC_CONTRIBUTOR(Choice::ifContributor, Choice::getContributor, Choice::setContributor,
      Contributor::new, EntityType.AGENT),

  DC_DATE(Choice::ifDate, Choice::getDate, Choice::setDate, Date::new, EntityType.TIMESPAN),

  DCTERMS_ISSUED(Choice::ifIssued, Choice::getIssued, Choice::setIssued, Issued::new,
      EntityType.TIMESPAN),

  DCTERMS_CREATED(Choice::ifCreated, Choice::getCreated, Choice::setCreated, Created::new,
      EntityType.TIMESPAN),

  DC_COVERAGE(Choice::ifCoverage, Choice::getCoverage, Choice::setCoverage, Coverage::new,
      EntityType.PLACE),

  DCTERMS_TEMPORAL(Choice::ifTemporal, Choice::getTemporal, Choice::setTemporal, Temporal::new,
      EntityType.TIMESPAN),

  DC_TYPE(Choice::ifType, Choice::getType, Choice::setType, Type::new, EntityType.CONCEPT),

  DCTERMS_SPATIAL(Choice::ifSpatial, Choice::getSpatial, Choice::setSpatial, Spatial::new,
      EntityType.PLACE),

  DC_SUBJECT(Choice::ifSubject, Choice::getSubject, Choice::setSubject, Subject::new,
      EntityType.CONCEPT),

  DCTERMS_MEDIUM(Choice::ifMedium, Choice::getMedium, Choice::setMedium, Medium::new,
      EntityType.CONCEPT),

  DC_FORMAT(Choice::ifFormat, Choice::getFormat, Choice::setFormat, Format::new,
      EntityType.CONCEPT);

  private final ChoiceContentHandler<?> choiceContentHandler;
  private final EntityType entityType;

  <T extends ResourceOrLiteralType> ProxyFieldType(Predicate<Choice> choiceChecker,
      Function<Choice, T> contentGetter, BiConsumer<Choice, T> contentSetter,
      Supplier<T> contentCreator, EntityType entityType) {
    this.choiceContentHandler = new ChoiceContentHandler<>(choiceChecker, contentGetter,
        contentSetter, contentCreator);
    this.entityType = entityType;
  }

  public final Set<String> extractFieldLinksForEnrichment(ProxyType proxy) {
    return extractFields(proxy).map(ResourceOrLiteralType::getResource).filter(Objects::nonNull)
        .map(Resource::getResource).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
  }

  @Override
  public Stream<? extends ResourceOrLiteralType> extractFields(ProxyType proxy) {
    return Optional.ofNullable(proxy.getChoiceList()).stream().flatMap(Collection::stream)
        .filter(choiceContentHandler.choiceChecker).map(choiceContentHandler.contentGetter)
        .filter(Objects::nonNull);
  }

  /**
   * Remove {@link Choice} fields from the proxy that match the provided link.
   *
   * @param proxy the proxy to be checked
   * @param link the link to be matched
   */
  public void removeMatchingFields(ProxyType proxy, String link) {
    proxy.getChoiceList().removeIf(choice -> doesChoiceMatchLink(choice, link));
  }

  private boolean doesChoiceMatchLink(Choice choice, String link) {
    boolean result = false;
    if (choiceContentHandler.choiceChecker.test(choice)) {
      final ResourceOrLiteralType resourceOrLiteralType = choiceContentHandler.contentGetter
          .apply(choice);
      if (resourceOrLiteralType != null && resourceOrLiteralType.getResource() != null && link
          .equals(resourceOrLiteralType.getResource().getResource())) {
        result = true;
      }
    }
    return result;
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

  @Override
  public EntityType getEntityType() {
    return entityType;
  }

  private static final class ChoiceContentHandler<T extends ResourceOrLiteralType> {

    protected final Predicate<Choice> choiceChecker;
    protected final Function<Choice, T> contentGetter;
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
