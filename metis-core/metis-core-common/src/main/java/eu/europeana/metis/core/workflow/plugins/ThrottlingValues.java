package eu.europeana.metis.core.workflow.plugins;

/**
 * Class encapsulating all possible throttling levels tuples
 *
 * @author Joana Sousa (joana.sousa@europeana.eu)
 * @since 2022-08-12
 */
public class ThrottlingValues {

    private final int weak;
    private final int medium;
    private final int strong;

    /**
     * Constructor
     *
     * @param weak The throttling details to represent level weak
     * @param medium The throttling details to represent level medium
     * @param strong The throttling details to represent level strong
     */
    public ThrottlingValues(int weak, int medium, int strong) {
        this.weak = weak;
        this.medium = medium;
        this.strong = strong;
    }

    /**
     * Return the details related to weak throttling level
     * @return The details about throttling level weak
     */
    public int getWeak() {
        return weak;
    }

    /**
     * Return the details related to medium throttling level
     * @return The details about throttling level medium
     */
    public int getMedium() {
        return medium;
    }

    /**
     * Return the details related to strong throttling level
     * @return The details about throttling level strong
     */
    public int getStrong() {
        return strong;
    }

    public int getThreadNumberFromThrottlingLevel(ThrottlingLevel throttlingLevel){
        int result;
        switch (throttlingLevel){
            case MEDIUM:
                result = medium;
                break;
            case STRONG:
                result = strong;
                break;
            default:
                result = weak;
                break;
        }

        return result;
    }
}
