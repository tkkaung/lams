package ntu.celt.eUreka2.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



public class AppState implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3380534925020134840L;
	private int rowsPerPage = 20; 
	private List<String> infoMsgs; 
	private List<String> warningMsgs; 
	private List<String> errorMsgs; 
	private Set<String> visitedProjs = new HashSet<String>();  
	
	

	
	public void setRowsPerPage(int rowsPerPage) {
		this.rowsPerPage = rowsPerPage;
	}

	public int getRowsPerPage() {
		return rowsPerPage;
	}
	
	public List<String> getInfoMsgs(){
		return infoMsgs;
	}
	public void recordInfoMsg(String msg){
		if(infoMsgs==null){
			infoMsgs = new ArrayList<String>();
			infoMsgs.add(msg);
		}
		else
			infoMsgs.add(msg);
	}
	public List<String> getWarningMsgs(){
		return warningMsgs;
	}
	public void recordWarningMsg(String msg){
		if(warningMsgs==null){
			warningMsgs = new ArrayList<String>();
			warningMsgs.add(msg);
		}
		else
			warningMsgs.add(msg);
	}
	public List<String> getErrorMsgs(){
		return errorMsgs;
	}
	public void recordErrorMsg(String msg){
		if(errorMsgs==null){
			errorMsgs = new ArrayList<String>();
			errorMsgs.add(msg);
		}
		else
			errorMsgs.add(msg);
	}
	public List<String> popInfoMsgs(){
		List<String> temp = infoMsgs;
		infoMsgs = null;
		return temp;
	}
	public List<String> popWarningMsgs(){
		List<String> temp = warningMsgs;
		warningMsgs = null;
		return temp;
	}
	public List<String> popErrorMsgs(){
		List<String> temp = errorMsgs;
		errorMsgs = null;
		return temp;
	}
	public boolean hasMsgsToDisplay(){
		if(infoMsgs==null && warningMsgs==null && errorMsgs==null)
			return false;
		return true;
	}
	public void setVisitedProjs(HashSet<String> visitedProjs) {
		this.visitedProjs = visitedProjs;
	}
	public Set<String> getVisitedProjs() {
		return visitedProjs;
	}
	public boolean hasVisitedProj(String projID){
		return this.visitedProjs.contains(projID);
	}
	public void addVisitedProj(String projID){
		visitedProjs.add(projID);
	}
}
