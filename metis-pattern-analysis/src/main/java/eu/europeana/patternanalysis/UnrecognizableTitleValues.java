package eu.europeana.patternanalysis;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * P6 title values grouped by language for reference and maintenance. Each language lists unrecognizable titles first, followed by
 * N/A values. Matching uses the combined vocabulary regardless of a title's language tag.
 */
enum UnrecognizableTitleValues {
  ENGLISH(Set.of("Unknown", "No title", "Untitled", "Without title"),
      Set.of("n/a", "na", "n.a.", "n.d.")),
  BULGARIAN(Set.of("Неизвестен", "Без заглавие"),
      Set.of("n/a", "na", "n.a.", "н.д.")),
  CROATIAN(Set.of("Nepoznato", "Bez naslova"),
      Set.of("n/a", "na", "n.a.", "n/p")),
  CZECH(Set.of("Neznámý", "Bez názvu", "nezn."),
      Set.of("n/a", "na", "n.a.")),
  DANISH(Set.of("Ukendt", "Uden titel"),
      Set.of("n/a", "na", "n.a.", "i.a.")),
  DUTCH(Set.of("Onbekend", "Zonder titel", "z.t."),
      Set.of("n/a", "na", "n.a.", "n.v.t.")),
  ESTONIAN(Set.of("Tundmatu", "Pealkirjata"),
      Set.of("n/a", "na", "n.a.")),
  FINNISH(Set.of("Tuntematon", "Ei nimekettä"),
      Set.of("n/a", "na", "n.a.")),
  FRENCH(Set.of("Inconnu", "Sans titre", "inc.", "s.t."),
      Set.of("n/a", "na", "n.a.", "n.d.", "s.o.")),
  GERMAN(Set.of("Unbekannt", "Ohne Titel", "unb.", "o.T."),
      Set.of("n/a", "na", "n.a.", "k.a.")),
  GREEK(Set.of("Άγνωστο", "Χωρίς τίτλο"),
      Set.of("n/a", "na", "n.a.", "δ/υ")),
  HUNGARIAN(Set.of("Ismeretlen", "Cím nélkül", "c.n."),
      Set.of("n/a", "na", "n.a.")),
  IRISH(Set.of("Anaithnid", "Gan teideal"),
      Set.of("n/a", "na", "n.a.")),
  ITALIAN(Set.of("Sconosciuto", "Senza titolo", "s.t."),
      Set.of("n/a", "na", "n.a.", "n.d.")),
  LATVIAN(Set.of("Nezināms", "Bez nosaukuma"),
      Set.of("n/a", "na", "n.a.", "n/d")),
  LITHUANIAN(Set.of("Nežinomas", "Be pavadinimo"),
      Set.of("n/a", "na", "n.a.")),
  MALTESE(Set.of("Mhux magħruf", "Mingħajr titlu"),
      Set.of("n/a", "na", "n.a.")),
  POLISH(Set.of("Nieznany", "Bez tytułu", "niezn.", "b.t."),
      Set.of("n/a", "na", "n.a.", "b.d.", "n/d")),
  PORTUGUESE(Set.of("Desconhecido", "Sem título"),
      Set.of("n/a", "na", "n.a.", "n.d.", "n/d")),
  ROMANIAN(Set.of("Necunoscut", "Fără titlu"),
      Set.of("n/a", "na", "n.a.")),
  SLOVAK(Set.of("Neznámy", "Bez názvu"),
      Set.of("n/a", "na", "n.a.")),
  SLOVENIAN(Set.of("Neznan", "Brez naslova"),
      Set.of("n/a", "na", "n.a.")),
  SPANISH(Set.of("Desconocido", "Sin título"),
      Set.of("n/a", "na", "n.a.", "n.d.", "n/d")),
  SWEDISH(Set.of("Okänd", "Utan titel", "Ingen titel", "Okänt", "Saknas"),
      Set.of("n/a", "na", "n.a.", "e.j."));

  private static final Set<String> MATCHING_VALUES =
      Arrays.stream(values())
            .flatMap(language ->
                Stream.concat(language.titleValues.stream(), language.notAvailableValues.stream()))
            .map(value -> value.toLowerCase(Locale.ROOT))
            .collect(Collectors.toUnmodifiableSet());

  private final Set<String> titleValues;
  private final Set<String> notAvailableValues;

  UnrecognizableTitleValues(Set<String> titleValues, Set<String> notAvailableValues) {
    this.titleValues = titleValues;
    this.notAvailableValues = notAvailableValues;
  }

  /**
   * Matches a complete value, optionally enclosed in square brackets, or the special value "[...]".
   *
   * @param title the title value
   * @return whether the title is unrecognizable, ignoring case
   */
  static boolean matches(String title) {
    if ("[...]".equals(title)) {
      return true;
    }
    String value = title;
    if (value.startsWith("[") && value.endsWith("]")) {
      value = value.substring(1, value.length() - 1);
    }
    return MATCHING_VALUES.contains(value.toLowerCase(Locale.ROOT));
  }
}
