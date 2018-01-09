package eu.europeana.metis.core.common;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang3.StringUtils;

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
      "Hungary", "HU"), ICELAND("Iceland", "IS"), INDIA("India", "IN"), ITALY("ITALY",
      "IT"), ISRAEL("Israel", "IL"), JAPAN("Japan", "JP"), KAZAKHSTAN("Kazakhstan", "KZ"), KOREA(
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

  public static Country toCountry(String isoCode) {
    for (Country country : Country.values()) {
      if (StringUtils.equals(country.getIsoCode(), isoCode)) {
        return country;
      }
    }
    return null;
  }

  public static Country getCountryFromName(String name) {
    for (Country country : Country.values()) {
      if (country.getName().equals(name)) {
        return country;
      }
    }
    return null;
  }

//  @JsonCreator
  public static Country getCountryFromEnumName(String name) {
    for (Country country : Country.values()) {
      if (country.name().equalsIgnoreCase(name)) {
        return country;
      }
    }
    return null;
  }
}
