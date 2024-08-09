package eu.europeana.metis.debias.detect.rest.controller;

import eu.europeana.metis.debias.detect.model.DetectionParameter;
import eu.europeana.metis.debias.detect.model.DetectionResult;
import eu.europeana.metis.debias.detect.rest.exceptions.DebiasException;
import eu.europeana.metis.debias.detect.service.DetectService;
import eu.europeana.metis.utils.RestEndpoints;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DetectionController {

  DetectService detectService;

  @PostMapping(value = RestEndpoints.DEBIAS_DETECTION, consumes = MediaType.APPLICATION_JSON_VALUE, produces = {
      MediaType.APPLICATION_JSON_VALUE})
  @Operation(description = "DeBias a list of values", responses = {@ApiResponse(responseCode = "200")})
  public DetectionResult debias(@RequestBody DetectionParameter detectionParameter) {
    try {
      return detectService.detect(detectionParameter);
    } catch (RuntimeException e) {
      throw new DebiasException(e.getMessage());
    }
  }
}
