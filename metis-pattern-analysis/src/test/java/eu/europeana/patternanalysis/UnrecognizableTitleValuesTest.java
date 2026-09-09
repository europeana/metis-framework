package eu.europeana.patternanalysis;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UnrecognizableTitleValuesTest {

  @ParameterizedTest
  @ValueSource(strings = {
      "Unknown", "No title", "Untitled", "Without title",
      "Неизвестен", "Без заглавие",
      "Nepoznato", "Bez naslova",
      "Neznámý", "Bez názvu", "nezn.",
      "Ukendt", "Uden titel",
      "Onbekend", "Zonder titel", "z.t.",
      "Tundmatu", "Pealkirjata",
      "Tuntematon", "Ei nimekettä",
      "Inconnu", "Sans titre", "inc.", "s.t.",
      "Unbekannt", "Ohne Titel", "unb.", "o.T.",
      "Άγνωστο", "Χωρίς τίτλο",
      "Ismeretlen", "Cím nélkül", "c.n.",
      "Anaithnid", "Gan teideal",
      "Sconosciuto", "Senza titolo",
      "Nezināms", "Bez nosaukuma",
      "Nežinomas", "Be pavadinimo",
      "Mhux magħruf", "Mingħajr titlu",
      "Nieznany", "Bez tytułu", "niezn.", "b.t.",
      "Desconhecido", "Sem título",
      "Necunoscut", "Fără titlu",
      "Neznámy", "Neznan", "Brez naslova",
      "Desconocido", "Sin título",
      "Okänd", "Utan titel", "Ingen titel", "Okänt", "Saknas"
  })
  void matchesUnrecognizableTitles(String title) {
    assertTrue(UnrecognizableTitleValues.matches(title));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "n/a", "na", "n.a.", "n.d.", "n/d", "s.o.", "k.a.", "n.v.t.", "i.a.", "e.j.", "b.d.", "н.д.", "δ/υ", "n/p"
  })
  void matchesNotAvailableValues(String title) {
    assertTrue(UnrecognizableTitleValues.matches(title));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "unknown", "UNKNOWN", "UnKnOwN", "SANS TITRE", "НЕИЗВЕСТЕН", "ΆΓΝΩΣΤΟ", "OKÄND", "N/A",
      "[unknown]", "[no title]", "[UnTiTlEd]", "[SANS TITRE]", "[n/a]", "[N.V.T.]", "[...]"
  })
  void matchesCaseAndBracketedVariants(String title) {
    assertTrue(UnrecognizableTitleValues.matches(title));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "", " ", "[unknown", "unknown]", "[[unknown]]", "(unknown)", "[]",
      "...", "nXaX", "n.a", "<br>"
  })
  void doesNotMatchOtherValues(String title) {
    assertFalse(UnrecognizableTitleValues.matches(title));
  }
}
