package eu.europeana.metis.dereference;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.apache.commons.text.StringSubstitutor;

/**
 * <p>
 *   Subclasses of this class generate URIs where a resource may be obtained from various input
 *   data, including the resource ID. The reality is that resource IDs may not be resolvable to the
 *   RDF+XML version of the resource (or may not be resolvable at all). Instances of this class are
 *   designed to do the conversion.
 * </p>
 * <p>
 *   This generation works based on a template for the resource URI that can be evaluated for
 *   different input. The template may (should) contain parameters declared using the following
 *   syntax: <code>${PARAMETER}</code> where <code>PARAMETER</code> represents a function pipeline
 *   consisting of a start function followed by zero or more piped functions, separated by the pipe
 *   character <code>|</code>.
 * </p>
 * <p>
 *   Example templates:
 *   <ul>
 *     <li>
 *       The template <code>"${resourceId}"</code> is the most basic template: this indicates that
 *       the resource URI is equal to the resource ID.
 *     </li>
 *     <li>
 *       A common use case is that a file extension is missing. This can be corrected in a template
 *       like this: <code>"${resourceId}.rdf"</code>.
 *     </li>
 *     <li>
 *       The template <code>"https://example.com/entities?id=${resourceId|urlQueryEscape}"</code>
 *       is an example of a start function followed by one piped function. This indicates that the
 *       resource ID should be URL-escaped and put into the query parameters for an API call.
 *     </li>
 *   </ul>
 * </p>
 */
public abstract class ResourceUriGenerator {

  private static final String PIPELINE_SEPARATOR = "|";

  /**
   * This is the start function that feeds the resource ID into the function pipeline.
   */
  public static final String RESOURCE_ID_FUNCTION = "resourceId";

  /**
   * This is the pipe function that performs URL query escaping of the result of the previous
   * function.
   */
  public static final String URL_QUERY_ESCAPE_FUNCTION = "urlQueryEscape";

  /**
   * This is input for Resource URI generation.
   * @param resourceId The resource ID of the resource for which to generate a URI.
   */
  public record Input(String resourceId) {}

  private static final Map<String, Function<Input, String>> registeredStartFunctions = new HashMap<>() {{
    put(RESOURCE_ID_FUNCTION, Input::resourceId);
  }};

  private static final Map<String, Function<String, String>> registeredPipedFunctions = new HashMap<>() {{
    put(URL_QUERY_ESCAPE_FUNCTION, input -> URLEncoder.encode(input, StandardCharsets.UTF_8));
  }};

  private static final ResourceUriGenerator IDENTITY_GENERATOR = new ResourceUriGenerator() {

    @Override
    public URI generateUri(Input input) throws URISyntaxException {
      return new URI(input.resourceId);
    }
  };

  private ResourceUriGenerator() {}

  /**
   * This method takes input for a resource and generates the resource URI based on this
   * generator's template.
   *
   * @param input The input.
   * @return The URI.
   * @throws URISyntaxException In the case of URI syntax issues.
   */
  public abstract URI generateUri(Input input) throws URISyntaxException;

  /**
   * This convenience method compiles the input from the parameters before calling
   * {@link #generateUri(Input)}.
   *
   * @param resourceId The resource ID.
   * @return The URI.
   * @throws URISyntaxException In the case of URI syntax issues.
   */
  public final URI generateUri(String resourceId) throws URISyntaxException {
    return this.generateUri(new Input(resourceId));
  }

  /**
   * This is a convenience method for creating a generator for which the resource URI is the same as
   * the resource ID. It is equivalent to a resource generator with template
   * <code>"${resourceId}"</code>.
   *
   * @return A resource URI generator.
   */
  public static ResourceUriGenerator identityGenerator() {
    return IDENTITY_GENERATOR;
  }

  /**
   * This is a convenience method for creating a generator just for adding a suffix (e.g., a file
   * extension) to the resource ID to create the resource URI.
   *
   * @param suffix The suffix to add to the resource ID.
   * @return A resource URI generator.
   * @deprecated This function will be removed in a future version.
   */
  @Deprecated
  public static ResourceUriGenerator forSuffix(String suffix) {
    return (suffix == null || suffix.isBlank()) ? identityGenerator()
        : forTemplate("${" + RESOURCE_ID_FUNCTION + "}" + suffix.trim());
  }

  /**
   * This method creates a resource URI generator for the given template.
   *
   * @param template The template to apply.
   * @return A resource URI generator.
   */
  public static ResourceUriGenerator forTemplate(String template) {
    return new ResourceUriGenerator() {

      @Override
      public URI generateUri(Input input) throws URISyntaxException {
        return new URI(new StringSubstitutor(key -> evaluate(key, input)).replace(template));
      }
    };
  }

  private static <T> String evaluateSegment(String segment, T input,
      Map<String, Function<T, String>> registeredFunctions) {
    final String trimmedSegment = segment.trim();
    if (trimmedSegment.isEmpty()) {
      throw new IllegalArgumentException("Empty parameter segment.");
    }
    final Function<T, String> function = registeredFunctions.get(trimmedSegment);
    if (function == null) {
      throw new IllegalArgumentException("Unknown parameter segment: " + trimmedSegment);
    }
    return function.apply(input);
  }

  private static String evaluate(String parameter, Input input) {

    // Split the parameter in its segments. Note: the split array always has at least one segment.
    final String[] chain = parameter.split(Pattern.quote(PIPELINE_SEPARATOR), -1);

    // Compute result by resolve the first segment followed by the subsequent ones.
    String result = evaluateSegment(chain[0], input, registeredStartFunctions);
    for (int i = 1; i < chain.length; i++) {
      result = evaluateSegment(chain[i], result, registeredPipedFunctions);
    }

    // Done.
    return result;
  }
}
