package ntu.celt.eUreka2.pages.modules.ipsp;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.ipsp.IPSPDAO;
import ntu.celt.eUreka2.modules.ipsp.IPSPSurvey;
import ntu.celt.eUreka2.modules.ipsp.IPSPSurveyUser;
import ntu.celt.eUreka2.modules.ipsp.IDimension;
import ntu.celt.eUreka2.modules.ipsp.IQuestionScore;
import ntu.celt.eUreka2.modules.ipsp.IQuestionType;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

@Import(stylesheet="Result.css")
public class AssesseeResult2 extends AbstractPageIPSP{
	private String pid;
	@Property
	private IPSPSurveyUser ipspUser;
	@Property
	private IPSPSurvey ipsp;
	@Property
	private Project project;
	@Property
	private User assessee;
	private final String HEADER_PREFIX = "HEADER_";
	@Property
	private List<IDimension> cdimensions;
	@Property
	private IDimension cdimension;
	
	
	@SuppressWarnings("unused")
	@Property
	private IQuestionScore que; 
	
	@SuppressWarnings("unused")
	@Property
	private int rowIndex;
	@Property
	private int tempInt;
	@Property
	private int dimNum;
	@Property
	private List<IPSPSurvey> ipsps;
	@Property
	private List<IPSPSurveyUser> ipspUserListSelf;
	@Property
	private List<IPSPSurveyUser> ipspUserListPeer;
	@Property
	private List<IPSPSurveyUser> ipspUserListAll;	

	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private IPSPDAO ipspDAO;
	@Inject
	private UserDAO uDAO;
	@Inject
	private Messages messages;
	
	
	
	
	void onActivate(EventContext ec) {
		pid = ec.get(String.class, 0);
		
		project = pDAO.getProjectById(pid);
		if(project==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectID", pid));
		assessee = getCurUser();
		
		
	}
	Object[] onPassivate() {
		return new Object[] {pid};
	}
	
	void setupRender(){
				
		ipsps =  ipspDAO.getIPSPSurveysByProject(project);
		if(ipsps.size() > 0){
			ipsp = ipsps.get(0);
		}
		cdimensions = ipspDAO.getAllIDimensions();
		
	}

	
	public String loadIPSPUserList() {
		ipspUserListSelf = ipspDAO.searchIPSPSurveyUser(project, assessee, assessee, IQuestionType.SELF,  true);
		if(ipspUserListSelf.size() > 0)
			ipspUser = ipspUserListSelf.get(0);
		
		ipspUserListPeer = ipspDAO.searchIPSPSurveyUser(project, null, assessee, IQuestionType.PEER,  true);
		
		ipspUserListAll = ipspDAO.searchIPSPSurveyUser(project, null, assessee, null,  true);
		
		
		
		return null;
	}
	
	
	public double getAveragePeerRating(Integer dimensionID) {
		double total = 0;
		double count = 0;
		for(IPSPSurveyUser pu : ipspUserListPeer) {
			total += pu.getScoreDim(dimensionID);
			count++;
		}
		if(count==0)
			return 0;
		return total/count;
	}
	
	public double getLowestRating(Integer dimensionID) {
		double min = 999;
		for(IPSPSurveyUser pu : ipspUserListAll) {
			double s = pu.getScoreDim(dimensionID);
			if(s<min)
				min = s;
		}
		return min;
	}
	public double getHighestRating(Integer dimensionID) {
		double max = 0;
		for(IPSPSurveyUser pu : ipspUserListAll) {
			double s = pu.getScoreDim(dimensionID);
			if(s>max)
				max = s;
		}
		return max;
	}
	
	
}
