package eu.europeana.indexing.solr.crf;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import eu.europeana.indexing.utils.SetUtils;

/**
 * This class provides functionality to extract the facet codes from web resources and combine them
 * into facet and/or filter tags that may be added to the web resource's persistence and thus allow
 * categorizing, filtering and searching them based on the facets' values.
 * 
 * @author jochen
 *
 */
public class TagExtractor {

  /**
   * <p>
   * This method returns all possible combinations of the facet tags: each facet's code(s) for the
   * given web resource will be collected and combined ('or'-ed) so that the web resource may be
   * queried on any combination of facet codes (as long as this combination consists of codes from
   * different facets).
   * </p>
   * <p>
   * As an example: suppose the web resource has values a1 and a2 for facet a, and b1 for facet b.
   * Then this method will return the following six possible combinations:
   * <ol>
   * <li>0 (the empty code)</li>
   * <li>a1</li>
   * <li>a2</li>
   * <li>b1</li>
   * <li>a1 | b1</li>
   * <li>a2 | b1</li>
   * </ol>
   * So it will not return [a1 | a2] or [a1 | a2 | b1] as this would combine multiple codes for the
   * same facets.
   * </p>
   * <p>
   * Note that all resulting codes will be shifted to the right position and will also have the bits
   * set that mark the media type (see {@link EncodedMediaType}).
   * </p>
   * 
   * @param webResource The web resource for which to retrieve the facet codes.
   * @return The set of facet codes.
   */
  public final Set<Integer> getFilterTags(WebResourceWrapper webResource) {

    // Get and check the media type.
    final EncodedMediaType mediaType = webResource.getMediaType();
    if (mediaType == EncodedMediaType.OTHER) {
      return Collections.emptySet();
    }

    // Get all the individual codes from all the facets.
    final List<Set<Integer>> codes = mediaType.getFacets().stream()
        .map(facet -> facet.evaluateAndShift(webResource)).collect(Collectors.toList());

    // Find all the combinations.
    final int shiftedMediaTypeCode = getShiftedMediaTypeCode(mediaType);
    return SetUtils.generateCombinations(codes, shiftedMediaTypeCode,
        (combination, code) -> combination | code);
  }

  /**
   * <p>
   * This method returns all the web resource's facet codes: each facet's code(s) for the given web
   * resource will be collected and returned, so that they may be used to list and search through
   * the facet values of the web resource.
   * </p>
   * <p>
   * As an example: suppose the web resource has values a1 and a2 for facet a, and b1 for facet b.
   * Then this method will return the following three codes:
   * <ol>
   * <li>a1</li>
   * <li>a2</li>
   * <li>b1</li>
   * </ol>
   * As opposed to {@link #getFilterTags(WebResourceWrapper)}, this method returns only the
   * individual codes, not any combination of them. As such, this result will be a subset of the
   * result of {@link #getFilterTags(WebResourceWrapper)}.
   * </p>
   * <p>
   * Note that all resulting codes will be shifted to the right position and will also have the bits
   * set that mark the media type (see {@link EncodedMediaType}).
   * </p>
   * 
   * @param webResource The web resource for which to retrieve the facet codes.
   * @return The set of facet codes.
   */
  public final Set<Integer> getFacetTags(WebResourceWrapper webResource) {

    // Get and check the media type.
    final EncodedMediaType mediaType = webResource.getMediaType();
    if (mediaType == EncodedMediaType.OTHER) {
      return Collections.emptySet();
    }

    // Get all the individual codes from all the facets and make sure there is always the media type
    // code 'or'-ed into them.
    final int shiftedMediaTypeCode = getShiftedMediaTypeCode(mediaType);
    return mediaType.getFacets().stream()
        .flatMap(facet -> facet.evaluateAndShift(webResource).stream())
        .map(code -> shiftedMediaTypeCode | code).collect(Collectors.toSet());

  }

  private static int getShiftedMediaTypeCode(EncodedMediaType mediaType) {
    return TechnicalFacet.MEDIA_TYPE.shift(mediaType.getCode());
  }
}
