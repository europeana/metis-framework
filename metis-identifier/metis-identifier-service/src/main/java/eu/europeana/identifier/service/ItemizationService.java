/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.identifier.service;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.identifier.service.exceptions.DeduplicationException;
import eu.europeana.identifier.service.utils.Decoupler;
import eu.europeana.identifier.service.utils.HttpRetriever;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.jibx.runtime.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Itemization service
 * Created by ymamakis on 2/8/16.
 */
public class ItemizationService {
    private IBindingFactory context;

    @Autowired
    private Decoupler decoupler;
    /**
     * Constructor to instantiate the JiBX context
     */
    public ItemizationService(){
        try {
            context = BindingDirectory.getFactory(RDF.class);
        } catch (JiBXException e) {
            e.printStackTrace();
        }
    }
    /**
     * Itemize a tgz file of EDM records
     * @param tgzFile The tgz file containing a list of EDM records
     * @return A list of itemized Europeana records
     */
    public List<String> itemize(File tgzFile) throws IOException,DeduplicationException,JiBXException{
        HttpRetriever ret = new HttpRetriever().createInstance(new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(tgzFile))));
        ret.setTarInputstream(new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(tgzFile))));
        List<String> records = new ArrayList<>();
        while (ret.hasNext()){
            records.add(ret.next());
        }
        return itemize(records);
    }

    /**
     * Itemize a list of EDM records coming in as a list of strings
     * @param records The list of records to itemize
     * @return A list of itemized records
     */
    public List<String> itemize(List<String> records) throws DeduplicationException, JiBXException {

        List<String> itemizedRecords = new ArrayList<>();
        IMarshallingContext uctx = context.createMarshallingContext();
        for(String record:records){
            List<RDF> rdfs =decoupler.decouple(record);
            for(RDF rdf:rdfs){
                uctx.setIndent(2);
                StringWriter stringWriter = new StringWriter();
                uctx.setOutput(stringWriter);
                uctx.marshalDocument(rdf);
                itemizedRecords.add(stringWriter.toString());

            }
        }
        return itemizedRecords;
    }

    /**
     * Download and itemize a tar.gz file from a URL
     * @param tarUrl The url the tar is located
     * @return The list of itemized records
     */
    public List<String> itemize(URL tarUrl) throws IOException,DeduplicationException,JiBXException {
        HttpRetriever ret = new HttpRetriever().createInstance(tarUrl);
        List<String> records = new ArrayList<>();
        while (ret.hasNext()){
            records.add(ret.next());
        }
        return itemize(records);
    }
}
