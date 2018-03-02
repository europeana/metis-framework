package eu.europeana.metis.service;

import eu.europeana.metis.mapping.model.*;
import eu.europeana.metis.mapping.persistence.*;
import eu.europeana.metis.utils.ArchiveUtils;
import eu.europeana.metis.xsd.XSDParser;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Date;
import java.util.Map;

/**
 *  Aservice for reading an XSD to a MApping template
 * Created by ymamakis on 6/13/16.
 */
@Service
public class XSDService {

    @Autowired
    private MongoMappingDao dao;

    @Autowired
    private MappingsDao mappingsDao;

    @Autowired
    private MappingSchemaDao mappingsSchemaDao;

    @Autowired
    private ElementDao elementDao;

    @Autowired
    private AttributeDao attributeDao;


    private String repository="/tmp";

    /**
     * Generate a template from a TGZ
     * @param bytes A byte array representing a tgz
     * @param rootFile The root XSD file
     * @param mappingName The mapping name to generate
     * @return The id of the mapping
     * @throws IOException
     */
    public String generateTemplateFromTgz(byte[] bytes,String rootFile, String mappingName, String rootXPath, MappingSchema schema, Map<String,String> namespaces) throws IOException {
        String fileName = repository+"/"+mappingName+"_"+new Date().getTime();
        FileUtils.writeByteArrayToFile(new File(fileName),bytes);
        ArchiveUtils.extract(fileName,repository+"/"+mappingName+"/");
        XSDParser parser = new XSDParser(repository+"/"+mappingName +"/"+rootFile);
        Mapping mapping = parser.buildTemplate(schema,"template_"+mappingName,rootXPath,namespaces);
        persistElementsAndAttributes(mapping.getMappings());
        mappingsDao.save(mapping.getMappings());
        mappingsSchemaDao.save(mapping.getTargetSchema());
        return dao.save(mapping).getId().toString();
    }
    /**
     * Generate a template from a url
     * @param url A url pointing to a tgz
     * @param rootFile The root XSD file
     * @param mappingName The mapping name to generate
     * @return The id of the mapping
     * @throws IOException
     */
    public String generateTemplateFromTgzUrl(String url,String rootFile, String mappingName, String rootXPath, MappingSchema schema, Map<String,String> namespaces) throws IOException {
        return generateTemplateFromTgz(downloadFromUrl(url),rootFile,mappingName,rootXPath,schema,namespaces);
    }


    private byte[] downloadFromUrl(String url) throws IOException {
        URL file = new URL(url);
        ReadableByteChannel rbc = Channels.newChannel(file.openStream());
        String fileName = "/tmp/test"+new Date().getTime();
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.getChannel().transferFrom(rbc,0,Long.MAX_VALUE);
        byte[] bytes = FileUtils.readFileToByteArray(new File(fileName));
        FileUtils.deleteQuietly(new File(fileName));
        return bytes;
    }


    private void persistElementsAndAttributes(Mappings mappings){
        if(mappings.getAttributes()!=null){
            for(Attribute attr: mappings.getAttributes()){
                attributeDao.save(attr);
            }
        }
        if(mappings.getElements()!=null){
            for(Element elem:mappings.getElements()){
                persistElementsAndAttributes(elem);
                elementDao.save(elem);
            }
        }
    }

    private void persistElementsAndAttributes(Element element){
        if(element.getAttributes()!=null){
            for(Attribute attr: element.getAttributes()){
                attributeDao.save(attr);
            }
        }
        if(element.getElements()!=null){
            for(Element elem:element.getElements()){
                persistElementsAndAttributes(elem);
                elementDao.save(elem);
            }
        }
    }

}
