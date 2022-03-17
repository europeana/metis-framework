package eu.europeana.metis.dereference.rest;

import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.service.DereferencingManagementService;
import eu.europeana.metis.dereference.vocimport.exception.VocabularyImportException;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.utils.RestEndpoints;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * REST controller for managing vocabularies and entities Created by gmamakis on 12-2-16.
 */
@Controller
@Api("/")
public class DereferencingManagementController {

  private static final Logger LOGGER = LoggerFactory.getLogger(DereferencingManagementController.class);

  private final DereferencingManagementService service;
  private final List<String> validUrlPrefixes;

  @Autowired
  public DereferencingManagementController(DereferencingManagementService service, List<String> validUrlPrefixes) {
    this.service = service;
    this.validUrlPrefixes = new ArrayList<>(validUrlPrefixes);
  }

  /**
   * Retrieve a list of all the registered vocabularies
   *
   * @return The List of all the registered vocabularies
   */
  @GetMapping(value = RestEndpoints.VOCABULARIES, produces = "application/json")
  @ResponseBody
  @ApiOperation(value = "Retrieve all the vocabularies", response = List.class)
  public List<Vocabulary> getAllVocabularies() {
    return service.getAllVocabularies();
  }

  /**
   * Empty Cache. This will remove ALL entries in the cache (Redis). If the same redis instance/cluster is used for multiple
   * services then the cache for other services is cleared as well.
   */
  @DeleteMapping(value = RestEndpoints.CACHE_EMPTY)
  @ResponseBody
  @ApiOperation(value = "Empty the cache")
  public void emptyCache() {
    service.emptyCache();
  }

  /**
   * Load the vocabularies from an online source. This does NOT purge the cache.
   *
   * @param directoryUrl The online location of the vocabulary directory
   * @return sting containing an error message otherwise empty
   */
  @PostMapping(value = RestEndpoints.LOAD_VOCABULARIES)
  @ResponseBody
  @ApiOperation(value = "Load and replace the vocabularies listed by the given vocabulary directory. Does NOT purge the cache.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Vocabularies loaded successfully."),
      @ApiResponse(code = 400, message = "Bad request parameters."),
      @ApiResponse(code = 502, message = "Problem accessing vocabulary repository.")
  })
  public ResponseEntity<String> loadVocabularies(
      @ApiParam("directory_url") @RequestParam("directory_url") String directoryUrl) {
    try {
      final Optional<URL> validatedLocationUrl = getValidatedLocationUrl(directoryUrl);
      if (validatedLocationUrl.isPresent()) {
        service.loadVocabularies(validatedLocationUrl.get());
        return ResponseEntity.ok().build();
      }
      return ResponseEntity.badRequest().body("The url of the directory to import is not valid.");
    } catch (BadContentException e) {
      LOGGER.warn("Could not load vocabularies", e);
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (VocabularyImportException e) {
      LOGGER.warn("Could not load vocabularies", e);
      return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(e.getMessage());
    }
  }

  private Optional<URL> getValidatedLocationUrl(String directoryUrl) throws BadContentException {
    try {
      URI uri = new URI(directoryUrl);
      String scheme = uri.getScheme();
      String remoteHost = uri.getHost();

      if ("https".equals(scheme) && validUrlPrefixes.contains(remoteHost)) {
        return Optional.of(uri.toURL());
      }
    } catch (URISyntaxException | MalformedURLException e) {
      throw new BadContentException(String.format("Provided directoryUrl '%s', failed to parse.", directoryUrl), e);
    }

    return Optional.empty();
  }
}
