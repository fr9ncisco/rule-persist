package com.acme.module.model;

import java.util.ArrayList;
import java.util.List;

public enum RuleArtefactType {

	DRL("drl"),
	XLS("xls"),
	CSV("csv"),
	UNKNOWN("unknown");
	
	private final String extension;
	private RuleArtefactType(String extension){this.extension = extension;}
	public final String getExtension(){return this.extension;}
	
	public static final List<String> getSupportedRuleFileExtensions(){
		List<String> result = new ArrayList<String>();
		for( RuleArtefactType rat:values()){
			if(!rat.equals(UNKNOWN))
				result.add(rat.extension);
		}
		return result;
	}
	
	public static final RuleArtefactType getRuleArtefactTypeFromExtension(String extension){
		for( RuleArtefactType rat:values()){
			if(rat.getExtension().equalsIgnoreCase(extension))
				return rat;
		}
		return UNKNOWN;
	}
}
