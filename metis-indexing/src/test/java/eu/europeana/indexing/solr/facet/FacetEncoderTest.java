package eu.europeana.indexing.solr.facet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.indexing.solr.facet.FacetEncoder.FacetWithValues;
import eu.europeana.indexing.solr.facet.value.AudioDuration;
import eu.europeana.indexing.solr.facet.value.AudioQuality;
import eu.europeana.indexing.solr.facet.value.ImageAspectRatio;
import eu.europeana.indexing.solr.facet.value.ImageColorEncoding;
import eu.europeana.indexing.solr.facet.value.ImageColorSpace;
import eu.europeana.indexing.solr.facet.value.ImageSize;
import eu.europeana.indexing.solr.facet.value.MimeTypeEncoding;
import eu.europeana.indexing.solr.facet.value.VideoDuration;
import eu.europeana.indexing.solr.facet.value.VideoQuality;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.WebResourceWrapper;
import eu.europeana.metis.schema.jibx.Aggregation;
import eu.europeana.metis.schema.jibx.CodecName;
import eu.europeana.metis.schema.jibx.HasMimeType;
import eu.europeana.metis.schema.jibx.HasView;
import eu.europeana.metis.schema.jibx.IsShownAt;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.schema.jibx.Type1;
import eu.europeana.metis.schema.jibx.WebResourceType;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link FacetEncoder} class
 */
class FacetEncoderTest {

  private FacetEncoder encoder;

  Set<MimeTypeEncoding> audioMimeTypeEncodings = Set.of(MimeTypeEncoding.TYPE_589, MimeTypeEncoding.TYPE_590,
      MimeTypeEncoding.TYPE_591,
      MimeTypeEncoding.TYPE_592, MimeTypeEncoding.TYPE_593, MimeTypeEncoding.TYPE_594,
      MimeTypeEncoding.TYPE_595, MimeTypeEncoding.TYPE_596, MimeTypeEncoding.TYPE_597,
      MimeTypeEncoding.TYPE_598, MimeTypeEncoding.TYPE_599, MimeTypeEncoding.TYPE_600,
      MimeTypeEncoding.TYPE_601, MimeTypeEncoding.TYPE_602, MimeTypeEncoding.TYPE_603,
      MimeTypeEncoding.TYPE_604, MimeTypeEncoding.TYPE_605, MimeTypeEncoding.TYPE_606,
      MimeTypeEncoding.TYPE_607, MimeTypeEncoding.TYPE_608, MimeTypeEncoding.TYPE_609,
      MimeTypeEncoding.TYPE_610, MimeTypeEncoding.TYPE_611, MimeTypeEncoding.TYPE_612,
      MimeTypeEncoding.TYPE_613, MimeTypeEncoding.TYPE_614, MimeTypeEncoding.TYPE_615,
      MimeTypeEncoding.TYPE_616, MimeTypeEncoding.TYPE_617, MimeTypeEncoding.TYPE_618,
      MimeTypeEncoding.TYPE_619, MimeTypeEncoding.TYPE_620);
  Set<AudioQuality> audioQualities = Set.of(AudioQuality.HIGH);
  Set<AudioDuration> audioDurations = Set.of(AudioDuration.TINY, AudioDuration.SHORT, AudioDuration.MEDIUM, AudioDuration.LONG);

  Set<MimeTypeEncoding> videoMimeTypeEncodings = Set.of(
      MimeTypeEncoding.TYPE_726, MimeTypeEncoding.TYPE_727, MimeTypeEncoding.TYPE_728,
      MimeTypeEncoding.TYPE_729, MimeTypeEncoding.TYPE_730, MimeTypeEncoding.TYPE_731,
      MimeTypeEncoding.TYPE_732, MimeTypeEncoding.TYPE_733, MimeTypeEncoding.TYPE_734,
      MimeTypeEncoding.TYPE_735, MimeTypeEncoding.TYPE_736, MimeTypeEncoding.TYPE_737,
      MimeTypeEncoding.TYPE_738, MimeTypeEncoding.TYPE_739, MimeTypeEncoding.TYPE_740,
      MimeTypeEncoding.TYPE_741, MimeTypeEncoding.TYPE_742, MimeTypeEncoding.TYPE_743,
      MimeTypeEncoding.TYPE_744, MimeTypeEncoding.TYPE_745, MimeTypeEncoding.TYPE_746,
      MimeTypeEncoding.TYPE_747, MimeTypeEncoding.TYPE_748, MimeTypeEncoding.TYPE_749,
      MimeTypeEncoding.TYPE_750, MimeTypeEncoding.TYPE_751, MimeTypeEncoding.TYPE_752,
      MimeTypeEncoding.TYPE_753, MimeTypeEncoding.TYPE_754, MimeTypeEncoding.TYPE_755,
      MimeTypeEncoding.TYPE_756, MimeTypeEncoding.TYPE_757, MimeTypeEncoding.TYPE_758,
      MimeTypeEncoding.TYPE_759, MimeTypeEncoding.TYPE_760, MimeTypeEncoding.TYPE_761,
      MimeTypeEncoding.TYPE_762, MimeTypeEncoding.TYPE_763, MimeTypeEncoding.TYPE_764
  );
  Set<VideoQuality> videoQualities = Set.of(VideoQuality.HIGH);
  Set<VideoDuration> videoDurations = Set.of(VideoDuration.SHORT, VideoDuration.MEDIUM, VideoDuration.LONG);

  Set<MimeTypeEncoding> imageMimeTypeEncodings = Set.of(
      MimeTypeEncoding.TYPE_627, MimeTypeEncoding.TYPE_628, MimeTypeEncoding.TYPE_629,
      MimeTypeEncoding.TYPE_630, MimeTypeEncoding.TYPE_631, MimeTypeEncoding.TYPE_632,
      MimeTypeEncoding.TYPE_633, MimeTypeEncoding.TYPE_634, MimeTypeEncoding.TYPE_635,
      MimeTypeEncoding.TYPE_636, MimeTypeEncoding.TYPE_637, MimeTypeEncoding.TYPE_638,
      MimeTypeEncoding.TYPE_639, MimeTypeEncoding.TYPE_640, MimeTypeEncoding.TYPE_641,
      MimeTypeEncoding.TYPE_642, MimeTypeEncoding.TYPE_643, MimeTypeEncoding.TYPE_644,
      MimeTypeEncoding.TYPE_645, MimeTypeEncoding.TYPE_646, MimeTypeEncoding.TYPE_647,
      MimeTypeEncoding.TYPE_648, MimeTypeEncoding.TYPE_649, MimeTypeEncoding.TYPE_650,
      MimeTypeEncoding.TYPE_651, MimeTypeEncoding.TYPE_652, MimeTypeEncoding.TYPE_653,
      MimeTypeEncoding.TYPE_654, MimeTypeEncoding.TYPE_655, MimeTypeEncoding.TYPE_656,
      MimeTypeEncoding.TYPE_657, MimeTypeEncoding.TYPE_658, MimeTypeEncoding.TYPE_659,
      MimeTypeEncoding.TYPE_660, MimeTypeEncoding.TYPE_661, MimeTypeEncoding.TYPE_662,
      MimeTypeEncoding.TYPE_663, MimeTypeEncoding.TYPE_664, MimeTypeEncoding.TYPE_665,
      MimeTypeEncoding.TYPE_666, MimeTypeEncoding.TYPE_667, MimeTypeEncoding.TYPE_668,
      MimeTypeEncoding.TYPE_669, MimeTypeEncoding.TYPE_670, MimeTypeEncoding.TYPE_671,
      MimeTypeEncoding.TYPE_672
  );
  Set<ImageColorSpace> imageColorSpaces = Set.of(
      ImageColorSpace.GRAYSCALE, ImageColorSpace.COLOR, ImageColorSpace.OTHER
  );
  Set<ImageSize> imageSizes = Set.of(ImageSize.SMALL, ImageSize.MEDIUM, ImageSize.LARGE, ImageSize.HUGE);
  Set<ImageAspectRatio> imageAspectRatios = Set.of(ImageAspectRatio.LANDSCAPE, ImageAspectRatio.PORTRAIT);
  Set<ImageColorEncoding> imageColorEncodings = Set.of(ImageColorEncoding.COLOR_1,
      ImageColorEncoding.COLOR_2, ImageColorEncoding.COLOR_3, ImageColorEncoding.COLOR_4,
      ImageColorEncoding.COLOR_5, ImageColorEncoding.COLOR_6, ImageColorEncoding.COLOR_7,
      ImageColorEncoding.COLOR_8, ImageColorEncoding.COLOR_9, ImageColorEncoding.COLOR_10,
      ImageColorEncoding.COLOR_11, ImageColorEncoding.COLOR_12, ImageColorEncoding.COLOR_13,
      ImageColorEncoding.COLOR_14, ImageColorEncoding.COLOR_15, ImageColorEncoding.COLOR_16,
      ImageColorEncoding.COLOR_17, ImageColorEncoding.COLOR_18, ImageColorEncoding.COLOR_19,
      ImageColorEncoding.COLOR_20, ImageColorEncoding.COLOR_21, ImageColorEncoding.COLOR_22,
      ImageColorEncoding.COLOR_23, ImageColorEncoding.COLOR_24, ImageColorEncoding.COLOR_25,
      ImageColorEncoding.COLOR_26, ImageColorEncoding.COLOR_27, ImageColorEncoding.COLOR_28,
      ImageColorEncoding.COLOR_29, ImageColorEncoding.COLOR_30, ImageColorEncoding.COLOR_31,
      ImageColorEncoding.COLOR_32, ImageColorEncoding.COLOR_33, ImageColorEncoding.COLOR_34,
      ImageColorEncoding.COLOR_35, ImageColorEncoding.COLOR_36, ImageColorEncoding.COLOR_37,
      ImageColorEncoding.COLOR_38, ImageColorEncoding.COLOR_39, ImageColorEncoding.COLOR_40,
      ImageColorEncoding.COLOR_41, ImageColorEncoding.COLOR_42, ImageColorEncoding.COLOR_43,
      ImageColorEncoding.COLOR_44, ImageColorEncoding.COLOR_45, ImageColorEncoding.COLOR_46,
      ImageColorEncoding.COLOR_47, ImageColorEncoding.COLOR_48, ImageColorEncoding.COLOR_49,
      ImageColorEncoding.COLOR_50, ImageColorEncoding.COLOR_51, ImageColorEncoding.COLOR_52,
      ImageColorEncoding.COLOR_53, ImageColorEncoding.COLOR_54, ImageColorEncoding.COLOR_55,
      ImageColorEncoding.COLOR_56, ImageColorEncoding.COLOR_57, ImageColorEncoding.COLOR_58,
      ImageColorEncoding.COLOR_59, ImageColorEncoding.COLOR_60, ImageColorEncoding.COLOR_61,
      ImageColorEncoding.COLOR_62, ImageColorEncoding.COLOR_63, ImageColorEncoding.COLOR_64,
      ImageColorEncoding.COLOR_65, ImageColorEncoding.COLOR_66, ImageColorEncoding.COLOR_67,
      ImageColorEncoding.COLOR_68, ImageColorEncoding.COLOR_69, ImageColorEncoding.COLOR_70,
      ImageColorEncoding.COLOR_71, ImageColorEncoding.COLOR_72, ImageColorEncoding.COLOR_73,
      ImageColorEncoding.COLOR_74, ImageColorEncoding.COLOR_75, ImageColorEncoding.COLOR_76,
      ImageColorEncoding.COLOR_77, ImageColorEncoding.COLOR_78, ImageColorEncoding.COLOR_79,
      ImageColorEncoding.COLOR_80, ImageColorEncoding.COLOR_81, ImageColorEncoding.COLOR_82,
      ImageColorEncoding.COLOR_83, ImageColorEncoding.COLOR_84, ImageColorEncoding.COLOR_85,
      ImageColorEncoding.COLOR_86, ImageColorEncoding.COLOR_87, ImageColorEncoding.COLOR_88,
      ImageColorEncoding.COLOR_89, ImageColorEncoding.COLOR_90, ImageColorEncoding.COLOR_91,
      ImageColorEncoding.COLOR_92, ImageColorEncoding.COLOR_93, ImageColorEncoding.COLOR_94,
      ImageColorEncoding.COLOR_95, ImageColorEncoding.COLOR_96, ImageColorEncoding.COLOR_97,
      ImageColorEncoding.COLOR_98, ImageColorEncoding.COLOR_99, ImageColorEncoding.COLOR_100,
      ImageColorEncoding.COLOR_101, ImageColorEncoding.COLOR_102, ImageColorEncoding.COLOR_103,
      ImageColorEncoding.COLOR_104, ImageColorEncoding.COLOR_105, ImageColorEncoding.COLOR_106,
      ImageColorEncoding.COLOR_107, ImageColorEncoding.COLOR_108, ImageColorEncoding.COLOR_109,
      ImageColorEncoding.COLOR_110, ImageColorEncoding.COLOR_111, ImageColorEncoding.COLOR_112,
      ImageColorEncoding.COLOR_113, ImageColorEncoding.COLOR_114, ImageColorEncoding.COLOR_115,
      ImageColorEncoding.COLOR_116, ImageColorEncoding.COLOR_117, ImageColorEncoding.COLOR_118,
      ImageColorEncoding.COLOR_119, ImageColorEncoding.COLOR_120, ImageColorEncoding.COLOR_121,
      ImageColorEncoding.COLOR_122, ImageColorEncoding.COLOR_123, ImageColorEncoding.COLOR_124,
      ImageColorEncoding.COLOR_125, ImageColorEncoding.COLOR_126, ImageColorEncoding.COLOR_127,
      ImageColorEncoding.COLOR_128, ImageColorEncoding.COLOR_129, ImageColorEncoding.COLOR_130,
      ImageColorEncoding.COLOR_131, ImageColorEncoding.COLOR_132, ImageColorEncoding.COLOR_133,
      ImageColorEncoding.COLOR_134, ImageColorEncoding.COLOR_135, ImageColorEncoding.COLOR_136,
      ImageColorEncoding.COLOR_137, ImageColorEncoding.COLOR_138);
  Set<MimeTypeEncoding> textMimeTypeEncodings = Set.of(
      MimeTypeEncoding.TYPE_621, MimeTypeEncoding.TYPE_622, MimeTypeEncoding.TYPE_623,
      MimeTypeEncoding.TYPE_624, MimeTypeEncoding.TYPE_625, MimeTypeEncoding.TYPE_626);

  List<WebResourceWrapper> getBasicWebResourceWrappers() {
    Type1 type1 = new Type1();
    type1.setResource("resource");

    CodecName codecName = new CodecName();
    codecName.setCodecName("codec");

    HasMimeType hasMimeType = new HasMimeType();
    hasMimeType.setHasMimeType("hasMimeType");

    Aggregation aggregation = new Aggregation();
    aggregation.setAbout("about");

    HasView hasView = new HasView();
    hasView.setResource("resourceHasView");
    aggregation.setHasViewList(List.of(hasView));

    IsShownAt isShownAt = new IsShownAt();
    isShownAt.setResource("resourceIsShown");
    aggregation.setIsShownAt(isShownAt);

    WebResourceType webResourceType = new WebResourceType();
    webResourceType.setType(type1);
    webResourceType.setCodecName(codecName);
    webResourceType.setHasMimeType(hasMimeType);
    webResourceType.setAbout("about");

    RDF rdf = new RDF();
    rdf.setAggregationList(List.of(aggregation));
    rdf.setWebResourceList(List.of(webResourceType));

    RdfWrapper rdfWrapper = new RdfWrapper(rdf);

    List<WebResourceWrapper> webResourceWrappers = rdfWrapper.getWebResourceWrappers();
    return webResourceWrappers;
  }

  @BeforeEach
  void setup() {
    encoder = new FacetEncoder();
  }

  @Test
  void getFacetSearchCodes() {
    Set<Integer> resultAudio = FacetEncoder.getFacetSearchCodes(EncodedFacetCollection.AUDIO,
        new FacetWithValues<>(EncodedFacet.AUDIO_QUALITY, audioQualities));

    assertEquals(1, resultAudio.size());
    assertTrue(resultAudio.contains(67117056));
  }

  @Test
  void getAudioFacetSearchCodes() {
    Set<Integer> result = encoder.getAudioFacetSearchCodes(audioMimeTypeEncodings, audioQualities, audioDurations);

    assertEquals(128, result.size());
  }

  @Test
  void getVideoFacetSearchCodes() {
    Set<Integer> result = encoder.getVideoFacetSearchCodes(videoMimeTypeEncodings, videoQualities, videoDurations);

    assertEquals(117, result.size());
  }

  @Test
  void getImageFacetSearchCodes() {
    Set<Integer> result = encoder.getImageFacetSearchCodes(imageMimeTypeEncodings, imageSizes, imageColorSpaces,
        imageAspectRatios,
        imageColorEncodings);

    assertEquals(152352, result.size());
  }

  @Test
  void getTextFacetSearchCodes() {
    Set<Integer> result = encoder.getTextFacetSearchCodes(textMimeTypeEncodings);

    assertEquals(6, result.size());
  }

  @Test
  void getFacetFilterCodes() {
    List<WebResourceWrapper> webResourceWrappers = getBasicWebResourceWrappers();

    Set<Integer> result = encoder.getFacetFilterCodes(webResourceWrappers.getFirst());

    assertEquals(0, result.size());
  }

  @Test
  void getAudioFacetFilterCodes() {
    Set<Integer> result = encoder.getAudioFacetFilterCodes(audioMimeTypeEncodings, audioQualities, audioDurations);

    assertEquals(330, result.size());
  }

  @Test
  void getVideoFacetFilterCodes() {
    Set<Integer> result = encoder.getVideoFacetFilterCodes(videoMimeTypeEncodings, videoQualities, videoDurations);

    assertEquals(320, result.size());
  }

  @Test
  void getImageFacetFilterCodes() {
    Set<Integer> result = encoder.getImageFacetFilterCodes(imageMimeTypeEncodings, imageSizes, imageColorSpaces,
        imageAspectRatios, imageColorEncodings);

    assertEquals(391980, result.size());
  }

  @Test
  void getTextFacetFilterCodes() {
    Set<Integer> result = encoder.getTextFacetFilterCodes(textMimeTypeEncodings);

    assertEquals(7, result.size());
  }

  @Test
  void getFacetValueCodes() {
    List<WebResourceWrapper> webResourceWrappers = getBasicWebResourceWrappers();

    Set<Integer> result = encoder.getFacetValueCodes(webResourceWrappers.getFirst());

    assertEquals(0, result.size());
  }

  @Test
  void getAudioFacetValueCodes() {
    Set<Integer> result = encoder.getAudioFacetValueCodes(audioMimeTypeEncodings, audioQualities, audioDurations);

    assertEquals(37, result.size());
  }

  @Test
  void getVideoFacetValueCodes() {
    Set<Integer> result = encoder.getVideoFacetValueCodes(videoMimeTypeEncodings, videoQualities, videoDurations);

    assertEquals(43, result.size());
  }

  @Test
  void getImageFacetValueCodes() {
    Set<Integer> result = encoder.getImageFacetValueCodes(imageMimeTypeEncodings, imageSizes, imageColorSpaces, imageAspectRatios,
        imageColorEncodings);

    assertEquals(193, result.size());
  }

  @Test
  void getTextFacetValueCodes() {
    Set<Integer> result = encoder.getTextFacetValueCodes(textMimeTypeEncodings);

    assertEquals(6, result.size());
  }
}
