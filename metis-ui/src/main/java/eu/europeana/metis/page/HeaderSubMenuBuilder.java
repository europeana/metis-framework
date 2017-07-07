package eu.europeana.metis.page;

import de.flapdoodle.embed.process.collections.Collections;
import eu.europeana.metis.config.NavigationPaths;
import eu.europeana.metis.templates.Submenu;
import eu.europeana.metis.templates.SubmenuItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HeaderSubMenuBuilder {
  private NavigationPaths navigationPaths;

  @Autowired
  public HeaderSubMenuBuilder(NavigationPaths navigationPaths) {
    this.navigationPaths = navigationPaths;
  }

  public Submenu buildMenuWhenNotAuthorized()
  {
    return getSubmenu(navigationPaths.getLogin(), navigationPaths.getLoginUrl(), navigationPaths.getRegister(), navigationPaths.getRegisterUrl());
  }

  public Submenu buildMenuForLoginPage()
  {
    return getSubmenu(navigationPaths.getRegister(), navigationPaths.getRegisterUrl(), navigationPaths.getHome(), navigationPaths.getHomeUrl());
  }

  public Submenu buildMenuRegister()
  {
    return getSubmenu(navigationPaths.getLogin(), navigationPaths.getLoginUrl(),  navigationPaths.getHome(), navigationPaths.getHomeUrl());
  }

  public Submenu buildMenuWhenAuthorized()
  {
    return getSubmenu(navigationPaths.getProfile(), navigationPaths.getProfileUrl(),  navigationPaths.getLogout(), navigationPaths.getLogoutUrl());
  }

  private static Submenu getSubmenu(String item1Name, String item1Url, String item2Name,
      String item2Url) {
    SubmenuItem submenuItem1 = getSubmenuItem(item1Name, item1Url, true);
    SubmenuItem submenuItem2 = getSubmenuItem(item2Name, item2Url, false);

    Submenu submenu = new Submenu();
    submenu.setItems(Collections.newArrayList(submenuItem1, submenuItem2));
    return submenu;
  }

  private static SubmenuItem getSubmenuItem(String text, String url, boolean isCurrent) {
    SubmenuItem submenuItem1 = new SubmenuItem();
    submenuItem1.setText(text);
    submenuItem1.setUrl(url);
    submenuItem1.setMessage(null);
    submenuItem1.setIsCurrent(isCurrent);
    submenuItem1.setIsDivider(null);
    submenuItem1.setSubtitle(null);
    submenuItem1.setSubmenu(false);
    return submenuItem1;
  }
}
