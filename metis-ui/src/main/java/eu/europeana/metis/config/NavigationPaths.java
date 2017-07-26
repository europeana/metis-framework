package eu.europeana.metis.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Created by erikkonijnenburg on 03/07/2017.
 */
@Service
public class NavigationPaths {

  private final String login;
  private final String loginUrl;
  private final String register;
  private final String registerUrl;
  private final String home;
  private final String homeUrl;
  private final String profile;
  private final String profileUrl;
  private final String logout;
  private final String logoutUrl;
  private final String dashBoardUrl;
  private final String requestsUrl;

  public String getLogin() {
    return login;
  }

  public String getLoginUrl() {
    return loginUrl;
  }

  public String getRegister() {
    return register;
  }

  public String getRegisterUrl() {
    return registerUrl;
  }

  public String getHome() {
    return home;
  }

  public String getHomeUrl() {
    return homeUrl;
  }

  public String getProfile() {
    return profile;
  }

  public String getProfileUrl() {return profileUrl; }

  public String getLogout() {
    return logout;
  }

  public String getLogoutUrl() {
    return logoutUrl;
  }

  public String getDashBoardUrl() { return dashBoardUrl; }

  public String getRequestsUrl() { return requestsUrl; }

  public MetisuiConfig getConfig() {
    return config;
  }

  public void setConfig(MetisuiConfig config) {
    this.config = config;
  }

  private MetisuiConfig config;

  @Autowired
  public NavigationPaths(MetisuiConfig config) {
    this.config = config;
    login = "Login";
    loginUrl = StringUtils.isEmpty(config.getContextRoot()) ? "/login" : ServletUriComponentsBuilder.fromHttpUrl(config.getContextRoot()).path("/login").toUriString();
    register = "Register";
    registerUrl =StringUtils.isEmpty(config.getContextRoot()) ? "/register":  ServletUriComponentsBuilder.fromHttpUrl(config.getContextRoot()).path("/register").toUriString();
    home = "Home";
    homeUrl =StringUtils.isEmpty(config.getContextRoot()) ? "/" : ServletUriComponentsBuilder.fromHttpUrl(config.getContextRoot()).toUriString();
    profile = "Profile";
    profileUrl =StringUtils.isEmpty(config.getContextRoot()) ? "/profile" :  ServletUriComponentsBuilder.fromHttpUrl(config.getContextRoot()).path("/profile").toUriString();
    logout = "Logout";
    logoutUrl = StringUtils.isEmpty(config.getContextRoot()) ? "/logout": ServletUriComponentsBuilder.fromHttpUrl(config.getContextRoot()).path("/logout").toUriString();

    dashBoardUrl = StringUtils.isEmpty(config.getContextRoot()) ? "/dashboard":  ServletUriComponentsBuilder.fromHttpUrl(config.getContextRoot()).path("/dashboard").toUriString();
    requestsUrl = StringUtils.isEmpty(config.getContextRoot()) ? "/requests": ServletUriComponentsBuilder.fromHttpUrl(config.getContextRoot()).path("/requests").toUriString();
  }



}
