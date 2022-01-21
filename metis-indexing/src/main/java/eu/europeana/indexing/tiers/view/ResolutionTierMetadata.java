package eu.europeana.indexing.tiers.view;

import static org.apache.commons.lang3.Validate.isTrue;

import eu.europeana.indexing.tiers.model.MediaTier;
import java.util.Objects;

/**
 * Class containing resolution tier metadata
 */
public class ResolutionTierMetadata {

  private final Long imageResolution;
  private final MediaTier imageResolutionTier;
  private final Long verticalResolution;
  private final MediaTier verticalResolutionTier;

  private ResolutionTierMetadata(Long imageResolution, MediaTier imageResolutionTier, Long verticalResolution,
      MediaTier verticalResolutionTier) {
    this.imageResolution = imageResolution;
    this.imageResolutionTier = imageResolutionTier;
    this.verticalResolution = verticalResolution;
    this.verticalResolutionTier = verticalResolutionTier;
  }

  ResolutionTierMetadata(ResolutionTierMetadata resolutionTierMetadataData) {
    this(resolutionTierMetadataData.getImageResolution(), resolutionTierMetadataData.getImageResolutionTier(),
        resolutionTierMetadataData.getVerticalResolution(), resolutionTierMetadataData.getVerticalResolutionTier());
  }

  public Long getImageResolution() {
    return imageResolution;
  }

  public MediaTier getImageResolutionTier() {
    return imageResolutionTier;
  }

  public Long getVerticalResolution() {
    return verticalResolution;
  }

  public MediaTier getVerticalResolutionTier() {
    return verticalResolutionTier;
  }

  /**
   * Class builder for creating {@link ResolutionTierMetadata}.
   */
  public static class ResolutionTierMetadataBuilder {

    private Long imageResolution;
    private MediaTier imageResolutionTier;
    private Long verticalResolution;
    private MediaTier verticalResolutionTier;

    public ResolutionTierMetadataBuilder setImageResolution(Long imageResolution) {
      this.imageResolution = imageResolution;
      return this;
    }

    public ResolutionTierMetadataBuilder setImageResolutionTier(MediaTier imageResolutionTier) {
      this.imageResolutionTier = imageResolutionTier;
      return this;
    }

    public ResolutionTierMetadataBuilder setVerticalResolution(Long verticalResolution) {
      this.verticalResolution = verticalResolution;
      return this;
    }

    public ResolutionTierMetadataBuilder setVerticalResolutionTier(MediaTier verticalResolutionTier) {
      this.verticalResolutionTier = verticalResolutionTier;
      return this;
    }

    /**
     * Creates an instance of {@link ResolutionTierMetadata} by verifying collected parameters.
     * <p>There are two dimensions of resolution tiers. Namely image and vertical. From those, both can be null or only one of
     * them non-null. An error will occur if both are non-null.</p>
     * <p>Furthermore the {@link ResolutionTierMetadataBuilder#imageResolution} and {@link
     * ResolutionTierMetadataBuilder#verticalResolution} will be nullified if they are zero.</p>
     *
     * @return the resolution tier metadata
     */
    public ResolutionTierMetadata build() {
      //Both null or only one of the two null
      final boolean areBothTiersNull = Objects.isNull(imageResolutionTier) && Objects.isNull(verticalResolutionTier);
      final boolean isOneOfTiersNull = Objects.isNull(imageResolutionTier) || Objects.isNull(verticalResolutionTier);
      isTrue(areBothTiersNull || isOneOfTiersNull);

      //If combinations okay nullify if zero
      imageResolution = (Objects.isNull(imageResolution) || imageResolution == 0) ? null : imageResolution;
      verticalResolution = (Objects.isNull(verticalResolution) || verticalResolution == 0) ? null : verticalResolution;

      return new ResolutionTierMetadata(imageResolution, imageResolutionTier, verticalResolution, verticalResolutionTier);
    }
  }
}
