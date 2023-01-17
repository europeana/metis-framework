package eu.europeana.metis.dereference.rest.controller;

import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.service.DereferencingManagementService;
import eu.europeana.metis.dereference.vocimport.exception.VocabularyImportException;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.utils.RestEndpoints;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
public class DereferencingManagementController {

  private static final Logger LOGGER = LoggerFactory.getLogger(DereferencingManagementController.class);

  private final DereferencingManagementService service;
  private final Set<String> allowedUrlDomains;

  /**
   * Constructor parameterized.
   *
   * @param service the dereferencing management service
   * @param allowedUrlDomains the allowed valid url prefixes
   */
  @Autowired
  public DereferencingManagementController(DereferencingManagementService service, Set<String> allowedUrlDomains) {
    this.service = service;
    this.allowedUrlDomains = new HashSet<>(allowedUrlDomains);
  }

  /**
   * Retrieve a list of all the registered vocabularies
   *
   * @return The List of all the registered vocabularies
   */
  @GetMapping(value = RestEndpoints.VOCABULARIES, produces = "application/json")
  @ResponseBody
  @Operation(description = "Retrieve all the vocabularies", responses = {
      @ApiResponse(responseCode = "200", content = {
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(hidden = true))
      }),
  })
  public List<Vocabulary> getAllVocabularies() {
    return service.getAllVocabularies();
  }

  /**
   * Empty Cache. This will remove ALL entries in the cache (Redis). If the same redis instance/cluster is used for multiple
   * services then the cache for other services is cleared as well.
   */
  @DeleteMapping(value = RestEndpoints.CACHE_EMPTY)
  @ResponseBody
  @Operation(description = "Empty the cache")
  public void emptyCache() {
    service.emptyCache();
  }

  /**
   * Empty the cache for all Resources without an XML representation
   */
  @DeleteMapping(value = RestEndpoints.CACHE_EMPTY_XML)
  @ResponseBody
  @Operation(description = "Empty the cache without XML representations")
  public void emptyCacheByEmptyXml() {
    service.purgeByNullOrEmptyXml();
  }

  /**
   * Empty the cache for a specific resource
   *
   * @param resourceId The resourceId to empty the cache for
   */
  @DeleteMapping(value = RestEndpoints.CACHE_EMPTY_RESOURCE)
  @ResponseBody
  @Operation(description = "Empty the cache by resource Id")
  public void emptyCacheByResourceId(
      @Parameter(description = "Id (URI) of resource to clear cache", required = true) @RequestParam(value = "resourceId") String resourceId) {
    service.purgeByResourceId(resourceId);
  }

  /**
   * Empty the cache for a specific vocabulary, with all associated entities
   *
   * @param vocabularyId The vocabularyId to empty the cache for
   */
  @DeleteMapping(value = RestEndpoints.CACHE_EMPTY_VOCABULARY)
  @ResponseBody
  @Operation(description = "Empty the cache by vocabulary Id")
  public void emptyCacheByVocabularyId(
      @Parameter(description = "Id of vocabulary to clear cache", required = true) @RequestParam(value = "vocabularyId") String vocabularyId) {
    service.purgeByVocabularyId(vocabularyId);
  }


  /**
   * Load the vocabularies from an online source. This does NOT purge the cache.
   *
   * @param directoryUrl The online location of the vocabulary directory
   * @return sting containing an error message otherwise empty
   */
  @PostMapping(value = RestEndpoints.LOAD_VOCABULARIES)
  @ResponseBody
  @Operation(description = "Load and replace the vocabularies listed by the given vocabulary directory. Does NOT purge the cache.",
      responses = {
          @ApiResponse(responseCode = "200", description = "Vocabularies loaded successfully."),
          @ApiResponse(responseCode = "400", description = "Bad request parameters."),
          @ApiResponse(responseCode = "502", description = "Problem accessing vocabulary repository.")
      })
  public ResponseEntity<String> loadVocabularies(
      @Parameter(name = "directory_url") @RequestParam("directory_url") String directoryUrl) {
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

  /**
   * Validates a String representation of a URL.
   * <p>The method will check that the url is:
   * <ul>
   *   <li>valid according to the protocol</li>
   *   <li>of https scheme</li>
   *   <li>part of the allowed domains</li>
   * </ul>
   * domain for the application to further access it.</p>
   *
   * @param directoryUrl the url to validate
   * @return the validated URL class
   * @throws BadContentException if the url failed during parsing
   */
  private Optional<URL> getValidatedLocationUrl(String directoryUrl) throws BadContentException {
    try {
      URI uri = new URI(directoryUrl);
      String scheme = uri.getScheme();
      String remoteHost = uri.getHost();

      if ("https".equals(scheme) && allowedUrlDomains.contains(remoteHost)) {
        return Optional.of(uri.toURL());
      }
    } catch (URISyntaxException | MalformedURLException e) {
      throw new BadContentException(String.format("Provided directoryUrl '%s', failed to parse.", directoryUrl), e);
    }

    return Optional.empty();
  }
}
