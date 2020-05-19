package eu.europeana.indexing.tiers.media;

import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.WebResourceLinkType;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class determines whether a given web resource represents embeddable media.
 * <p>The internal static collection of urls {@link #URL_MATCHING_LIST} that is used to match
 * embeddable media, contains urls that can contain a wildcard (*). The wildcard represents any
 * number of characters. Internally a wildcard is added to the end of each url as well. For example
 * the following url "http://www.ina.fr/intermediate_path/video/some_suffix" will be a successful
 * match.
 * </p>
 */
final class EmbeddableMedia {

  private static final Collection<String> URL_MATCHING_LIST = Arrays.asList(
      //SoundCould
      "http://soundcloud.com/",
      "https://soundcloud.com/",
      //Vimeo
      "http://player.vimeo.com/video/",
      "https://player.vimeo.com/video/",
      "https://vimeo.com/",
      "http://vimeo.com/",
      //YouTube
      "https://*.youtube.com/watch",
      "https://*.youtube.com/v/",
      "https://youtu.be/",
      //SketchFab
      "https://sketchfab.com/models/",
      "https://skfb.ly/",
      //Dismarc
      "http://www.dismarc.org/player/",
      "http://eusounds.ait.co.at/player/",
      //Europeana
      "http://www.ccma.cat/tv3/alacarta/programa/titol/video/",
      "http://www.ina.fr/video/",
      "http://www.ina.fr/*/video/",
      "http://api.picturepipe.net/api/html/widgets/public/playout_cloudfront?token=",
      "https://api.picturepipe.net/api/html/widgets/public/playout_cloudfront?token=",
      "http://archives.crem-cnrs.fr/archives/items/",
      "http://www.theeuropeanlibrary.org/tel4/newspapers/issue/fullscreen/",
      //BritishLibrary
      "http://sounds.bl.uk/embed/",
      //EUScreen
      "http://www.euscreen.eu/item.html"
  );

  // Create patterns from the urls, quote url, wildcards are allowed in the pattern so we do not quote those
  // and we also add a wildcard at the end of each url
  private static final Collection<Pattern> PATTERNS = URL_MATCHING_LIST.stream()
      .map(EmbeddableMedia::quotedRegexFromString).map(Pattern::compile)
      .collect(Collectors.toList());

  // Quote the string but not asterisk(*) characters. Asterisk character get converted to the regex
  // equivalent (.*).
  // Add \A at the beginning of the string to match beginning of input.
  // Add (.*) at the end of the string).
  private static String quotedRegexFromString(String string) {
    return Arrays.stream(string.split("\\*")).map(Pattern::quote)
        .collect(Collectors.joining(".*", "\\A", ".*"));
  }


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
    return PATTERNS.stream().map(pattern -> pattern.matcher(url)).anyMatch(Matcher::matches);
  }
}
