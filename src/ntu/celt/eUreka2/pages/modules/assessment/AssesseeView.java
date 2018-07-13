package ntu.celt.eUreka2.pages.modules.assessment;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.AuditTrailDAO;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.AuditTrail;
import ntu.celt.eUreka2.entities.Project;
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
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.upload.services.UploadedFile;

public class AssesseeView extends AbstractPageAssessment{
	@SuppressWarnings("unused")
	@Property
	private Project project;
	@Property
	private int aId;
	@Property
	private AssmtFile tempFile;
	@SuppressWarnings("unused")
	@Property
	private AssmtInstructorFile tempInstructorFile;
	
	
	@Property
	private Assessment assmt;
	@Property
	private AssessmentUser assmtUser;
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
	@Property
    private UploadedFile file;
	@Property
    private UploadedFile file2;
	@Inject
	private AttachedFileManager attFileManager;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private AuditTrailDAO auditDAO;
	@Property
	@InjectPage
	private AssesseeHome assesseeHome;
	
	
	@Inject
	private AssessmentDAO aDAO;
	@Inject
	private Messages messages;
	
	void onActivate(EventContext ec) {
		aId = ec.get(Integer.class, 0);
		
		assmt = aDAO.getAssessmentById(aId);
		if(assmt==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AssessmentID", aId));
		
		project = assmt.getProject();
		
		assmtUser = assmt.getAssmtUser(getCurUser());
		if(assmtUser==null){
			assmtUser = new AssessmentUser();
//			assmtUser.setAssessedDate(new Date());
//			assmtUser.setAssessor(assessor)
			assmtUser.setAssessee(getCurUser());
			assmtUser.setAssessment(assmt);
		}
		
	}
	Object[] onPassivate() {
		return new Object[] {aId};
	}
	
	void setupRender(){
		
		if(!canViewAssessment(assmt)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
	}
	
	@CommitAfter
    public Object onSuccessFromForm() throws IOException
    {
	if(file != null){
		AssmtFile f = new AssmtFile();
		f.setId(Util.generateUUID());
		f.setFileName(file.getFileName());
		f.setAliasName(null);
		f.setContentType(file.getContentType());
		f.setSize(file.getSize());
		f.setUploadTime(new Date());
		f.setCreator(getCurUser());
		f.setPath(attFileManager.saveAttachedFile(file, f.getId(), getModuleName(), project.getId()));
		
		assmtUser.addFile(f);

		if (assmt.getAssmtUser(getCurUser()) == null){
			assmt.addAssmtUsers(assmtUser);
			aDAO.updateAssessment(assmt);
		}
		else{
			aDAO.updateAssessment(assmt);
		}
		
		AuditTrail ad = new AuditTrail(project, getModuleName(), String.valueOf(assmt.getId())
				, "Student uploads file", getCurUser());
		ad.setNewValue(f.getFileName());
		auditDAO.saveAuditTrail(ad);
		
		return linkSource.createPageRenderLinkWithContext(AssesseeView.class, assmt.getId());
		}
		return null;
	}
    
	public boolean hasGraded(){
		if(assmtUser==null)
			return false;
		return true;
	}
	
	public List<AssessCriterion> getFirstCriterions(){
		return assmt.getCriterias().get(0).getCriterions();
	}
}
