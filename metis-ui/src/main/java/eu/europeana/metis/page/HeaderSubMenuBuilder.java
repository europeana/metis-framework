package eu.europeana.metis.page;

import de.flapdoodle.embed.process.collections.Collections;
import eu.europeana.metis.templates.Submenu;
import eu.europeana.metis.templates.SubmenuItem;
import org.springframework.stereotype.Service;

@Service
public final class HeaderSubMenuBuilder {

  private static final String login = "Login";
  private static final String loginUrl = "/login";
  private static final String register = "Register";
  private static final String registerUrl = "/register";
  private static final String home = "Home";
  private static final String homeUrl = "/";
  private static final String profile = "Profile";
  private static final String profileUrl = "/profile";
  private static final String logout = "Logout";
  private static final String logoutUrl = "/logout";

  public static Submenu buildMenuWhenNotAuthorized()
  {
    return getSubmenu(login, loginUrl, register, registerUrl);
  }

  public static Submenu buildMenuForLoginPage()
  {
    return getSubmenu(register, registerUrl, home, homeUrl);
  }

  public static Submenu buildMenuRegister()
  {
    return getSubmenu(login, loginUrl, home, homeUrl);
  }

  public static Submenu buildMenuWhenAuthorized()
  {
    return getSubmenu(profile, profileUrl, logout, logoutUrl);
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
