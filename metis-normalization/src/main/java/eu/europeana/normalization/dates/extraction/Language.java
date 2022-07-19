package eu.europeana.normalization.dates.extraction;

import java.util.Locale;

/**
 * The official languages of the European Union. Used in the PatternMonthName
 */
public enum Language {
  Bulgarian(new Locale("bg")), Croatian(new Locale("hr")), Czech(new Locale("cs")), Danish(new Locale("da")),
  Dutch(new Locale("nl")), English(new Locale("en")), Estonian(new Locale("et")), Finnish(new Locale("fi")),
  French(new Locale("fr")), German(new Locale("de")), Greek(new Locale("el")), Hungarian(new Locale("hu")),
  Irish(new Locale("ga")), Italian(new Locale("it")), Latvian(new Locale("lv")), Lithuanian(new Locale("lt")),
  Maltese(new Locale("mt")), Polish(new Locale("pl")), Portuguese(new Locale("pt")), Romanian(new Locale("ro")),
  Slovak(new Locale("sk")), Slovenian(new Locale("sl")), Spanish(new Locale("es")), Swedish(new Locale("sv"));

  Locale locale;

  private Language(Locale locale) {
    this.locale = locale;
  }

  public Locale getLocale() {
    return locale;
  }

}
