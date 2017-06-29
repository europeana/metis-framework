package eu.europeana.metis.common;

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
public interface AbstractMetisPage {
  /**
   * @return a Mapping-To-EDM page Java model.
   */
  Map<String, Object> buildModel();
  /**
   * @return list of css-files to apply for a Metis page.
   */
  List<CssFile> resolveCssFiles();

  /**
   * @return list of js-files to apply for a Metis page.
   */
  List<JsFile> resolveJsFiles();

  /**
   * @return list of js-vars to apply for a Metis page.
   */
  List<JsVar> resolveJsVars();

  /**
   * @return list of breadcrumbs for Metis page.
   */
  List<Breadcrumb> resolveBreadcrumbs();

  void addPageContent();

  /**
   * @return Metis header object model.
   */
  MetisHeader buildMetisHeader();

  /**
   * @return Metis footer object model.
   */
  MetisFooter buildMetisFooter();
}
