package eu.europeana.metis.preview.service;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.edm.utils.MongoConstructor;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.metis.dereference.service.xslt.XsltTransformer;
import eu.europeana.metis.identifier.RestClient;
import eu.europeana.metis.preview.persistence.RecordDao;
import eu.europeana.validation.client.ValidationClient;
import eu.europeana.validation.model.ValidationResult;
import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Preview Service implementation to upload (and potentially transform from EDM-External to EDM-Internal) records
 * Created by ymamakis on 9/2/16.
 */
@Component
public class PreviewService {

    private String previewPortalUrl;

    private String crosswalkPath;

    /**
     * Constructor for the preview service
     * @param previewPortalUrl The preview portal URL
     * @param crosswalkPath The crosswalk between EDM-External and EDM-INTERNAL
     */
   public PreviewService(String previewPortalUrl,String crosswalkPath) throws JiBXException{
       bfact = BindingDirectory.getFactory(RDF.class);
       this.previewPortalUrl = previewPortalUrl;
       this.crosswalkPath = crosswalkPath;
   }
    private static IBindingFactory bfact;

    @Autowired
    private RecordDao dao;
    @Autowired
    private RestClient identifierClient;
    @Autowired
    private ValidationClient validationClient;

    public PreviewService() throws JiBXException {
        bfact = BindingDirectory.getFactory(RDF.class);
    }


    /**
     * Persist temporarily (24h) records in the preview portal
     * @param records The records to persist as list of XML strings
     * @param collectionId The collection id to apply (can be null)
     * @param applyCrosswalk Whether the records are in EDM-External (thus need conversion to EDM-Internal)
     * @return The preview URL of the records along with the result of the validation
     * @throws JiBXException
     * @throws IllegalAccessException
     * @throws IOException
     * @throws InstantiationException
     * @throws SolrServerException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws TransformerException
     * @throws ParserConfigurationException
     */
    public ExtendedValidationResult createRecords(List<String> records, String collectionId, boolean applyCrosswalk) throws JiBXException, IllegalAccessException, IOException, InstantiationException, SolrServerException, NoSuchMethodException, InvocationTargetException, TransformerException, ParserConfigurationException {
        if (collectionId == null) {
            collectionId = CollectionUtils.generateCollectionId();
        }
        ExtendedValidationResult list = new ExtendedValidationResult();
        list.setSuccess(true);
        List<ValidationResult> results = new ArrayList<>();
        dao.deleteCollection(collectionId);
        dao.commit();
        for (String record : records) {
            IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
            if (applyCrosswalk) {
                XsltTransformer transformer = new XsltTransformer();
                record = transformer.transform(record, FileUtils.readFileToString(new File(this.getClass().getClassLoader().getResource(crosswalkPath).getFile())));
            }
            ValidationResult result = validationClient.validateRecord("EDM-INTERNAL",record,"undefined");
            if(result.isSuccess()) {
                RDF rdf = (RDF) uctx.unmarshalDocument(new StringReader(record));
                String id = identifierClient.generateIdentifier(collectionId, rdf.getProvidedCHOList().get(0).getAbout()).replace("\"","");
                rdf.getProvidedCHOList().get(0).setAbout(id);
                FullBeanImpl fBean = new MongoConstructor()
                        .constructFullBean(rdf);
                fBean.setAbout(id);
                fBean.setEuropeanaCollectionName(new String[]{collectionId});

                dao.createRecord(fBean);
            } else {
                results.add(result);
                list.setSuccess(false);
            }
        }
        dao.commit();
        list.setResultList(results);
        list.setPortalUrl(previewPortalUrl + collectionId+"*");
        return list;
    }

    /**
     * Delete records every 24 hrs
     * @throws IOException
     * @throws SolrServerException
     */
    @Scheduled(fixedRate = 24*60*60*1000)
    public void deleteRecords() throws IOException, SolrServerException {
        dao.deleteRecordIdsByTimestamp();
    }
}
