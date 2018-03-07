package eu.europeana.normalization;

import eu.europeana.normalization.common.model.NormalizationBatchResult;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;

/**
 * Command line test for the NormalizationLanguageClient
 *
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 16/05/2016
 */
public class TestNormalization {

  public static void main(String[] args) {
    try {
      System.out.println("Working Directory = " +
          System.getProperty("user.dir"));

      //change to correct uri
      List<String> recs = new ArrayList<>();

      //change to correct file location
      FileInputStream in = new FileInputStream(new File(
          "src/test/samples/edm-record.xml"));
      recs.add(IOUtils.toString(in, "UTF-8"));
      in.close();

      //change to correct file location
      in = new FileInputStream(new File(
          "src/test/samples/edm-record-internal.xml"));
      recs.add(IOUtils.toString(in, "UTF-8"));
      in.close();

      NormalizationBatchResult normalizedEdm = NormalizerFactory.getNormalizer().normalize(recs);
      System.out.println(normalizedEdm);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
