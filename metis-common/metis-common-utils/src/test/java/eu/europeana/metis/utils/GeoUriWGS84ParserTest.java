package eu.europeana.metis.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.utils.GeoUriWGS84Parser.GeoCoordinates;
import org.junit.jupiter.api.Test;

class GeoUriWGS84ParserTest {

  @Test
  void parse_invalid() {

    //URI cannot have spaces
    assertThrows(BadContentException.class, () -> GeoUriWGS84Parser.parse("geo: 37.786971,-122.399677"));
    assertThrows(BadContentException.class, () -> GeoUriWGS84Parser.parse("geo:37.786971,-122.399677; u=35"));

    //Non geo
    assertThrows(BadContentException.class, () -> GeoUriWGS84Parser.parse("test:"));

    //URI cannot be without dimensions
    assertThrows(BadContentException.class, () -> GeoUriWGS84Parser.parse("geo:"));
    //URI must have at least one part
    assertThrows(BadContentException.class, () -> GeoUriWGS84Parser.parse("geo:;"));

    //Validate order of crs and u parameters
    assertThrows(BadContentException.class, () -> GeoUriWGS84Parser.parse("geo:37.786971,-122.399677;u=35;crs=wgs84"));
    assertThrows(BadContentException.class,
        () -> GeoUriWGS84Parser.parse("geo:37.786971,-122.399677;crs=wgs84;parameter1=value1;u=35"));
    assertThrows(BadContentException.class,
        () -> GeoUriWGS84Parser.parse("geo:37.786971,-122.399677;parameter1=value1;crs=wgs84;u=35"));

    //Validate crs value
    assertThrows(BadContentException.class, () -> GeoUriWGS84Parser.parse("geo:37.786971,-122.399677;crs=Moon-2011;u=35"));

    //Coordinates must be present and of correct length
    assertThrows(BadContentException.class, () -> GeoUriWGS84Parser.parse("geo:;crs=wgs84"));
    assertThrows(BadContentException.class, () -> GeoUriWGS84Parser.parse("geo:37.786971,;crs=wgs84"));
    assertThrows(BadContentException.class, () -> GeoUriWGS84Parser.parse("geo:37.786971;crs=wgs84"));
    assertThrows(BadContentException.class, () -> GeoUriWGS84Parser.parse("geo:37.786971,100,100,10;crs=wgs84"));
    //Invalid coordinate
    assertThrows(BadContentException.class, () -> GeoUriWGS84Parser.parse("geo:test,-122.399677;crs=wgs84"));
    //Invalid range coordinates
    assertThrows(BadContentException.class, () -> GeoUriWGS84Parser.parse("geo:-100,200;crs=wgs84"));
    assertThrows(BadContentException.class, () -> GeoUriWGS84Parser.parse("geo:578991.875,578991.875"));
    assertThrows(BadContentException.class, () -> GeoUriWGS84Parser.parse("geo:-90.123456,100"));


  }

  @Test
  void parse_valid() throws Exception {
    assertDoesNotThrow(() -> GeoUriWGS84Parser.parse("geo:37.786971,-122.399677;crs=wgs84;u=35"));
    assertDoesNotThrow(() -> GeoUriWGS84Parser.parse("geo:37.786971,-122.399677;u=35"));
    assertDoesNotThrow(() -> GeoUriWGS84Parser.parse("geo:37.786971,-122.399677;crs=wgs84;u=35;parameter1=value1"));
    assertDoesNotThrow(() -> GeoUriWGS84Parser.parse("geo:37.786971,-122.399677;u=35;parameter1=value1"));

    assertDoesNotThrow(() -> GeoUriWGS84Parser.parse("geo:37.786971,-122.399677"));
    assertDoesNotThrow(() -> GeoUriWGS84Parser.parse("geo:37.786971,-122.399677,10"));
    assertDoesNotThrow(() -> GeoUriWGS84Parser.parse("geo:37.1234567,-122.1234567,10"));
    assertDoesNotThrow(() -> GeoUriWGS84Parser.parse("geo:37,-122"));

    final GeoCoordinates geoCoordinates = GeoUriWGS84Parser.parse("geo:37.786971,-122.399677");
    assertEquals(Double.parseDouble("37.786971"), geoCoordinates.getLatitude());
    assertEquals(Double.parseDouble("-122.399677"), geoCoordinates.getLongitude());

    final GeoCoordinates geoCoordinatesWithAltitude = GeoUriWGS84Parser.parse("geo:37.786971,-122.399677,1000.500600");
    assertEquals(Double.parseDouble("37.786971"), geoCoordinatesWithAltitude.getLatitude());
    assertEquals(Double.parseDouble("-122.399677"), geoCoordinatesWithAltitude.getLongitude());
    assertEquals(Double.parseDouble("1000.500600"), geoCoordinatesWithAltitude.getAltitude());

    //Should truncate the extra decimal points
    final GeoCoordinates geoCoordinatesWithLongDecimalPoints = GeoUriWGS84Parser.parse(
        "geo:40.123456789,45.123456789,1000.123456789");
    assertEquals(Double.parseDouble("40.1234567"), geoCoordinatesWithLongDecimalPoints.getLatitude());
    assertEquals(Double.parseDouble("45.1234567"), geoCoordinatesWithLongDecimalPoints.getLongitude());
    assertEquals(Double.parseDouble("1000.1234567"), geoCoordinatesWithLongDecimalPoints.getAltitude());
  }
}