package eu.europeana.metis.framework.rest;

import eu.europeana.metis.framework.dataset.Dataset;
import eu.europeana.metis.framework.exceptions.NoDatasetFoundException;
import eu.europeana.metis.framework.organization.Organization;
import eu.europeana.metis.framework.service.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


/**
 * The dataset controller
 * Created by ymamakis on 2/18/16.
 */
@Controller
public class DatasetController {

    @Autowired
    private DatasetService datasetService;

    /**
     * Method to create a dataset
     * @param org The organization to assign the dataset to
     * @param ds The dataset to persist
     */
    @RequestMapping (value = "/dataset",method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    public void createDataset(@RequestBody Organization org, @RequestBody Dataset ds){
        datasetService.createDataset(org,ds);

    }

    /**
     * Update a dataset
     * @param ds The dataset to update
     */
    @RequestMapping (value = "/dataset",method = RequestMethod.PUT, consumes = "application/json")
    @ResponseBody
    public void updateDataset(@RequestBody Dataset ds){
        datasetService.updateDataset(ds);

    }

    /**
     * Delete a dataset
     * @param org  The organization associated with the dataset
     * @param ds The dataset to delete
     */
    @RequestMapping (value = "/dataset",method = RequestMethod.DELETE, consumes = "application/json")
    @ResponseBody
    public void deleteDataset(@RequestBody Organization org,@RequestBody Dataset ds){
        datasetService.deleteDataset(org, ds);

    }

    /**
     * Get a dataset by name
     * @param name The name of the dataset
     * @return The Dataset
     */
    @RequestMapping (value = "/dataset/{name}",method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Dataset getByName(@PathVariable String name) throws NoDatasetFoundException{
        return datasetService.getByName(name);
    }
}
