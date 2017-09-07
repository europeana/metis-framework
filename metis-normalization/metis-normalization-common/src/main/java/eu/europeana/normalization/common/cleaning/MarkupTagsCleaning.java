/* LanguageNormalizer.java - created on 16/03/2016, Copyright (c) 2011 The European Library, all rights reserved */
package eu.europeana.normalization.common.cleaning;

import eu.europeana.normalization.common.NormalizeDetails;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import net.htmlparser.jericho.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main Class to be used by applications applying this lib's langage normalization techniques
 *
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 16/03/2016
 */
public class MarkupTagsCleaning extends EdmRecordNormalizerBase {

  private static final Logger LOGGER = LoggerFactory.getLogger(MarkupTagsCleaning.class);

  ;
  protected final Mode mode;
  MarkupCleaner cleaner;

  /**
   * Creates a new instance of this class.
   */
  public MarkupTagsCleaning() {
    this(Mode.ALL_MARKUP);
  }

  /**
   * Creates a new instance of this class.
   */
  public MarkupTagsCleaning(Mode mode) {
    super();
    this.mode = mode;
    if (mode == Mode.HTML_ONLY) {
      cleaner = new HtmlMarkupCleaner();
    } else {
      cleaner = new AllMarkupCleaner();
    }
  }

  public List<String> normalize(String htmlText) {
    String ret = cleaner.clean(htmlText);
    if (ret.length() == 0) {
      return Collections.EMPTY_LIST;
    }
    return new ArrayList<String>(1) {
      private static final long serialVersionUID = 1L;

      {
        add(ret);
      }
    };
  }

  public List<NormalizeDetails> normalizeDetailed(String htmlText) {
    String ret = cleaner.clean(htmlText);
    if (ret.length() == 0) {
      return Collections.emptyList();
    }
    return new ArrayList<NormalizeDetails>(1) {
      private static final long serialVersionUID = 1L;

      {
        add(new NormalizeDetails(ret, 1));
      }
    };
  }

  public enum Mode {HTML_ONLY, ALL_MARKUP}

  public interface MarkupCleaner {

    String clean(String input);
  }

  public class HtmlMarkupCleaner implements MarkupCleaner {

    private final String[] tagsTab = {"!doctype", "a", "abbr", "acronym", "address", "applet",
        "area", "article", "aside", "audio", "b", "base", "basefont", "bdi", "bdo", "bgsound",
        "big", "blink", "blockquote", "body", "br", "button", "canvas", "caption", "center", "cite",
        "code", "col", "colgroup", "content", "data", "datalist", "dd", "decorator", "del",
        "details", "dfn", "dir", "div", "dl", "dt", "element", "em", "embed", "fieldset",
        "figcaption", "figure", "font", "footer", "form", "frame", "frameset", "h1", "h2", "h3",
        "h4", "h5", "h6", "head", "header", "hgroup", "hr", "html", "i", "iframe", "img", "input",
        "ins", "isindex", "kbd", "keygen", "label", "legend", "li", "link", "listing", "main",
        "map", "mark", "marquee", "menu", "menuitem", "meta", "meter", "nav", "nobr", "noframes",
        "noscript", "object", "ol", "optgroup", "option", "output", "p", "param", "plaintext",
        "pre", "progress", "q", "rp", "rt", "ruby", "s", "samp", "script", "section", "select",
        "shadow", "small", "source", "spacer", "span", "strike", "strong", "style", "sub",
        "summary", "sup", "table", "tbody", "td", "template", "textarea", "tfoot", "th", "thead",
        "time", "title", "tr", "track", "tt", "u", "ul", "var", "video", "wbr", "xmp"};
    private Pattern pattern;


    public HtmlMarkupCleaner() {
      StringBuffer tags = new StringBuffer();
      for (int i = 0; i < tagsTab.length; i++) {
        tags.append(tagsTab[i].toLowerCase());
        if (i < tagsTab.length - 1) {
          tags.append('|');
        }
      }
      pattern = Pattern.compile("</?(" + tags.toString() + ")" +
              "((\\s+\\w+(\\s*=\\s*(?:\".*?\"|'.*?'|[^'\">\\s]+))?)+\\s*|\\s*)/?>"
          , Pattern.CASE_INSENSITIVE + Pattern.DOTALL);


    }

    public String clean(String input) {
      return pattern.matcher(input).replaceAll("");
    }


  }

  public class AllMarkupCleaner implements MarkupCleaner {

    @Override
    public String clean(String input) {
      Source source = new Source(input);
      return source.getTextExtractor().toString();
    }

  }


}
