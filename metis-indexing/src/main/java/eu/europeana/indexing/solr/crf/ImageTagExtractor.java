package eu.europeana.indexing.solr.crf;

import java.util.HashSet;
import java.util.Set;
import eu.europeana.corelib.definitions.jibx.WebResourceType;

/**
 * Extracts the pure tags from an image resource and generates the fake tags.
 */
public class ImageTagExtractor extends TagExtractor {

    @Override
    public Set<Integer> getFilterTags(WebResourceType webResource) {
        final Set<Integer> filterTags = new HashSet<>();
        final Integer mediaTypeCode = MediaType.IMAGE.getEncodedValue();

        final Set<Integer> colorCodes = new HashSet<>();
        colorCodes.addAll(TechnicalFacet.IMAGE_COLOUR_PALETTE.evaluateAndShift(webResource));
        colorCodes.add(0);

        final Set<Integer> mimeTypeCodes = new HashSet<>();
        mimeTypeCodes.addAll(TechnicalFacet.MIME_TYPE.evaluateAndShift(webResource));
        mimeTypeCodes.add(0);

        final Set<Integer> fileSizeCodes = new HashSet<>();
        fileSizeCodes.addAll(TechnicalFacet.IMAGE_SIZE.evaluateAndShift(webResource));
        fileSizeCodes.add(0);

        final Set<Integer> colorSpaceCodes = new HashSet<>();
        colorSpaceCodes.addAll(TechnicalFacet.IMAGE_COLOUR_SPACE.evaluateAndShift(webResource));
        colorSpaceCodes.add(0);

        final Set<Integer> aspectRatioCodes = new HashSet<>();
        aspectRatioCodes.addAll(TechnicalFacet.IMAGE_ASPECT_RATIO.evaluateAndShift(webResource));
        aspectRatioCodes.add(0);

        for (Integer mimeType : mimeTypeCodes) {
            for (Integer fileSize : fileSizeCodes) {
                for (Integer colorSpace : colorSpaceCodes) {
                    for (Integer aspectRatio : aspectRatioCodes) {
                        for (Integer color : colorCodes) {
                            filterTags.add(mediaTypeCode | mimeType | fileSize | colorSpace | aspectRatio | color);
                        }
                    }
                }
            }
        }

        return filterTags;
    }

    @Override
    public Set<Integer> getFacetTags(WebResourceType webResource) {
        final Set<Integer> facetTags = new HashSet<>();

        final Integer mediaTypeCode = MediaType.IMAGE.getEncodedValue();

        for (Integer mimeTypeCode: TechnicalFacet.MIME_TYPE.evaluateAndShift(webResource)) {
            facetTags.add(mediaTypeCode | mimeTypeCode);
        }

        for (Integer fileSizeCode: TechnicalFacet.IMAGE_SIZE.evaluateAndShift(webResource)) {
            facetTags.add(mediaTypeCode | fileSizeCode);
        }

        for (Integer colorSpaceCode : TechnicalFacet.IMAGE_COLOUR_SPACE.evaluateAndShift(webResource)) {
            facetTags.add(mediaTypeCode | colorSpaceCode);
        }

        for (Integer aspectRatioCode : TechnicalFacet.IMAGE_ASPECT_RATIO.evaluateAndShift(webResource)) {
            facetTags.add(mediaTypeCode | aspectRatioCode);
        }

        for(final Integer colorCode : TechnicalFacet.IMAGE_COLOUR_PALETTE.evaluateAndShift(webResource)) {
            facetTags.add(mediaTypeCode | colorCode);
        }

        return facetTags;
    }
}
