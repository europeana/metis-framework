package eu.europeana.metis.authentication.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import eu.europeana.metis.exception.BadContentException;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-31
 */
public enum AccountRole {
  METIS_ADMIN, EUROPEANA_DATA_OFFICER, PROVIDER_VIEWER;

  /**
   * Maps the string representation in json to the enum value.
   *
   * @param name the string representation of the enum field
   * @return {@link AccountRole}
   * @throws BadContentException if the value does not match any of the enum fields
   */
  @JsonCreator
  public static AccountRole getAccountRoleFromEnumName(String name) throws BadContentException {
    for (AccountRole acountRole: AccountRole.values()) {
      if(acountRole.name().equalsIgnoreCase(name)){
        return acountRole;
      }
    }
    throw new BadContentException("Account Role is not valid");
  }
}
