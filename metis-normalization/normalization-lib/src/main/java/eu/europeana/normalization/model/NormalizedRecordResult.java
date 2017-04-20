package eu.europeana.normalization.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class NormalizedRecordResult {
	NormalizationReport report;
	String normalizedRecordInEdmXml;
	String errorMessage=null;
	
	public NormalizedRecordResult(String errorMessage, String edmRec) {
		this.errorMessage=errorMessage;
		this.normalizedRecordInEdmXml=edmRec;
	}
	
	public NormalizedRecordResult(String normalizedRecordInEdmXml, NormalizationReport report) {
		super();
		this.normalizedRecordInEdmXml = normalizedRecordInEdmXml;
		this.report = report;
	}
	public NormalizationReport getReport() {
		return report;
	}
	public void setReport(NormalizationReport report) {
		this.report = report;
	}
	public String getNormalizedRecordInEdmXml() {
		return normalizedRecordInEdmXml;
	}
	public void setNormalizedRecordInEdmXml(String normalizedRecordInEdmXml) {
		this.normalizedRecordInEdmXml = normalizedRecordInEdmXml;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	@JsonIgnore
	public boolean isError() {
		return errorMessage!=null;
	}
	
}
