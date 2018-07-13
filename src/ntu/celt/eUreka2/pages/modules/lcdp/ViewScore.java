package ntu.celt.eUreka2.pages.modules.lcdp;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.lcdp.LCDPDAO;
import ntu.celt.eUreka2.modules.lcdp.LCDPSurvey;
import ntu.celt.eUreka2.modules.lcdp.LCDPSurveyUser;
import ntu.celt.eUreka2.modules.lcdp.PQuestionScore;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;


public class ViewScore extends AbstractPageLCDP{
	private long id;
	@Property
	private LCDPSurveyUser lcdpUser;
	@Property
	private LCDPSurvey lcdp;
	@Property
	private Project project;
	
	
	@Property
	private PQuestionScore que; 
	
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
	private LCDPDAO lcdpDAO;
	@Inject
	private Messages messages;
	
	void onActivate(EventContext ec) {
		id = ec.get(long.class, 0);
		
		lcdpUser = lcdpDAO.getLCDPSurveyUserById(id);
		if(lcdpUser==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProfUserID", id));
		lcdp = lcdpUser.getSurvey();
		project = lcdp.getProject();
	}
	Object[] onPassivate() {
		return new Object[] {id};
	}
	
	void setupRender(){
		

	}
	
	
	public String loadCalculateScores(){
		int dimCount = 3;
		mapTotal = new HashMap<Integer, Integer>();
		mapCount = new HashMap<Integer, Integer>();
		mapAvg = new HashMap<Integer, Double>();
		for (int i=1; i<=dimCount; i++){
			mapTotal.put(i, 0);
			mapCount.put(i, 0);
		}
		
		Iterator<PQuestionScore> scores = lcdpUser.getQuestionScores().iterator();
		while(scores.hasNext()){
			que = scores.next();
			if(que.getQuestion().isDimLeadership()){
				int dimNum = 1;
				mapTotal.put(dimNum, mapTotal.get(dimNum) + que.getScore());
				mapCount.put(dimNum, mapCount.get(dimNum) + 1);
			}
			if(que.getQuestion().isDimManagement()){
				int dimNum = 2;
				mapTotal.put(dimNum, mapTotal.get(dimNum) + que.getScore());
				mapCount.put(dimNum, mapCount.get(dimNum) + 1);
							
			}
			if(que.getQuestion().isDimCommand()){
				int dimNum = 3;
				mapTotal.put(dimNum, mapTotal.get(dimNum) + que.getScore());
				mapCount.put(dimNum, mapCount.get(dimNum) + 1);
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
	

/*	public String loadAVGscore(String dimID){
		avgScore = lcdpDAO.getAverageScoreByDimension(lcdpUser, dimID);
		mapAvg.put(dimID, avgScore);
		
		return null;
	}
	public boolean hasAVGScore(){
		if(avgScore==-1)
			return false;
		return true;
	}
	
	public double getSTDEVscore(String dimID){
		return lcdpDAO.getSTDEVScoreByDimension(lcdpUser, dimID);
	}
	public long getCOUNTscore(String dimID){
		return lcdpDAO.getCountScoreByDimension(lcdpUser, dimID);
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
		lcdpUser = lcdpDAO.getLCDPSurveyUserById(puId);
		if(lcdpUser==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProfUserID", puId));
		
		project = lcdpUser.getSurvey().getProject();
		
		if(!canClearScore(project))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		lcdpUser.getQuestionScores().clear();
		lcdpUser.setLastQuestionNum(0);
		lcdpUser.increaseResetCount();
		lcdpUser.setFinished(false);
		
		lcdpDAO.saveLCDPSurveyUser(lcdpUser);
		
		return null;
		
	}
	
}
