package ntu.celt.eUreka2.pages.modules.resources;

import java.util.Date;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.resources.Resource;
import ntu.celt.eUreka2.modules.resources.ResourceDAO;
import ntu.celt.eUreka2.modules.resources.ResourceFolder;
import ntu.celt.eUreka2.modules.resources.ResourceLink;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;

public class ResourceEdit extends AbstractPageResource {
	@Property
	private Resource resrc;
	@Property
	private String resrcName;
	
	@Property
	private Project curProj;
	private String rid;
	
	@Inject
	private ResourceDAO rDAO;
	@Inject
	private Messages messages;
	@Inject
	private PageRenderLinkSource linkSource;
    
	
	void onActivate(String id) {
		rid = id;
	}
	String onPassivate() {
		return rid;
	}
	void setupRender(){
		resrc = rDAO.getResourceById(rid);
		if(resrc==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ResourceID", rid));
		curProj = resrc.getProj();
		
		if(!canEditResource(resrc))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		resrcName = resrc.getName();
	}
	
	
	
	
	void onPrepareForSubmitFromForm(){
		resrc = rDAO.getResourceById(rid);
		curProj = resrc.getProj();
		
	}
	@Component
	private Form form;
	@Component(id="resrcName")
	private TextField nameField;
	void onValidateFormFromForm(){
		if(!canEditResource(resrc)){
			form.recordError(messages.format("not-authorized-to-edit-x", resrc.getName()));
		}
		if(!resrc.getName().equalsIgnoreCase(resrcName) && rDAO.checkResourceNameExist(resrcName, curProj.getId(), (resrc.getParent()==null? null:resrc.getParent().getId()) )){
			form.recordError(nameField, messages.format("resource-file-name-x-exist", resrcName));
		}
	}
	@CommitAfter
	Object onSuccessFromForm(){
		resrc.setName(resrcName);
		resrc.setMdate(new Date());
		resrc.setEditor(getCurUser());
		resrc.setDes(Util.filterOutRestrictedHtmlTags(resrc.getDes()));
		if(resrc.isLink()){
			ResourceLink rl = (ResourceLink) resrc;
			rl.setUrl(Util.checkAndAddHTTP(rl.getUrl()));
		}
		
		rDAO.updateResource(resrc);
		
		return linkSource.createPageRenderLinkWithContext(Home.class, curProj.getId(), resrc.getParent());
	}
	
	
	
	
	
	
	public String getTitle(){
		if(resrc.isFolder()){
			return messages.get("edit")+" "+messages.get("folder");
		}
		else if(resrc.isLink()){
			return messages.get("edit")+" "+messages.get("link");
		}
		else if(resrc.isFile()){
			return messages.get("edit")+" "+messages.get("file");
		}
		return "";
	}
	
	public String getBreadcrumbLink(){
		String str ="";
		ResourceFolder folder = resrc.getParent();
		while(folder!=null){
			str = ","+Util.encode(Util.truncateString(folder.getName(),50))+"=modules/resources/home?"+folder.getProj().getId()+":"+folder.getId() + str;
			folder = folder.getParent();
		}
		return str;
	}
	
}
