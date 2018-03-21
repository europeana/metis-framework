package eu.europeana.metis.preview.persistence;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.exceptions.MongoRuntimeException;
import eu.europeana.corelib.edm.exceptions.MongoUpdateException;
import eu.europeana.corelib.edm.utils.MongoConstructor;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 * Created by ymamakis on 9/5/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class, loader = AnnotationConfigContextLoader.class)
public class TestRecordDao {


    @Autowired
    private RecordDao recordDao;

    @Autowired
    private SolrServer solrServer;


    @Test
    public void test(){
        try {
            IBindingFactory factory =  BindingDirectory.getFactory(RDF.class);
            IUnmarshallingContext uctx = factory.createUnmarshallingContext();
            StringReader reader = new StringReader(IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("BibliographicResource_2000081662432.rdf")));
            RDF rdf = (RDF) uctx.unmarshalDocument(reader);
            rdf.getProvidedCHOList().get(0).setAbout("/12345/" + rdf.getProvidedCHOList().get(0).getAbout());
            FullBeanImpl fBean = new MongoConstructor()
                    .constructFullBean(rdf);

            fBean.setEuropeanaCollectionName(new String[]{"12345"});
            recordDao.createRecord(fBean);
            ModifiableSolrParams params = new ModifiableSolrParams();
            params.add("q","europeana_id:"+ ClientUtils.escapeQueryChars(fBean.getAbout()));
            solrServer.commit();
            Assert.assertNotNull(solrServer.query(params).getResults());
            Assert.assertEquals(1,solrServer.query(params).getResults().getNumFound() );
            //FIXME Disabling the deletion because of the Mongo replication failing in flapdoodle currently
            //recordDao.deleteRecordIdsByTimestamp();

        } catch (JiBXException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (MongoDBException e) {
            e.printStackTrace();
        } catch (MongoRuntimeException e) {
            e.printStackTrace();
        } catch (MongoUpdateException e) {
			e.printStackTrace();
		}
    }

//    @After
//    public void destroy(){
//        //FIXME Disabling the deletion because of the Mongo replication failing in flapdoodle currently
//        //MongoReplicaSet.stop();
//
//        mongoProvider.stop();
//    }
}
