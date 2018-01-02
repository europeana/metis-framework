package eu.europeana.metis.authentication.rest.client;

import eu.europeana.metis.authentication.user.MetisUser;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-12-28
 */
public class Main {

  public static void main(String[] args)
  {
    AuthenticationClient authenticationClient = new AuthenticationClient("http://localhost:8080/metis-authentication-rest-test");
    MetisUser userByAccessTokenInHeader = authenticationClient
        .getUserByAccessTokenInHeader("Bearer pKz2WYLgrKsZ9E9OAMC2qKpQeTYhAJ8");
    System.out.println(userByAccessTokenInHeader.getAccountRole());
  }

}
