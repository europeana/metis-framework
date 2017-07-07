package eu.europeana.metis.page;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.flapdoodle.embed.process.collections.Collections;
import eu.europeana.metis.common.MetisPage;
import eu.europeana.metis.config.MetisuiConfig;
import eu.europeana.metis.templates.PageConfig;
import eu.europeana.metis.templates.Version;
import eu.europeana.metis.templates.page.landingpage.Excerpt;
import eu.europeana.metis.templates.page.landingpage.Headline;
import eu.europeana.metis.templates.page.landingpage.MetisLandingPageModel;
import java.util.List;
import java.util.Map;

/**
 * This web-page represents a base Landing page and all user account pages like: Login page,
 * User Profile page, Register User page.
 *
 * @author alena
 */
public abstract class MetisLandingPage extends MetisPage {

  protected MetisLandingPageModel metisLandingPageModel;

  public MetisLandingPage(MetisuiConfig config) {
    super(config);
  }

  @Override
  public Map<String, Object> buildModel() {
    metisLandingPageModel = new MetisLandingPageModel();
    metisLandingPageModel.setIsJava(true);
    metisLandingPageModel.setCssFiles(resolveCssFiles());
    metisLandingPageModel.setJsFiles(resolveJsFiles());
    metisLandingPageModel.setJsVars(resolveJsVars());
    metisLandingPageModel.setBreadcrumbs(resolveBreadcrumbs());
    metisLandingPageModel.setPageTitle("Europeana Metis");
    metisLandingPageModel.setImageRoot("https://europeanastyleguidetest.a.cdnify.io");
    metisLandingPageModel.setPageConfig(createPageConfig());
    metisLandingPageModel.setVersion(createVersion());
    metisLandingPageModel.setHeadline(createHeadline());
    metisLandingPageModel.setExcerpt(createExcerpt());
    metisLandingPageModel.setMetisHeader(buildMetisHeader());
    metisLandingPageModel.setI18n(buildI18n());

    addPageContent();

    metisLandingPageModel.setMetisFooter(buildMetisFooter());

    ObjectMapper m = new ObjectMapper();
    return m.convertValue(metisLandingPageModel, Map.class);
  }

  private PageConfig createPageConfig() {
    PageConfig pageConfig = new PageConfig();
    pageConfig.setNewsletter(true);
    return pageConfig;
  }

  private Version createVersion() {
    Version version = new Version();
    version.setIsAlpha(false);
    version.setIsBeta(true);
    return version;
  }

  private List<Headline> createHeadline() {
    Headline headline1 = new Headline();
    Headline headline2 = new Headline();
    Headline headline3 = new Headline();
    headline1.setShort("false");
    headline2.setMedium("false");
    headline3.setLong("false");

    return Collections.newArrayList(headline1, headline2, headline3);
  }

  private List<Excerpt> createExcerpt() {
    Excerpt excerpt1 = new Excerpt();
    Excerpt excerpt2 = new Excerpt();
    Excerpt excerpt3 = new Excerpt();
    Excerpt excerpt4 = new Excerpt();
    excerpt1.setVshort("false");
    excerpt2.setShort("false");
    excerpt3.setMedium("false");
    excerpt4.setLong("false");

    return Collections.newArrayList(excerpt1, excerpt2, excerpt3, excerpt4);
  }
}
