package eu.europeana.metis.authentication.user;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-31
 */
public enum AccountRole {
  METIS_ADMIN, EUROPEANA_DATA_OFFICER, NULL;

  @JsonCreator
  public static AccountRole getAccountRoleFromEnumName(String name){
    for (AccountRole acountRole: AccountRole.values()) {
      if(acountRole.name().equalsIgnoreCase(name)){
        return acountRole;
      }
    }
    return NULL;
  }
}
