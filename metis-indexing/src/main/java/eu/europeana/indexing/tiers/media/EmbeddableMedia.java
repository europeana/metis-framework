package eu.europeana.indexing.tiers.media;

import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.WebResourceLinkType;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;

/**
 * This class determines whether a given web resource represents embeddable media.
 */
final class EmbeddableMedia {

  private static final Collection<String> PREFIXES = Arrays.asList(
      "urn:soundcloud:"
      , "http://player.vimeo.com/video/"
      , "https://vimeo.com/"
      , "https://sketchfab.com/models/"
      , "http://sounds.bl.uk/embed/"
      , "http://eusounds.ait.co.at/"
      , "http://images3.noterik.com/edna/domain/euscreenxl/"

      , "http://www.ccma.cat/tv3/alacarta/programa/titol/video/"
      , "http://www.ina.fr/video/"
      , "http://www.ina.fr/*/video/"
      , "http://api.picturepipe.net/api/html/widgets/public/playout_cloudfront?token="
      , "https://api.picturepipe.net/api/html/widgets/public/playout_cloudfront?token="
      , "http://archives.crem-cnrs.fr/archives/items/"
      , "http://www.theeuropeanlibrary.org/tel4/newspapers/issue/fullscreen/"
  );

  private EmbeddableMedia() {
  }

  /**
   * Determines whether the web resource represents embeddable media.
   *
   * @param entity The RDF entity.
   * @return Whether the resource represents embeddable media.
   */
  static boolean hasEmbeddableMedia(RdfWrapper entity) {
    return entity.getUrlsOfTypes(EnumSet.of(WebResourceLinkType.IS_SHOWN_BY)).stream()
        .anyMatch(EmbeddableMedia::isEmbeddableMedia);
  }

  private static boolean isEmbeddableMedia(String url) {
    return PREFIXES.stream().anyMatch(url::startsWith);
  }
}
