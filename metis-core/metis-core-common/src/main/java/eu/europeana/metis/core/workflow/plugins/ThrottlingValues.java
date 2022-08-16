package eu.europeana.metis.core.workflow.plugins;

/**
 * Class encapsulating all possible throttling levels tuples
 *
 * @author Joana Sousa (joana.sousa@europeana.eu)
 * @since 2022-08-12
 */
public class ThrottlingValues {

    private final ThrottlingLevelValuePair weak;
    private final ThrottlingLevelValuePair medium;
    private final ThrottlingLevelValuePair strong;

    /**
     * Constructor
     *
     * @param weak The throttling details to represent level weak
     * @param medium The throttling details to represent level medium
     * @param strong The throttling details to represent level strong
     */
    public ThrottlingValues(ThrottlingLevelValuePair weak, ThrottlingLevelValuePair medium, ThrottlingLevelValuePair strong) {
        this.weak = weak;
        this.medium = medium;
        this.strong = strong;
    }

    /**
     * Return the details related to weak throttling level
     * @return The details about throttling level weak
     */
    public ThrottlingLevelValuePair getWeak() {
        return weak;
    }

    /**
     * Return the details related to medium throttling level
     * @return The details about throttling level medium
     */
    public ThrottlingLevelValuePair getMedium() {
        return medium;
    }

    /**
     * Return the details related to strong throttling level
     * @return The details about throttling level strong
     */
    public ThrottlingLevelValuePair getStrong() {
        return strong;
    }

    public int getThreadNumberFromLevel(ThrottlingLevelValuePair.ThrottlingLevel throttlingLevel){
        int result;
        switch (throttlingLevel){
            case MEDIUM:
                result = medium.getNumberOfThreads();
                break;
            case STRONG:
                result = strong.getNumberOfThreads();
                break;
            default:
                result = weak.getNumberOfThreads();
                break;
        }

        return result;
    }
}
