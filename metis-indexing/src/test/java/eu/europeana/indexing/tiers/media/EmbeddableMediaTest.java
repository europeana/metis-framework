package eu.europeana.indexing.tiers.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.schema.jibx.HasMimeType;
import eu.europeana.metis.schema.jibx.WebResourceType;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests for {@link EmbeddableMedia} class
 */
class EmbeddableMediaTest {

  private static WebResourceType getResource(String mimeType) {
    WebResourceType webResourceType = new WebResourceType();
    HasMimeType hasMimeTypeXml = new HasMimeType();
    hasMimeTypeXml.setHasMimeType(mimeType);
    webResourceType.setHasMimeType(hasMimeTypeXml);

    return webResourceType;
  }

  private static Stream<Arguments> embeddableMedia() {
    return Stream.of(
        Arguments.of("http://sounds.bl.uk/embed/", true, List.of()),

        Arguments.of("http://eusounds.ait.co.at/player/", true, List.of()),
        Arguments.of("http://www.dismarc.org/player/", true, List.of()),

        Arguments.of("http://api.picturepipe.net/api/html/widgets/public/playout_cloudfront?token=", true, List.of()),
        Arguments.of("http://archives.crem-cnrs.fr/archives/items/", true, List.of()),
        Arguments.of("http://www.ccma.cat/tv3/alacarta/programa/titol/video/", true, List.of()),
        Arguments.of("http://www.ina.fr/*/video/", true, List.of()),
        Arguments.of("http://www.ina.fr/video/", true, List.of()),
        Arguments.of("http://www.theeuropeanlibrary.org/tel4/newspapers/issue/fullscreen/", true, List.of()),
        Arguments.of("https://api.picturepipe.net/api/html/widgets/public/playout_cloudfront?token=", true, List.of()),

        Arguments.of("http://www.euscreen.eu/item.html", true, List.of()),
        Arguments.of("https://www.euscreen.eu/item.html*", true, List.of()),

        Arguments.of("https://sketchfab.com/3d-models", true, List.of()),
        Arguments.of("https://sketchfab.com/models/", true, List.of()),
        Arguments.of("https://skfb.ly/", true, List.of()),

        Arguments.of("http://soundcloud.com/", true, List.of()),
        Arguments.of("https://soundcloud.com/", true, List.of()),

        Arguments.of("http://player.vimeo.com/video/", true, List.of()),
        Arguments.of("http://vimeo.com/", true, List.of()),
        Arguments.of("https://player.vimeo.com/video/", true, List.of()),
        Arguments.of("https://vimeo.com/", true, List.of()),

        Arguments.of("https://*.youtube.com/v/", true, List.of()),
        Arguments.of("https://*.youtube.com/watch", true, List.of()),
        Arguments.of("https://youtu.be/", true, List.of()),
        Arguments.of("https://www.google.com", false, List.of()),
        Arguments.of("https://get.webgl.org/", false, List.of()),
        Arguments.of("https://getemoji.com/", false, List.of()),
        Arguments.of("https://www.cssfontstack.com/", false, List.of()),
        Arguments.of("https://api64.ipify.org/?format=json", false, List.of()),

        Arguments.of("https://oembed.com/api/oembed.xml?url=https%3A%2F%2Fvimeo.com%2F24416915", true,
            List.of(getResource("application/xml+oembed"))),
        Arguments.of("https://oembed.com/api/oembed.json?url=https%3A%2F%2Fvimeo.com%2F24416915", true,
            List.of(getResource("application/json+oembed"))),
        Arguments.of("https://oembed.com/api/oembed?url=https%3A%2F%2Fvimeo.com%2F24416915", false,
            List.of(getResource("image/jpeg"))),
        Arguments.of("https://oembed.com/api/oembed?url=https%3A%2F%2Fvimeo.com%2F24416915", false,
            List.of(getResource("video/mp4")))
    );
  }

  @ParameterizedTest
  @MethodSource("embeddableMedia")
  void hasEmbeddableMedia(String url, boolean expectedEmbeddable, List<WebResourceType> resourceTypeList) {
    
    final RdfWrapper entity = mock(RdfWrapper.class);
    when(entity.getUrlsOfTypes(any())).thenReturn(Set.of(url));
    when(entity.getWebResources()).thenReturn(resourceTypeList);

    assertEquals(expectedEmbeddable, EmbeddableMedia.hasEmbeddableMedia(entity));
  }
}
