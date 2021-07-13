package eu.europeana.metis.repository.rest;

import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for record management.
 */
@RestController
@RequestMapping("records")
@Tags(@Tag(name = RecordController.CONTROLLER_TAG_NAME,
        description = "Controller providing access to record management functionality."))
@Api(tags = RecordController.CONTROLLER_TAG_NAME)
public class RecordController {

  public static final String CONTROLLER_TAG_NAME = "RecordController";

}
