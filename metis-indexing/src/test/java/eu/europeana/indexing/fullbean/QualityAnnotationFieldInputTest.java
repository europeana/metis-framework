package eu.europeana.indexing.fullbean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import eu.europeana.corelib.solr.entity.QualityAnnotationImpl;
import eu.europeana.metis.schema.jibx.Created;
import eu.europeana.metis.schema.jibx.HasBody;
import eu.europeana.metis.schema.jibx.HasTarget;
import eu.europeana.metis.schema.jibx.QualityAnnotation;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType.ConfidenceLevel;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType.Lang;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType.Resource;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType.WasGeneratedBy;
import java.math.BigDecimal;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

class QualityAnnotationFieldInputTest {

  @NotNull
  private static QualityAnnotation getQualityAnnotation() {
    QualityAnnotation qualityAnnotation = new QualityAnnotation();
    qualityAnnotation.setCreated(getCreated());
    HasBody hasBody = new HasBody();
    hasBody.setResource("bodyResource");
    HasTarget hasTarget = new HasTarget();
    hasTarget.setResource("targetResource");
    qualityAnnotation.setHasBody(hasBody);
    qualityAnnotation.setHasTargetList(List.of(hasTarget));
    return qualityAnnotation;
  }

  @NotNull
  private static Created getCreated() {
    Created created = new Created();
    Lang lang = new Lang();
    lang.setLang("NL");
    created.setLang(lang);
    created.setString("created");
    Resource resource = new Resource();
    resource.setResource("resource");
    created.setResource(resource);
    ConfidenceLevel confidenceLevel = new ConfidenceLevel();
    confidenceLevel.setConfidenceLevel(BigDecimal.TEN);
    created.setConfidenceLevel(confidenceLevel);
    WasGeneratedBy wasGeneratedBy = new WasGeneratedBy();
    wasGeneratedBy.setWasGeneratedBy("generatedBy");
    created.setWasGeneratedBy(wasGeneratedBy);
    return created;
  }

  @Test
  void apply() {
    QualityAnnotationFieldInput qualityAnnotationFieldInput = new QualityAnnotationFieldInput();

    QualityAnnotationImpl qualityAnnotationResult = qualityAnnotationFieldInput.apply(getQualityAnnotation());

    assertNotNull(qualityAnnotationResult);
    assertNull(qualityAnnotationResult.getAbout());
    assertEquals("created", qualityAnnotationResult.getCreated());
    assertEquals("bodyResource", qualityAnnotationResult.getBody());
    assertEquals("targetResource", qualityAnnotationResult.getTarget()[0]);
  }
}
