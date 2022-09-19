package eu.europeana.normalization.dates.extraction;

import java.util.Locale;

/**
 * The official languages of the European Union. Used in the PatternMonthName
 */
public enum EuropeanLanguage {
  BULGARIAN(new Locale("bg")),
  CROATIAN(new Locale("hr")),
  CZECH(new Locale("cs")),
  DANISH(new Locale("da")),
  DUTCH(new Locale("nl")),
  ENGLISH(new Locale("en")),
  ESTONIAN(new Locale("et")),
  FINNISH(new Locale("fi")),
  FRENCH(new Locale("fr")),
  GERMAN(new Locale("de")),
  GREEK(new Locale("el")),
  HUNGARIAN(new Locale("hu")),
  IRISH(new Locale("ga")),
  ITALIAN(new Locale("it")),
  LATVIAN(new Locale("lv")),
  LITHUANIAN(new Locale("lt")),
  MALTESE(new Locale("mt")),
  POLISH(new Locale("pl")),
  PORTUGUESE(new Locale("pt")),
  ROMANIAN(new Locale("ro")),
  SLOVAK(new Locale("sk")),
  SLOVENIAN(new Locale("sl")),
  SPANISH(new Locale("es")),
  SWEDISH(new Locale("sv"));

  private final Locale locale;

  EuropeanLanguage(Locale locale) {
    this.locale = locale;
  }

  public Locale getLocale() {
    return locale;
  }

}
