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
package eu.europeana.identifier.service.test;

import eu.europeana.identifier.service.Application;
import eu.europeana.identifier.service.ItemizationService;
import eu.europeana.identifier.service.exceptions.DeduplicationException;
import eu.europeana.identifier.service.utils.HttpRetriever;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.jibx.runtime.JiBXException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ymamakis on 2/9/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class, loader = AnnotationConfigContextLoader.class)
public class ItemizationServiceTest {

    @Autowired
    ItemizationService itemizationService;
    @Test
    public void testFile() throws JiBXException, IOException, DeduplicationException {
        List<String> records = itemizationService.itemize(new File("src/test/resources/records.tgz"));
        Assert.assertTrue(records.size()>0);
        Assert.assertTrue(records.size()==10234);
        HttpRetriever ret = new HttpRetriever().createInstance(new TarArchiveInputStream(
                new GzipCompressorInputStream(new FileInputStream(new File("src/test/resources/records.tgz")))));
        Assert.assertTrue(records.size()>ret.getNumber_of_recs());

    }

    @Test
    public void testRecords() throws JiBXException, IOException, DeduplicationException{
        List<String> readFiles = readFile();
        List<String> records = itemizationService.itemize(readFiles);
        Assert.assertTrue(records.size()>0);
        Assert.assertTrue(records.size()==10234);

        Assert.assertTrue(records.size()>readFiles.size());
    }

    private List<String> readFile() {
    List<String> files = new ArrayList<>();
        TarArchiveInputStream tarInputstream = null;
        try {
            tarInputstream = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream("src/test/resources/records.tgz")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        TarArchiveEntry entry;
        try {
           while( (entry = tarInputstream.getNextTarEntry() )!=null){

                if (!entry.isDirectory()) {


                    byte[] content = new byte[(int) entry.getSize()];

                    tarInputstream.read(content, 0, (int) entry.getSize());

                    files.add(new String(content, "UTF-8"));
                }
            }
        }catch(IOException e){

        }
        return files;
    }

    @Test
    public void testUrl()throws JiBXException, IOException, DeduplicationException{

        List<String> records = itemizationService.itemize(new File("src/test/resources/records.tgz").toURI().toURL());
        Assert.assertTrue(records.size()>0);
        Assert.assertTrue(records.size()==10234);
        HttpRetriever ret = new HttpRetriever().createInstance(new TarArchiveInputStream(
                new GzipCompressorInputStream(new FileInputStream(new File("src/test/resources/records.tgz")))));
        Assert.assertTrue(records.size()>ret.getNumber_of_recs());
    }
}
