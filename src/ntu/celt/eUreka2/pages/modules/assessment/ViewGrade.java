package ntu.celt.eUreka2.pages.modules.assessment;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.assessment.AssessCriteria;
import ntu.celt.eUreka2.modules.assessment.AssessCriterion;
import ntu.celt.eUreka2.modules.assessment.Assessment;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.assessment.AssessmentUser;
import ntu.celt.eUreka2.modules.assessment.AssmtFile;
import ntu.celt.eUreka2.modules.assessment.AssmtInstructorFile;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileManager;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.upload.services.UploadedFile;

public class ViewGrade extends AbstractPageAssessment{
	@Property
	private Project project;
	private int aId;
	private int uId;
	@Property
	private Assessment assmt;
	@Property
	private User user;
	@Property
	private AssessmentUser assmtUser;
	@Property
	private String rubricOrder;
	
	
	@SuppressWarnings("unused")
	@Property
	private AssessCriteria tempRCrit; 
	@SuppressWarnings("unused")
	@Property
	private AssessCriterion tempRCriterion; 
	@SuppressWarnings("unused")
	@Property
	private int rowIndex;
	@SuppressWarnings("unused")
	@Property
	private int colIndex;
	
	@SuppressWarnings("unused")
	@Property
	private AssmtFile tempFile;
	@SuppressWarnings("unused")
	@Property
	private AssmtInstructorFile tempInstructorFile;
	
	@Property
    private UploadedFile file;

	@Inject
	private AssessmentDAO aDAO;
	@Inject
	private UserDAO uDAO;
	@Inject
	private Messages messages;
	@Inject
	private AttachedFileManager attFileManager;
	@Inject
	private Request request;
	
	void onActivate(EventContext ec) {
		aId = ec.get(Integer.class, 0);
		uId = ec.get(Integer.class, 1);
		
		assmt = aDAO.getAssessmentById(aId);
		if(assmt==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AssessmentID", aId));
		user = uDAO.getUserById(uId);
		if(user==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "UserID", uId));
		
		assmtUser = assmt.getAssmtUser(user);
		if(assmtUser==null)
			throw new RecordNotFoundException(messages.format("err-no-graded-data-for-x", user.getDisplayName()));
			
		project = assmt.getProject();
		rubricOrder = getRubricOrderBy(assmt);
	}
	Object[] onPassivate() {
		return new Object[] {aId, uId};
	}
	
	
	
	
	void setupRender(){
		if(!canViewAssessmentGrade(project))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
			
	}
	
	
	
	
	public String getSelectedClassGMATL(AssessCriterion crion){
		return getSelectedClassGMAT(crion, "L");
	}
	public String getSelectedClassGMATH(AssessCriterion crion){
		return getSelectedClassGMAT(crion, "H");
	}
	public String getSelectedClassGMAT(AssessCriterion crion, String HL){
		if(assmtUser == null )
			return "";
		int index = assmtUser.getSelectedCriterions().indexOf(crion);
		if(index != -1){
			if(assmtUser.getSelectedGMATHL().size()>index){
				if(HL.equals(assmtUser.getSelectedGMATHL().get(index)))
					return "selected";
			}
		}
		return "";
	}
	
	public String getAssmtUserDisplayGradeClass(){
		if(assmtUser==null)
			return null;
		if("-".equals(assmtUser.getTotalScoreDisplay()))
			return null;
		return convertScoreToGradeClass(assmtUser.getTotalScore(), 100); 
	}
/*	public int getTotalScore(){
		int total =0 ;
		for(AssessCriterion ac : assmtUser.getSelectedCriterions()){
			total += ac.getScore();
		}
		return total;
	}
	public String getSelCrterions(){
		String str = "" ;
		for(AssessCriterion ac : assmtUser.getSelectedCriterions()){
			str += ac.getId()+",";
		}
		return str;
	}
*/	
	public List<AssessCriterion> getFirstCriterions(){
		return assmt.getCriterias().get(0).getCriterions();
	}
	
	
	@CommitAfter
    public void onSuccessFromForm() throws IOException
    {
		AssmtInstructorFile f = new AssmtInstructorFile();
		f.setId(Util.generateUUID());
		f.setFileName(file.getFileName());
		f.setAliasName(null);
		f.setContentType(file.getContentType());
		f.setSize(file.getSize());
		f.setUploadTime(new Date());
		f.setCreator(getCurUser());
		f.setPath(attFileManager.saveAttachedFile(file, f.getId(), getModuleName(), project.getId()));
		
		assmtUser.addInstructorFile(f);
		
		
		aDAO.updateAssessment(assmt);
		
    }
	
	@InjectComponent
	private Zone attachedFilesInstructorZone;
	@CommitAfter
	Object onActionFromRemoveFileInstructor(String fId){
		AssmtInstructorFile f = aDAO.getAssmtInstructorFileById(fId);
		if(f==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "FileID", fId));
		AssessmentUser assmtUser = f.getAssmtUser();
		Assessment assmt = assmtUser.getAssessment();
		if(!canEditAssessment(assmt))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		attFileManager.removeAttachedFile(f);
		assmtUser.removeInstructorFile(f);
		aDAO.updateAssessment(assmt);
		if(request.isXHR())
			return attachedFilesInstructorZone.getBody();
		return null;
	}
	
	@InjectComponent
	private Zone attachedFilesStudentZone;
	@CommitAfter
	Object onActionFromRemoveFileStudent(String fId){
		AssmtFile f = aDAO.getAssmtFileById(fId);
		if(f==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "FileID", fId));
		AssessmentUser assmtUser = f.getAssmtUser();
		Assessment assmt = assmtUser.getAssessment();
		if(!canEditAssessment(assmt))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		attFileManager.removeAttachedFile(f);
		assmtUser.removeFile(f);
		aDAO.updateAssessment(assmt);
		if(request.isXHR())
			return attachedFilesStudentZone.getBody();
		return null;
	}

	
	
	
}
