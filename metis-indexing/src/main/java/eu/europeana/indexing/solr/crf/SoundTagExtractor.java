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
        mimeTypeCodes.addAll(TechnicalFacet.MIME_TYPE.evaluateAndShift(webResource));
        mimeTypeCodes.add(0);

        final Set<Integer> qualityCodes = new HashSet<>();
        qualityCodes.addAll(TechnicalFacet.SOUND_QUALITY.evaluateAndShift(webResource));
        qualityCodes.add(0);

        final Set<Integer> durationCodes = new HashSet<>();
        durationCodes.addAll(TechnicalFacet.SOUND_DURATION.evaluateAndShift(webResource));
        durationCodes.add(0);

        for (Integer mimeType : mimeTypeCodes) {
            for (Integer quality : qualityCodes) {
                for (Integer duration : durationCodes) {
                    filterTags.add(mediaTypeCode | mimeType | quality | duration);
                }
            }
        }

        return filterTags;
    }

    @Override
    public Set<Integer> getFacetTags(WebResourceType webResource) {
        final Set<Integer> facetTags = new HashSet<>();
        final Integer mediaTypeCode = MediaType.AUDIO.getEncodedValue();

        for (Integer mimeTypeCode: TechnicalFacet.MIME_TYPE.evaluateAndShift(webResource)) {
            facetTags.add(mediaTypeCode | mimeTypeCode);
        }

        for (Integer qualityCode: TechnicalFacet.SOUND_QUALITY.evaluateAndShift(webResource)) {
            facetTags.add(mediaTypeCode | qualityCode);
        }

        for (Integer durationCode: TechnicalFacet.SOUND_DURATION.evaluateAndShift(webResource)) {
            facetTags.add(mediaTypeCode | durationCode);
        }
        
        return facetTags;
    }
}
