package ntu.celt.eUreka2.modules.backuprestore;

import java.util.ArrayList;
import java.util.List;

public class ResultSummary {
	private List<String> addedList = new ArrayList<String>();
	private List<String> deletedList = new ArrayList<String>();
	private List<String> replacedList = new ArrayList<String>();
	private List<String> ignoredList = new ArrayList<String>();
	private List<String> exportedList = new ArrayList<String>();
	private String message;
	private String errorMessage;
	private String warnMessage;
	
	public List<String> getAddedList() {
		return addedList;
	}
	public void setAddedList(List<String> addedList) {
		this.addedList = addedList;
	}
	public void addAddedList(String value) {
		this.addedList.add(value);
	}
	public List<String> getDeletedList() {
		return deletedList;
	}
	public void setDeletedList(List<String> deletedList) {
		this.deletedList = deletedList;
	}
	public void addDeletedList(String value) {
		this.deletedList.add(value);
	}
	public List<String> getReplacedList() {
		return replacedList;
	}
	public void setReplacedList(List<String> replacedList) {
		this.replacedList = replacedList;
	}
	public void addReplacedList(String value) {
		this.replacedList.add(value);
	}
	public List<String> getIgnoredList() {
		return ignoredList;
	}
	public void setIgnoredList(List<String> ignoredList) {
		this.ignoredList = ignoredList;
	}
	public void addIgnoredList(String value) {
		this.ignoredList.add(value);
	}
	public List<String> getExportedList() {
		return exportedList;
	}
	public void setExportedList(List<String> exportedList) {
		this.exportedList = exportedList;
	}
	public void addExportedList(String value) {
		this.exportedList.add(value);
	}
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public boolean hasError(){
		if(errorMessage != null)
			return true;
		return false;
	}
	public String getWarnMessage() {
		return warnMessage;
	}
	public void setWarnMessage(String warnMessage) {
		this.warnMessage = warnMessage;
	}
	public boolean hasWarnMessage(){
		if(warnMessage != null)
			return true;
		return false;
	}
	
	
}
