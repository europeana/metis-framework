package eu.europeana.metis.mapping.rest.controllers;

import eu.europeana.metis.mapping.statistics.DatasetStatistics;
import eu.europeana.metis.service.StatisticsService;
import eu.europeana.metis.utils.ArchiveUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

import static eu.europeana.metis.RestEndpoints.STATISTICS_CALCULATE;

/**
 * A statistics controller exposing a REST API to manage them for a given datset
 * Created by ymamakis on 6/16/16.
 */
@Controller
@Api(value="/",description = "Statistics REST API")
public class StatisticsController {

    @Autowired
    private StatisticsService service;

    /**
     * Calculate the statistics for a dataset
     * @param datasetId The dataset id
     * @param file The tgz file containing the records of the dataset
     * @return The DatasetStatistics after the analysis is complete
     * @throws IOException
     * @throws XMLStreamException
     */
    @RequestMapping(method = RequestMethod.POST,value = STATISTICS_CALCULATE,
            produces = "application/json")
    @ApiOperation(value="Calculate the statistics for a dataset")
    @ResponseBody
    public DatasetStatistics calculateStatistics(@ApiParam("datasetId") @PathVariable(value = "datasetId") String datasetId,
                                                 @ApiParam("file") @RequestParam("file") MultipartFile file)
            throws IOException, XMLStreamException {
        return service.calculateStatistics(datasetId, ArchiveUtils.extractRecords(file.getInputStream()));
    }



    @RequestMapping(method = RequestMethod.GET,value = "/statistics/{datasetId}",
            produces = "application/json")
    @ApiOperation(value = "Get statistics")
    @ResponseBody
    public DatasetStatistics getStatistics(@ApiParam("datasetId") @PathVariable(value = "datasetId") String datasetId,
                                           @RequestParam(value = "from") int from,@RequestParam(value="to") int to) {
        return service.get(datasetId,from, to);
    }
}
