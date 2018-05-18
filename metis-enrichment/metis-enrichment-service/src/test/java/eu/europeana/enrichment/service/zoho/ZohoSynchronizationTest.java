package eu.europeana.enrichment.service.zoho;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.enrichment.api.external.model.zoho.ZohoOrganization;
import eu.europeana.enrichment.api.internal.OrganizationTermList;
import eu.europeana.enrichment.service.EntityService;
import eu.europeana.enrichment.service.exception.ZohoAccessException;
import eu.europeana.metis.authentication.dao.ZohoApiFields;

/**
 * Disabled integration. Need to implement betamax with https connectivity for Zoho
 * 
 * @author GordeaS
 *
 */
@Ignore
public class ZohoSynchronizationTest extends BaseZohoAccessSetup {

  String mongoHost;
  int mongoPort;
  EntityService entityService;
  
  Set<String> allowedRoles;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    Properties props = loadProperties("/metis.properties");
    mongoHost = props.getProperty("mongo.hosts");
    mongoPort = Integer.valueOf(props.getProperty("mongo.port"));
    entityService = new EntityService(mongoHost, mongoPort);
    initSearchCriteria();
  }

  private void initSearchCriteria() {
    allowedRoles = new HashSet<String>();
    allowedRoles.add("Data Provider");
    allowedRoles.add("Provider");
    allowedRoles.add("Aggregator");
  }
  
  @After
  public void tearDown() throws Exception {
    entityService.close();
  }

//  @Test
  public void getDeletedOrganizationsFromZohoTest() throws ZohoAccessException {

    List<String> organizationUrls = new ArrayList<String>();
    List<String> deletedZohoOrganizationUrls = new ArrayList<String>();
    List<String> deletedZohoOrganizationList = null;
    int MAX_ITEMS_PER_PAGE = 200;
    int startPage = 1;
    boolean hasNext = true;
    while (hasNext) {
      deletedZohoOrganizationList = zohoAccessService.getDeletedOrganizations(startPage);
      deletedZohoOrganizationUrls.addAll(deletedZohoOrganizationList);
      startPage += 1;
      // if no more organizations exist in Zoho
      if (deletedZohoOrganizationList.size() < MAX_ITEMS_PER_PAGE)
        hasNext = false;
    }
    
    assertNotNull(deletedZohoOrganizationUrls);
    if (deletedZohoOrganizationUrls.size() > 0)
      LOGGER.info("Number of deleted Zoho organizations: " + deletedZohoOrganizationUrls.size());

    for (String deletedZohoOrganizationUrl : deletedZohoOrganizationUrls) {
      Organization dbOrganization = entityService.getOrganizationById(deletedZohoOrganizationUrl);
      if (dbOrganization != null)
        organizationUrls.add(deletedZohoOrganizationUrl);
    }
    assertNotNull(organizationUrls);
    if (organizationUrls.size() > 0)
      LOGGER.info("Number of organizations to delete: " + organizationUrls.size());
  }

//  @Test
  public void getUpdatedOrganizationsFromZohoTest() throws ZohoAccessException {

    List<String> organizationUrls = new ArrayList<String>();
    List<ZohoOrganization> allOrganizationList;
    int start = 1;
    final int rows = 100;
    boolean hasNext = true;
    while (hasNext) {
      allOrganizationList = zohoAccessService.getOrganizations(start, rows, null, null);
      Organization edmOrg;
      List<String> dbOrganizatinoRoles = null;
      Organization dbOrganization;
      
      for (ZohoOrganization org : allOrganizationList) {
          /* validate Zoho organization */
          boolean zohoOrganizationValidation = validateOrganizationsByFilterCriteria(org);
          /* retrieve related organization from database */
          edmOrg = zohoAccessService.toEdmOrganization(org);
          dbOrganization = entityService.getOrganizationById(edmOrg.getAbout());
          /* extract roles from organization object */
          if (dbOrganization != null)
            dbOrganizatinoRoles = entityService.getOrganizationRoles(dbOrganization);
          /* if organization is Zoho validated and not exist in database - create one */
          if (zohoOrganizationValidation && (dbOrganization == null)) {
            organizationUrls.add(edmOrg.getAbout());
          }
          /* validate DB organization */
          boolean dbOrganizationValidation = false;
          if (dbOrganizatinoRoles != null)
            dbOrganizationValidation = validateDbOrganizationsByFilterCriteria(dbOrganizatinoRoles);
          /* if organization match search criteria in database but not in Zoho - 
           * remove from database
           */
          if (dbOrganizationValidation && !zohoOrganizationValidation) {
            organizationUrls.add(edmOrg.getAbout());
          }
      }
      start += rows;
      // if no more organizations exist in Zoho
      if (allOrganizationList.size() < rows)
        hasNext = false;
    }
    
    assertNotNull(organizationUrls);
    if (organizationUrls.size() > 0) {
      LOGGER.info("Number of organizations to update: " + organizationUrls.size());
      int count = 0;
      for (String orgnizationUrl : organizationUrls) {
        System.out.println(count + ": " + orgnizationUrl);
        count++;
      }
    }
  }
  
  @Test
  public void deleteDatabaseEntriesByListTest() {    
      Organization dbOrganization = entityService.getOrganizationById(TEST_BNF_URL);
      if (dbOrganization != null) {
        /* create two test organizations from existing enrichment organization */
        OrganizationImpl org1 = initTestOrganizationImpl(dbOrganization, TEST_BNF_URL_TMP_1, TEST_BNF_PREF_LABEL_1);
        OrganizationImpl org2 = initTestOrganizationImpl(dbOrganization, TEST_BNF_URL_TMP_2, TEST_BNF_PREF_LABEL_2);
        /* store created test organizations in database */
        OrganizationTermList otl1 = entityService.storeOrganization(org1, null, null);
        OrganizationTermList otl2 = entityService.storeOrganization(org2, null, null);
        assertNotNull(otl1);
        assertNotNull(otl2);
        /* validate URLs and labels of nearly stored organizations */
        Organization storedOrg1 = entityService.getOrganizationById(org1.getAbout());
        assertTrue(storedOrg1.getAbout().equals(TEST_BNF_URL_TMP_1));
        assertTrue(storedOrg1.getEdmAcronym().get(Locale.ENGLISH.getLanguage()).get(0)
            .equals(TEST_BNF_PREF_LABEL_1));
        Organization storedOrg2 = entityService.getOrganizationById(org2.getAbout());
        assertTrue(storedOrg2.getAbout().equals(TEST_BNF_URL_TMP_2));
        assertTrue(storedOrg2.getEdmAcronym().get(Locale.ENGLISH.getLanguage()).get(0)
            .equals(TEST_BNF_PREF_LABEL_2));
        /* remove nearly stored organizations by list of URLs */
        entityService.deleteOrganizations(
            new ArrayList<String>(Arrays.asList(org1.getAbout(), org2.getAbout())));
        /* verify that organizations were removed */
        Organization removedOrg1 = entityService.getOrganizationById(org1.getAbout());
        Organization removedOrg2 = entityService.getOrganizationById(org2.getAbout());
        assertTrue(removedOrg1 == null);
        assertTrue(removedOrg2 == null);        
      }
  }

  /**
   * This method initializes test OrganizationImpl object from given 
   * enrichment organization object from database.
   * @param dbOrg The organization object from database
   * @param url The test URL
   * @param label The test label
   * @return
   */
  private OrganizationImpl initTestOrganizationImpl(Organization dbOrg, String url, String label) {
    OrganizationImpl org = new OrganizationImpl();
    org.setAbout(url);
    org.setPrefLabel(createMapLabel(label));
    org.setAddress(dbOrg.getAddress());
    org.setAltLabel(dbOrg.getAltLabel());
    org.setDcDescription(dbOrg.getDcDescription());
    org.setEdmEuropeanaRole(dbOrg.getEdmEuropeanaRole());
    org.setEdmAcronym(createMapLabel(label));
    return org;
  }
  
  /**
   * This method creates test label map of type Map<String, List<String>> 
   * by given label
   * @param label The test label
   * @return label map
   */
  private Map<String, List<String>> createMapLabel(String label) {
    Map<String, List<String>> labelMap = new HashMap<String, List<String>>();
    List<String> labelMapValues = new ArrayList<String>();
    labelMapValues.add(label);
    labelMap.put(Locale.ENGLISH.getLanguage(), labelMapValues);
    return labelMap;
  }
  
  public boolean validateDbOrganizationsByFilterCriteria(List<String> organizationRoles) {
    boolean res = false;
    
    for (String organizationRole : organizationRoles) {
      if (allowedRoles.contains(organizationRole))
        res = true;
    }    
    return res;    
  }
  
  /**
   * This method validates that organization roles match to
   * the filter criteria.
   * @param orgList
   * @return filtered and validated orgnization list
   */
  public boolean validateOrganizationsByFilterCriteria(
      ZohoOrganization organization) {
    
    boolean res = false;
    
    if (organization.getRole() != null) {
      String[] organizationRoles = organization.getRole().split(ZohoApiFields.SEMICOLON);
      for (String organizationRole : organizationRoles) {
        if (allowedRoles.contains(organizationRole))
          res = true;
      }
    }
    
    return res;
  }  

}
