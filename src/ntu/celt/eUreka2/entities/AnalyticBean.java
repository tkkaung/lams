package ntu.celt.eUreka2.entities;

import java.io.Serializable;
import java.util.Date;

public class AnalyticBean  {
	
	
	private Project proj;
	private long pageLoad;
	private long numVisit;
	private ProjRole projRole;
	private String moduleName;
	
	
	public AnalyticBean() {
		super();
	}

	public AnalyticBean(Project proj,  long pageLoad, long numVisit) {
		super();
		this.proj = proj;
		this.pageLoad = pageLoad;
		this.numVisit = numVisit;
	}

	public Project getProj() {
		return proj;
	}

	public void setProj(Project proj) {
		this.proj = proj;
	}

	

	public long getPageLoad() {
		return pageLoad;
	}

	public void setPageLoad(long pageLoad) {
		this.pageLoad = pageLoad;
	}

	public void setNumVisit(long numVisit) {
		this.numVisit = numVisit;
	}

	public long getNumVisit() {
		return numVisit;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setProjRole(ProjRole projRole) {
		this.projRole = projRole;
	}

	public ProjRole getProjRole() {
		return projRole;
	}
	
	
	
}
