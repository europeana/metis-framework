package eu.europeana.indexing.solr.crf;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum TechnicalFacet {
  
  MEDIA_TYPE(25, 3),
  
  MIME_TYPE(15, 10, MediaType.TEXT, MediaType.IMAGE, MediaType.AUDIO, MediaType.VIDEO),

  IMAGE_SIZE(12, 3, MediaType.IMAGE),
  IMAGE_COLOUR_SPACE(10, 2, MediaType.IMAGE),
  IMAGE_ASPECT_RATIO(8, 2, MediaType.IMAGE),
  IMAGE_COLOUR_PALETTE(0, 8, MediaType.IMAGE),

  SOUND_QUALITY(13, 2, MediaType.AUDIO),
  VIDEO_QUALITY(13, 2, MediaType.VIDEO),

  SOUND_DURATION(10, 3, MediaType.AUDIO),
  VIDEO_DURATION(10, 3, MediaType.VIDEO);

  private static Map<MediaType, Set<TechnicalFacet>> facetsPerType = new EnumMap<>(MediaType.class);

  private final int bitPos;
  private final int numOfBits;
  private final Set<MediaType> mediaTypes;

  private TechnicalFacet(final int bitPos, final int numOfBits, MediaType... mediaTypes) {
    this.bitPos = bitPos;
    this.numOfBits = numOfBits;
    this.mediaTypes = Stream.of(mediaTypes).collect(Collectors.toSet());
  }

  public int getBitPos() {
    return bitPos;
  }

  // TODO JOCHEN never needed! Use it when doing bit shifting (which should be done here) to make
  // sure that nothing spills over (do & with mask).
  public int getNumOfBits() {
    return numOfBits;
  }

  private static Set<TechnicalFacet> findForMediaType(final MediaType mediaType) {
    return Arrays.stream(TechnicalFacet.values())
        .filter(facet -> facet.mediaTypes.contains(mediaType)).collect(Collectors.toSet());
  }

  public synchronized Set<TechnicalFacet> getFacets(final MediaType mediaType) {
    final Set<TechnicalFacet> result =
        facetsPerType.computeIfAbsent(mediaType, TechnicalFacet::findForMediaType);
    return Collections.unmodifiableSet(result);
  }
}
