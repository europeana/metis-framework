package eu.europeana.indexing.solr.crf;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import eu.europeana.corelib.definitions.jibx.Duration;
import eu.europeana.corelib.definitions.jibx.Height;
import eu.europeana.corelib.definitions.jibx.WebResourceType;

/**
 * Extracts the pure tags from a video resource and generates the fake tags.
 */
public class VideoTagExtractor extends TagExtractor {

  // TODO JOCHEN Don't create extractor for every single web resource! Goes also for other extractors.

  public VideoTagExtractor(String mimeType) {
    super(mimeType);
  }

    public static Integer getQualityCode(Height height) {
        if (height == null || height.getLong() < 576) return 0;
        else return 1;
    }

    public static Integer getDurationCode(Duration duration) {
        if (duration == null || StringUtils.isBlank(duration.getDuration())) return 0;
        final long durationNumber;
        try {
            durationNumber = Long.parseLong(duration.getDuration());
        } catch (NumberFormatException e) {
            return 0;
        }
        if (durationNumber <= 240000) return 1;
        else if (durationNumber <= 800000) return 2;
        else return 3;
    }

    @Override
    public Set<Integer> getFilterTags(WebResourceType webResource) {
        final Set<Integer> filterTags = new HashSet<>();
      
        // TODO JOCHEN See todo below.

        final Integer mediaTypeCode = MediaType.VIDEO.getEncodedValue();

        final Integer qualityCode = getQualityCode(webResource.getHeight());
        final Integer durationCode = getDurationCode(webResource.getDuration());

        final Set<Integer> mimeTypeCodes = new HashSet<>();
        mimeTypeCodes.add(getMimeTypeCode());
        mimeTypeCodes.add(0);

        final Set<Integer> qualityCodes = new HashSet<>();
        qualityCodes.add(qualityCode);
        qualityCodes.add(0);

        final Set<Integer> durationCodes = new HashSet<>();
        durationCodes.add(durationCode);
        durationCodes.add(0);

        for (Integer mimeType : mimeTypeCodes) {
            for (Integer quality : qualityCodes) {
                for (Integer duration : durationCodes) {
                    final Integer result = mediaTypeCode |
                                          (mimeType << TagEncoding.MIME_TYPE.getBitPos()) |
                                          (quality  << TagEncoding.VIDEO_QUALITY.getBitPos()) |
                                          (duration << TagEncoding.VIDEO_DURATION.getBitPos());

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

        facetTag = mediaTypeCode | (getMimeTypeCode() << TagEncoding.MIME_TYPE.getBitPos());
        facetTags.add(facetTag);

        final Integer qualityCode = getQualityCode(webResource.getHeight());
        facetTag = mediaTypeCode | (qualityCode << TagEncoding.VIDEO_QUALITY.getBitPos());
        facetTags.add(facetTag);

        final Integer durationCode = getDurationCode(webResource.getDuration());
        facetTag = mediaTypeCode | (durationCode << TagEncoding.VIDEO_DURATION.getBitPos());
        facetTags.add(facetTag);

        return facetTags;
    }

}
