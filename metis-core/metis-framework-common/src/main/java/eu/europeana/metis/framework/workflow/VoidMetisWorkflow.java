package eu.europeana.metis.framework.workflow;

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
    public void setParameters(Map<String, String> parameters) {

    }

    @Override
    public Map<String, String> getParameters() {
        return null;
    }

    @Override
    public void execute() {
        System.out.println("Welcome to the jungle");
    }

    @Override
    public boolean supports(String s) {
        return s.equalsIgnoreCase(name);
    }
}
