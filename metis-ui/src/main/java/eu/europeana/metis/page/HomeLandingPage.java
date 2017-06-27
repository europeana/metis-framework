package eu.europeana.metis.page;

import eu.europeana.metis.templates.Content;
import eu.europeana.metis.templates.CssFile;
import eu.europeana.metis.templates.page.landingpage.Banner;
import eu.europeana.metis.templates.page.landingpage.HeroConfig;
import eu.europeana.metis.templates.page.landingpage.LandingPageContent;
import eu.europeana.metis.ui.mongo.domain.UserDTO;
import java.util.List;

public class HomeLandingPage extends MetisLandingPage {

  public HomeLandingPage(UserDTO userDTO) {
    super(userDTO);
  }

  @Override
  public void addPageContent() {
    buildHomePageContent();
  }

  @Override
  public List<CssFile> resolveCssFiles() {
    return super.resolveCssFiles();
  }


  /**
   * The content for the User Login page.
   */
  private void buildHomePageContent() {
    Content content = createContent(createBanner(), createHeroConfig());
    LandingPageContent landingPageContent = createLandingPage(content);
    metisLandingPageModel.setLandingPageContent(landingPageContent);
  }

  private LandingPageContent createLandingPage(Content content) {
    LandingPageContent landingPageContent = new LandingPageContent();
    landingPageContent.setIsHome(true);
    landingPageContent.setContent(content);
    return landingPageContent;
  }

  private Content createContent(Banner banner, HeroConfig heroConfig) {
    Content content = new Content();
    content.setHeroConfig(heroConfig);
    content.setBanner(banner);
    return content;
  }

  private HeroConfig createHeroConfig() {
    HeroConfig heroConfig = new HeroConfig();
    heroConfig
        .setAttributionText("Cyclopides metis L., Cyclopides qua... Museum Fur Naturkunde Berlin");
    heroConfig.setAttributionUrl(
        "http://www.europeana.eu/portal/fr/record/11622/_MFN_DRAWERS_MFN_GERMANY_http___coll_mfn_berlin_de_u_MFNB_Lep_Hesperiidae_D146.html");
    heroConfig.setBrandColour("brand-colour-site");
    heroConfig.setBrandOpacity("brand-opacity100");
    heroConfig.setBrandPosition("brand-bottomleft");
    heroConfig.setHeroImage(
        "https://europeana-styleguide-test.s3.amazonaws.com/images/metis/hero_metis_1600x650_jade.png");
    heroConfig.setLicenseCC0("true");
    return heroConfig;
  }

  private Banner createBanner() {
    Banner banner = new Banner();
    banner.setCtaText("Register to metis here");
    banner.setCtaUrl("#");
    banner.setInfoLink("Learn more about Metis");
    banner.setInfoUrl("#");
    banner.setText(
        "Ever wondered how to automatically digest huge amounts of data with the push of a button?");
    banner.setTitle("What can you do with Metis?");
    return banner;
  }


}
