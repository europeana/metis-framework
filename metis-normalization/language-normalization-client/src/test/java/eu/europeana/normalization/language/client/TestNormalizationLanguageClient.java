package eu.europeana.normalization.language.client;


import eu.europeana.normalization.language.client.NormalizationLanguageClient;

/**
 * Command line test for the NormalizationLanguageClient
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 16/05/2016
 */
public class TestNormalizationLanguageClient {

    public static void main(String[] args) {
        if (args.length >= 1) {
            NormalizationLanguageClient client = new NormalizationLanguageClient();
            try {
                System.out.println(client.normalize(args[0]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else
            System.out.println("No parameter with value to normalize was set. Exiting...");
    }
}
