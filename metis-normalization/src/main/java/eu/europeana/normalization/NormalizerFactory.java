package eu.europeana.normalization;

import java.util.ArrayList;
import java.util.List;
import eu.europeana.normalization.normalizers.ChainedNormalizer;
import eu.europeana.normalization.normalizers.RecordNormalizeAction;
import eu.europeana.normalization.settings.NormalizerSettings;
import eu.europeana.normalization.util.NormalizationConfigurationException;

/**
 * This class creates instances of {@link Normalizer}. The default steps for a normalization are:
 * <ol>
 * <li>{@link NormalizerStep#CLEAN_SPACE_CHARACTERS}</li>
 * <li>{@link NormalizerStep#CLEAN_MARKUP_TAGS}</li>
 * <li>{@link NormalizerStep#NORMALIZE_LANGUAGE_REFERENCES}</li>
 * <li>{@link NormalizerStep#REMOVE_DUPLICATE_STATEMENTS}</li>
 * </ol>
 * But an alternative order may be provided, and steps may be omitted or performed more than once.
 */
public class NormalizerFactory {

  private final NormalizerSettings settings;

  private static final NormalizerStep[] DEFAULT_NORMALIZER_STEPS =
      {NormalizerStep.CLEAN_SPACE_CHARACTERS, NormalizerStep.CLEAN_MARKUP_TAGS,
          NormalizerStep.NORMALIZE_LANGUAGE_REFERENCES, NormalizerStep.REMOVE_DUPLICATE_STATEMENTS};

  /**
   * Constructor for default settings.
   */
  public NormalizerFactory() {
    this(new NormalizerSettings());
  }

  /**
   * Constructor.
   * 
   * @param settings The settings to be applied to this normalization.
   */
  public NormalizerFactory(NormalizerSettings settings) {
    this.settings = settings;
  }

  /**
   * This method creates a normalizer with the default steps and order.
   * 
   * @return A normalizer.
   * @throws NormalizationConfigurationException In case the normalizer could not be set up.
   */
  public Normalizer getNormalizer() throws NormalizationConfigurationException {
    return getNormalizer(DEFAULT_NORMALIZER_STEPS);
  }

  /**
   * This method creates a normalizer.
   * 
   * @param normalizerSteps The steps to be performed and the order in which to perform them.
   * @return A normalizer.
   * @throws NormalizationConfigurationException In case the normalizer could not be set up.
   */
  public Normalizer getNormalizer(NormalizerStep... normalizerSteps)
      throws NormalizationConfigurationException {

    // Sanity checks
    if (normalizerSteps == null || normalizerSteps.length == 0) {
      throw new NormalizationConfigurationException("The list of steps cannot be empty.", null);
    }

    // Create actions for the steps.
    final List<RecordNormalizeAction> actions = new ArrayList<>(normalizerSteps.length);
    for (NormalizerStep step : normalizerSteps) {
      actions.add(step.createAction(settings).getAsRecordNormalizer());
    }

    // Create chain normalizer and use it to set up the normalizer.
    return new NormalizerImpl(new ChainedNormalizer(actions.toArray(new RecordNormalizeAction[0])));
  }
}
