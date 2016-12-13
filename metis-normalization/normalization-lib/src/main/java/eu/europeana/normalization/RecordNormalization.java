package eu.europeana.normalization;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import eu.europeana.normalization.model.NormalizationReport;


public interface RecordNormalization {

	public NormalizationReport normalize(Document edm);
	
}
