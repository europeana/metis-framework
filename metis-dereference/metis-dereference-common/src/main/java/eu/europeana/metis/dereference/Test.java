package eu.europeana.metis.dereference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class Test {

  public static void main(String[] args) throws InterruptedException {
    for (int i = 0; i < 20000; i++) {
      if (i % 100 == 0) {
        Thread.sleep(1000);
      }
      new Thread(() -> {
        try {
          URL url = new URL(
                  "http://localhost:8080/metis_dereference_rest_war_exploded/dereference?uri=https%3A%2F%2Fd-nb.info%2Fgnd%2F10000666-8");
          HttpURLConnection con = (HttpURLConnection) url.openConnection();
          con.setRequestMethod("GET");
          BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
          String inputLine;
          StringBuffer content = new StringBuffer();
          while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
          }
          System.out.println(content);
          in.close();
          con.disconnect();
        } catch (MalformedURLException e) {
          e.printStackTrace();
        } catch (ProtocolException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }).start();
    }
  }
}
