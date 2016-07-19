package eu.europeana.metis.utils;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Archive Utils
 * Created by ymamakis on 6/13/16.
 */
public class ArchiveUtils {

    private final static int BUFFER = 4096;

    /**
     * Extract a local TGZ to a local directory
     * @param zipPath The location of the tgz
     * @param targetPath The target path of the tgz
     * @throws IOException
     */
    public static void  extract(String zipPath,String targetPath) throws IOException {
        FileInputStream fin = new FileInputStream(zipPath);
        BufferedInputStream in = new BufferedInputStream(fin);
        GzipCompressorInputStream gzIn = new GzipCompressorInputStream(in);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(gzIn);
        TarArchiveEntry entry;
        while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
            if (entry.isDirectory()) {
                File f = new File(targetPath+ entry.getName());
                f.mkdirs();
            }
            else {
                int count;
                byte data[] = new byte[BUFFER];
                FileOutputStream fos = new FileOutputStream(targetPath
                        + entry.getName());
                BufferedOutputStream dest = new BufferedOutputStream(fos,
                        BUFFER);
                while ((count = tarIn.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.close();
            }
        }
        tarIn.close();
    }

    /**
     * Extract records from an input stream
     * @param is The input stream to extract records from
     * @return The list of records
     * @throws IOException
     */
    public static List<String>  extractRecords(InputStream is) throws IOException {
        BufferedInputStream in = new BufferedInputStream(is);
        GzipCompressorInputStream gzIn = new GzipCompressorInputStream(in);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(gzIn);
        TarArchiveEntry entry;
        List<String> records = new ArrayList<>();
        while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
            if (!entry.isDirectory()) {
                BufferedReader br = new BufferedReader(new InputStreamReader(tarIn)); // Read directly from tarInput
              //  System.out.println("For File = " + currentEntry.getName());
                String total="";
                String line;
                while ((line = br.readLine()) != null) {
                    total+=line;
                }
                records.add(total);
                //dest.close();
            }
        }
        tarIn.close();
        return records;
    }

}
