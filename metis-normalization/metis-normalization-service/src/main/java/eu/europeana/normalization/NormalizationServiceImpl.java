/* LanguageNormalizer.java - created on 16/03/2016, Copyright (c) 2011 The European Library, all rights reserved */
package eu.europeana.normalization;

import eu.europeana.normalization.model.NormalizedRecordResult;
import eu.europeana.normalization.util.XmlUtil;
import java.io.StringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import eu.europeana.normalization.model.NormalizationReport;

/**
 * The main Class to be used by applications applying this lib's langage normalization techniques
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 16/03/2016
 */
public class NormalizationServiceImpl implements NormalizationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NormalizationServiceImpl.class);

    RecordNormalization normalizer;
    
    /**
     * Creates a new instance of this class.
     * 
     * @param normalizer
     */
    public NormalizationServiceImpl(RecordNormalization normalizer) {
        super();
        this.normalizer = normalizer;
    }

	@Override
  public NormalizationReport normalize(Document edm) {
		return normalizer.normalize(edm);
	}

  @Override
  public NormalizedRecordResult processNormalize(String record) {
    try {
      if (record == null) {
        return new NormalizedRecordResult("Missing required parameter 'record'", record);
      }
      Document recordDom = null;
      try {
        recordDom = XmlUtil.parseDom(new StringReader(record));
      } catch (Exception e) {
        return new NormalizedRecordResult(
            "Error parsing XML in parameter 'record': " + e.getMessage(), record);
      }
      NormalizationReport report = normalize(recordDom);
      String writeDomToString = XmlUtil.writeDomToString(recordDom);
      NormalizedRecordResult result = new NormalizedRecordResult(writeDomToString, report);
      return result;
    } catch (Throwable e) {
      LOGGER.info(e.getMessage(), e);
      return new NormalizedRecordResult("Unexpected error: " + e.getMessage(), record);
    }
  }

}
