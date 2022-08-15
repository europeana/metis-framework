package eu.europeana.metis.core.workflow.plugins;

/**
 * Tuple class that saves the throttling level together with the number of threads associated with it
 *
 * @author Joana Sousa (joana.sousa@europeana.eu)
 * @since 2022-08-12
 */
public class ThrottlingLevelValuePair {

    private final ThrottlingLevel throttlingLevel;
    private final int numberOfThreads;

    /**
     * Constructor
     *
     * @param throttlingLevel The level of the throttling as an enum
     * @param numberOfThreads The number of threads associated with the level
     */
    public ThrottlingLevelValuePair(ThrottlingLevel throttlingLevel, int numberOfThreads) {
        this.throttlingLevel = throttlingLevel;
        this.numberOfThreads = numberOfThreads;
    }

    /**
     * Returns the throttling level
     * @return The enum of throttling level
     */
    public ThrottlingLevel getThrottlingLevel() {
        return throttlingLevel;
    }

    /**
     * Returns the number of threads associated with the level
     * @return The number of threads
     */
    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    /**
     * An enum class representing all possible throttling levels
     */
    public enum ThrottlingLevel {
        WEAK,
        MEDIUM,
        STRONG
    }
}
