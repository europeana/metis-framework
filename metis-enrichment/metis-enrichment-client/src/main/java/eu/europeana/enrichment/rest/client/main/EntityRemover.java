package eu.europeana.enrichment.rest.client.main;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.springframework.web.client.RestTemplate;
import eu.europeana.enrichment.api.external.UriList;

/**
 *
 * @author ymamakis
 */
public class EntityRemover {


    public static void main(String... args) {
        if (args == null || args.length < 2) {
            System.out.println("No targetUrl/file provided");
            System.out.println("Syntax is remove.sh <enrichment_endpoint> <csv_location>");
            return;
        }

        if (!new File(args[1]).exists() || !new File(args[1]).isFile()) {
            System.out.println("File does not exist or is not a file");
            System.out.println("Syntax is remove.sh <enrichment_endpoint> <csv_location>");
            return;
        }
        EntityRemover remover = new EntityRemover();
        remover.delete(args[1], args[0]);
    }

    private void delete(String filePath, String urlPath) {
        try {
            UriList lst = new UriList();
            lst.setUris(FileUtils.readLines(new File(filePath)));

            RestTemplate template = new RestTemplate();
            template.delete(urlPath,lst);


        } catch (IOException ex) {
            Logger.getLogger(EntityRemover.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
