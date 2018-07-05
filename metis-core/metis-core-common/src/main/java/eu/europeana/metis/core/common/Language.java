package eu.europeana.metis.core.common;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * The name of the dataset (enumerated)
 * Created by ymamakis on 2/17/16.
 */

@JsonSerialize(using = LanguageSerializer.class)
@JsonDeserialize(using = LanguageDeserializer.class)
public enum Language {

  AR("Arabic"), AZ("Azerbaijani"), BE("Belarusian"), BG("Bulgarian"), BS("Bosnian"), CA(
      "Catalan"), CNR("Montenegrin"), CS("Czech"), CY("Welsh"), DA("Danish"), DE("German"), EL(
      "Greek"), EN("English"), ES("Spanish"), ET("Estonian"), EU("Basque"), FI("Finnish"), FR(
      "French"), GA("Irish"), GD("Gaelic (Scottish)"), GL("Galician"), HE("Hebrew"), HI(
      "Hindi"), HR("Croatian (hrvatski jezik)"), HU("Hungarian"), HY("Armenian"), IE(
      "Interlingue"), IS("Icelandic"), IT("Italian"), JA("Japanese"), KA("Georgian"), KO(
      "Korean"), LT("Lithuanian"), LV("Latvian (Lettish)"), MK("Macedonian"), MT("Maltese"), MUL(
      "Multilingual Content"), NL("Dutch"), NO("Norwegian"), PL("Polish"), PT("Portugese"), RO(
      "Romanian"), RU("Russian"), SK("Slovak"), SL("Slovenian"), SQ("Albanian"), SR("Serbian"), SV(
      "Swedish"), TR("Turkish"), UK("Ukrainian"), YI("Yiddish"), ZH("Chinese");

  private String name;

  Language(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  /**
   * Lookup of a {@link Language} enum from a provided enum String representation of the enum value.
   * <p>e.g. if provided enumName is EL then the returned Language will be Language.EL</p>
   *
   * @param enumName the String representation of an enum value
   * @return the {@link Language} that represents the provided value or null if not found
   */
  public static Language getLanguageFromEnumName(String enumName) {
    for (Language language : Language.values()) {
      if (language.name().equalsIgnoreCase(enumName)) {
        return language;
      }
    }
    return null;
  }

  /**
   * Provides the languages sorted by the {@link #getName()} field
   *
   * @return the list of languages sorted
   */
  public static List<Language> getLanguageListSortedByName() {
    List<Language> languages = Arrays.asList(Language.values());
    languages.sort(Comparator.comparing(Language::getName));
    return languages;
  }
}
