package ntu.celt.eUreka2.pages.modules.teameffectiveness;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.teameffectiveness.TEDAO;
import ntu.celt.eUreka2.modules.teameffectiveness.TESurvey;
import ntu.celt.eUreka2.modules.teameffectiveness.TESurveyUser;
import ntu.celt.eUreka2.modules.teameffectiveness.TEDimension;
import ntu.celt.eUreka2.modules.teameffectiveness.TEQuestionScore;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;


public class ViewScore extends AbstractPageTE{
	private long id;
	@Property
	private TESurveyUser teUser;
	@Property
	private TESurvey te;
	@Property
	private Project project;
	@Property
	private List<TEDimension> cdimensions;
	@Property
	private TEDimension cdimension;
	
	@Property
	private TEQuestionScore queScore; 
	
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
	private TEDAO teDAO;
	@Inject
	private Messages messages;
	
	void onActivate(EventContext ec) {
		id = ec.get(long.class, 0);
		
		teUser = teDAO.getTESurveyUserById(id);
		if(teUser==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProfUserID", id));
		te = teUser.getSurvey();
		project = te.getProject();
	}
	Object[] onPassivate() {
		return new Object[] {id};
	}
	
	void setupRender(){
		cdimensions = teDAO.getAllTEDimensions();

	}
	
	
	public String loadCalculateScores(){
		int dimCount = TEDimension.NUM_DIMENSIONS;
		mapTotal = new HashMap<Integer, Integer>();
		mapCount = new HashMap<Integer, Integer>();
		mapAvg = new HashMap<Integer, Double>();
		for (int i=1; i<=dimCount; i++){
			mapTotal.put(i, 0);
			mapCount.put(i, 0);
		}
		
		Iterator<TEQuestionScore> scores = teUser.getQuestionScores().iterator();
		while(scores.hasNext()){
			queScore = scores.next();
			
			for(int i=0; i<TEDimension.NUM_DIMENSIONS; i++){
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
	
	public Integer getDimensionScore(TEQuestionScore queScore, TEDimension cdimension){
		Integer s = queScore.getScoreDim(cdimension.getId());
		if (s == 0)
			return null;
		return s;
	}
	
	

/*	public String loadAVGscore(String dimID){
		avgScore = teDAO.getAverageScoreByDimension(teUser, dimID);
		mapAvg.put(dimID, avgScore);
		
		return null;
	}
	public boolean hasAVGScore(){
		if(avgScore==-1)
			return false;
		return true;
	}
	
	public double getSTDEVscore(String dimID){
		return teDAO.getSTDEVScoreByDimension(teUser, dimID);
	}
	public long getCOUNTscore(String dimID){
		return teDAO.getCountScoreByDimension(teUser, dimID);
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
		teUser = teDAO.getTESurveyUserById(puId);
		if(teUser==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProfUserID", puId));
		
		project = teUser.getSurvey().getProject();
		
		if(!canClearScore(project))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		teUser.getQuestionScores().clear();
		teUser.setLastQuestionNum(0);
		
		teUser.increaseResetCount();
		teUser.setFinished(false);
		
		teDAO.saveTESurveyUser(teUser);
		
		return null;
		
	}
	
}
