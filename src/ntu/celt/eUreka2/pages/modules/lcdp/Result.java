package ntu.celt.eUreka2.pages.modules.lcdp;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.lcdp.LCDPDAO;
import ntu.celt.eUreka2.modules.lcdp.LCDPSurvey;
import ntu.celt.eUreka2.modules.lcdp.LCDPSurveyUser;
import ntu.celt.eUreka2.modules.lcdp.PQuestionScore;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;


@Import(stylesheet="Result.css")
public class Result extends AbstractPageLCDP{
	private String pid;
	private int uid;
	@Property
	private LCDPSurveyUser lcdpUser;
	@Property
	private LCDPSurvey lcdp;
	@Property
	private Project project;
	@Property
	private User assessee;
	
	
	@SuppressWarnings("unused")
	@Property
	private PQuestionScore que; 
	
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
	private List<LCDPSurvey> lcdps;
	@Property
	private int dimCount = 3;
	@Property
	private String[] dimNames = {"Leadership", "Management", "Command"};
	
	
	
//	@Property
//	private double avgScore = 0;	
	
	
	private Map<String, Double> mapAvgScore ;

	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private LCDPDAO lcdpDAO;
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
		
		lcdps =  lcdpDAO.getLCDPSurveysByProject(project);
		if(lcdps.size() > 0){
			lcdp = lcdps.get(0);
		}
			
	}

	public String getDimName(int dimNum){
		return dimNames[dimNum-1];
	}
	
	private final int TOTAL_WIDTH = 1280;
	private final int COLUMN_WIDTH = 126;
	private final int BORDER_WIDTH = 4;
	private final int HIDDEN_COL_WIDTH = 3;
	private final int MAX_SCORE = 7;
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
			Double avg = getAvg(lcdp, lDimensionID);
			
			mapAvgScore.put(mapKey,avg );
		}
		return mapAvgScore.get(mapKey);
		
	}
	public double getAvg(LCDPSurvey lcdp , Integer lDimensionID){
		double total = 0;
		double count = 0;
		List<LCDPSurveyUser> lcdpUserList = lcdpDAO.searchLCDPSurveyUser(lcdp, null, assessee, true);
		
		if(lcdpUserList.size()==0)
			return -999; //return something very high, so user will not see it.
		
		for(LCDPSurveyUser pu : lcdpUserList){
			double avg = lcdpDAO.getAverageScoreByDimension(pu, lDimensionID);
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
			mapAvgScore.put(mapKey, getAvg(lcdp, lDimensionID));
		}
		return mapAvgScore.get(mapKey);
		
	}
	public double getAvgStyleGroupSelfPre(String StyleGroup){
		String mapKey =  StyleGroup + "_";
		if(! mapAvgScore.containsKey(mapKey)){
			mapAvgScore.put(mapKey, getAvgStyleGroup(lcdp, StyleGroup));
		}
		return mapAvgScore.get(mapKey);
	}

	
	
	public double getAvgStyleGroup(LCDPSurvey lcdp , String StyleGroup){
		double total = 0;
		double count = 0;
		List<LCDPSurveyUser> lcdpUserList = lcdpDAO.searchLCDPSurveyUser(lcdp, null, assessee, true);
		
		if(lcdpUserList.size()==0)
			return -999; //return something very high, so user will not see it.
		
		for(LCDPSurveyUser pu : lcdpUserList){
			double avg = lcdpDAO.getAverageScoreByDimensionStyleGroup(pu, StyleGroup);
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
//		avgScore = lcdpDAO.getAverageScoreByDimension(lcdpUser, ldim.getId());
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
//		return lcdpDAO.getSTDEVScoreByDimension(lcdpUser, ldim.getId());
//	}
//	public long getCOUNTscore(LDimension ldim){
//		return lcdpDAO.getCountScoreByDimension(lcdpUser, ldim.getId());
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
