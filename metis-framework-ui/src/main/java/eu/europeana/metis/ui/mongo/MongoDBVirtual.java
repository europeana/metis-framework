package eu.europeana.metis.ui.mongo;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.*;

import eu.europeana.metis.framework.common.HarvestingMetadata;
import eu.europeana.metis.framework.dao.DatasetDao;
import eu.europeana.metis.framework.dao.OrganizationDao;
import eu.europeana.metis.framework.dataset.Dataset;
import eu.europeana.metis.framework.exceptions.NoOrganizationExceptionFound;
import eu.europeana.metis.framework.mongo.MongoProvider;
import eu.europeana.metis.framework.organization.Organization;

public class MongoDBVirtual {	
	DatasetDao dsDao;
	Organization org;
	OrganizationDao orgDao;
    public void createDataset(Dataset dataset) {
         try {
             MongoProvider provider = new MongoProvider("localhost",10000, "test",null,null);
             dsDao = new DatasetDao();
             orgDao = new OrganizationDao();
             org = new Organization();
             org.setOrganizationId("orgId");
             ArrayList<Dataset> datasets = new ArrayList<>();
             datasets.add(dataset);
			org.setDatasets(datasets);
             org.setOrganizationUri("testUri");
             org.setHarvestingMetadata(new HarvestingMetadata());
             
             Field field;
			try {
				field = DatasetDao.class.getDeclaredField("provider");
				field.setAccessible(true);
				field.set(dsDao, provider);
				
				field = OrganizationDao.class.getDeclaredField("provider");
				field.setAccessible(true);
				field.set(orgDao, provider);
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
             dsDao.createDatasetForOrganization(org, dataset);
             orgDao.create(org);
             System.out.println("Dataset: " + dsDao.getByName(dataset.getName()).getName() + " is saved in a virtual MongoDB");
             try {
				List<Dataset> allDatasetsByOrganization = orgDao.getAllDatasetsByOrganization("orgId");
				System.out.println("Datasets:");
				for (Dataset ds : allDatasetsByOrganization) {
					System.out.println(ds.getName());
				}
			} catch (NoOrganizationExceptionFound e) {
				e.printStackTrace();
			}
         } catch (UnknownHostException e) {
             e.printStackTrace();
         }
    }
}
