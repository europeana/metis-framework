package eu.europeana.metis.ui.mongo.service;

import eu.europeana.metis.ui.ldap.dao.UserDao;
import eu.europeana.metis.ui.ldap.domain.LdapUser;
import eu.europeana.metis.ui.mongo.dao.MongoUserDao;
import eu.europeana.metis.ui.mongo.dao.RoleRequestDao;
import eu.europeana.metis.ui.mongo.domain.Role;
import eu.europeana.metis.ui.mongo.domain.RoleRequest;
import eu.europeana.metis.ui.mongo.domain.User;
import eu.europeana.metis.ui.mongo.domain.UserDTO;
import eu.europeana.metis.ui.mongo.domain.UserOrganizationRole;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.LdapShaPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Created by ymamakis on 11/24/16.
 */
@Service
public class UserService {

  private final UserDao userDao;
  private final MongoUserDao mongoUserDao;
  private final RoleRequestDao roleRequestDao;

  @Autowired
  public UserService(UserDao userDao, MongoUserDao mongoUserDao, RoleRequestDao roleRequestDao) {
    this.userDao = userDao;
    this.mongoUserDao = mongoUserDao;
    this.roleRequestDao = roleRequestDao;
  }

  /**
   * Updates a user in LDAP and MongoDB
   *
   * @param userDto The user representations wrapper
   */
  public void updateUserFromDTO(UserDTO userDto) {
    if (userDto.getLdapUser() != null) {
      updateUserInLdap(userDto.getLdapUser());
      if (userDto.getUser() != null) {
        updateUserInMongo(userDto.getUser());
      }
    }
  }

  public void deleteLdapUser(String email) {
    userDao.disable(email);
  }

  public void createUser(String firstName, String lastName, String email, String password)
  {
    LdapUser ldapUser = new LdapUser();
    ldapUser.setFirstName(firstName);
    ldapUser.setLastName(lastName);
    ldapUser.setEmail(email);
    LdapShaPasswordEncoder enc = new LdapShaPasswordEncoder();
    ldapUser.setPassword(enc.encodePassword(password, null));
    createLdapUser(ldapUser);

    User user = new User();
    user.setEmail(email);
    user.setCreated(new Date());
    createMongoUser(user);
  }

  public void createUser(UserDTO userDTO)
  {
    createLdapUser(userDTO.getLdapUser());
    createMongoUser(userDTO.getUser());
  }

  private void createLdapUser(LdapUser ldapUser) {
    userDao.create(ldapUser);
  }

  private void createMongoUser(User user) {
    mongoUserDao.save(user);
  }
//
//    /**
//     * Create a user in MongoDB
//     *
//     * @param dbUser   The user in MongoDB
//     * @param requests The request for specific roles in organizations
//     */
//    public void createDBUser(DBUser dbUser, RoleRequest... requests) {
//        dbUserDao.save(dbUser);
//        if (requests != null) {
//            for (RoleRequest request : requests) {
//                createRequest(request);
//            }
//        }
//    }

  /**
   * Create a request for a role in an organization
   *
   * @param userId The user identifier
   * @param organizationId The organization identifier
   * @param isDeleteRequest If it is a delete request
   */
  public void createRequest(String userId, String organizationId, boolean isDeleteRequest) {
    RoleRequest request = new RoleRequest();
    request.setUserId(userId);
    request.setOrganizationId(organizationId);
    request.setRequestStatus("pending");
    request.setRequestDate(new Date());
    request.setId(new ObjectId());
    request.setDeleteRequest(isDeleteRequest);
    roleRequestDao.save(request);
  }

  /**
   * Filter out the roles that can be assigned to Organization
   *
   * @param organizationRole The organizationRole of the organization
   * @return The Roles that can be assigned to the user
   */
  public List<Role> getRolesFromOrganizationRole(
      eu.europeana.metis.core.common.OrganizationRole organizationRole) {
    List<Role> roles = new ArrayList<>();
    for (Role roleToCheck : Role.values()) {
      if (roleToCheck.isAssignableTo(organizationRole)) {
        roles.add(roleToCheck);
      }
    }
    return roles;
  }

  /**
   * Get user role requests
   *
   * @param email The email of the user
   * @return The list of role requests for a user
   */
  public List<RoleRequest> getUserRequests(String email) {
    Query<RoleRequest> requestQuery = roleRequestDao.createQuery();
    requestQuery.filter("userId", email);
    return roleRequestDao.find(requestQuery).asList();
  }

  /**
   * Approve a request for a role in an organization
   *
   * @param request The user request to approve
   */
  public void approveRequest(RoleRequest request, Role role) {
    updateRequest(request, role, "approved");
  }

  /**
   * Reject a role request
   *
   * @param request The request to reject
   */
  public void rejectRequest(RoleRequest request) {
    updateRequest(request, null, "rejected");
  }

  public User getUserByRequestID(String id) {
    RoleRequest roleRequest = roleRequestDao.findOne("_id", new ObjectId(id));
    if (roleRequest == null) {
      return null;
    }
    return mongoUserDao.findOne("email", roleRequest.getUserId());
  }

  private void updateRequest(RoleRequest request, Role role, String status) {
    UpdateOperations<RoleRequest> ops = roleRequestDao.createUpdateOperations();
    ops.set("requestStatus", status);
    Query<RoleRequest> query = roleRequestDao.createQuery();
    query.filter("userId", request.getUserId());
    query.filter("organizationId", request.getOrganizationId());
    roleRequestDao.getDatastore().update(query, ops, true);
    if (!StringUtils.equals("rejected", status)) {
      User user = mongoUserDao.findOne("email", request.getUserId());
      if (user != null) {
        List<UserOrganizationRole> orgIds = user
            .getUserOrganizationRoles();

        if (!request.isDeleteRequest()) {
          if (orgIds == null) {
            orgIds = new ArrayList<>();
          }
          UserOrganizationRole userOrganizationRole = new UserOrganizationRole();
          userOrganizationRole.setOrganizationId(request.getOrganizationId());
          userOrganizationRole.setRole(role);
          orgIds.add(userOrganizationRole);
          user.setUserOrganizationRoles(orgIds);

        } else {
          List<UserOrganizationRole> newUserOrganizationRoles = new ArrayList<>();
          for (UserOrganizationRole checkUserOrganizationRole : orgIds) {
            if (!StringUtils
                .equals(request.getOrganizationId(), checkUserOrganizationRole.getOrganizationId())) {
              newUserOrganizationRoles.add(checkUserOrganizationRole);
            }
          }
          user.setUserOrganizationRoles(newUserOrganizationRoles);
        }
        updateUserInMongo(user);
      }
    }
  }

  /**
   * Approve a user
   *
   * @param email The email of the user to approve
   */
  public void approveUser(String email) {
    userDao.approve(email);

  }

  private void updateUserInMongo(User user) {
    UpdateOperations<User> ops = mongoUserDao.createUpdateOperations();
    Query<User> query = mongoUserDao.createQuery();
    query.filter("id", user.getId());
    if (user.getCountry() != null) {
      ops.set("country", user.getCountry());
    } else {
      ops.unset("country");
    }
    ops.set("modified", new Date());
    if (user.getEuropeanaNetworkMember() != null) {
      ops.set("europeanaNetworkMember", user.getEuropeanaNetworkMember());
    } else {
      ops.unset("europeanaNetworkMember");
    }
    if (user.getNotes() != null) {
      ops.set("notes", user.getNotes());
    } else {
      ops.unset("notes");
    }
    if (user.getSkypeId() != null) {
      ops.set("skypeId", user.getSkypeId());
    } else {
      ops.unset("skypeId");
    }
//  Disabled this because of an Morphia error ValidationException(format("Could not resolve path '%s' against '%s'.)
//  Related to the @Embedded annotation of this field
    if (user.getUserOrganizationRoles() != null) {
      ops.set("userOrganizationRoles", user.getUserOrganizationRoles());
    } else {
      ops.unset("userOrganizationRoles");
    }
    if (user.getEmail() != null) {
      ops.set("email", user.getEmail());
      mongoUserDao.getDatastore().update(query, ops, true);
    }
  }

  private void updateUserInLdap(LdapUser ldapUser) {
    userDao.update(ldapUser);
  }

  /**
   * Retrieve a user
   *
   * @param email The email of the user
   * @return A user representation as it exists in LDAP and MongoDb
   */
  public UserDTO getUser(String email) {
    UserDTO userDto = new UserDTO();
    userDto.setLdapUser(userDao.findByPrimaryKey(email));
    userDto.setUser(mongoUserDao.findOne("email", email));
    return userDto;
  }

  /**
   * Get all requests (paginated)
   *
   * @param from The offset
   * @param to Until
   * @return The list of all requests
   */
  public List<RoleRequest> getAllRequests(Integer from, Integer to) {
    Query<RoleRequest> requestQuery = roleRequestDao.createQuery();
    if (from != null) {
      requestQuery.offset(from);
      if (to != null) {
        requestQuery.limit(to - from);
      }
    }
    return roleRequestDao.find(requestQuery).asList();
  }

  /**
   * Get all pending requests (paginated)
   *
   * @param from The offset
   * @param to Until
   * @return The list of all pending requests
   */
  public List<RoleRequest> getAllPendingRequests(Integer from, Integer to) {
    Query<RoleRequest> requestQuery = roleRequestDao.createQuery();
    if (from != null) {
      requestQuery.offset(from);
      if (to != null) {
        requestQuery.limit(to - from);
      }
    }
    requestQuery.filter("requestStatus", "pending");
    return roleRequestDao.find(requestQuery).asList();
  }

  /**
   * Get all pending requests (paginated) for user
   *
   * @param email The email of the user
   * @param from The offset
   * @param to Until
   * @return The list of all pending requests for a user
   */
  public List<RoleRequest> getAllPendingRequestsForUser(String email, Integer from, Integer to) {
    Query<RoleRequest> requestQuery = roleRequestDao.createQuery();
    if (from != null) {
      requestQuery.offset(from);
      if (to != null) {
        requestQuery.limit(to - from);
      }
    }
    requestQuery.filter("userId", email);
    requestQuery.filter("requestStatus", "pending");
    return roleRequestDao.find(requestQuery).asList();
  }

  /**
   * Get all  requests (paginated) for user
   *
   * @param email The email of the user
   * @param from The offset
   * @param to Until
   * @return The list of all pending requests for a user
   */
  public List<RoleRequest> getAllRequestsForUser(String email, Integer from, Integer to) {
    Query<RoleRequest> requestQuery = roleRequestDao.createQuery();
    requestQuery.filter("userId", email);
    if (from != null) {
      requestQuery.offset(from);
      if (to != null) {
        requestQuery.limit(to - from);
      }
    }
    return roleRequestDao.find(requestQuery).asList();
  }

  public List<String> getAllAdminUsers() {
    return userDao.findUsersByRole(Role.EUROPEANA_ADMIN);
  }
}
