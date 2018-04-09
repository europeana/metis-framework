package eu.europeana.indexing.solr.crf;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import eu.europeana.corelib.definitions.jibx.CodecName;
import eu.europeana.corelib.definitions.jibx.Duration;
import eu.europeana.corelib.definitions.jibx.SampleRate;
import eu.europeana.corelib.definitions.jibx.SampleSize;
import eu.europeana.corelib.definitions.jibx.WebResourceType;

/**
 * Extracts the pure tags from an audio resource and generates the fake tags.
 * 
 * TODO JOCHEN doesn't currently have the file type to determine whether it is high definition.
 * Currently looks at codec name. But in the case of audio, no codec name is saved (see
 * MediaProcessor.processAudioVideo).
 */
public class SoundTagExtractor extends TagExtractor {

  private static final Set<String> LOSSLESS_AUDIO_FILE_TYPES = Stream
      .of("alac", "flac", "ape", "shn", "wav", "wma", "aiff", "dsd").collect(Collectors.toSet());
  
  public SoundTagExtractor(String mimeType) {
    super(mimeType);
  }

  public static Integer getQualityCode(final SampleSize sampleSize, final SampleRate sampleRate,
      CodecName codecName) {
    final boolean highDefSampling = sampleSize != null && sampleRate != null
        && sampleSize.getLong() >= 16 && sampleRate.getLong() >= 44100;
    final boolean losslessFile = codecName != null && codecName.getCodecName() != null
        && LOSSLESS_AUDIO_FILE_TYPES.contains(codecName.getCodecName().toLowerCase().trim());
    return (highDefSampling || losslessFile) ? 1 : 0;
  }

    private static Integer getDurationCode(Duration duration) {
      if (duration == null || StringUtils.isBlank(duration.getDuration())) return 0;
      final long durationNumber;
      try {
          durationNumber = Long.parseLong(duration.getDuration());
      } catch (NumberFormatException e) {
          return 0;
      }
      if (durationNumber <= 30000L) return 1;
      else if (durationNumber <= 180000L) return 2;
      else if (durationNumber <= 360000L) return 3;
      else return 4;
    }

    @Override
    public Set<Integer> getFilterTags(WebResourceType webResource) {
        final Set<Integer> filterTags = new HashSet<>();
        final Integer mediaTypeCode = MediaType.AUDIO.getEncodedValue();

        final Integer qualityCode = getQualityCode(webResource.getSampleSize(), webResource.getSampleRate(), webResource.getCodecName());
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
                            (quality << TagEncoding.SOUND_QUALITY.getBitPos()) |
                            (duration << TagEncoding.SOUND_DURATION.getBitPos());

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
        
        facetTag = mediaTypeCode | (getMimeTypeCode() << TagEncoding.MIME_TYPE.getBitPos());
        facetTags.add(facetTag);

        final Integer qualityCode = getQualityCode(webResource.getSampleSize(), webResource.getSampleRate(), webResource.getCodecName());
        facetTag = mediaTypeCode | (qualityCode << TagEncoding.SOUND_QUALITY.getBitPos());
        facetTags.add(facetTag);

        final Integer durationCode = getDurationCode(webResource.getDuration());
        facetTag = mediaTypeCode | (durationCode << TagEncoding.SOUND_DURATION.getBitPos());
        facetTags.add(facetTag);

        return facetTags;
    }
}
