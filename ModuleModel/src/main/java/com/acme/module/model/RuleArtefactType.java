package com.acme.module.model;

import java.util.ArrayList;
import java.util.List;

public enum RuleArtefactType {

	DRL("drl"),
	XLS("xls"),
	CSV("csv"),
	UNKNOWN("unknown");

	final String extension;

	RuleArtefactType(String extension) {
		this.extension = extension;
	}

	public static List<String> getSupportedRuleFileExtensions() {
		List<String> result = new ArrayList<String>();
		for( RuleArtefactType rat:values()){
			if(!rat.equals(UNKNOWN))
				result.add(rat.extension);
		}
		return result;
	}

	public static RuleArtefactType getRuleArtefactTypeFromExtension(String extension) {
		for( RuleArtefactType rat:values()){
			if(rat.getExtension().equalsIgnoreCase(extension))
				return rat;
		}
		return UNKNOWN;
	}

	public final String getExtension() {
		return this.extension;
	}
}
