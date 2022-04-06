package eu.europeana.metis.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.utils.GeoUriParser.GeoCoordinates;
import org.junit.jupiter.api.Test;

class GeoUriParserTest {

  @Test
  void parse() throws Exception {

    //URI cannot have spaces
    assertThrows(BadContentException.class, () -> GeoUriParser.parse("geo: 37.786971,-122.399677"));
    assertThrows(BadContentException.class, () -> GeoUriParser.parse("geo:37.786971,-122.399677; u=35"));

    //Non geo
    assertThrows(BadContentException.class, () -> GeoUriParser.parse("test:"));

    //URI cannot be without dimensions
    assertThrows(BadContentException.class, () -> GeoUriParser.parse("geo:"));
    //URI must have at least one part
    assertThrows(BadContentException.class, () -> GeoUriParser.parse("geo:;"));

    //Validate order of crs and u parameters
    assertThrows(BadContentException.class, () -> GeoUriParser.parse("geo:37.786971,-122.399677;u=35;crs=wgs84"));
    assertThrows(BadContentException.class,
        () -> GeoUriParser.parse("geo:37.786971,-122.399677;crs=wgs84;parameter1=value1;u=35"));
    assertThrows(BadContentException.class,
        () -> GeoUriParser.parse("geo:37.786971,-122.399677;parameter1=value1;crs=wgs84;u=35"));

    //Validate crs value
    assertThrows(BadContentException.class, () -> GeoUriParser.parse("geo:37.786971,-122.399677;crs=Moon-2011;u=35"));

    //Coordinates must be present and of correct length
    assertThrows(BadContentException.class, () -> GeoUriParser.parse("geo:;crs=wgs84"));
    assertThrows(BadContentException.class, () -> GeoUriParser.parse("geo:37.786971,;crs=wgs84"));
    assertThrows(BadContentException.class, () -> GeoUriParser.parse("geo:37.786971;crs=wgs84"));
    assertThrows(BadContentException.class, () -> GeoUriParser.parse("geo:37.786971,100,100,10;crs=wgs84"));
    //Invalid coordinate
    assertThrows(BadContentException.class, () -> GeoUriParser.parse("geo:test,-122.399677;crs=wgs84"));
    //Invalid range coordinates
    assertThrows(BadContentException.class, () -> GeoUriParser.parse("geo:-100,200;crs=wgs84"));

    assertDoesNotThrow(() -> GeoUriParser.parse("geo:37.786971,-122.399677;crs=wgs84;u=35"));
    assertDoesNotThrow(() -> GeoUriParser.parse("geo:37.786971,-122.399677;u=35"));
    assertDoesNotThrow(() -> GeoUriParser.parse("geo:37.786971,-122.399677;crs=wgs84;u=35;parameter1=value1"));
    assertDoesNotThrow(() -> GeoUriParser.parse("geo:37.786971,-122.399677;u=35;parameter1=value1"));

    assertDoesNotThrow(() -> GeoUriParser.parse("geo:37.786971,-122.399677"));
    assertDoesNotThrow(() -> GeoUriParser.parse("geo:37.786971,-122.399677,10"));
    assertDoesNotThrow(() -> GeoUriParser.parse("geo:37,-122"));

    final GeoCoordinates geoCoordinates = GeoUriParser.parse("geo:37.786971,-122.399677");
    assertEquals(Double.parseDouble("37.786971"), geoCoordinates.getLatitude());
    assertEquals(Double.parseDouble("-122.399677"), geoCoordinates.getLongitude());

    final GeoCoordinates geoCoordinatesWithAltitude = GeoUriParser.parse("geo:37.786971,-122.399677,1000.500600");
    assertEquals(Double.parseDouble("37.786971"), geoCoordinatesWithAltitude.getLatitude());
    assertEquals(Double.parseDouble("-122.399677"), geoCoordinatesWithAltitude.getLongitude());
    assertEquals(Double.parseDouble("1000.500600"), geoCoordinatesWithAltitude.getAltitude());
  }
}