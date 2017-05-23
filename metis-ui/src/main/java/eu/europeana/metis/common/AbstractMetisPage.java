package eu.europeana.metis.common;

import eu.europeana.metis.page.PageView;
import eu.europeana.metis.templates.CssFile;
import eu.europeana.metis.templates.JsFile;
import eu.europeana.metis.templates.JsVar;
import eu.europeana.metis.templates.MetisFooter;
import eu.europeana.metis.templates.MetisHeader;
import eu.europeana.metis.templates.page.landingpage.Breadcrumb;
import java.util.List;
import java.util.Map;

/**
 * Abstract Metis application page.
 *
 * @author alena
 */
public abstract class AbstractMetisPage {
  /**
   * @return a Mapping-To-EDM page Java model.
   */
  public abstract Map<String, Object> buildModel();
  /**
   * @return list of css-files to apply for a Metis page.
   */
  public abstract List<CssFile> resolveCssFiles();

  /**
   * @return list of js-files to apply for a Metis page.
   */
  public abstract List<JsFile> resolveJsFiles();

  /**
   * @return list of js-vars to apply for a Metis page.
   */
  public abstract List<JsVar> resolveJsVars();

  /**
   * @return list of breadcrumbs for Metis page.
   */
  public abstract List<Breadcrumb> resolveBreadcrumbs();

  public abstract void addPageContent();

  /**
   * @return Metis header object model.
   */
  public abstract MetisHeader buildHeader(PageView pageView);

  /**
   * @return Metis footer object model.
   */
  public abstract MetisFooter buildFooter();
}
