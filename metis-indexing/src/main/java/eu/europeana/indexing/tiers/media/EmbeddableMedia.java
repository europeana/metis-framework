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

  // Create pattern that will contain all special regex characters and with that
  // we'll find those characters on other Strings to properly escape them
  static final String ESCAPED_REGEX_SPECIAL_CHARACTERS = "<([{^\\-=$!|]})?*+.>".chars()
      .mapToObj(c -> "\\" + (char) c).collect(Collectors.joining());
  static final Pattern escapedCharactersPattern = Pattern
      .compile("[" + ESCAPED_REGEX_SPECIAL_CHARACTERS + "]");

  // Create patterns from the urls, escape all regex special characters,
  // we want to use wildcards(*) so we unescape them and add a wildcard at the end of each url
  private static final Collection<Pattern> PATTERNS = URL_MATCHING_LIST.stream()
      .map(escapedCharactersPattern::matcher).map(matcher -> matcher.replaceAll("\\\\$0"))
      .map(prefix -> prefix.replace("\\*", ".*"))
      .map(prefix -> prefix.concat(".*")).map(Pattern::compile)
      .collect(Collectors.toList());

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
