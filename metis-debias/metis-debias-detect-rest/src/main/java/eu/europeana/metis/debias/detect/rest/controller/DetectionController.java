package eu.europeana.metis.debias.detect.rest.controller;

import eu.europeana.metis.debias.detect.model.DeBiasResult;
import eu.europeana.metis.debias.detect.model.request.DetectionParameter;
import eu.europeana.metis.debias.detect.model.response.DetectionDeBiasResult;
import eu.europeana.metis.debias.detect.service.DetectService;
import eu.europeana.metis.utils.RestEndpoints;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Detection controller.
 */
@RestController
public class DetectionController {

  /**
   * The Detect service.
   */
  DetectService detectService;

  @Autowired
  public DetectionController(DetectService detectService) {
    this.detectService = detectService;
  }

  /**
   * DeBias detection result.
   *
   * @param detectionParameter {@link DetectionParameter} the detection parameter
   * @return {@link DetectionDeBiasResult} response of result
   */
  @PostMapping(value = RestEndpoints.DEBIAS_DETECTION, consumes = MediaType.APPLICATION_JSON_VALUE, produces = {
      MediaType.APPLICATION_JSON_VALUE})
  @Operation(description = "DeBias a list of values", responses = {@ApiResponse(responseCode = "200"),@ApiResponse(responseCode = "422")})
  public DeBiasResult debias(@RequestBody DetectionParameter detectionParameter) {
    return detectService.detect(detectionParameter);
  }
}
