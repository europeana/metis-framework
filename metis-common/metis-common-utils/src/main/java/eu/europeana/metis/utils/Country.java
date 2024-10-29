package eu.europeana.metis.utils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Countries supported by METIS
 */
public enum Country {

  //@formatter:off
  ALBANIA("Albania", "AL"),
  ANDORRA("Andorra", "AD"),
  ARMENIA("Armenia", "AM"),
  AUSTRIA("Austria", "AT"),
  AZERBAIJAN("Azerbaijan", "AZ"),
  AUSTRALIA("Australia", "AU"),
  BELARUS("Belarus", "BY"),
  BELGIUM("Belgium", "BE"),
  BOSNIA_AND_HERZEGOVINA("Bosnia and Herzegovina", "BA"),
  BULGARIA("Bulgaria", "BG"),
  CANADA("Canada", "CA"),
  CHINA("China", "CN"),
  CROATIA("Croatia", "HR"),
  CYPRUS("Cyprus", "CY"),
  CZECH_REPUBLIC("Czech Republic", "CZ"),
  DENMARK("Denmark", "DK"),
  ESTONIA("Estonia", "EE"),
  EUROPE("Europe", "EU"),
  FINLAND("Finland", "FI"),
  FRANCE("France", "FR"),
  GEORGIA("Georgia", "GE"),
  GERMANY("Germany", "DE"),
  GREECE("Greece", "GR"),
  HOLY_SEE_VATICAN_CITY_STATE("Holy See (Vatican City State)", "VA"),
  HUNGARY("Hungary", "HU"),
  ICELAND("Iceland", "IS"),
  INDIA("India", "IN"),
  IRELAND("Ireland", "IE"),
  ITALY("Italy", "IT"),
  ISRAEL("Israel", "IL"),
  JAPAN("Japan", "JP"),
  KAZAKHSTAN("Kazakhstan", "KZ"),
  KOREA("Korea, Republic of", "KR"),
  LATVIA("Latvia", "LV"),
  LEBANON("Lebanon", "LB"),
  LIECHTENSTEIN("Liechtenstein", "LI"),
  LITHUANIA("Lithuania", "LT"),
  LUXEMBOURG("Luxembourg", "LU"),
  NORTH_MACEDONIA("North Macedonia", "MK"),
  MALTA("Malta", "MT"),
  MOLDOVA("Moldova", "MD"),
  MONACO("Monaco", "MC"),
  MONTENEGRO("Montenegro", "ME"),
  NETHERLANDS("Netherlands", "NL"),
  NORWAY("Norway", "NO"),
  POLAND("Poland", "PL"),
  PORTUGAL("Portugal", "PT"),
  ROMANIA("Romania", "RO"),
  RUSSIA("Russia", "RU"),
  SAN_MARINO("San Marino", "SM"),
  SERBIA("Serbia", "RS"),
  SLOVAKIA("Slovakia", "SK"),
  SLOVENIA("Slovenia", "SI"),
  SPAIN("Spain", "ES"),
  SWEDEN("Sweden", "SE"),
  SWITZERLAND("Switzerland", "CH"),
  TURKEY("Turkey", "TR"),
  UKRAINE("Ukraine", "UA"),
  UNITED_KINGDOM("United Kingdom", "GB"),
  UNITED_STATES("United States of America", "US");
  //@formatter:on

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
   *
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
   * Lookup of a {@link Country} enum from a provided ISO code.
   * <p>e.g. if provided code is GR then the returned Country will be Country.GREECE</p>
   *
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

  /**
   * Method that returns the enum with the corresponding given string value
   * @param countryName The string value to match the enum values with
   * @return The enum country values that matches the given string value
   */
  public static Country fromCountryNameToIsoCode(String countryName){
    Country result = null;
    for(Country country : values()){
      if(country.getName().equals(countryName)){
        result = country;
        break;
      }
    }

    if(result == null){
      throw new IllegalArgumentException("Country name "+countryName+" not found");
    }

    return result;

  }

  /**
   * Provides the countries sorted by the {@link #getName()} field
   *
   * @return the list of countries sorted
   */
  public static List<Country> getCountryListSortedByName() {
    List<Country> countries = Arrays.asList(Country.values());
    countries.sort(Comparator.comparing(Country::getName));
    return countries;
  }
}
