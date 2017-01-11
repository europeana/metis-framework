package eu.europeana.metis.ui.mongo.service;

import eu.europeana.metis.ui.ldap.dao.UserDao;
import eu.europeana.metis.ui.ldap.domain.User;
import eu.europeana.metis.ui.mongo.dao.DBUserDao;
import eu.europeana.metis.ui.mongo.dao.RoleRequestDao;
import eu.europeana.metis.ui.mongo.domain.DBUser;
import eu.europeana.metis.ui.mongo.domain.OrganizationRole;
import eu.europeana.metis.ui.mongo.domain.RoleRequest;
import eu.europeana.metis.ui.mongo.domain.UserDTO;
import org.apache.commons.lang.StringUtils;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ymamakis on 11/24/16.
 */
@Service
public class UserService {

    @Autowired
    private UserDao userDao;
    @Autowired
    private DBUserDao dbUserDao;
    @Autowired
    private RoleRequestDao roleRequestDao;

    /**
     * Updates a user in LDAP and MongoDB
     *
     * @param userDto The user representations wrapper
     */
    public void updateUserFromDTO(UserDTO userDto) {
        if (userDto.getUser() != null) {
            updateUserInLdap(userDto.getUser());
            if (userDto.getDbUser() != null) {
            	updateUserInMongo(userDto.getDbUser());            	
            }
        }
    }

    /**
     * Deactivate a user in LDAP
     *
     * @param email the email of the user
     */
    public void deleteUser(String email) {
        userDao.disable(email);
    }

    /**
     * Create a user in LDAP
     *
     * @param user The user to create
     */
    public void createLdapUser(User user) {
        userDao.create(user);
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
     * @param request The request for a role in an organization
     */
    public void createRequest(RoleRequest request) {
        roleRequestDao.save(request);
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
    public void approveRequest(RoleRequest request, String role) {
        updateRequest(request,role,"approved");
    }

    public void rejectReques(RoleRequest request){
        updateRequest(request, null,"rejected");
    }

    public void updateRequest(RoleRequest request,String role, String status){
        UpdateOperations<RoleRequest> ops = roleRequestDao.createUpdateOperations();
        ops.set("requestStatus", status);
        Query<RoleRequest> query = roleRequestDao.createQuery();
        query.filter("id", request.getId());
        roleRequestDao.update(query, ops);
        if (!StringUtils.equals("rejected",status)) {
            DBUser user = dbUserDao.findOne("email", request.getUserId());
            if (user != null) {
                List<OrganizationRole> orgIds = user.getOrganizationRoles();

                if (!request.isDeleteRequest()) {
                    if (orgIds == null) {
                        orgIds = new ArrayList<>();
                    }
                    OrganizationRole orgRole = new OrganizationRole();
                    orgRole.setOrganizationId(request.getOrganizationId());
                    orgRole.setRole(role);
                    orgIds.add(orgRole);
                    user.setOrganizationRoles(orgIds);

                } else {
                    List<OrganizationRole> newRoles = new ArrayList<>();
                    for (OrganizationRole checkRole:orgIds){
                        if(!StringUtils.equals(request.getOrganizationId(),checkRole.getOrganizationId())){
                            newRoles.add(checkRole);
                        }
                    }
                    user.setOrganizationRoles(newRoles);
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

    private void updateUserInMongo(DBUser user) {
        UpdateOperations<DBUser> ops = dbUserDao.createUpdateOperations();
        Query<DBUser> query = dbUserDao.createQuery();
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
        if (user.getOrganizationRoles() != null) {
            ops.set("organizationRoles", user.getOrganizationRoles());
        } else {
            ops.unset("organizationRoles");
        }
        if (user.getEmail() != null) {
        	ops.set("email", user.getEmail());
        	dbUserDao.getDatastore().update(query, ops, true);       	
        }
    }

    private void updateUserInLdap(User user) {
        userDao.update(user);
    }

    /**
     * Retrieve a user
     *
     * @param email The email of the user
     * @return A user representation as it exists in LDAP and MongoDb
     */
    public UserDTO getUser(String email) {
        UserDTO userDto = new UserDTO();
        userDto.setUser(userDao.findByPrimaryKey(email));
        userDto.setDbUser(dbUserDao.findOne("email", email));
        return userDto;
    }

    /**
     * Get all requests (paginated)
     *
     * @param from The offset
     * @param to   Until
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
     * @param to   Until
     * @return The list of all pending requests
     */
    public List<RoleRequest> getAllPendingRequests(Integer from, Integer to) {
        Query<RoleRequest> requestQuery = roleRequestDao.createQuery();
        if(from!=null) {
            requestQuery.offset(from);
            if(to!=null) {
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
     * @param to   Until
     * @return The list of all pending requests for a user
     */
    public List<RoleRequest> getAllPendingRequestsForUser(String email, Integer from, Integer to) {
        Query<RoleRequest> requestQuery = roleRequestDao.createQuery();
        if(from!=null) {
            requestQuery.offset(from);
            if(to!=null) {
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
     * @param to   Until
     * @return The list of all pending requests for a user
     */
    public List<RoleRequest> getAllRequestsForUser(String email, Integer from, Integer to) {
        Query<RoleRequest> requestQuery = roleRequestDao.createQuery();
        requestQuery.filter("userId", email);
        if(from!=null) {
            requestQuery.offset(from);
            if(to!=null) {
                requestQuery.limit(to - from);
            }
        }
        return roleRequestDao.find(requestQuery).asList();
    }


}
