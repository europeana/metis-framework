package eu.europeana.metis.mediaprocessing;

import eu.europeana.metis.mediaprocessing.http.MimeTypeDetectHttpClient;
import java.io.IOException;
import java.net.URL;

public class MainTest {

  public static void main(String[] args) throws IOException {

    String url = "http://ogham.celt.dias.ie/resources/sites/Boleycarrigeen/50._Boleycarrigeen/50._Boleycarrigeen.obj";
    MimeTypeDetectHttpClient client = new MimeTypeDetectHttpClient(10000, 10000, 100000);
    System.out.println(client.download(new URL(url)));

  }

}
