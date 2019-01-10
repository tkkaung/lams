package ntu.celt.eUreka2.pages.modules.teameffectiveness;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.teameffectiveness.TEDAO;
import ntu.celt.eUreka2.modules.teameffectiveness.TESurvey;
import ntu.celt.eUreka2.modules.teameffectiveness.TESurveyUser;
import ntu.celt.eUreka2.modules.teameffectiveness.TEDimension;
import ntu.celt.eUreka2.modules.teameffectiveness.TEQuestionScore;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

@Import(stylesheet="Result.css")
public class AssesseeResult extends AbstractPageTE{
	private String pid;
	@Property
	private TESurveyUser teUser;
	@Property
	private TESurvey te;
	@Property
	private Project project;
	@Property
	private User assessee;
	private final String HEADER_PREFIX = "HEADER_";
	@Property
	private List<TEDimension> cdimensions;
	@Property
	private TEDimension cdimension;
	
	
	@SuppressWarnings("unused")
	@Property
	private TEQuestionScore que; 
	
	@SuppressWarnings("unused")
	@Property
	private int rowIndex;
	@Property
	private int tempInt;
	@Property
	private int dimNum;
	@SuppressWarnings("unused")
	@Property
	private String emptyString = "&nbsp;";	
	@Property
	private List<TESurvey> tes;
	
	
	private Map<String, Double> mapAvgScore ;

	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private TEDAO teDAO;
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
		
		mapAvgScore = new HashMap<String, Double>();
		
	}
	Object[] onPassivate() {
		return new Object[] {pid};
	}
	
	void setupRender(){
		
		
		tes =  teDAO.getTESurveysByProject(project);
		if(tes.size() > 0){
			te = tes.get(0);
		}
		cdimensions = teDAO.getAllTEDimensions();
	
	}

	
	
	private final int TOTAL_WIDTH = 1280;
	private final int COLUMN_WIDTH = 178;
	private final int BORDER_WIDTH = 4;
	private final int HIDDEN_COL_WIDTH = 3;
	private final int MAX_SCORE = 15;
	public int getTotalWidth(){
		return TOTAL_WIDTH;
	}
	public int getColumnWidth(){
		return COLUMN_WIDTH;
	}
	public int getChartWidth(){
		return (COLUMN_WIDTH + BORDER_WIDTH) * MAX_SCORE;
	}
	public int getDimColWidth(){
		return 360;//TOTAL_WIDTH - getChartWidth() -  HIDDEN_COL_WIDTH;
	}
	
	public int computeWidth(double score){
		return (int) (score * (getChartWidth()+HIDDEN_COL_WIDTH) / MAX_SCORE); 
	}
	
	public double getNormsStart(Integer lDimensionID){
		return 0;
		
/*		double avg = getAverageNorms(project, lDimensionID);
		double stdev = getSTDEVNorms(project, lDimensionID);
		
		if((avg - stdev) < 0)
			return 0;
		return avg - stdev;*/
	}
	public double getNormsWidth(Integer lDimensionID){
		return 0;
/*		double avg = getAverageNorms(project, lDimensionID);
		double stdev = getSTDEVNorms(project, lDimensionID);
		
		if((avg + stdev) > MAX_SCORE)
			return stdev + (MAX_SCORE - avg);
		
		return 2*stdev ;*/
	}
	public double getAvgSelf(Integer lDimensionID){
		String mapKey = lDimensionID + "_";
		if(! mapAvgScore.containsKey(mapKey)){
			Double avg = getAvg(te, lDimensionID);
			
			mapAvgScore.put(mapKey,avg );
		}
		return mapAvgScore.get(mapKey);
		
	}
	public double getAvg(TESurvey te , Integer lDimensionID){
		double total = 0;
		double count = 0;
		List<TESurveyUser> teUserList = teDAO.searchTESurveyUser(te, null, assessee, true);
		
		if(teUserList.size()==0)
			return -999; //return something very high, so user will not see it.
		
		for(TESurveyUser pu : teUserList){
			double avg = teDAO.getAverageScoreByDimension(pu, lDimensionID);
			if(avg!=-1){
				total += avg;
				count++;
			}
		}
			
		return total/count;
	}
	
	
	public boolean isFirstResultTable(String dimensionStr){
		if(dimensionStr.equals("Perceived Team Effectiveness")||dimensionStr.equals("Team viability")||dimensionStr.equals("Individual Satisfaction with the team"))
				return true;
		return false;
	}
	
	public boolean isSecondResultTable(String dimensionStr){
		if(dimensionStr.equals("Perceived Team Effectiveness")||dimensionStr.equals("Team viability")||dimensionStr.equals("Individual Satisfaction with the team"))
				return false;
		return true;
	}

	
	
	
	
}
