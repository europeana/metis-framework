package eu.europeana.metis.core.common;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Countries supported by METIS
 */
@JsonSerialize(using = CountrySerializer.class)
@JsonDeserialize(using = CountryDeserializer.class)
public enum Country {

  ALBANIA("Albania", "AL"), ANDORRA("Andorra", "AD"), ARMENIA("Armenia", "AM"), AUSTRIA("Austria",
      "AUT"), AZERBAIJAN("Azerbaijan", "AZ"), AUSTRALIA("Australia", "AU"), BELARUS("Belarus",
      "BY"), BELGIUM("Belgium", "BE"), BOSNIA_AND_HERZEGOVINA("Bosnia and Herzegovina",
      "BA"), BULGARIA("Bulgaria", "BG"), CANADA("Canada", "CA"), CHINA("China", "CN"), CROATIA(
      "Croatia", "HR"), CYRPUS("Cyprus", "CY"), CZECH_REPUBLIC("Czech Republic", "CZ"), DENMARK(
      "Denmark", "DK"), ESTONIA("Estonia", "EE"), EUROPE("Europe", "EU"), FINLAND("Finland",
      "FI"), FRANCE("France", "FR"), GEORGIA("Georgia", "GE"), GERMANY("Germany", "DE"), GREECE(
      "Greece", "GR"), HOLY_SEE_VATICAN_CITY_STATE("Holy See (Vatican City State)", "VA"), HUNGARY(
      "Hungary", "HU"), ICELAND("Iceland", "IS"), INDIA("India", "IN"), IRELAND("Ireland",
      "IE"), ITALY("Italy", "IT"), ISRAEL("Israel", "IL"), JAPAN("Japan", "JP"), KAZAKHSTAN("Kazakhstan", "KZ"), KOREA(
      "Republic of Korea", "KR"), LATVIA("Latvia", "LV"), LEBANON("Lebanon", "LB"), LIECHTENSTEIN(
      "Liecthenstein", "LI"), LITHUANIA("Lithuania", "LT"), LUXEMBOURG("Luxembourg",
      "LU"), MACEDONIA("Macedonia (the former Yugoslav Republic of)", "MK"), MALTA("Malta",
      "MT"), MOLDOVA("Moldova (Republic of)", "MD"), MONACO("Monaco", "MC"), MONTENEGRO(
      "Montenegro", "ME"), NETHERLANDS("Netherlands", "NL"), NORWAY("Norway", "NO"), POLAND(
      "Poland", "PO"), PORTUGAL("Portugal", "PO"), ROMANIA("Romania", "RO"), RUSSIA(
      "Russian Federation", "RU"), SAN_MARINO("San Marino", "SM"), SERBIA("Serbia", "RS"), SLOVAKIA(
      "Slovakia", "SK"), SLOVENIA("Slovenia", "SI"), SPAIN("Spain", "ES"), SWEDEN("Sweden",
      "SE"), SWITZERLAND("Switzerland", "CH"), TURKEY("Turkey", "TR"), UKRAINE("Ukraine",
      "UA"), UNITED_KINGDOM("United Kingdom of Great Britain and Northern Ireland",
      "GB"), UNITED_STATES("United States of America", "US");

  private String name;
  private String isoCode;

  Country(String name, String isoCode) {
    this.name = name;
    this.isoCode = isoCode;
  }

  public String getName() {
    return this.name;
  }

  public String getIsoCode() {
    return this.isoCode;
  }

  /**
   * Lookup of a {@link Country} enum from a provided enum String representation of the enum value.
   * <p>e.g. if provided enumName is GREECE then the returned Country will be Country.GREECE</p>
   * @param enumName the String representation of an enum value
   * @return the {@link Country} that represents the provided value or null if not found
   */
  public static Country getCountryFromEnumName(String enumName) {
    for (Country country : Country.values()) {
      if (country.name().equalsIgnoreCase(enumName)) {
        return country;
      }
    }
    return null;
  }

  /**
   * Lookup of a {@link Country} enum from a provided enum String representation of the enum value.
   * <p>e.g. if provided enumName is GREECE then the returned Country will be Country.GREECE</p>
   * @param isoCode the String representation of an isoCode
   * @return the {@link Country} that represents the provided value or null if not found
   */
  public static Country getCountryFromIsoCode(String isoCode) {
    for (Country country : Country.values()) {
      if (country.getIsoCode().equalsIgnoreCase(isoCode)) {
        return country;
      }
    }
    return null;
  }
}
