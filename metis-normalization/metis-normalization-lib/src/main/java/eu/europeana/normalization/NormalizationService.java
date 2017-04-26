/* LanguageNormalizer.java - created on 16/03/2016, Copyright (c) 2011 The European Library, all rights reserved */
package eu.europeana.normalization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;

import eu.europeana.normalization.util.nlp.IndexUtilUnicode;
import eu.europeana.normalization.model.NormalizationReport;
import eu.europeana.normalization.util.MapOfLists;

/**
 * The main Class to be used by applications applying this lib's langage normalization techniques
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 16/03/2016
 */
public class NormalizationService {
    private static java.util.logging.Logger log = java.util.logging.Logger.getLogger(NormalizationService.class.getName());

    RecordNormalization normalizer;
    
    /**
     * Creates a new instance of this class.
     * 
     * @param targetVocab
     */
    public NormalizationService(RecordNormalization normalizer) {
        super();
        this.normalizer = normalizer;
    }

	public NormalizationReport normalize(Document edm) {
		return normalizer.normalize(edm);
	}


}
