package eu.europeana.metis.repository.rest;

import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for HTTP harvesting.
 */
@RestController
@RequestMapping("http")
@Tags(@Tag(name = HttpHarvestController.CONTROLLER_TAG_NAME,
        description = "Controller providing access to HTTP (zip) harvesting functionality."))
@Api(tags = HttpHarvestController.CONTROLLER_TAG_NAME)
public class HttpHarvestController {

  public static final String CONTROLLER_TAG_NAME = "HttpHarvestController";

}
