package eu.europeana.normalization.normalizers;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import eu.europeana.normalization.RecordNormalization;
import eu.europeana.normalization.model.NormalizationReport;

public class ChainedNormalization implements RecordNormalization {

	List<RecordNormalization> normalizations;
	
	public ChainedNormalization() {
		super();
		this.normalizations = new ArrayList<>();
	}
	
	public ChainedNormalization(List<RecordNormalization> normalizations) {
		super();
		this.normalizations = new ArrayList<>(normalizations);
	}

	public ChainedNormalization(RecordNormalization... normalizations) {
		this();
		for(RecordNormalization norm: normalizations) {
			addNormalization(norm);
		}
	}
	
	public void addNormalization(RecordNormalization norm) {
		normalizations.add(norm);
	}
	
	@Override
	public NormalizationReport normalize(Document edm) {
		NormalizationReport report=new NormalizationReport();
		for(RecordNormalization normOp: normalizations) {
			report.mergeWith(normOp.normalize(edm));
		}
		return report;
	}


}
