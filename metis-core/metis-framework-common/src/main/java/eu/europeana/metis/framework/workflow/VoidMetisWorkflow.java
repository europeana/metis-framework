package eu.europeana.metis.framework.workflow;

import java.util.List;
import java.util.Map;

/**
 * Created by ymamakis on 11/15/16.
 */
public class VoidMetisWorkflow implements AbstractMetisWorkflow {
    private String name;

    public VoidMetisWorkflow(){
        this.name="void";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setParameters(Map<String, List<String>> parameters) {

    }

    @Override
    public Map<String, List<String>> getParameters() {
        return null;
    }

    @Override
    public void execute() {
        System.out.println("Welcome to the jungle");
    }

    @Override
    public CloudStatistics monitor(String cloudUrl) {
        return null;
    }


    @Override
    public boolean supports(String s) {
        return s.equalsIgnoreCase(name);
    }
}
