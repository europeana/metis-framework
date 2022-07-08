package eu.europeana.metis.utils;

import eu.europeana.metis.exception.BadContentException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Contains functionality to parse and validate geo uri
 */
public final class GeoUriWGS84Parser {

  private static final String DECIMAL_POINT_REGEX = "(?:\\.\\d+)?";
  private static final String ZEROES_DECIMAL_POINT_REGEX = "(?:\\.0+)?";
  private static final String LATITUDE_REGEX =
      "^[+-]?(?:90" + ZEROES_DECIMAL_POINT_REGEX + "|(?:\\d|[1-8]\\d)" + DECIMAL_POINT_REGEX + ")$";
  private static final Pattern LATITUDE_PATTERN = Pattern.compile(LATITUDE_REGEX);
  private static final String LONGITUDE_REGEX =
      "^[+-]?(?:180" + ZEROES_DECIMAL_POINT_REGEX + "|(?:\\d|[1-9]\\d|1[0-7]\\d)" + DECIMAL_POINT_REGEX + ")$";
  private static final Pattern LONGITUDE_PATTERN = Pattern.compile(LONGITUDE_REGEX);
  private static final String ALTITUDE_REGEX = "^[+-]?\\d+" + DECIMAL_POINT_REGEX + "$";
  private static final Pattern ALTITUDE_PATTERN = Pattern.compile(ALTITUDE_REGEX);
  private static final String CRS_WGS_84 = "wgs84";
  private static final int MAX_NUMBER_COORDINATES = 3;
  private static final int MAX_DECIMAL_POINTS_TO_KEEP = 7;

  private GeoUriWGS84Parser() {
  }

  /**
   * Parse a provided geo uri in wgs84 coordinate reference system (CRS) and validate its contents.
   * <p>The parsing of the string follows closely but not exhaustively the specification located at
   * https://datatracker.ietf.org/doc/html/rfc5870</p>
   * <p>The checks that are performed to the provided string are as follows:
   * <ul>
   *   <li>There should not be any spaces</li>
   *   <li>It should start with "geo:"</li>
   *   <li>There should be at least one part after the scheme and that should be the coordinates</li>
   *   <li>If crs parameter is present it should be "wgs84"</li>
   *   <li>The "u" parameter should be just after crs if crs is present or just after the coordinates</li>
   *   <li>The coordinates should have 2 or 3 dimensions</li>
   *   <li>The coordinates should be of valid structure and valid range</li>
   *   <li>The coordinates if they have decimal points they will be truncated after 7th point</li>
   * </ul>
   * </p>
   *
   * @param geoUriString the geo uri string
   * @return the geo coordinates, null will never be returned
   * @throws BadContentException if the geo uri parsing encountered an error
   */
  public static GeoCoordinates parse(String geoUriString) throws BadContentException {
    final String[] geoUriParts = validateGeoUriAndGetParts(geoUriString);

    //Finally, check the coordinates part and validate
    return validateGeoCoordinatesAndGet(geoUriParts[0]);
  }

  private static String[] validateGeoUriAndGetParts(String geoUriString) throws BadContentException {
    //Validate that there aren't any space characters in the URI
    if (!geoUriString.matches("^\\S+$")) {
      throw new BadContentException("URI cannot have spaces");
    }
    //Validate geo URI
    if (!geoUriString.matches("^geo:.*$")) {
      throw new BadContentException("Invalid scheme value");
    }

    final String[] schemeAndParts = geoUriString.split(":");
    if (schemeAndParts.length <= 1) {
      throw new BadContentException("There are no parts in the geo URI");
    }

    //Find all parts
    final String[] geoUriParts = schemeAndParts[1].split(";");
    //Must be at least one part available
    if (geoUriParts.length < 1) {
      throw new BadContentException("Invalid geo uri parts length");
    }

    //Find all other parameters
    final LinkedList<GeoUriParameter> geoUriParameters = Arrays.stream(geoUriParts, 1, geoUriParts.length).map(s -> {
      final String[] split = s.split("=");
      return new GeoUriParameter(split[0], split[1]);
    }).collect(Collectors.toCollection(LinkedList::new));

    //If crs present, it must be the exact first after the dimensions. If not present then there is a default
    String crs = CRS_WGS_84;
    for (int i = 0; i < geoUriParameters.size(); i++) {
      if ("crs".equalsIgnoreCase(geoUriParameters.get(i).getName())) {
        crs = geoUriParameters.get(i).getValue();
        if (i != 0) {
          throw new BadContentException("Invalid geo uri 'crs' parameter position");
        }
      }
      if ("u".equalsIgnoreCase(geoUriParameters.get(i).getName()) && i > 1) {
        throw new BadContentException("Invalid geo uri 'u' parameter position");
      }
    }
    //Validate value of crs
    if (!CRS_WGS_84.equalsIgnoreCase(crs)) {
      throw new BadContentException(String.format("Crs parameter value is not %s", CRS_WGS_84));
    }
    return geoUriParts;
  }

  /**
   * Generate a geo coordinates from a geoUriPart string.
   * <p>The provided string is validated against:
   *   <ul>
   *     <li>the total coordinates available</li>
   *     <li>the validity of each number and its range</li>
   *     <li>the convertibility to a {@link Double}</li>
   *   </ul>
   *   The decimal points are also truncated up to a maximum allowed.
   * </p>
   *
   * @param geoUriPart the string that should contain the coordinates
   * @return the geo coordinates
   * @throws BadContentException if the geo coordinates were not valid
   */
  private static GeoCoordinates validateGeoCoordinatesAndGet(String geoUriPart) throws BadContentException {
    final String[] coordinates = geoUriPart.split(",");
    if (coordinates.length < 2 || coordinates.length > MAX_NUMBER_COORDINATES) {
      throw new BadContentException("Coordinates are not of valid length");
    }
    final Matcher latitudeMatcher = LATITUDE_PATTERN.matcher(coordinates[0]);
    final Matcher longitudeMatcher = LONGITUDE_PATTERN.matcher(coordinates[1]);
    final GeoCoordinates geoCoordinates;
    if (latitudeMatcher.matches() && longitudeMatcher.matches()) {
      Double altitude = null;
      if (coordinates.length == MAX_NUMBER_COORDINATES) {
        final Matcher altitudeMatcher = ALTITUDE_PATTERN.matcher(coordinates[2]);
        if (altitudeMatcher.matches()) {
          altitude = Double.parseDouble(truncateDecimalPoints(altitudeMatcher.group(0)));
        }
      }
      geoCoordinates = new GeoCoordinates(
          Double.parseDouble(truncateDecimalPoints(latitudeMatcher.group(0))),
          Double.parseDouble(truncateDecimalPoints(longitudeMatcher.group(0))), altitude);
    } else {
      throw new BadContentException("Coordinates are invalid");
    }
    return geoCoordinates;
  }

  private static String truncateDecimalPoints(String decimalNumber) {
    final String[] decimalNumberParts = decimalNumber.split("\\.");
    final StringBuilder decimalNumberTruncated = new StringBuilder();
    if (decimalNumberParts.length >= 1) {
      decimalNumberTruncated.append(decimalNumberParts[0]);
    }
    if (decimalNumberParts.length > 1) {
      decimalNumberTruncated.append(".");
      decimalNumberTruncated.append(decimalNumberParts[1], 0,
          Math.min(decimalNumberParts[1].length(), MAX_DECIMAL_POINTS_TO_KEEP));
    }
    return decimalNumberTruncated.toString();
  }

  /**
   * Class containing geo coordinates (latitude, longitude)
   */
  public static class GeoCoordinates {

    private final Double latitude;
    private final Double longitude;
    private final Double altitude;

    /**
     * Constructor with required parameters
     *
     * @param latitude the latitude
     * @param longitude the longitude
     * @param altitude the altitude
     */
    public GeoCoordinates(Double latitude, Double longitude, Double altitude) {
      this.latitude = latitude;
      this.longitude = longitude;
      this.altitude = altitude;
    }

    public Double getLatitude() {
      return latitude;
    }

    public Double getLongitude() {
      return longitude;
    }

    public Double getAltitude() {
      return altitude;
    }
  }

  /**
   * Class wrapping the name and value of geo uri parameters.
   */
  private static class GeoUriParameter {

    private final String name;
    private final String value;

    public GeoUriParameter(String name, String value) {
      this.name = name;
      this.value = value;
    }

    public String getName() {
      return name;
    }

    public String getValue() {
      return value;
    }
  }

}
