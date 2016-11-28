package eu.europeana.metis.framework.rest.response;

import eu.europeana.metis.framework.common.Address;
import eu.europeana.metis.framework.common.Contact;
import eu.europeana.metis.framework.common.HarvestingMetadata;
import eu.europeana.metis.framework.organization.Organization;

/**
 * Created by ymamakis on 11/11/16.
 */
public class MetisOrganizationView extends PublicOrganizationView{

    private String organizationId;
    private Contact mainContact;
    private String technicalContact;
    private Address address;
    private boolean dea;
    private boolean deaSent;
    private boolean deaSigned;
    private String deaNotes;
    private HarvestingMetadata metadata;

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }



    public Contact getMainContact() {
        return mainContact;
    }

    public void setMainContact(Contact mainContact) {
        this.mainContact = mainContact;
    }

    public String getTechnicalContact() {
        return technicalContact;
    }

    public void setTechnicalContact(String technicalContact) {
        this.technicalContact = technicalContact;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public boolean isDea() {
        return dea;
    }

    public void setDea(boolean dea) {
        this.dea = dea;
    }

    public boolean isDeaSent() {
        return deaSent;
    }

    public void setDeaSent(boolean deaSent) {
        this.deaSent = deaSent;
    }

    public boolean isDeaSigned() {
        return deaSigned;
    }

    public void setDeaSigned(boolean deaSigned) {
        this.deaSigned = deaSigned;
    }

    public String getDeaNotes() {
        return deaNotes;
    }

    public void setDeaNotes(String deaNotes) {
        this.deaNotes = deaNotes;
    }

    public HarvestingMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(HarvestingMetadata metadata) {
        this.metadata = metadata;
    }

    public static MetisOrganizationView generateResponse(Organization organization){
        MetisOrganizationView view = new MetisOrganizationView();
        view.setDea(organization.isDea());
        view.setMetadata(organization.getHarvestingMetadata());
        view.setOrganizationId(organization.getOrganizationId());

        return view;
    }
}
