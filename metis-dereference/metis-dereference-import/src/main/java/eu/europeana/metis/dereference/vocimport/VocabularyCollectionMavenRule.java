package eu.europeana.metis.dereference.vocimport;

import eu.europeana.metis.dereference.vocimport.exception.VocabularyImportException;

import java.io.IOException;
import java.nio.file.Path;
import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

/**
 * This is a Maven-enabled enforcer rule that can be used in a maven project. For an example of how
 * this rule may be used in a maven POM file:
 *
 * <pre>{@code
 * <build><plugins>
 *     <plugin>
 *         <groupId>org.apache.maven.plugins</groupId>
 *         <artifactId>maven-enforcer-plugin</artifactId>
 *         <version>3.0.0-M3</version>
 *         <dependencies>
 *             [INCLUDE THIS PROJECT AS DEPENDENCY IF NEEDED]
 *         </dependencies>
 *         <executions>
 *             <execution>
 *                 <id>enforce</id>
 *                 <configuration>
 *                     <rules>
 *                         <myCustomRule implementation="eu.europeana.metis.dereference.vocimport.VocabularyCollectionMavenRule">
 *                             <lenientOnLackOfExamples>true</lenientOnLackOfExamples>
 *                             <lenientOnMappingTestFailures>false</lenientOnMappingTestFailures>
 *                             <lenientOnExampleRetrievalFailures>true</lenientOnExampleRetrievalFailures>
 *                             <vocabularyDirectoryFile>src/main/resources/directory.yml</vocabularyDirectoryFile>
 *                         </myCustomRule>
 *                     </rules>
 *                 </configuration>
 *                 <goals>
 *                     <goal>enforce</goal>
 *                 </goals>
 *             </execution>
 *         </executions>
 *     </plugin>
 * </plugins></build>
 * }</pre>
 */
public class VocabularyCollectionMavenRule implements EnforcerRule {

  /**
   * Whether the the rule is lenient on vocabulary mappings without examples.
   */
  private boolean lenientOnLackOfExamples = false;

  /**
   * Whether the rule is lenient on errors and unmet expectations when applying the mapping to the
   * example and counterexample values.
   */
  private boolean lenientOnMappingTestFailures = false;

  /**
   * Whether the rule is lenient on example or counterexample retrieval (download) issues.
   */
  private boolean lenientOnExampleRetrievalFailures = false;

  /**
   * The relative path to the vocabulary directory file from the root directory of the project. This
   * value is obligatory and it is a relative path (so it should not start with a path separator
   * '/'.
   */
  private String vocabularyDirectoryFile = null;

  /**
   * No-arguments constructor, required for maven instantiation.
   */
  public VocabularyCollectionMavenRule() {
  }

  /**
   * Constructor.
   *
   * @param lenientOnLackOfExamples Whether the the rule is lenient on vocabulary mappings without
   * examples.
   * @param lenientOnMappingTestFailures Whether the rule is lenient on errors and unmet
   * expectations when applying the mapping to the example and counterexample values.
   * @param lenientOnExampleRetrievalFailures Whether the rule is lenient on example or
   * counterexample retrieval (download) issues.
   * @param vocabularyDirectoryFile The relative path to the vocabulary directory file from the root
   * directory of the project. This value is obligatory and it is a relative path (so it should not
   * start with a path separator.
   */
  public VocabularyCollectionMavenRule(boolean lenientOnLackOfExamples,
          boolean lenientOnMappingTestFailures, boolean lenientOnExampleRetrievalFailures,
          String vocabularyDirectoryFile) {
    this.lenientOnLackOfExamples = lenientOnLackOfExamples;
    this.lenientOnMappingTestFailures = lenientOnMappingTestFailures;
    this.lenientOnExampleRetrievalFailures = lenientOnExampleRetrievalFailures;
    this.vocabularyDirectoryFile = vocabularyDirectoryFile;
  }

  @Override
  public void execute(EnforcerRuleHelper enforcerRuleHelper) throws EnforcerRuleException {

    // Get the environment: the log and the project.
    final Log log = enforcerRuleHelper.getLog();
    final MavenProject project;
    try {
      project = enforcerRuleHelper.getComponent(MavenProject.class);
    } catch (ComponentLookupException e) {
      throw new EnforcerRuleException("Could not retrieve the project properties.", e);
    }

    // Get the vocabulary directory file
    final Path baseDirectory = project.getBasedir().toPath();
    final Path vocabularyDirectory = baseDirectory.resolve(vocabularyDirectoryFile);

    try {
    // Prepare validation
    final VocabularyCollectionImporter importer = new VocabularyCollectionImporterFactory()
            .createImporter(baseDirectory, vocabularyDirectory);
    final VocabularyCollectionValidatorImpl validator = new VocabularyCollectionValidatorImpl(
            importer, lenientOnLackOfExamples, lenientOnMappingTestFailures,
            lenientOnExampleRetrievalFailures);
    log.info("");
    log.info("Validating vocabulary collection: " + importer.getDirectoryLocation().toString());

    // Perform validation

      validator.validate(vocabulary -> log.info("  Vocabulary found: " + vocabulary.getName()),
              log::warn);
    } catch (IOException | VocabularyImportException e) {
      log.error(e.getMessage());
      throw new EnforcerRuleException("Vocabulary collection validation failed.", e);
    }

    // Done
    log.info("Finished validating vocabulary collection.");
  }

  @Override
  public boolean isCacheable() {
    return false;
  }

  @Override
  public boolean isResultValid(EnforcerRule enforcerRule) {
    return false;
  }

  @Override
  public String getCacheId() {
    return "" + lenientOnLackOfExamples + "_" + lenientOnMappingTestFailures + "_"
            + lenientOnExampleRetrievalFailures + "_" + vocabularyDirectoryFile;
  }
}
