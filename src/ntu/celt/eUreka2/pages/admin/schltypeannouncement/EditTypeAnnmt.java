package ntu.celt.eUreka2.pages.admin.schltypeannouncement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjTypeDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.SchlTypeAnnouncementDAO;
import ntu.celt.eUreka2.dao.SchoolDAO;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.entities.ProjType;
import ntu.celt.eUreka2.entities.SchlTypeAnnouncement;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.internal.Util;

import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.BeanEditForm;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.chenillekit.tapestry.core.components.DateTimeField;

public class EditTypeAnnmt extends AbstractSchlTypeAnnouncement{
	@Property
	private SchlTypeAnnouncement _annmt;
	private int _id = 0; 
	private List<Integer> accessibleTypeIDs ;
	
	@Inject
    private SchlTypeAnnouncementDAO _schlTypeAnnmtDAO;
	@Inject
    private SchoolDAO _schoolDAO;
	@Inject
    private ProjTypeDAO _projTypeDAO;
	@Inject
	private Messages _messages;
	
	
	void onActivate(int id) {
    	_id = id;
    }
    int onPassivate() {
       return _id;
    }
    public boolean isCreateMode(){
		if(_id == 0)
			return true;
		return false;
	}
       
    public String getTitle(){
    	if(isCreateMode())
    		return _messages.get("add-new")+" "+_messages.get("typeannouncement");
    	return _messages.get("edit")+" "+_messages.get("typeannouncement");
    }
    public String getTitle2(){
    	return _messages.get("manage")+" "+_messages.get("typeannouncement");
    }
   
    void setupRender() {
		if(!getCurUser().hasPrivilege(PrivilegeSystem.PROJ_TYPE_ANNOUNCEMENT)) {
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		accessibleTypeIDs = getAccessibleProjTypeIDs(getCurUser());
		
		if(isCreateMode()){
    		_annmt = new SchlTypeAnnouncement();
    		_annmt.setEnabled(true);
    	}
    	else {
	    	_annmt = _schlTypeAnnmtDAO.getSchlTypeAnnouncementById(_id);
	    	if(_annmt==null)
				throw new RecordNotFoundException(_messages.format("entity-not-exists", "AnnouncementID", _id));
	    	if(!canManageTypeAnnmts(_annmt))
	    		throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
    	}
    }
    

    void onPrepareForSubmitFromForm(){
    	if(isCreateMode()){
    		_annmt = new SchlTypeAnnouncement();
    		_annmt.setEnabled(true);
    	}
    	else {
	    	_annmt = _schlTypeAnnmtDAO.getSchlTypeAnnouncementById(_id);
    	}
    }
    
    @Component(id="form")
	private BeanEditForm form;
    @Component(id="edate")
    private DateTimeField edateField;
    
    void onValidateFormFromForm(){
    	if(_annmt.getSdate()!=null && _annmt.getEdate()!=null
    			&&	_annmt.getSdate().after(_annmt.getEdate())){
    			form.recordError(edateField, _messages.get("endDate-must-be-after-startDate"));
   		}
    	
    }
    @CommitAfter
    Object onSuccessFromForm()
    {
    	if(isCreateMode()){
    		_annmt.setCdate(new Date());
    	}
    	_annmt.getHasReadUsers().clear();
    	_annmt.setMdate(new Date());
    	_annmt.setCreator(getCurUser());
    	_annmt.setContent(Util.filterOutRestrictedHtmlTags(_annmt.getContent()));

    	_schlTypeAnnmtDAO.save(_annmt);
		return ManageTypeAnnmt.class;
    }
    
  
    public SelectModel getSchoolModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		List<School> schoolList = _schoolDAO.getAllSchools();
		for (School s : schoolList) {
			OptionModel optModel = new OptionModelImpl(s.getDisplayNameLong(), s);
			optModelList.add(optModel);
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
    public SelectModel getProjTypeModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		List<ProjType> tList = _projTypeDAO.getAllTypes();
		
		//*** filter 
		for(int i=tList.size()-1; i>=0; i--){
			if(!accessibleTypeIDs.contains(tList.get(i).getId()))
				tList.remove(i);
		}
		
		for (ProjType t : tList) {
			OptionModel optModel = new OptionModelImpl(t.getDisplayName(), t);
			optModelList.add(optModel);
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
}
