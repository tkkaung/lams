package ntu.celt.eUreka2.pages.admin.backuprestore;

import java.util.List;

import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;

import ntu.celt.eUreka2.modules.backuprestore.ResultSummary;

public class ResultPage {
	@Persist
	private List<ResultSummary> results;
	@Property
	private ResultSummary result;
	@Persist
	private String callBackPageLink;
	@Persist
	private String callBackPageName;
	
	
	/* do not use onPassivate and onActivate when use @Persist with the 'results'
	Object[] onPassivate() {
		return new Object[] { results };
	}
	void onActivate( List<MResult> results) {
		this.results = results;
	}
	 */
	
	
	public void setResults(List<ResultSummary> results) {
		this.results = results;
	}
	public List<ResultSummary> getResults() {
		return results;
	}
	public void setCallBackPageLink(String callBackPageLink) {
		this.callBackPageLink = callBackPageLink;
	}
	public String getCallBackPageLink() {
		return callBackPageLink;
	}
	public void setCallBackPageName(String callBackPageName) {
		this.callBackPageName = callBackPageName;
	}
	public String getCallBackPageName() {
		return callBackPageName;
	}
	
	
	public long getTotalChanged(){
		long count = 0;
		count += result.getAddedList().size();
		count += result.getDeletedList().size();
		count += result.getExportedList().size();
		count += result.getIgnoredList().size();
		count += result.getReplacedList().size();
		
		return count;
	}
	
}
