package ntu.celt.eUreka2.pages.modules.group;

import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.group.Group;
import ntu.celt.eUreka2.modules.group.GroupDAO;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Grid;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.apache.tapestry5.services.Request;

public class GroupIndex  extends AbstractPageGroup{
	@Property
	private String projId;
	@Property
	private Project curProj;
	
	@SuppressWarnings("unused")
	@Property
	private Group group;
	@SuppressWarnings("unused")
	@Property
	private List<Group> groups;
	private String[] selGroupIDs;
	@SuppressWarnings("unused")
	@Property
	private int numNonLeaderMembers;
	
	enum SubmitType {DELETE_GROUP};
	private SubmitType submitType ;
	
	
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private GroupDAO groupDAO;
	@Inject
	private Messages messages;
	@Inject
    private BeanModelSource beanModelSource;
    @Inject
    private PropertyConduitSource propertyConduitSource; 
    @Inject
	private Request request;
	
	@InjectComponent
	private Grid grid;
	@Property
	private int rowIndex;


	@SessionState
	private AppState appState;
	
	
	void onActivate(EventContext ec) {
		projId = ec.get(String.class, 0);
		
	}
	Object[] onPassivate() {
		return new Object[] {projId };
	}
	
	void setupRender(){
		curProj = projDAO.getProjectById(projId);
		if(curProj==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjId", projId));
		}
		
		if(!canManageGroup(curProj)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		groups = groupDAO.getGroupsByProject(curProj);
		numNonLeaderMembers = getNonLeaderMembers(curProj).size();
	}

	void onSelectedFromDelete() {
		submitType = SubmitType.DELETE_GROUP;
	}
	
	@Component(id = "GridForm")
	private Form _form;
	void onValidateFormFromGridForm() {
		curProj = projDAO.getProjectById(projId);
			
		selGroupIDs = request.getParameters("gridChkBox");
		if(selGroupIDs==null || selGroupIDs.length==0){
			_form.recordError(messages.get("select-at-least-one-item"));
		}
	}
	
	@CommitAfter
	void onSuccessFromGridForm() {
		if(selGroupIDs!=null){
		for(String id : selGroupIDs){
			Group group = groupDAO.getGroupById(Long.parseLong(id));

			switch(submitType){
			case DELETE_GROUP:
				if(groupDAO.isGroupBeingUsePE(group)){
					appState.recordErrorMsg( String.format("Cannot delete, the group '%S' is being used by Peer Evaluation Module", group.getGroupType()) );
				//	System.err.println("......PE used: ......." + "....,,,,. group:" + id + "____"+group.getGroupType() );
					
				}
				else if(groupDAO.isGroupBeingUseAS(group)){
					appState.recordErrorMsg( String.format("Cannot delete, the group '%S' is being used by Assessment Module", group.getGroupType()) );
				//	System.err.println("......AS used: ......." + "....,,,,. group:" + id + "____"+group.getGroupType() );
					
				}
				else{
					groupDAO.deleteGroup(group);
				}
				break;
			}
			
		}}
	}

	// IMPORT group from CSV

	
	public int getNo(){
		return (grid.getCurrentPage()-1)*grid.getRowsPerPage()+ rowIndex + 1;
	}
	public BeanModel<Group> getModel(){
		BeanModel<Group> model = beanModelSource.createEditModel(Group.class, messages);
        model.include("groupType", "allowSelfEnroll","maxPerGroup", "mdate", "bbID");
        model.add("No", null);
        model.add("chkBox", null);
		model.add("numOfGroup", propertyConduitSource.create(Group.class, "groupUsers.size()")); 
		
        model.add("Enrollment", null);
        model.get("Enrollment").label("test1" + messages.get("Enrollment-label"));
        
        model.reorder("No","chkBox", "groupType", "numOfGroup", "maxPerGroup", "allowSelfEnroll", "Enrollment", "mdate");
		
        return model;
	}
}
