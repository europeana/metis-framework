package eu.europeana.metis.identifier;

import java.io.UnsupportedEncodingException;

/**
 * Created by ymamakis on 9/14/16.
 */
public class IdentifierClientMain {
    public static void main(String[] args){
        RestClient client = new RestClient();

        try {
            System.out.println(client.generateIdentifier("0000002", "#UEDIN:214"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
