package eu.europeana.enrichment.harvester.dbpedia;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class DbPediaDumpsCollector {

	public DbPediaDumpsCollector() {
		// TODO Auto-generated constructor stub
		
	}
	
	public static void main(String[] args) {

		DbPediaDumpsCollector dbpc = new DbPediaDumpsCollector();

      
        dbpc.harvestDBPediaDumps();
        
    }

	public void harvestDBPediaDumps() {
		File file = new File("src/main/resources/dbpedia_dump_list.txt");
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
			for(String line; (line = br.readLine()) != null; ) {
				if (line.startsWith("http://data"))
					collectControlledDumps(line);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void collectControlledDumps(String dumpResource){
		try{
		URL website = new URL(dumpResource);
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream("src/main/resources/dbpediadumps/"+dumpResource.substring(dumpResource.lastIndexOf('/')));
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
