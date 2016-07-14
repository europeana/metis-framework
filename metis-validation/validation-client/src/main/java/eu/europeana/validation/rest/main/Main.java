package eu.europeana.validation.rest.main;

import eu.europeana.validation.rest.ValidationClient;

/**
 * Created by ymamakis on 12/24/15.
 */
public class Main {

    public static void main(String[] args){
        if (args.length==3){
            ValidationClient client = new ValidationClient();
            try{
            if (args[0].equals("single")){
                client.validateSingle(args[1],args[2]);
            } else {
                client.validateBatch(args[1],args[2]);
            }}
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
