package eu.europeana.indexing.tiers.view;

import static org.apache.commons.lang3.Validate.isTrue;

import eu.europeana.indexing.tiers.model.MediaTier;
import java.util.Objects;

public class ResolutionTierMetadataData {

  private final Long imageResolution;
  private final MediaTier imageResolutionTier;
  private final Long verticalResolution;
  private final MediaTier verticalResolutionTier;

  private ResolutionTierMetadataData(Long imageResolution, MediaTier imageResolutionTier, Long verticalResolution,
      MediaTier verticalResolutionTier) {
    this.imageResolution = imageResolution;
    this.imageResolutionTier = imageResolutionTier;
    this.verticalResolution = verticalResolution;
    this.verticalResolutionTier = verticalResolutionTier;
  }

  ResolutionTierMetadataData(ResolutionTierMetadataData resolutionTierMetadataData) {
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

  public static class ResolutionTierPreInitializationBuilder {

    private Long imageResolution;
    private MediaTier imageResolutionTier;
    private Long verticalResolution;
    private MediaTier verticalResolutionTier;

    public ResolutionTierPreInitializationBuilder setImageResolution(Long imageResolution) {
      this.imageResolution = imageResolution;
      return this;
    }

    public ResolutionTierPreInitializationBuilder setImageResolutionTier(MediaTier imageResolutionTier) {
      this.imageResolutionTier = imageResolutionTier;
      return this;
    }

    public ResolutionTierPreInitializationBuilder setVerticalResolution(Long verticalResolution) {
      this.verticalResolution = verticalResolution;
      return this;
    }

    public ResolutionTierPreInitializationBuilder setVerticalResolutionTier(MediaTier verticalResolutionTier) {
      this.verticalResolutionTier = verticalResolutionTier;
      return this;
    }

    public ResolutionTierMetadataData createResolutionTierData() {
      //Both null or only one of the two null
      final boolean areBothTiersNull = Objects.isNull(imageResolutionTier) && Objects.isNull(verticalResolutionTier);
      final boolean isOneOfTiersNull = Objects.isNull(imageResolutionTier) || Objects.isNull(verticalResolutionTier);
      isTrue(areBothTiersNull || isOneOfTiersNull);

      //If combinations okay nullify if zeroed
      imageResolution = (Objects.isNull(imageResolution) || imageResolution == 0) ? null : imageResolution;
      verticalResolution = (Objects.isNull(verticalResolution) || verticalResolution == 0) ? null : verticalResolution;

      return new ResolutionTierMetadataData(imageResolution, imageResolutionTier, verticalResolution, verticalResolutionTier);
    }
  }
}
