package eu.europeana.metis.ui.mongo.domain;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.util.Date;

/**
 * A request for an assignment to an organization
 * Created by ymamakis on 11/24/16.
 */
@Entity
@Indexes(@Index(options = @IndexOptions(unique = true), fields = {@Field("organizationId"),@Field("userId")}))
public class RoleRequest {
    @Id
    private ObjectId id;

    /**
     * The id of the organization
     */
    @Indexed
    @Property
    private String organizationId;

    /**
     * The id of a user
     */
    @Indexed
    @Property
    private String userId;


    /**
     * The request date
     */
    private Date requestDate;

    /**
     * The request status
     */
    @Indexed
    private String requestStatus;

    private boolean deleteRequest;


    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public boolean isDeleteRequest() {
        return deleteRequest;
    }

    public void setDeleteRequest(boolean deleteRequest) {
        this.deleteRequest = deleteRequest;
    }
}
