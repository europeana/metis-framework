package eu.europeana.indexing.solr.crf;

import java.util.HashSet;
import java.util.Set;
import eu.europeana.corelib.definitions.jibx.WebResourceType;

/**
 * Extracts the pure tags from a video resource and generates the fake tags.
 */
public class VideoTagExtractor extends TagExtractor {

  // TODO JOCHEN Don't create extractor for every single web resource! Goes also for other extractors.

    @Override
    public Set<Integer> getFilterTags(WebResourceType webResource) {
        final Set<Integer> filterTags = new HashSet<>();
      
        // TODO JOCHEN See todo below.

        final Integer mediaTypeCode = MediaType.VIDEO.getEncodedValue();

        final Set<Integer> mimeTypeCodes = new HashSet<>();
        mimeTypeCodes.addAll(TechnicalFacetUtils.getMimeTypeCode(webResource));
        mimeTypeCodes.add(0);

        final Set<Integer> qualityCodes = new HashSet<>();
        qualityCodes.addAll(TechnicalFacetUtils.getVideoQualityCode(webResource));
        qualityCodes.add(0);

        final Set<Integer> durationCodes = new HashSet<>();
        durationCodes.addAll(TechnicalFacetUtils.getVideoDurationCode(webResource));
        durationCodes.add(0);

        for (Integer mimeType : mimeTypeCodes) {
            for (Integer quality : qualityCodes) {
                for (Integer duration : durationCodes) {
                    final Integer result = mediaTypeCode |
                                          (mimeType << TechnicalFacet.MIME_TYPE.getBitPos()) |
                                          (quality  << TechnicalFacet.VIDEO_QUALITY.getBitPos()) |
                                          (duration << TechnicalFacet.VIDEO_DURATION.getBitPos());

                    filterTags.add(result);
                }
            }
        }

        return filterTags;
    }

    @Override
    public Set<Integer> getFacetTags(WebResourceType webResource) {
        final Set<Integer> facetTags = new HashSet<>();
      
        // TODO JOCHEN Find generic way to do this for all four media types (don't forget common tag
        // extractor!) and two kinds (facet and filter). Plan: 
        // 1. Add Function<WebResource, Integer> to TagEncoding enum. Check implementation with excel sheet!
        // 2. Add list of TagEncodings to MediaType enum.
        // 3. Make two methods (that share some code) for extracting filter and facet tags.
        // 4. No need for different extractors anymore!  
        // 5. Remove unnecessary code! (run unused code detector)
        // 6. Convert CommonTagExtractor into MimeType Enum.
        // 7. Make sure that mime types are always used startsWith instead of equals: mimetypes may be followed by specifications. 
  
        final Integer mediaTypeCode = MediaType.VIDEO.getEncodedValue();

        Integer facetTag;

        for (Integer mimeTypeCode: TechnicalFacetUtils.getMimeTypeCode(webResource)) {
            facetTag = mediaTypeCode | (mimeTypeCode << TechnicalFacet.MIME_TYPE.getBitPos());
            facetTags.add(facetTag);
        }

        for(Integer qualityCode: TechnicalFacetUtils.getVideoQualityCode(webResource)) {
            facetTag = mediaTypeCode | (qualityCode << TechnicalFacet.VIDEO_QUALITY.getBitPos());
            facetTags.add(facetTag);
        }

        for (Integer durationCode: TechnicalFacetUtils.getVideoDurationCode(webResource)) {
            facetTag = mediaTypeCode | (durationCode << TechnicalFacet.VIDEO_DURATION.getBitPos());
            facetTags.add(facetTag);
        }
        
        return facetTags;
    }

}
