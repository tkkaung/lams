package ntu.celt.eUreka2.pages.modules.big5;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.big5.BIG5DAO;
import ntu.celt.eUreka2.modules.big5.BIG5Survey;
import ntu.celt.eUreka2.modules.big5.BIG5SurveyUser;
import ntu.celt.eUreka2.modules.big5.BDimension;
import ntu.celt.eUreka2.modules.big5.BQuestionScore;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;


public class ViewScore extends AbstractPageBIG5{
	private long id;
	@Property
	private BIG5SurveyUser big5User;
	@Property
	private BIG5Survey big5;
	@Property
	private Project project;
	@Property
	private List<BDimension> cdimensions;
	@Property
	private BDimension cdimension;
	
	@Property
	private BQuestionScore queScore; 
	
	@SuppressWarnings("unused")
	@Property
	private int rowIndex;
/*	@Property
	private int tempInt;
	
	@Property
	private double avgScore = 0;	
	private Map<String, Double> mapAvg = new HashMap<String, Double>();
*/	
	private Map<Integer, Integer> mapTotal = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> mapCount = new HashMap<Integer, Integer>();
	private Map<Integer, Double> mapAvg = new HashMap<Integer, Double>();
	
	
	@Inject
	private BIG5DAO big5DAO;
	@Inject
	private Messages messages;
	
	void onActivate(EventContext ec) {
		id = ec.get(long.class, 0);
		
		big5User = big5DAO.getBIG5SurveyUserById(id);
		if(big5User==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProfUserID", id));
		big5 = big5User.getSurvey();
		project = big5.getProject();
	}
	Object[] onPassivate() {
		return new Object[] {id};
	}
	
	void setupRender(){
		cdimensions = big5DAO.getAllBDimensions();

	}
	
	
	public String loadCalculateScores(){
		int dimCount = BDimension.NUM_DIMENSIONS;
		mapTotal = new HashMap<Integer, Integer>();
		mapCount = new HashMap<Integer, Integer>();
		mapAvg = new HashMap<Integer, Double>();
		for (int i=1; i<=dimCount; i++){
			mapTotal.put(i, 0);
			mapCount.put(i, 0);
		}
		
		Iterator<BQuestionScore> scores = big5User.getQuestionScores().iterator();
		while(scores.hasNext()){
			queScore = scores.next();
			
			for(int i=0; i<BDimension.NUM_DIMENSIONS; i++){
				int dimID = i+1;
				int score = queScore.getScoreDim(dimID);
				if(score != 0){
					mapTotal.put(dimID, mapTotal.get(dimID) + score);
					mapCount.put(dimID, mapCount.get(dimID) + 1);
				}
			}
		}
		for (int i=1; i<=dimCount; i++){
			if(mapCount.get(i)==0){
				mapAvg.put(i, 0.0);
			}
			else{
				double avg = (double) mapTotal.get(i) / mapCount.get(i);
				mapAvg.put(i, avg);
			}
		}
		
		return "";
	}
	public int getTotalDim(int DimNum){
		return mapTotal.get(DimNum);
	}
	public double getAvgDim(int DimNum){
		return mapAvg.get(DimNum);
	}
	public String getAvgDimDisplay(int DimNum){
		return Util.formatDecimal2(mapAvg.get(DimNum));
	}
	
	public Integer getDimensionScore(BQuestionScore queScore, BDimension cdimension){
		Integer s = queScore.getScoreDim(cdimension.getId());
		if (s == 0)
			return null;
		return s;
	}

/*	public String loadAVGscore(String dimID){
		avgScore = big5DAO.getAverageScoreByDimension(big5User, dimID);
		mapAvg.put(dimID, avgScore);
		
		return null;
	}
	public boolean hasAVGScore(){
		if(avgScore==-1)
			return false;
		return true;
	}
	
	public double getSTDEVscore(String dimID){
		return big5DAO.getSTDEVScoreByDimension(big5User, dimID);
	}
	public long getCOUNTscore(String dimID){
		return big5DAO.getCountScoreByDimension(big5User, dimID);
	}
	
	public double getAVG(List<String> dimIDList){
		double total = 0;
		int count = 0;
		for(String dimID : dimIDList){
			total += mapAvg.get(dimID);
			count++;
		}
		if(count==0)
			return -1;
		return (total / count);
	}
	public double getSTDEV(List<String> dimIDList){
		List<Double> values = new ArrayList<Double>();
		for(String dimID : dimIDList){
			values.add(mapAvg.get(dimID));
		}
		
		if(values.size()==0)
			return -1;
		else{
			return Util.getStdDev(values);
		}
	}*/
	
	
	@CommitAfter
	public Object onActionFromClearGrade(long puId){
		big5User = big5DAO.getBIG5SurveyUserById(puId);
		if(big5User==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProfUserID", puId));
		
		project = big5User.getSurvey().getProject();
		
		if(!canClearScore(project))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		big5User.getQuestionScores().clear();
		big5User.setLastQuestionNum(0);
		
		big5User.increaseResetCount();
		big5User.setFinished(false);
		
		big5DAO.saveBIG5SurveyUser(big5User);
		
		return null;
		
	}
	
}
