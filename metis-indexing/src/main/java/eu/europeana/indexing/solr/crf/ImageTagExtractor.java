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
        colorCodes.addAll(TechnicalFacetUtils.getImageColorCodes(webResource));
        colorCodes.add(0);

        final Set<Integer> mimeTypeCodes = new HashSet<>();
        mimeTypeCodes.addAll(TechnicalFacetUtils.getMimeTypeCode(webResource));
        mimeTypeCodes.add(0);

        final Set<Integer> fileSizeCodes = new HashSet<>();
        fileSizeCodes.addAll(TechnicalFacetUtils.getImageSizeCode(webResource));
        fileSizeCodes.add(0);

        final Set<Integer> colorSpaceCodes = new HashSet<>();
        colorSpaceCodes.addAll(TechnicalFacetUtils.getImageColorSpaceCode(webResource));
        colorSpaceCodes.add(0);

        final Set<Integer> aspectRatioCodes = new HashSet<>();
        aspectRatioCodes.addAll(TechnicalFacetUtils.getImageAspectRatioCode(webResource));
        aspectRatioCodes.add(0);

        for (Integer mimeType : mimeTypeCodes) {
            for (Integer fileSize : fileSizeCodes) {
                for (Integer colorSpace : colorSpaceCodes) {
                    for (Integer aspectRatio : aspectRatioCodes) {
                        for (Integer color : colorCodes) {
                            final Integer result = mediaTypeCode |
                                                    (mimeType << TechnicalFacet.MIME_TYPE.getBitPos())  |
                                                    (fileSize << TechnicalFacet.IMAGE_SIZE.getBitPos()) |
                                                    (colorSpace << TechnicalFacet.IMAGE_COLOUR_SPACE.getBitPos()) |
                                                    (aspectRatio << TechnicalFacet.IMAGE_ASPECT_RATIO.getBitPos()) |
                                                    (color << TechnicalFacet.IMAGE_COLOUR_PALETTE.getBitPos());

                            filterTags.add(result);

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

        Integer facetTag;

        for (Integer mimeTypeCode: TechnicalFacetUtils.getMimeTypeCode(webResource)) {
            facetTag = mediaTypeCode | (mimeTypeCode << TechnicalFacet.MIME_TYPE.getBitPos());
            facetTags.add(facetTag);
        }

        for (Integer fileSizeCode: TechnicalFacetUtils.getImageSizeCode(webResource)) {
            facetTag = mediaTypeCode | (fileSizeCode << TechnicalFacet.IMAGE_SIZE.getBitPos());
            facetTags.add(facetTag);
        }

        for (Integer colorSpaceCode : TechnicalFacetUtils.getImageColorSpaceCode(webResource)) {
            facetTag = mediaTypeCode | (colorSpaceCode << TechnicalFacet.IMAGE_COLOUR_SPACE.getBitPos());
            facetTags.add(facetTag);
        }

        for (Integer aspectRatioCode : TechnicalFacetUtils.getImageAspectRatioCode(webResource)) {
            facetTag = mediaTypeCode | (aspectRatioCode << TechnicalFacet.IMAGE_ASPECT_RATIO.getBitPos());
            facetTags.add(facetTag);
        }

        for(final Integer colorCode : TechnicalFacetUtils.getImageColorCodes(webResource)) {
            facetTag = mediaTypeCode | (colorCode << TechnicalFacet.IMAGE_COLOUR_PALETTE.getBitPos());
            facetTags.add(facetTag);
        }

        return facetTags;
    }
}
