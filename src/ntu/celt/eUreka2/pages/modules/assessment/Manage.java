package ntu.celt.eUreka2.pages.modules.assessment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.AuditTrailDAO;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.AuditTrail;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.assessment.AssessCriteria;
import ntu.celt.eUreka2.modules.assessment.AssessCriterion;
import ntu.celt.eUreka2.modules.assessment.Assessment;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.scheduling.ExportFileStreamResponse;

import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.corelib.components.Grid;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.apache.tapestry5.services.Request;

import au.com.bytecode.opencsv.CSVWriter;

public class Manage extends AbstractPageAssessment{
	@Property
	private Project project;
	@Property
	private String pid;
	@Property
	private List<Assessment> assmts;
	@Property
	private Assessment assmt;
	@SuppressWarnings("unused")
	@Property
	private int rowIndex;
	private Float prevWeight;
	
	
	@SessionState
	private AppState appState;
	
	
	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private AssessmentDAO aDAO;
	@Inject
	private Request request;
	@Inject
	private Messages messages;
	@Inject
	private BeanModelSource beanModelSource;
	@Inject
	private PropertyConduitSource propertyConduitSource;
	@Inject
	private AuditTrailDAO auditDAO;

	void onActivate(String id) {
		pid = id;
		
		project = pDAO.getProjectById(pid);
		if(project==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectId", pid));
		
	}
	String onPassivate() {
		return pid;
	}
	
	
	void setupRender() {
		if(!canCreateAssessment(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		assmts =  aDAO.getAssessmentsByProject(project);
	}
	
	
	@CommitAfter
	void onActionFromDelete(int aId){
		assmt = aDAO.getAssessmentById(aId);
		if(assmt==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AssessmentId", aId));
		project = assmt.getProject();
		if(!canDeleteAssessment(assmt))
			throw new NotAuthorizedAccessException(messages.format("not-allow-to-delete", assmt.getName() ));
		
		aDAO.deleteAssessment(assmt);
		aDAO.reorderAssessmentNumber(project);
		
		AuditTrail ad = new AuditTrail(project, getModuleName(), String.valueOf(assmt.getId())
				, "Delete" , getCurUser());
		auditDAO.saveAuditTrail(ad);
		

		
		appState.recordInfoMsg(messages.format("successfully-delete-x", assmt.getName()));
	}
	void onPrepareForSubmitFromEditWeightageForm(){
		String assmtId = request.getParameter("assmtId");  //obtain value from 'hidden' input
		assmt = aDAO.getAssessmentById(Integer.parseInt(assmtId));
		prevWeight = assmt.getWeightage();
	}
	@CommitAfter
	void onSuccessFromEditWeightageForm(){
		String prevAuditValue = "";
		String newAuditValue = "";
		
		
		assmt.setMdate(new Date());
		//assmt.setWeightage(weightage)
		aDAO.updateAssessment(assmt);
		
		
		
		if(!assmt.getWeightage().equals(prevWeight)){
			prevAuditValue += "Weightage: " + (prevWeight==null ? "" : prevWeight)
				+ System.lineSeparator();
			newAuditValue +=  "Weightage: " + assmt.getWeightage() + System.lineSeparator();
		}
		
		AuditTrail ad = new AuditTrail(project, getModuleName(), String.valueOf(assmt.getId())
				, "Change Weightage" , getCurUser());
		if (!prevAuditValue.isEmpty() || !newAuditValue.isEmpty()){
			ad.setPrevValue(prevAuditValue);
			ad.setNewValue(newAuditValue);
			
			auditDAO.saveAuditTrail(ad);
		}

	}
	
	
	@CommitAfter
	void onSwapOrder(int id1, int id2){
		Assessment assmt1 = aDAO.getAssessmentById(id1);
		Assessment assmt2 = aDAO.getAssessmentById(id2);
		int tempOrder = assmt1.getOrderNumber();
		assmt1.setOrderNumber(assmt2.getOrderNumber());
		assmt2.setOrderNumber(tempOrder);
		aDAO.updateAssessment(assmt1);
		aDAO.updateAssessment(assmt2);
	}
	@InjectComponent
	private Grid grid;
	public Assessment getNextAssmt(int rowIndex){
		return (Assessment) grid.getDataSource().getRowValue(rowIndex+1);
	}
	public Assessment getPrevAssmt(int rowIndex){
		return (Assessment) grid.getDataSource().getRowValue(rowIndex-1);
	}
	
	public boolean isSetsdateORedate(Assessment assmt){
		if(assmt.getSdate()!=null ||assmt.getEdate()!=null)
			return true;
		return false;
	}
	public BeanModel<Assessment> getModel() {
		BeanModel<Assessment> model = beanModelSource.createEditModel(Assessment.class, messages);
		model.include("name","mdate","orderNumber","weightage");
		model.get("orderNumber").label(messages.get("assmt-OrderNumber-label"));
		model.get("name").label(messages.get("assessment"));
		model.add("group",propertyConduitSource.create(Assessment.class, "group.groupType"));
		model.add("rubric",propertyConduitSource.create(Assessment.class, "rubric.name"));
		model.add("creator",propertyConduitSource.create(Assessment.class, "creator.displayname"));
		model.add("permission",null);
		model.add("release", propertyConduitSource.create(Assessment.class, "rdatedisplay"));
		model.add("action",null);
		
		model.reorder("orderNumber","name","creator","mdate","group", "rubric","weightage","permission", "release", "action");
		
		return model;
	}
	
	public int getTotalSize() {
		if (assmts == null)
			return 0;
		return assmts.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	public boolean isTotalWeight100(){
		if(getTotalWeight()==100)
			return true;
		return false;
	}
	public int getTotalWeight(){
		int total = 0;
		for(Assessment asst: assmts){
			total += asst.getWeightage();
		}
		return total;
	}
	
	StreamResponse onActionFromExport(int aId){
		assmt = aDAO.getAssessmentById(aId);
		if(assmt==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AssessmentId", aId));
		project = assmt.getProject();
		
	    char seperator = ',';
	    
	    String headers[] = {"name", "shortName", "des"
		//		,"sdate", "edate"
				,"rdate"
				, "allowViewGradeCriteria", "allowViewScoredCriteria", "allowViewGrade"
				, "allowViewGradeDetail", "allowSubmitFile", "possibleScore", "rubric-ID", "weightage"
				, "criteria-row", "criteria-col", "allowViewComment", "allowReleaseResult", "allowViewCommentRightAway"};
		
		String datas[] = {assmt.getName(), 
				assmt.getShortName(), 
				assmt.getDes(), 
		//		(assmt.getSdate()==null? "" : Util.formatDateTime(assmt.getSdate(), "yyyy-MMM-dd")),
		//		Util.formatDateTime(assmt.getEdate(), "yyyy-MMM-dd"), 
				Util.formatDateTime(assmt.getRdate(), "dd.MMM.yyyy"),
				Boolean.toString(assmt.isAllowViewGradeCriteria()),
				Boolean.toString(assmt.isAllowViewScoredCriteria()),
				Boolean.toString(assmt.isAllowViewGrade()),
				Boolean.toString(assmt.getAllowViewGradeDetail()),
				Boolean.toString(assmt.getAllowSubmitFile()),
				assmt.getPossibleScore() + "",
				(assmt.getRubric()==null? "" : assmt.getRubric().getId()+""),
				assmt.getWeightage() + "",
				(assmt.getCriterias().size()>0? assmt.getCriterias().size()+"" : "" ),
				(assmt.getCriterias().size()>0? assmt.getCriterias().get(0).getCriterions().size()+"" : "" ),
				Boolean.toString(assmt.getAllowViewComment()),
				Boolean.toString(assmt.getAllowReleaseResult()),
				Boolean.toString(assmt.getAllowViewCommentRightAway())
				
		};
		
	    
	    
		
		//write the workbook to outputStream, then convert it to inputStream
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(bout), seperator);
		
		csvWriter.writeNext(headers);
		csvWriter.writeNext(datas);
		
		if(assmt.getCriterias().size() > 0){
			ArrayList<String> crionData = new ArrayList<String>();
			crionData.add("");
			crionData.add("");
			crionData.add("");
			AssessCriteria acrit = assmt.getCriterias().get(0);
			for(AssessCriterion acrion : acrit.getCriterions()){
				crionData.add(acrion.getScore() + "");
			}
			String[] strArr = new String[crionData.size()];
		    strArr = crionData.toArray(strArr);
			csvWriter.writeNext( strArr);
		}
		
		for(AssessCriteria acrit : assmt.getCriterias()){
			ArrayList<String> critData = new ArrayList<String>();
			critData.add(acrit.getName());
			critData.add(acrit.getDes());
			critData.add(acrit.getWeightage() + "");
			for(AssessCriterion acrion : acrit.getCriterions()){
				critData.add(acrion.getDes());
			}
			String[] strArr = new String[critData.size()];
		    strArr = critData.toArray(strArr);
			csvWriter.writeNext( strArr);
		}
		try {
			csvWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ByteArrayInputStream bis = new ByteArrayInputStream(bout.toByteArray());

		String fileName = ("assmt_" + project.getDisplayName() + "_"+assmt.getName()).replace(" ", "_")+".csv";
		String contentType = "text/csv";
		return new ExportFileStreamResponse(bis, fileName, contentType);
	}
}
