import eu.europeana.metis.mapping.model.Attribute;
import eu.europeana.metis.mapping.model.MappingType;
import eu.europeana.metis.mapping.model.SimpleMapping;
import eu.europeana.metis.mapping.validation.ValidationRule;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ymamakis on 9/14/16.
 */
public  class ElementContructor {
    public static <T extends Attribute> T constructElement(T elem, List<ValueDTO> values, List<ValidationRule> rule){
        List<SimpleMapping> sms= new ArrayList<>();
        for(ValueDTO value:values) {
            SimpleMapping sm = new SimpleMapping();
            if (value.isConstant()) {
                sm.setConstant(value.getValue());
                sm.setType(MappingType.CONSTANT);
            } else {
                sm.setSourceField(value.getValue());
                sm.setType(MappingType.XPATH);
            }

            sms.add(sm);
        }
        elem.setMappings(sms);
        if(rule!=null){
            elem.setRules(rule);
        }
        return elem;
    }
}
