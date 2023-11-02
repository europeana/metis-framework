package eu.europeana.indexing.tiers.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import eu.europeana.indexing.utils.RdfWrapper;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests for {@link EmbeddableMedia} class
 */
class EmbeddableMediaTest {

  @ParameterizedTest
  @MethodSource("embeddableMedia")
  void hasEmbeddableMedia(String url, boolean expectedEmbeddable) {
    final RdfWrapper entity = mock(RdfWrapper.class);
    when(entity.getUrlsOfTypes(any())).thenReturn(Set.of(url));
    assertEquals(expectedEmbeddable, EmbeddableMedia.hasEmbeddableMedia(entity));
  }

  private static Stream<Arguments> embeddableMedia() {
    return Stream.of(Arguments.of("http://sounds.bl.uk/embed/", true),

        Arguments.of("http://eusounds.ait.co.at/player/", true),
        Arguments.of("http://www.dismarc.org/player/", true),

        Arguments.of("http://api.picturepipe.net/api/html/widgets/public/playout_cloudfront?token=", true),
        Arguments.of("http://archives.crem-cnrs.fr/archives/items/", true),
        Arguments.of("http://www.ccma.cat/tv3/alacarta/programa/titol/video/", true),
        Arguments.of("http://www.ina.fr/*/video/", true),
        Arguments.of("http://www.ina.fr/video/", true),
        Arguments.of("http://www.theeuropeanlibrary.org/tel4/newspapers/issue/fullscreen/", true),
        Arguments.of("https://api.picturepipe.net/api/html/widgets/public/playout_cloudfront?token=", true),

        Arguments.of("http://www.euscreen.eu/item.html", true),
        Arguments.of("https://www.euscreen.eu/item.html*", true),

        Arguments.of("https://sketchfab.com/3d-models", true),
        Arguments.of("https://sketchfab.com/models/", true),
        Arguments.of("https://skfb.ly/", true),

        Arguments.of("http://soundcloud.com/", true),
        Arguments.of("https://soundcloud.com/", true),

        Arguments.of("http://player.vimeo.com/video/", true),
        Arguments.of("http://vimeo.com/", true),
        Arguments.of("https://player.vimeo.com/video/", true),
        Arguments.of("https://vimeo.com/", true),

        Arguments.of("https://*.youtube.com/v/", true),
        Arguments.of("https://*.youtube.com/watch", true),
        Arguments.of("https://youtu.be/", true),

        Arguments.of("https://www.google.com", false),
        Arguments.of("https://get.webgl.org/", false),
        Arguments.of("https://getemoji.com/", false),
        Arguments.of("https://www.cssfontstack.com/", false),
        Arguments.of("https://api64.ipify.org/?format=json", false));
  }
}
