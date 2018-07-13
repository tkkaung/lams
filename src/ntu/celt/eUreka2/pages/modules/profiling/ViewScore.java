package ntu.celt.eUreka2.pages.modules.profiling;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.assessment.Assessment;
import ntu.celt.eUreka2.modules.assessment.AssessmentUser;
import ntu.celt.eUreka2.modules.profiling.LDimension;
import ntu.celt.eUreka2.modules.profiling.ProfileUser;
import ntu.celt.eUreka2.modules.profiling.Profiling;
import ntu.celt.eUreka2.modules.profiling.ProfilingDAO;
import ntu.celt.eUreka2.modules.profiling.QuestionScore;
import ntu.celt.eUreka2.pages.modules.assessment.Home;

import org.apache.commons.collections.map.HashedMap;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;

import com.sun.org.apache.bcel.internal.generic.NEW;

public class ViewScore extends AbstractPageProfiling{
	private long id;
	@Property
	private ProfileUser profUser;
	@Property
	private Profiling prof;
	@Property
	private Project project;
	
	
	@SuppressWarnings("unused")
	@Property
	private QuestionScore que; 
	
	@SuppressWarnings("unused")
	@Property
	private int rowIndex;
	@Property
	private int tempInt;
	@Property
	private LDimension ldim;
	@Property
	private List<LDimension> ldims;
	@Property
	private List<LDimension> ldimsNonLeadershipStyle ;
	@Property
	private List<LDimension> ldimsTransformational;
	@Property
	private List<LDimension> ldimsTransactional ;
	
	
	@Property
	private double avgScore = 0;	
	private Map<String, Double> mapAvg = new HashMap<String, Double>();
	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private ProfilingDAO profDAO;
	@Inject
	private Messages messages;
	@Inject
	private PageRenderLinkSource linkSource;
	
	void onActivate(EventContext ec) {
		id = ec.get(long.class, 0);
		
		profUser = profDAO.getProfileUserById(id);
		if(profUser==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProfUserID", id));
		prof = profUser.getProfile();
		project = prof.getProject();
	}
	Object[] onPassivate() {
		return new Object[] {id};
	}
	
	void setupRender(){
		ldims = profDAO.getAllLDimensions();
		
		ldimsNonLeadershipStyle = new ArrayList<LDimension>();
		ldimsTransformational = new ArrayList<LDimension>();
		ldimsTransactional = new ArrayList<LDimension>();
		
		//keep only Transformational and Transactional
		for(int i=ldims.size()-1; i>=0; i--){
			ldim = ldims.get(i);
			if(LDimension._S_1_TRANSFORMATIONAL.equals(ldim.getStyleGroup())){
				ldimsTransformational.add(ldim);
			}
			if(LDimension._S_2_TRANSACTIONAL.equals(ldim.getStyleGroup())){
				ldimsTransactional.add(ldim);
			}
			
			if(!LDimension._S_1_TRANSFORMATIONAL.equals(ldim.getStyleGroup()) 
					&& !LDimension._S_2_TRANSACTIONAL.equals(ldim.getStyleGroup()) ){
				ldimsNonLeadershipStyle.add(0, ldim);
				ldims.remove(i);
			}
		}
	}
	

	public String loadAVGscore(LDimension ldim){
		avgScore = profDAO.getAverageScoreByDimension(profUser, ldim.getId());
		mapAvg.put(ldim.getId(), avgScore);
		
		return null;
	}
	public boolean hasAVGScore(){
		if(avgScore==-1)
			return false;
		return true;
	}
	
	public double getSTDEVscore(LDimension ldim){
		return profDAO.getSTDEVScoreByDimension(profUser, ldim.getId());
	}
	public long getCOUNTscore(LDimension ldim){
		return profDAO.getCountScoreByDimension(profUser, ldim.getId());
	}
	
	public double getAVG(List<LDimension> ldimList){
		double total = 0;
		int count = 0;
		for(LDimension ldim : ldimList){
			total += mapAvg.get(ldim.getId());
			count++;
		}
		if(count==0)
			return -1;
		return (total / count);
	}
	public double getSTDEV(List<LDimension> ldimList){
		List<Double> values = new ArrayList<Double>();
		for(LDimension ldim : ldimList){
			values.add(mapAvg.get(ldim.getId()));
		}
		
		if(values.size()==0)
			return -1;
		else{
			return Util.getStdDev(values);
		}
	}
	
	
	@CommitAfter
	public Object onActionFromClearGrade(long puId){
		profUser = profDAO.getProfileUserById(puId);
		if(profUser==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProfUserID", puId));
		
		project = profUser.getProfile().getProject();
		
		if(!canClearScore(project))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		profUser.getQuestionScores().clear();
		profUser.setLastQuestionNum(0);
		profUser.increaseResetCount();
		profUser.setFinished(false);
		
		profDAO.saveProfileUser(profUser);
		
		return null;
		
	}
	
}
