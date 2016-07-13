import eu.europeana.validation.edm.rest.main.Main;

/**
 * Created by ymamakis on 12/24/15.
 */
public class MainTest {
    public static void main(String[] args){
        String[] arg = new String[3];
        arg[0]="single";
        arg[1]="EDM-INTERNAL";
        arg[2]="/home/ymamakis/test_validation/Item_35834473.xml";
        Main.main(arg);
    }
}
