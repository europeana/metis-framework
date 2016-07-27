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
package eu.europeana.identifier.service.utils;

import eu.europeana.corelib.definitions.jibx.*;
import eu.europeana.corelib.utils.EuropeanaUriUtils;
import org.apache.commons.lang.StringUtils;
import org.jibx.runtime.*;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that normalizes and validates the internal EDM record identifiers
 * Created by ymamakis on 2/10/16.
 */
public class IdentifierNormalizer {
    private static IBindingFactory factory;

    static {
        try {
            factory = BindingDirectory.getFactory(RDF.class);
        } catch (JiBXException e) {
            e.printStackTrace();
        }
    }
    public String normalize(String record) throws JiBXException{

        IUnmarshallingContext umctx = factory.createUnmarshallingContext();
        IMarshallingContext mctx = factory.createMarshallingContext();
        RDF edmOBJ = (RDF) umctx.unmarshalDocument(new StringReader(record));

        String id = edmOBJ.getProvidedCHOList().get(0).getAbout();
        String collectionId = StringUtils.substringBefore(
                edmOBJ.getEuropeanaAggregationList().get(0).getDatasetName().getString(),"_");
        String finalId = EuropeanaUriUtils.createEuropeanaId(collectionId,id);

        ProvidedCHOType pType = edmOBJ.getProvidedCHOList().get(0);
        pType.setAbout(IdentifierPrefix.PROVIDEDCHO+finalId);
        edmOBJ.getProvidedCHOList().set(0,pType);
        Aggregation aggregation = edmOBJ.getAggregationList().get(0);
        aggregation.setAbout(IdentifierPrefix.PROVIDER_AGGREGATION+finalId);
        AggregatedCHO aCHO = aggregation.getAggregatedCHO();
        aCHO.setResource(IdentifierPrefix.PROVIDEDCHO+finalId);
        aggregation.setAggregatedCHO(aCHO);
        edmOBJ.getAggregationList().set(0,aggregation);
        EuropeanaAggregationType europeanaAggregationType = edmOBJ.getEuropeanaAggregationList().get(0);
        europeanaAggregationType.setAbout(IdentifierPrefix.EUROPEANA_AGGREGATION+finalId);
        AggregatedCHO euCHO = europeanaAggregationType.getAggregatedCHO();
        euCHO.setResource(IdentifierPrefix.PROVIDEDCHO+finalId);
        europeanaAggregationType.setAggregatedCHO(euCHO);
        edmOBJ.getEuropeanaAggregationList().set(0,europeanaAggregationType);
        List<ProxyType> proxyTypeList = new ArrayList<>();
        for (ProxyType proxyType: edmOBJ.getProxyList()){
            if(proxyType.getEuropeanaProxy()!=null){
                proxyType.setAbout(IdentifierPrefix.EUROPEANA_PROXY+finalId);
                List<ProxyIn> proxyInList = proxyType.getProxyInList();
                if(proxyInList==null){
                    proxyInList = new ArrayList<>();
                    ProxyIn pIn= new ProxyIn();
                    pIn.setResource(IdentifierPrefix.EUROPEANA_AGGREGATION+finalId);
                    proxyInList.add(pIn);
                } else {
                    ProxyIn pIn= proxyInList.get(0);
                    pIn.setResource(IdentifierPrefix.EUROPEANA_AGGREGATION+finalId);
                    proxyInList.add(pIn);
                }
                proxyType.setProxyInList(proxyInList);

            } else {
                proxyType.setAbout(IdentifierPrefix.PROVIDER_PROXY+finalId);
                List<ProxyIn> proxyInList = proxyType.getProxyInList();
                if(proxyInList==null){
                    proxyInList = new ArrayList<>();
                    ProxyIn pIn= new ProxyIn();
                    pIn.setResource(IdentifierPrefix.PROVIDER_AGGREGATION+finalId);
                    proxyInList.add(pIn);
                } else {
                    ProxyIn pIn= proxyInList.get(0);
                    pIn.setResource(IdentifierPrefix.PROVIDER_AGGREGATION+finalId);
                    proxyInList.add(pIn);
                }
                proxyType.setProxyInList(proxyInList);
            }
            ProxyFor pFor = proxyType.getProxyFor();
            if(pFor==null){
                pFor = new ProxyFor();
            }
            pFor.setResource(IdentifierPrefix.PROVIDEDCHO+finalId);
            proxyType.setProxyFor(pFor);
            proxyTypeList.add(proxyType);
        }

        edmOBJ.setProxyList(proxyTypeList);
        mctx.setIndent(2);
        StringWriter stringWriter = new StringWriter();
        mctx.setOutput(stringWriter);
        mctx.marshalDocument(edmOBJ);
        return stringWriter.toString();
    }
}
