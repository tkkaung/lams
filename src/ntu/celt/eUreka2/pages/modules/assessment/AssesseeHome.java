package ntu.celt.eUreka2.pages.modules.assessment;

import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.assessment.Assessment;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.assessment.AssessmentUser;
import ntu.celt.eUreka2.modules.assessment.AssmtFile;
import ntu.celt.eUreka2.modules.assessment.AssmtInstructorFile;
import ntu.celt.eUreka2.modules.assessment.PrivilegeAssessment;

import org.apache.tapestry5.Link;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PageRenderLinkSource;

public class AssesseeHome extends AbstractPageAssessment{
	@Property
	private Project project;
	@Property
	private String pid;
	@Property
	private List<Assessment> assmts;
	@SuppressWarnings("unused")
	@Property
	private Assessment assmt;
	@Property
	private int rowIndex;
	
	@SuppressWarnings("unused")
	@Property
	private AssmtFile tempFile;
	@SuppressWarnings("unused")
	@Property
	private AssmtInstructorFile tempInstructorFile;
	
	
	@SessionState
	private AppState appState;
	
	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private AssessmentDAO aDAO;
	@Inject
	private Messages messages;
	@Inject
	private BeanModelSource beanModelSource;
	@Inject
	private PageRenderLinkSource linkSource;
	

	
	void onActivate(String id) {
		pid = id;
		project = pDAO.getProjectById(pid);
		if(project==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectID", pid));
		
	}
	String onPassivate() {
		return pid;
	}
	
	
	

	public boolean canViewScoredCrit(Assessment assmt){

		if(canAdminAccess(assmt.getProject()))
			return true;
		ProjUser pu = assmt.getProject().getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeAssessment.VIEW_ASSESSMENT_GRADES )){
			return true;
		}

		if (!canRelease(assmt)) return false;
		
		if(assmt.isAllowViewScoredCriteria()){
			return true;
		}
		return false;
	}
	public boolean canViewGrade(Assessment assmt){
		if(canAdminAccess(assmt.getProject()))
			return true;
		ProjUser pu = project.getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeAssessment.VIEW_ASSESSMENT_GRADES )){
			return true;
		}
	
		if (!canRelease(assmt)) return false;

	
		if(assmt.isAllowViewGrade()){
			return true;
		}
		
		return false;
	}
	
	public boolean canViewGradeDetail(AssessmentUser assmtUser){
		if (assmtUser == null) return false;
		Assessment assmt = assmtUser.getAssessment();
		
		if(canAdminAccess(assmt.getProject()))
			return true;

		ProjUser pu = assmt.getProject().getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeAssessment.VIEW_ASSESSMENT_GRADES )){
			return true;
		}

		if (!canRelease(assmt)) return false;

		if(assmt.getAllowViewGradeDetail()
			&& assmtUser.getAssessee().equals(getCurUser())){
			return true;
		}
		return false;
	}
	

	
	
	
	public boolean canUploadFile(Assessment assmt){
		if(canAdminAccess(assmt.getProject()))
			return true;

		ProjUser pu = assmt.getProject().getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeAssessment.VIEW_ASSESSMENT )){
			return true;
		}
		if(assmt.getAllowSubmitFile()){
			return true;
		}
		
		return false;
	}
	
	
	
	public void setupRender() {
		
		assmts = aDAO.getVisibleAssessments(project);
		
	}
	
	
	public boolean isSetsdateORedate(Assessment assmt){
		if(assmt.getSdate()!=null ||assmt.getEdate()!=null)
			return true;
		return false;
	}
	
	
	private boolean hasAllowViewGradeCriteria(){
		for(Assessment as : assmts){
			if(as.isAllowViewGradeCriteria())
				return true;
		}
		return false;
	}
	private boolean hasAllowViewScoredCriteria(){
		for(Assessment as : assmts){
			if(as.isAllowViewScoredCriteria())
				return true;
		}
		return false;
	}
	private boolean hasAllowViewGrade(){
		for(Assessment as : assmts){
			if(as.isAllowViewGrade())
				return true;
		}
		return false;
	}
	private boolean hasAllowViewCommentAndComment(){
		for(Assessment as : assmts){
			assmtUser = as.getAssmtUser(getCurUser());
			if(assmtUser!=null  && assmtUser.hasComments()
					&& (as.getAllowViewComment() || as.getAllowViewCommentRightAway()) )
				return true;
		}
		return false;
	}
	private boolean hasAllowSubmitFile(){
		for(Assessment as : assmts){
			if(as.getAllowSubmitFile() )
				return true;
			assmtUser = as.getAssmtUser(getCurUser());
			if(assmtUser!=null && assmtUser.getFiles().size() > 0)
				return true;
		}
		return false;
	}
	private boolean hasInstructorUploadedFiles(){
		for(Assessment as : assmts){
			assmtUser = as.getAssmtUser(getCurUser());
			if(assmtUser!=null && assmtUser.getInstructorFiles().size()>0)
				return true;
		}
		return false;
	}
	
	
	public BeanModel<Assessment> getModel() {
		BeanModel<Assessment> model = beanModelSource.createEditModel(Assessment.class, messages);
		model.include("name");
		model.get("name").label(messages.get("assessment-name-des-label"));
		
		model.add("order",null);
		model.get("order").label(messages.get("empty-label"));
		
		if(hasAllowViewGradeCriteria()){
			model.add("gradingCriteria",null);
		}
		if(hasAllowViewScoredCriteria()){
			model.add("scoredCriteria",null);
		}
		if(hasAllowViewGrade()){
			model.add("grade",null);
		}
		if(hasAllowViewCommentAndComment()){
			model.add("comment",null);
		}
		if (hasAllowSubmitFile()){
			model.add("upload",null);
		}
		if (hasInstructorUploadedFiles()){
			model.add("instructorUploaded",null);
		}
		
		
		model.reorder("order","name");
		
		return model;
	}
	
	public int getRowNum(){
		return rowIndex+1;
	}
	public int getTotalSize() {
		if (assmts == null)
			return 0;
		return assmts.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	
	@Property
	private AssessmentUser assmtUser;
	public AssessmentUser loadAssmtUser(Assessment assmt){
		assmtUser = assmt.getAssmtUser(getCurUser());
		return null;
	}
	public boolean hasGraded(){
		if(assmtUser==null)
			return false;
		return true;
	}
	
	
	public String getAssmtUserDisplayGrade(){
		if(assmtUser==null)
			return "-";
		if(assmtUser.getTotalGrade()==null){
			if(assmtUser.getAssessment().getCriterias().size()==0){ //if not use rubric (put score directly)
				return assmtUser.getTotalScoreDisplay();
			}
//			return assmtUser.getTotalScoreDisplay();
			return "-";
		}
		else{
			logViewTime(assmtUser);
			return assmtUser.getTotalGrade();
		}
	}
	public String getAssmtUserScore(){
		return assmtUser.getTotalScoreDisplay();
	}
	
	public Link getViewCommmentURL(String assmtId){
		return linkSource.createPageRenderLinkWithContext(AssesseeViewComment.class, assmtId);
	}
	
	@CommitAfter
	public void logViewTime(AssessmentUser assmtUser){
		if(assmtUser.getAssessee().equals(getCurUser())){ //only log view from student
			Date d = new Date();
//			d = Util.getDateHHmm(d);
			if(assmtUser.getAssesseeViewTimes().size()>1){
				assmtUser.getAssesseeViewTimes().remove(1); //remove the index 1, (so to keep only the first and last)
			}
			assmtUser.addAssesseeViewTimes(d);
			aDAO.saveAssessmentUser(assmtUser);
		}
	}
	
}
