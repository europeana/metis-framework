package eu.europeana.enrichment.cache.proxy;

import eu.europeana.enrichment.service.RedisInternalEnricher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by ymamakis on 9/28/16.
 */
@Controller
public class EnrichmentProxy {

    @Autowired
    private RedisInternalEnricher enricher;

    @RequestMapping(value = "/recreate")
    @ResponseStatus(value = HttpStatus.OK)
    public void populate(){
        enricher.recreate();
    }

    @RequestMapping(value = "/check")
    @ResponseBody
    public String check(){
        return enricher.check();
    }

}
