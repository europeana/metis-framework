package eu.europeana.indexing.solr.crf;

import java.util.HashSet;
import java.util.Set;
import eu.europeana.corelib.definitions.jibx.WebResourceType;

/**
 * Extracts the pure tags from an audio resource and generates the fake tags.
 * 
 * TODO JOCHEN doesn't currently have the file type to determine whether it is high definition.
 * Currently looks at codec name. But in the case of audio, no codec name is saved (see
 * MediaProcessor.processAudioVideo).
 */
public class SoundTagExtractor extends TagExtractor {

    @Override
    public Set<Integer> getFilterTags(WebResourceType webResource) {
        final Set<Integer> filterTags = new HashSet<>();
        final Integer mediaTypeCode = MediaType.AUDIO.getEncodedValue();

        final Set<Integer> mimeTypeCodes = new HashSet<>();
        mimeTypeCodes.addAll(TechnicalFacetUtils.getMimeTypeCode(webResource));
        mimeTypeCodes.add(0);

        final Set<Integer> qualityCodes = new HashSet<>();
        qualityCodes.addAll(TechnicalFacetUtils.getAudioQualityCode(webResource));
        qualityCodes.add(0);

        final Set<Integer> durationCodes = new HashSet<>();
        durationCodes.addAll(TechnicalFacetUtils.getAudioDurationCode(webResource));
        durationCodes.add(0);

        for (Integer mimeType : mimeTypeCodes) {
            for (Integer quality : qualityCodes) {
                for (Integer duration : durationCodes) {
                    final Integer result = mediaTypeCode |
                            (mimeType << TechnicalFacet.MIME_TYPE.getBitPos()) |
                            (quality << TechnicalFacet.SOUND_QUALITY.getBitPos()) |
                            (duration << TechnicalFacet.SOUND_DURATION.getBitPos());

                    filterTags.add(result);

                }
            }
        }

        return filterTags;
    }

    @Override
    public Set<Integer> getFacetTags(WebResourceType webResource) {
        final Set<Integer> facetTags = new HashSet<>();
        final Integer mediaTypeCode = MediaType.AUDIO.getEncodedValue();

        Integer facetTag;
        
        for (Integer mimeTypeCode: TechnicalFacetUtils.getMimeTypeCode(webResource)) {
            facetTag = mediaTypeCode | (mimeTypeCode << TechnicalFacet.MIME_TYPE.getBitPos());
            facetTags.add(facetTag);
        }

        for (Integer qualityCode: TechnicalFacetUtils.getAudioQualityCode(webResource)) {
            facetTag = mediaTypeCode | (qualityCode << TechnicalFacet.SOUND_QUALITY.getBitPos());
            facetTags.add(facetTag);
        }

        for (Integer durationCode: TechnicalFacetUtils.getAudioDurationCode(webResource)) {
            facetTag = mediaTypeCode | (durationCode << TechnicalFacet.SOUND_DURATION.getBitPos());
            facetTags.add(facetTag);
        }
        
        return facetTags;
    }
}
