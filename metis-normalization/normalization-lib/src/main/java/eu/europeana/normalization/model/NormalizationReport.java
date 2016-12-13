package eu.europeana.normalization.model;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class NormalizationReport {
	
	Map<String, NormalizationOperationReport> operations=new Hashtable<>();

	public void mergeWith(NormalizationReport other) {
		Set<Entry<String, NormalizationOperationReport>> entrySet = other.getOperations().entrySet();
		for(Entry<String, NormalizationOperationReport> op: entrySet) {
			NormalizationOperationReport myOpRep = operations.get(op.getKey());
			if(myOpRep==null)
				operations.put(op.getKey(), op.getValue());
			else {
				myOpRep.mergeWith(op.getValue());
			}
		}
		
	}

	public Map<String, NormalizationOperationReport> getOperations() {
		return operations;
	}

	public void increment(String op, ConfidenceLevel confidence) {
		NormalizationOperationReport normalizationOperationReport = operations.get(op);
		if(normalizationOperationReport ==null) {
			normalizationOperationReport=new NormalizationOperationReport(op);
			operations.put(op, normalizationOperationReport);
		}
		normalizationOperationReport.increment(confidence);
	}
	
	
}
