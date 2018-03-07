package eu.europeana.validation.rest.exceptions;

/**
 * Created by ymamakis on 2/24/16.
 */
public class ValidationException extends Exception {

    private final String id;

    private final String nodeId;
    /**
     * Creates class instance
     */
    public ValidationException() {
        super();
        id = null;
        nodeId = null;
    }

    /**
     * Creates exception instance based on provided parameters
     * @param id record id that causes the problem
     * @param nodeId
     * @param message message provided by validation engine
     */
    public ValidationException(String id, String nodeId, String message) {
        super(message);
        this.id = id;
        this.nodeId = nodeId;
    }

    public String getId() {
        return this.id;
    }

    public String getNodeId() {  return nodeId; }
}
