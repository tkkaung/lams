package ntu.celt.eUreka2.pages.modules.profiling;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.profiling.LDimension;
import ntu.celt.eUreka2.modules.profiling.ProfileUser;
import ntu.celt.eUreka2.modules.profiling.Profiling;
import ntu.celt.eUreka2.modules.profiling.ProfilingDAO;
import ntu.celt.eUreka2.modules.profiling.QuestionScore;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;


@Import(stylesheet="Result.css")
public class Result extends AbstractPageProfiling{
	private String pid;
	private int uid;
	@Property
	private ProfileUser profUser;
	@Property
	private Profiling prof;
	@Property
	private Project project;
	@Property
	private User assessee;
	private final String HEADER_PREFIX = "HEADER_";
	
	
	@SuppressWarnings("unused")
	@Property
	private QuestionScore que; 
	
	@SuppressWarnings("unused")
	@Property
	private int rowIndex;
	@Property
	private int tempInt;
	@SuppressWarnings("unused")
	@Property
	private String emptyString = "&nbsp;";	
	@Property
	private List<Profiling> profs;
	@Property
	private Profiling profSELFpre;
	@Property
	private Profiling profSELFpost;
	@Property
	private Profiling profPEER;
	@Property
	private Profiling profINSTRUCTOR;
	
	@Property
	private List<LDimension> ldims ;
	@Property
	private String dimID;
	@Property
	private String[] ldimIDsOrder = { 
			null, HEADER_PREFIX + LDimension._S_2_TRANSACTIONAL,
			LDimension._7_MNG_BY_EXCEPT_FIREMAN, 
			LDimension._6_MNG_BY_EXCEPT_POLICEMAN, 
			LDimension._5_CONTINGENT_REWARD,
			LDimension._8_LASSEIZ_FAIRE,
			null, HEADER_PREFIX + LDimension._S_1_TRANSFORMATIONAL,
			LDimension._4_IDEALIZED_INFLUENCE, 
			LDimension._2_INTELLECTUAL_SIMULATION,  LDimension._1_INDIVIDUAL_CONSIDERATION ,
			LDimension._3_INSPIRATION_MOTIVATION,
			null,
			LDimension._9_LDR_FLEX,
			null,
			LDimension._10_PERCEIVED_LEADERSHIP_EFFECTIVENESS
			
			};	
	
	
	
	
//	@Property
//	private double avgScore = 0;	
	
	private Map<String, LDimension> mapDimensions = new HashMap<String, LDimension>();
	
	private Map<String, Double> mapAvgScore ;

	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private ProfilingDAO profDAO;
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
		ldims = profDAO.getAllLDimensions();
		
		for(LDimension ldim : ldims){
			mapDimensions.put(ldim.getId(), ldim);
		}
		
		profs =  profDAO.getProfilingsByProject(project);
		profSELFpre = profs.get(0);
		profSELFpost = profs.get(1);
		profPEER = profs.get(2);
		profINSTRUCTOR = profs.get(3); 
		
	}

	public LDimension getDimension(String id){
		return mapDimensions.get(id);
	}
	public boolean isHeader(String id){
		if(id.startsWith(HEADER_PREFIX))
			return true;
		return false;
	}
	public String getHeader(String id){
		return id.replace(HEADER_PREFIX, "");
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
	
	public double getNormsStart(String lDimensionID){
		double avg = getAverageNorms(project, lDimensionID);
		double stdev = getSTDEVNorms(project, lDimensionID);
		
		if((avg - stdev) < 0)
			return 0;
		return avg - stdev;
	}
	public double getNormsWidth(String lDimensionID){
		double avg = getAverageNorms(project, lDimensionID);
		double stdev = getSTDEVNorms(project, lDimensionID);
		
		if((avg + stdev) > MAX_SCORE)
			return stdev + (MAX_SCORE - avg);
		
		return 2*stdev ;
	}
	public double getNormsStartByStyleGroup(String StyleGroup){
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
		String mapKey = lDimensionID + "SelfPre";
		if(! mapAvgScore.containsKey(mapKey)){
			mapAvgScore.put(mapKey, getAvg(profSELFpre, lDimensionID));
		}
		return mapAvgScore.get(mapKey);
		
	}
	public double getAvgSelfPost(String lDimensionID){
		String mapKey = lDimensionID + "SelfPost";
		if(! mapAvgScore.containsKey(mapKey)){
			mapAvgScore.put(mapKey, getAvg(profSELFpost, lDimensionID));
		}
		return mapAvgScore.get(mapKey);
	}
	public double getAvgPeer(String lDimensionID){
		String mapKey = lDimensionID + "Peer";
		if(! mapAvgScore.containsKey(mapKey)){
			mapAvgScore.put(mapKey, getAvg(profPEER, lDimensionID));
		}
		return mapAvgScore.get(mapKey);
	}
	public double getAvgInstructor(String lDimensionID){
		String mapKey = lDimensionID + "Instructor";
		if(! mapAvgScore.containsKey(mapKey)){
			mapAvgScore.put(mapKey, getAvg(profINSTRUCTOR, lDimensionID));
		}
		return mapAvgScore.get(mapKey);
	}
	public double getAvgOthers(String lDimensionID){
		String mapKey = lDimensionID + "Others";
		if(! mapAvgScore.containsKey(mapKey)){
			
			double avg = (Math.max(getAvgInstructor(lDimensionID), 0)   //do MAX with 0, because don't want to get negative value 
					+ Math.max(getAvgPeer(lDimensionID), 0)) 
					/ 2;
			mapAvgScore.put(mapKey, avg);
		}
		return mapAvgScore.get(mapKey);
	}
	public double getAvgStyleGroupSelfPre(String StyleGroup){
		String mapKey =  StyleGroup + "SelfPre";
		if(! mapAvgScore.containsKey(mapKey)){
			mapAvgScore.put(mapKey, getAvgStyleGroup(profSELFpre, StyleGroup));
		}
		return mapAvgScore.get(mapKey);
	}
	public double getAvgStyleGroupSelfPost(String StyleGroup){
		String mapKey =  StyleGroup + "SelfPost";
		if(! mapAvgScore.containsKey(mapKey)){
			mapAvgScore.put(mapKey, getAvgStyleGroup(profSELFpost, StyleGroup));
		}
		return mapAvgScore.get(mapKey);
	}
	public double getAvgStyleGroupPeer(String StyleGroup){
		String mapKey =  StyleGroup + "Peer";
		if(! mapAvgScore.containsKey(mapKey)){
			mapAvgScore.put(mapKey, getAvgStyleGroup(profPEER, StyleGroup));
		}
		return mapAvgScore.get(mapKey);
	}
	public double getAvgStyleGroupInstructor(String StyleGroup){
		String mapKey =  StyleGroup + "Instructor";
		if(! mapAvgScore.containsKey(mapKey)){
			mapAvgScore.put(mapKey, getAvgStyleGroup(profINSTRUCTOR, StyleGroup));
		}
		return mapAvgScore.get(mapKey);
	}
	public double getAvgStyleGroupOthers(String StyleGroup){
		String mapKey =  StyleGroup + "Others";
		if(! mapAvgScore.containsKey(mapKey)){
			double avg = (Math.max(getAvgStyleGroupInstructor(StyleGroup), 0)   //do MAX with 0, because don't want to get negative value 
					+ Math.max(getAvgStyleGroupPeer(StyleGroup), 0)) 
					/ 2;
			mapAvgScore.put(mapKey, avg);
		}
		return mapAvgScore.get(mapKey);
	}
	
	
	public double getAvg(Profiling prof , String lDimensionID){
		double total = 0;
		double count = 0;
		List<ProfileUser> profUserList = profDAO.searchProfileUser(prof, null, assessee, null, true);
		
		if(profUserList.size()==0)
			return -0; //return something very high, so user will not see it.
		
		for(ProfileUser pu : profUserList){
			double avg = profDAO.getAverageScoreByDimension(pu, lDimensionID);
			if(avg!=-1){
				total += avg;
				count++;
			}
		}
			
		return total/count;
	}
	public double getAvgStyleGroup(Profiling prof , String StyleGroup){
		double total = 0;
		double count = 0;
		List<ProfileUser> profUserList = profDAO.searchProfileUser(prof, null, assessee, null, true);
		
		if(profUserList.size()==0)
			return -0; //return something very high, so user will not see it.
		
		for(ProfileUser pu : profUserList){
			double avg = profDAO.getAverageScoreByDimensionStyleGroup(pu, StyleGroup);
			if(avg!=-1){
				total += avg;
				count++;
			}
		}
			
		return total/count;
	}
	
	
	
	public String getDimID_10PLE(){
		return LDimension._10_PERCEIVED_LEADERSHIP_EFFECTIVENESS;
	}
	public String getDimStyleGroup_Transactional(){
		return LDimension._S_2_TRANSACTIONAL;
	}
	public String getDimStyleGroup_Transformational(){
		return LDimension._S_1_TRANSFORMATIONAL;
	}
	public String getDimStyleGroup_LeadershipFlex(){
		return LDimension._S_3_LEADERSHIP_FLEXIBILITY;
	}
	public String getDimStyleGroup_PerceivedLeadership(){
		return LDimension._S_4_PERCEIVED_LEADERSHIP_EFFECTIVENESS;
	}
	
	public String getCalibrationPointColor(Double selfScore, Double OtherScore){
		if(selfScore > OtherScore)
			return "red";
		else if(selfScore < OtherScore)
			return "blue";
		return "black";
	}
	
	
//	public String loadAVGscore(LDimension ldim){
//		avgScore = profDAO.getAverageScoreByDimension(profUser, ldim.getId());
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
//		return profDAO.getSTDEVScoreByDimension(profUser, ldim.getId());
//	}
//	public long getCOUNTscore(LDimension ldim){
//		return profDAO.getCountScoreByDimension(profUser, ldim.getId());
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
