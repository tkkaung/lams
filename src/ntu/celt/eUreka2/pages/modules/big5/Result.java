package ntu.celt.eUreka2.pages.modules.big5;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.big5.BIG5DAO;
import ntu.celt.eUreka2.modules.big5.BIG5Survey;
import ntu.celt.eUreka2.modules.big5.BIG5SurveyUser;
import ntu.celt.eUreka2.modules.big5.BDimension;
import ntu.celt.eUreka2.modules.big5.BQuestionScore;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;


@Import(stylesheet="Result.css")
public class Result extends AbstractPageBIG5{
	private String pid;
	private int uid;
	@Property
	private BIG5SurveyUser big5User;
	@Property
	private BIG5Survey big5;
	@Property
	private Project project;
	@Property
	private User assessee;
	
	
	@SuppressWarnings("unused")
	@Property
	private BQuestionScore que; 
	
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
	private List<BIG5Survey> big5s;
	@Property
	private List<BDimension> cdimensions;
	@Property
	private BDimension cdimension;
	
	
	
//	@Property
//	private double avgScore = 0;	
	
	
	private Map<String, Double> mapAvgScore ;

	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private BIG5DAO big5DAO;
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
		
		big5s =  big5DAO.getBIG5SurveysByProject(project);
		if(big5s.size() > 0){
			big5 = big5s.get(0);
		}
		cdimensions = big5DAO.getAllBDimensions();

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
			Double avg = getAvg(big5, lDimensionID);
			
			mapAvgScore.put(mapKey,avg );
		}
		return mapAvgScore.get(mapKey);
		
	}
	public double getAvg(BIG5Survey big5 , Integer lDimensionID){
		double total = 0;
		double count = 0;
		List<BIG5SurveyUser> big5UserList = big5DAO.searchBIG5SurveyUser(big5, null, assessee, true);
		
		if(big5UserList.size()==0)
			return -999; //return something very high, so user will not see it.
		
		for(BIG5SurveyUser pu : big5UserList){
			double avg = big5DAO.getAverageScoreByDimension(pu, lDimensionID);
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
			mapAvgScore.put(mapKey, getAvg(big5, lDimensionID));
		}
		return mapAvgScore.get(mapKey);
		
	}
	public double getAvgStyleGroupSelfPre(String StyleGroup){
		String mapKey =  StyleGroup + "_";
		if(! mapAvgScore.containsKey(mapKey)){
			mapAvgScore.put(mapKey, getAvgStyleGroup(big5, StyleGroup));
		}
		return mapAvgScore.get(mapKey);
	}

	
	
	public double getAvgStyleGroup(BIG5Survey big5 , String StyleGroup){
		double total = 0;
		double count = 0;
		List<BIG5SurveyUser> big5UserList = big5DAO.searchBIG5SurveyUser(big5, null, assessee, true);
		
		if(big5UserList.size()==0)
			return -999; //return something very high, so user will not see it.
		
		for(BIG5SurveyUser pu : big5UserList){
			double avg = big5DAO.getAverageScoreByDimensionStyleGroup(pu, StyleGroup);
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
//		avgScore = big5DAO.getAverageScoreByDimension(big5User, ldim.getId());
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
//		return big5DAO.getSTDEVScoreByDimension(big5User, ldim.getId());
//	}
//	public long getCOUNTscore(LDimension ldim){
//		return big5DAO.getCountScoreByDimension(big5User, ldim.getId());
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
