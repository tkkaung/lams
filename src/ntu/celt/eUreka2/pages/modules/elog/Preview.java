package ntu.celt.eUreka2.pages.modules.elog;


import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.elog.Elog;
import ntu.celt.eUreka2.modules.elog.ElogDAO;
import ntu.celt.eUreka2.modules.elog.ElogFile;
import ntu.celt.eUreka2.modules.elog.ElogStatus;
import ntu.celt.eUreka2.modules.elog.PrivilegeElog;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Response;

public class Preview extends AbstractPageElog  {
	private String eid;
	@Property
	private Elog elog;
	@Property
	private Project project;
	@SuppressWarnings("unused")
	@Property
	private ElogFile attFile;
	
	@Inject
	private ElogDAO eDAO;
	@Inject
	private Messages messages;
	@Inject
	private Response response;
	
	void onActivate(String id) {
		eid = id;
	}
	String onPassivate() {
		return eid;
	}
	
	public String getBreadcrumb(){
		if(canApproveElog(project))
			return messages.get("elog-approve-list")+"=modules/elog/approveoverview?"+project.getId()
				+"," + messages.get("preview-elog")+"=modules/elog/view?"+elog.getId();
		return messages.get("preview-elog")+"=modules/elog/view?"+elog.getId();
	}
	
	
	public void setupRender() {
		elog = eDAO.getElogById(eid);
		if(elog==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ElogId", eid));
		
		project = elog.getProject();
		
		if(!canViewElog(elog)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		response.setIntHeader("X-XSS-Protection", 0); //to let browser execute script after the script was added, 
													// e.g. youtube iframe was added
	}
	
	public boolean canViewElog(Elog e){
		if(e.getProject().isReference() && e.getStatus().equals(ElogStatus.APPROVED))
			return true;
		if(canAdminAccess(project))
			return true;
		ProjUser pu = e.getProject().getMember(getCurUser());
		if(pu==null)
			return false;
		if(e.getAuthor().equals(getCurUser()))
			return true;
		if(pu.hasPrivilege(PrivilegeElog.VIEW_APPROVAL_LIST))
			return true;
		//if(e.getStatus().equals(ElogStatus.APPROVED) && pu.hasPrivilege(PrivilegeElog.VIEW_ELOG))
		//	return true;
		if(e.isPublished() && pu.hasPrivilege(PrivilegeElog.VIEW_ELOG))
			return true;
		
		return false;
	}
	
	public boolean isApproved(Elog elog){
		if(elog.getStatus().equals(ElogStatus.APPROVED))
			return true;
		return false;
	}
	
	
}
