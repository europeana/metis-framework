/*
 * Copyright 2007-2012 The Europeana Foundation
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

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;


/**
 * Retrieves the specified zip file over the remote http location and performs
 * an iteration within the zipped file contents
 *
 * @author Georgios Markakis <gwarkx@hotmail.com>
 * @since 5 Mar 2012
 */
public class HttpRetriever implements Iterator<String> {


  private TarArchiveInputStream tarInputstream;

  private int number_of_recs;

  private int records_read;

  private String dest;


  /**
   * Private Class constructor (can be instantiated only with factory method)
   */
  public HttpRetriever() {

  }


  /**
   * @param url
   * @param destination
   * @return
   * @throws IOException
   */
  public HttpRetriever createInstance(URL url, String destination)
      throws IOException {
    dest = destination;
    HttpRetriever ret = createInstance(url);

    return ret;
  }

  public HttpRetriever createInstance(TarArchiveInputStream stream) throws IOException {
    HttpRetriever ret = new HttpRetriever();

    TarArchiveEntry entry;

    while ((entry = stream.getNextTarEntry()) != null) {

      if (entry.isDirectory()) {

        entry = stream.getNextTarEntry();

      } else {
        ret.number_of_recs++;
      }
    }

    stream.close();

    return ret;
  }

  /**
   * Static synchronized factory method that returns an instance of this
   * class. It first copies the remote file locally and then instantiates the
   * iterator.
   *
   * @param url The url from which to fetch the file
   * @return an instance of this class
   */
  public HttpRetriever createInstance(URL url)
      throws IOException {

    HttpRetriever retriever = new HttpRetriever();

    File destFile;

    //First copy the remote URI to a
    if (dest == null) {
      destFile = new File("/tmp/" + new Date().getTime() + ".tar.gz");
    } else {
      destFile = new File(dest);
    }

    FileUtils.copyURLToFile(url, destFile, 10000000, 100000000);
    try (TarArchiveInputStream tarfile = new TarArchiveInputStream(
        new GzipCompressorInputStream(new FileInputStream(destFile)))) {

      TarArchiveEntry entry;

      while ((entry = tarfile.getNextTarEntry()) != null) {

        if (entry.isDirectory()) {

          entry = tarfile.getNextTarEntry();

        } else {
          retriever.number_of_recs++;
        }
      }
    }

    TarArchiveInputStream iterabletarfile = new TarArchiveInputStream(
        new GzipCompressorInputStream(new FileInputStream(destFile)));

    retriever.tarInputstream = iterabletarfile;

    return retriever;
  }


  /*
   * (non-Javadoc)
   *
   * @see java.util.Iterator#hasNext()
   */
  @Override
  public boolean hasNext() {
    if (records_read > number_of_recs) {
      return false;
    } else {
      return true;
    }

  }

  /*
   * (non-Javadoc)
   *
   * @see java.util.Iterator#next()
   */
  @Override
  public String next() {

    TarArchiveEntry entry;
    try {
      entry = tarInputstream.getNextTarEntry();
      if (entry != null) {
        while (entry.isDirectory()) {
          entry = tarInputstream.getNextTarEntry();
        }

        byte[] content = new byte[(int) entry.getSize()];

        tarInputstream.read(content, 0, (int) entry.getSize());

        String xml = new String(content, "UTF-8");
        records_read++;
        return xml;
      }
      return "";
    } catch (IOException e) {
      records_read++;
      return "";
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see java.util.Iterator#remove()
   */
  @Override
  public void remove() {
    throw new UnsupportedOperationException("Operation not supported");
  }

  // Getters & Setters

  /**
   * @return the number_of_recs
   */
  public int getNumber_of_recs() {
    return number_of_recs;
  }

  /**
   * @param number_of_recs the number_of_recs to set
   */
  public void setNumber_of_recs(int number_of_recs) {
    this.number_of_recs = number_of_recs;
  }

  public void setTarInputstream(TarArchiveInputStream stream) {
    tarInputstream = stream;
  }
}
