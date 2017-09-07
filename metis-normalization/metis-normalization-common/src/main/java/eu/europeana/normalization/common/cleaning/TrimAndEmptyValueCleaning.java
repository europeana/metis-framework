/* LanguageNormalizer.java - created on 16/03/2016, Copyright (c) 2011 The European Library, all rights reserved */
package eu.europeana.normalization.common.cleaning;

import eu.europeana.normalization.common.NormalizeDetails;
import eu.europeana.normalization.common.ValueNormalization;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main Class to be used by applications applying this lib's langage normalization techniques
 *
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 16/03/2016
 */
public class TrimAndEmptyValueCleaning extends EdmRecordNormalizerBase implements
    ValueNormalization {

  private static final Logger LOGGER = LoggerFactory.getLogger(TrimAndEmptyValueCleaning.class);

  /**
   * Creates a new instance of this class.
   */
  public TrimAndEmptyValueCleaning() {
    super();
  }

  public List<String> normalize(String value) {
    String ret = value.trim();
    if (ret.length() == 0) {
      return Collections.EMPTY_LIST;
    }
    return new ArrayList<String>(1) {
      private static final long serialVersionUID = 1L;

      {
        add(ret);
      }
    };
  }

  public List<NormalizeDetails> normalizeDetailed(String value) {
    String ret = value.trim();
    if (ret.length() == 0) {
      return Collections.emptyList();
    }
    return new ArrayList<NormalizeDetails>(1) {
      private static final long serialVersionUID = 1L;

      {
        add(new NormalizeDetails(ret, 1));
      }
    };
  }

}
