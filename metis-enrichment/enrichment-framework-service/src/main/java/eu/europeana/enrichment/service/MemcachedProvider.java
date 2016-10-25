package eu.europeana.enrichment.service;

import net.spy.memcached.MemcachedClient;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by ymamakis on 9/26/16.
 */
public class MemcachedProvider {
    private InetSocketAddress address;

    public MemcachedProvider(String host,int port){
        address = new InetSocketAddress(host,port);
    }
    public MemcachedClient getClient(){
        try {
            return new MemcachedClient(address);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
