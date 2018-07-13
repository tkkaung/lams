package ntu.celt.eUreka2.pages.modules.care;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.care.CAREDAO;
import ntu.celt.eUreka2.modules.care.CARESurvey;
import ntu.celt.eUreka2.modules.care.CARESurveyUser;
import ntu.celt.eUreka2.modules.care.CDimension;
import ntu.celt.eUreka2.modules.care.CQuestionScore;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;


@Import(stylesheet="Result.css")
public class Result extends AbstractPageCARE{
	private String pid;
	private int uid;
	@Property
	private CARESurveyUser careUser;
	@Property
	private CARESurvey care;
	@Property
	private Project project;
	@Property
	private User assessee;
	
	
	@SuppressWarnings("unused")
	@Property
	private CQuestionScore que; 
	
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
	private List<CARESurvey> cares;
	@Property
	private List<CDimension> cdimensions;
	@Property
	private CDimension cdimension;
	
	
	
//	@Property
//	private double avgScore = 0;	
	
	
	private Map<String, Double> mapAvgScore ;

	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private CAREDAO careDAO;
	@Inject
	private UserDAO uDAO;
	@Inject
	private Messages messages;
	
	
	
	
	void onActivate(EventContext ec) {
		pid = ec.get(String.class, 0);
		uid = ec.get(int.class, 1);
		
		project = pDAO.getProjectById(pid);
		if(project==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectID", pid));
		assessee = uDAO.getUserById(uid);
		if(assessee==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "UserID", uid));

		mapAvgScore = new HashMap<String, Double>();
		
	}
	Object[] onPassivate() {
		return new Object[] {pid, uid};
	}
	
	void setupRender(){
		
		cares =  careDAO.getCARESurveysByProject(project);
		if(cares.size() > 0){
			care = cares.get(0);
		}
		cdimensions = careDAO.getAllCDimensions();

	}

	
	private final int TOTAL_WIDTH = 1280;
	private final int COLUMN_WIDTH = 178;
	private final int BORDER_WIDTH = 4;
	private final int HIDDEN_COL_WIDTH = 3;
	private final int MAX_SCORE = 5;
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
		double avg = getAverageNorms(project, lDimensionID);
		double stdev = getSTDEVNorms(project, lDimensionID);
		
		if((avg - stdev) < 0)
			return 0;
		return avg - stdev;
	}
	public double getNormsWidth(Integer lDimensionID){
		double avg = getAverageNorms(project, lDimensionID);
		double stdev = getSTDEVNorms(project, lDimensionID);
		
		if((avg + stdev) > MAX_SCORE)
			return stdev + (MAX_SCORE - avg);
		
		return 2*stdev ;
	}
	public double getAvgSelf(Integer lDimensionID){
		String mapKey = lDimensionID + "_";
		if(! mapAvgScore.containsKey(mapKey)){
			Double avg = getAvg(care, lDimensionID);
			
			mapAvgScore.put(mapKey,avg );
		}
		return mapAvgScore.get(mapKey);
		
	}
	public double getAvg(CARESurvey care , Integer lDimensionID){
		double total = 0;
		double count = 0;
		List<CARESurveyUser> careUserList = careDAO.searchCARESurveyUser(care, null, assessee, true);
		
		if(careUserList.size()==0)
			return -999; //return something very high, so user will not see it.
		
		for(CARESurveyUser pu : careUserList){
			double avg = careDAO.getAverageScoreByDimension(pu, lDimensionID);
			if(avg!=-1){
				total += avg;
				count++;
			}
		}
			
		return total/count;
	}
	
	/*public double getNormsStartByStyleGroup(String StyleGroup){
		double avg = getAverageNormsByStyleGroup(project, StyleGroup);
		double stdev = getSTDEVNormsByStyleGroup(project, StyleGroup);
		
		if((avg - stdev) < 0)
			return 0;
		return avg - stdev;
	}
	public double getNormsWidthByStyleGroup(String StyleGroup){
		double avg = getAverageNormsByStyleGroup(project, StyleGroup);
		double stdev = getSTDEVNormsByStyleGroup(project, StyleGroup);
		
		if((avg + stdev) > MAX_SCORE)
			return stdev + (MAX_SCORE - avg);
		
		return 2*stdev ;
	}
	
	
	public double getAvgSelfPre(String lDimensionID){
		String mapKey = lDimensionID + "_1";
		if(! mapAvgScore.containsKey(mapKey)){
			mapAvgScore.put(mapKey, getAvg(care, lDimensionID));
		}
		return mapAvgScore.get(mapKey);
		
	}
	public double getAvgStyleGroupSelfPre(String StyleGroup){
		String mapKey =  StyleGroup + "_";
		if(! mapAvgScore.containsKey(mapKey)){
			mapAvgScore.put(mapKey, getAvgStyleGroup(care, StyleGroup));
		}
		return mapAvgScore.get(mapKey);
	}

	
	
	public double getAvgStyleGroup(CARESurvey care , String StyleGroup){
		double total = 0;
		double count = 0;
		List<CARESurveyUser> careUserList = careDAO.searchCARESurveyUser(care, null, assessee, true);
		
		if(careUserList.size()==0)
			return -999; //return something very high, so user will not see it.
		
		for(CARESurveyUser pu : careUserList){
			double avg = careDAO.getAverageScoreByDimensionStyleGroup(pu, StyleGroup);
			if(avg!=-1){
				total += avg;
				count++;
			}
		}
			
		return total/count;
	}
	
	
	
	public String getCalibrationPointColor(Double selfScore, Double OtherScore){
		if(selfScore > OtherScore)
			return "red";
		else if(selfScore < OtherScore)
			return "blue";
		return "black";
	}*/
	
	
//	public String loadAVGscore(LDimension ldim){
//		avgScore = careDAO.getAverageScoreByDimension(careUser, ldim.getId());
//		mapAvg.put(ldim.getId(), avgScore);
//		
//		return null;
//	}
//	public boolean hasAVGScore(){
//		if(avgScore==-1)
//			return false;
//		return true;
//	}
//	
//	public double getSTDEVscore(LDimension ldim){
//		return careDAO.getSTDEVScoreByDimension(careUser, ldim.getId());
//	}
//	public long getCOUNTscore(LDimension ldim){
//		return careDAO.getCountScoreByDimension(careUser, ldim.getId());
//	}
//	
//	public double getAVG(List<LDimension> ldimList){
//		double total = 0;
//		int count = 0;
//		for(LDimension ldim : ldimList){
//			total += mapAvg.get(ldim.getId());
//			count++;
//		}
//		if(count==0)
//			return -1;
//		return (total / count);
//	}
//	public double getSTDEV(List<LDimension> ldimList){
//		List<Double> values = new ArrayList<Double>();
//		for(LDimension ldim : ldimList){
//			values.add(mapAvg.get(ldim.getId()));
//		}
//		
//		if(values.size()==0)
//			return -1;
//		else{
//			return Util.getStdDev(values);
//		}
//	}
	
	
}
