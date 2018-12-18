package ntu.celt.eUreka2.pages.modules.ipsp;


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.ipsp.IPSPDAO;
import ntu.celt.eUreka2.modules.ipsp.IPSPSurvey;
import ntu.celt.eUreka2.modules.ipsp.IPSPSurveyUser;
import ntu.celt.eUreka2.modules.ipsp.IDimension;
import ntu.celt.eUreka2.modules.ipsp.IQuestionScore;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;


public class ViewScore extends AbstractPageIPSP{
	private long id;
	@Property
	private IPSPSurveyUser ipspUser;
	@Property
	private IPSPSurvey ipsp;
	@Property
	private Project project;
	@Property
	private List<IDimension> cdimensions;
	@Property
	private IDimension cdimension;
	
	@Property
	private IQuestionScore queScore; 
	
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
	private IPSPDAO ipspDAO;
	@Inject
	private Messages messages;
	
	void onActivate(EventContext ec) {
		id = ec.get(long.class, 0);
		
		ipspUser = ipspDAO.getIPSPSurveyUserById(id);
		if(ipspUser==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProfUserID", id));
		ipsp = ipspUser.getSurvey();
		project = ipsp.getProject();
	}
	Object[] onPassivate() {
		return new Object[] {id};
	}
	
	void setupRender(){
		cdimensions = ipspDAO.getAllIDimensions();

	}
	
	
	public String loadCalculateScores(){
		int dimCount = IDimension.NUM_DIMENSIONS;
		mapTotal = new HashMap<Integer, Integer>();
		mapCount = new HashMap<Integer, Integer>();
		mapAvg = new HashMap<Integer, Double>();
		for (int i=1; i<=dimCount; i++){
			mapTotal.put(i, 0);
			mapCount.put(i, 0);
		}
		
		Iterator<IQuestionScore> scores = ipspUser.getQuestionScores().iterator();
		while(scores.hasNext()){
			queScore = scores.next();
			
			for(int i=0; i<IDimension.NUM_DIMENSIONS; i++){
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
	
	public Integer getDimensionScore(IQuestionScore queScore, IDimension cdimension){
		Integer s = queScore.getScoreDim(cdimension.getId());
		if (s == 0)
			return null;
		return s;
	}

/*	public String loadAVGscore(String dimID){
		avgScore = ipspDAO.getAverageScoreByDimension(ipspUser, dimID);
		mapAvg.put(dimID, avgScore);
		
		return null;
	}
	public boolean hasAVGScore(){
		if(avgScore==-1)
			return false;
		return true;
	}
	
	public double getSTDEVscore(String dimID){
		return ipspDAO.getSTDEVScoreByDimension(ipspUser, dimID);
	}
	public long getCOUNTscore(String dimID){
		return ipspDAO.getCountScoreByDimension(ipspUser, dimID);
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
		ipspUser = ipspDAO.getIPSPSurveyUserById(puId);
		if(ipspUser==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProfUserID", puId));
		
		project = ipspUser.getSurvey().getProject();
		
		if(!canClearScore(project))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		ipspUser.getQuestionScores().clear();
		ipspUser.setLastQuestionNum(0);
		
		ipspUser.increaseResetCount();
		ipspUser.setFinished(false);
		
		ipspDAO.saveIPSPSurveyUser(ipspUser);
		
		return null;
		
	}
	
}
