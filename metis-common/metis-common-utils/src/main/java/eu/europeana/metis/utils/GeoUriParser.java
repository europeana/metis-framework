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
public final class GeoUriParser {

  private static final String LATITUDE_PATTERN = "^[+-]?(?:90(?:\\.0{1,6})?|(?:[0-9]|[1-8][0-9])(?:\\.[0-9]{1,6})?)$";
  private static final String LONGITUDE_PATTERN = "^[+-]?(?:180(?:\\.0{1,6})?|(?:[0-9]|[1-9][0-9]|1[0-7][0-9])(?:\\.[0-9]{1,6})?)$";
  private static final Pattern latitudePattern = Pattern.compile(LATITUDE_PATTERN);
  private static final Pattern longitudePattern = Pattern.compile(LONGITUDE_PATTERN);
  private static final String CRS_WGS_84 = "wgs84";

  private GeoUriParser() {
  }

  /**
   * Parse a provided geo uri in wgs84 coordinate reference system (CRS) and validate it's contents.
   *
   * @param geoUriString the geo uri string
   * @return the geo coordinates
   * @throws BadContentException if the geo uri parsing encountered an error
   */
  public static GeoCoordinates parse(String geoUriString) throws BadContentException {
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

    //
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
      throw new BadContentException("Crs parameter value is not WGS-84");
    }

    //Finally, check the coordinates part and validate
    final String[] coordinates = geoUriParts[0].split(",");
    if (coordinates.length < 2) {
      throw new BadContentException("Coordinates are not of valid length");
    }
    final Matcher latitudeMatcher = latitudePattern.matcher(coordinates[0]);
    final Matcher longitudeMatcher = longitudePattern.matcher(coordinates[1]);
    final GeoCoordinates geoCoordinates;
    if (latitudeMatcher.matches() && longitudeMatcher.matches()) {
      geoCoordinates = new GeoCoordinates(Double.parseDouble(latitudeMatcher.group(0)),
          Double.parseDouble(longitudeMatcher.group(0))
      );
    } else {
      throw new BadContentException("Coordinates are invalid");
    }
    return geoCoordinates;
  }

  /**
   * Class containing geo coordinates (latitude, longitude)
   */
  public static class GeoCoordinates {

    private final Double latitude;
    private final Double longitude;

    /**
     * Constructor with required parameters
     *
     * @param latitude the latitude
     * @param longitude the longitude
     */
    public GeoCoordinates(Double latitude, Double longitude) {
      this.latitude = latitude;
      this.longitude = longitude;
    }

    public Double getLatitude() {
      return latitude;
    }

    public Double getLongitude() {
      return longitude;
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
