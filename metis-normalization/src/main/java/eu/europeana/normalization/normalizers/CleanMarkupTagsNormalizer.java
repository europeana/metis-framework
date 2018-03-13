package eu.europeana.normalization.normalizers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.htmlparser.jericho.Source;

/**
 * This normalizer removes markup (HTML) tags from text values.
 *
 * @author Nuno Freire (nfreire@gmail.com)
 */
public class CleanMarkupTagsNormalizer extends EdmValueNormalizer {

  /**
   * The modes that are supported by this normalizer.
   */
  public enum Mode {

    /** This cleans HTML code only. **/
    HTML_ONLY(new HtmlMarkupCleaner()),

    /** This cleans all markup. **/
    ALL_MARKUP(input -> new Source(input).getTextExtractor().toString());

    private final Function<String, String> cleaner;

    private Mode(Function<String, String> cleaner) {
      this.cleaner = cleaner;
    }
  }

  private final Mode mode;

  /**
   * Constructor for the default mode (which is {@value Mode#ALL_MARKUP}).
   */
  public CleanMarkupTagsNormalizer() {
    this(Mode.ALL_MARKUP);
  }

  /**
   * Constructor.
   * 
   * @param mode The mode of this normalizer.
   */
  public CleanMarkupTagsNormalizer(Mode mode) {
    this.mode = mode;
  }

  @Override
  public List<NormalizedValueWithConfidence> normalizeValue(String htmlText) {
    final String ret = mode.cleaner.apply(htmlText);
    if (ret.length() == 0) {
      return Collections.emptyList();
    }
    return Collections.singletonList(new NormalizedValueWithConfidence(ret, 1));
  }

  private static class HtmlMarkupCleaner implements Function<String, String> {

    private static final String[] HTML_TAGS_TO_CLEAN =
        {"!doctype", "a", "abbr", "acronym", "address", "applet", "area", "article", "aside",
            "audio", "b", "base", "basefont", "bdi", "bdo", "bgsound", "big", "blink", "blockquote",
            "body", "br", "button", "canvas", "caption", "center", "cite", "code", "col",
            "colgroup", "content", "data", "datalist", "dd", "decorator", "del", "details", "dfn",
            "dir", "div", "dl", "dt", "element", "em", "embed", "fieldset", "figcaption", "figure",
            "font", "footer", "form", "frame", "frameset", "h1", "h2", "h3", "h4", "h5", "h6",
            "head", "header", "hgroup", "hr", "html", "i", "iframe", "img", "input", "ins",
            "isindex", "kbd", "keygen", "label", "legend", "li", "link", "listing", "main", "map",
            "mark", "marquee", "menu", "menuitem", "meta", "meter", "nav", "nobr", "noframes",
            "noscript", "object", "ol", "optgroup", "option", "output", "p", "param", "plaintext",
            "pre", "progress", "q", "rp", "rt", "ruby", "s", "samp", "script", "section", "select",
            "shadow", "small", "source", "spacer", "span", "strike", "strong", "style", "sub",
            "summary", "sup", "table", "tbody", "td", "template", "textarea", "tfoot", "th",
            "thead", "time", "title", "tr", "track", "tt", "u", "ul", "var", "video", "wbr", "xmp"};

    private final Pattern pattern;

    public HtmlMarkupCleaner() {
      final String tags = Arrays.stream(HTML_TAGS_TO_CLEAN).map(String::toLowerCase)
          .map(Pattern::quote).collect(Collectors.joining("|"));
      final String regex = "</?(" + tags + ")"
          + "((\\s+\\w+(\\s*=\\s*(?:\".*?\"|'.*?'|[^'\">\\s]+))?)+\\s*|\\s*)/?>";
      pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
    }

    @Override
    public String apply(String input) {
      return pattern.matcher(input).replaceAll("");
    }
  }
}
