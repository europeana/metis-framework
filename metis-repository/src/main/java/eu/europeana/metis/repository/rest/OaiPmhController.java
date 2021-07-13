package eu.europeana.metis.repository.rest;

import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for OAI-PMH harvesting.
 */
@RestController
@RequestMapping("oai")
@Tags(@Tag(name = OaiPmhController.CONTROLLER_TAG_NAME,
        description = "Controller providing access to OAI-PMH harvesting functionality."))
@Api(tags = OaiPmhController.CONTROLLER_TAG_NAME)
public class OaiPmhController {

  public static final String CONTROLLER_TAG_NAME = "OaiPmhController";

}
