package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an implementation of {@link ReferenceTerm} that provides context in the sense that it is
 * aware of the field type(s) in which the reference term was found.
 */
public class ReferenceTermContext extends AbstractReferenceTerm implements TermContext {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceTermContext.class);

  private final Set<FieldType<?>> fieldTypes;

  /**
   * Create an instance of this class.
   *
   * @param reference  The String version of the reference.
   * @param fieldTypes The field types.
   * @return An instance of this class.
   */
  public static ReferenceTermContext createFromString(String reference,
      Set<FieldType<?>> fieldTypes) {
    final URL referenceUrl = convertToURL(reference);
    if (referenceUrl == null) {
      LOGGER.debug("Invalid enrichment reference found: {}", reference);
      return null;
    }
    return new ReferenceTermContext(referenceUrl, fieldTypes);
  }

  /**
   * Converts a string version of a reference to a URL, doing some validation tests.
   *
   * @param reference The String version of a reference.
   * @return The URL version of the reference.
   */
  private static URL convertToURL(String reference) {
    try {
      final URI uri = new URI(reference);
      if (!uri.isAbsolute()) {
        throw new MalformedURLException("URL is not absolute");
      }
      return uri.toURL();
    } catch (MalformedURLException | URISyntaxException | IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * Determines whether this instance's reference is equal to some String value. This is done by
   * creating a URL of the input value in the same way as the reference of this instance would have
   * been, and then normalising both to strings before comparing them.
   *
   * @param toCompare The String value to compare the reference with.
   * @return Whether this instance's reference is equal to the input value.
   */
  public boolean referenceEquals(String toCompare) {
    return Objects.equals(urlToString(convertToURL(toCompare)), urlToString(getReference()));
  }

  private ReferenceTermContext(URL reference, Set<FieldType<?>> fieldTypes) {
    super(reference);
    this.fieldTypes = Set.copyOf(fieldTypes);
  }

  @Override
  public Set<EntityType> getCandidateTypes() {
    return fieldTypes.stream().map(FieldType::getEntityType).collect(Collectors.toSet());
  }

  @Override
  public Set<FieldType<?>> getFieldTypes() {
    return fieldTypes;
  }

  @Override
  public boolean valueEquals(ResourceOrLiteralType resourceOrLiteralType) {
    return Optional.ofNullable(resourceOrLiteralType.getResource())
        .map(resource -> this.referenceEquals(resource.getResource())).orElse(false);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final ReferenceTermContext that = (ReferenceTermContext) o;
    // Note: avoid using reference URL for equality as it may do a domain name check.
    return Objects.equals(getFieldTypes(), that.getFieldTypes()) && Objects
            .equals(getReferenceAsString(), that.getReferenceAsString());
  }

  @Override
  public int hashCode() {
    // Note: avoid using reference URL for computing the hash as it may do a domain name check.
    return Objects.hash(getFieldTypes(), getReferenceAsString());
  }
}
