package ntu.celt.eUreka2.pages.modules.message;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.EvenOdd;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.message.Message;
import ntu.celt.eUreka2.modules.message.MessageDAO;
import ntu.celt.eUreka2.modules.message.MessageType;
import ntu.celt.eUreka2.modules.message.PrivilegeMessage;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.apache.tapestry5.services.Request;

public class MessageIndex extends AbstractPageMessage {
	@SessionState
	private AppState appState;
	@Property
	private Project curProj;
	private String projId ;
	@Property
	private MessageType type;
	@Property
	private MessageType tempType;
	private Long unreadCount;
	@Property
	private String searchText;
	
	@Property
	private List<Message> msgs;
	@Property
	private Message msg;
	@Property
	private EvenOdd evenOdd ;
	enum SubmitType {DELETE_FOREVER, DELETE, READ, UNREAD, FLAG, UNFLAG, MOVE_TYPE};
	private SubmitType submitType ;
	@Property
	private MessageType moveToType;
	
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private MessageDAO msgDAO;
	@Inject
    private BeanModelSource beanModelSource;
    @Inject
    private Messages _messages;
	@Inject
	private Request request;
    @Inject
    private PropertyConduitSource propertyConduitSource; 
    @Inject
	private PageRenderLinkSource linkSource;
	
	
	void onActivate(EventContext ec) {
		if(ec.getCount()==1){
			projId = ec.get(String.class, 0);
		}
		if(ec.getCount()==2){
			projId = ec.get(String.class, 0);
			type = ec.get(MessageType.class, 1);
		}
	}
	Object[] onPassivate() {
		if(type==null) 
			return new Object[] {projId};
		else return new Object[] {projId, type};
	}
	
	void setupRender(){
		curProj = projDAO.getProjectById(projId);
		if(curProj==null){
			throw new RecordNotFoundException(_messages.format("entity-not-exists", "ProjectID", projId));
		}
		if(!canViewMessage(curProj)){
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		
		if(type==null)
			type = MessageType.INBOX;
		
		msgs = msgDAO.getMessages(getCurUser(), curProj, type);
		evenOdd = new EvenOdd();
	}
	
	//----------------------------------
	
	
	public String getTypeSelectedClass(){
		if(tempType.equals(type))
			return "selected";
		return null;
	}
	
	public Long getUnreadCount(MessageType t){
		unreadCount = msgDAO.getCountUnreadMessage(getCurUser(), curProj, t);
		return unreadCount;
	}
	public Long getUnreadCount(){
		return unreadCount;
	}
	Object onSuccessFromSearchForm(){
		return linkSource.createPageRenderLinkWithContext(MessageSearch.class, projId, searchText);
	}
	//----------------------------------
	
	
	

	@Inject
	private Block defaultActionToolBar;
	@Inject
	private Block TrashActionToolBar;
	public Block getActionToolBar(MessageType type){
		switch(type){
	//		case INBOX: return defaultActionToolBar;
	//		case DRAFT: return defaultActionToolBar;
	//		case SENT: return defaultActionToolBar;
			case TRASH: return TrashActionToolBar;
		}
		return defaultActionToolBar;
	}
	
	
	public BeanModel<Message> getModel(MessageType type){
		BeanModel<Message> model = beanModelSource.createEditModel(Message.class, _messages);
        model.include("subject","content","flag", "hasRead");
        model.add("chkbox",null);
        
		switch(type){
			case INBOX: 
				model.add("sender", propertyConduitSource.create(Message.class, "sender.displayName"));
				model.add("date", propertyConduitSource.create(Message.class, "sendDate")); 
				model.reorder("chkbox","flag","hasRead","subject","content","sender","date");
				break;
			case DRAFT:
			case SENT:
				model.add("receiver", propertyConduitSource.create(Message.class, "recipientsDisplay")); 
		        model.add("date", propertyConduitSource.create(Message.class, "sendDate")); 
		        model.reorder("chkbox","flag","hasRead","subject","content","receiver","date");
				break;
			case TRASH: 
				model.add("sender", propertyConduitSource.create(Message.class, "sender.displayName"));
				model.add("receiver", propertyConduitSource.create(Message.class, "recipientsDisplay")); 
		        model.add("date", propertyConduitSource.create(Message.class, "sendDate")); 
				model.reorder("chkbox","flag","hasRead","subject","content","sender","receiver","date");
				break;
		}
        return model;
	}
	
	public String getRowClass(){
		String readStatus = "unread";
		if(msg.isHasRead())
			readStatus = "read";
		return evenOdd.getNext() + " " + readStatus;
	}
	public Link getViewOrEditLink(MessageType t){
		if(MessageType.DRAFT.equals(t))
			return linkSource.createPageRenderLinkWithContext(MessageEdit.class, msg.getId());
		return linkSource.createPageRenderLinkWithContext(MessageView.class, msg.getId());
	}
	
	@CommitAfter
	void onActionFromToggleFlag(Long id){
		Message m = msgDAO.getMessageById(id);
		m.setFlag(! m.isFlag());
		msgDAO.saveMessage(m);
	}
	@CommitAfter
	void onActionFromToggleRead(Long id){
		Message m = msgDAO.getMessageById(id);
		m.setHasRead(! m.isHasRead());
		msgDAO.saveMessage(m);
	}
	
	
	public SelectModel getMoveToTypeModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		for (MessageType t : MessageType.values()) {
			if(!type.equals(t)){
				OptionModel optModel = new OptionModelImpl(getTypeNameDisplay(t), t);
				optModelList.add(optModel);
			}
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	

	void onSelectedFromDeleteForever(){
		submitType = SubmitType.DELETE_FOREVER;
	}
	void onSelectedFromDelete(){
		submitType = SubmitType.DELETE;
	}
	void onSelectedFromMarkRead(){
		submitType = SubmitType.READ;
	}
	void onSelectedFromMarkUnread(){
		submitType = SubmitType.UNREAD;
	}
	void onSelectedFromMarkFlag(){
		submitType = SubmitType.FLAG;
	}
	void onSelectedFromMarkUnflag(){
		submitType = SubmitType.UNFLAG;
	}
	void onSelectedFromMove(){
		submitType = SubmitType.MOVE_TYPE;
	}
	@Component(id = "gridForm")
	private Form gridForm;

	void onValidateFormFromGridForm() {
		if(submitType != null){
			String[] selectedId = request.getParameters("chkBox");
			if(selectedId==null || selectedId.length==0){
				gridForm.recordError(_messages.get("select-at-least-one-item"));
			}
		}
	}
	@CommitAfter
	void onSuccessFromGridForm(){
		if(submitType != null){
		String[] selectedId = request.getParameters("chkBox");
		for(String id : selectedId){
			Message m = msgDAO.getMessageById(Long.parseLong(id));
			
			switch(submitType){
			case DELETE_FOREVER:
				msgDAO.deleteMessage(m);
				break;
			case DELETE:
				m.setType(MessageType.TRASH);
				m.setDeleteDate(new Date());
				msgDAO.saveMessage(m);
				break;
			case READ:
				m.setHasRead(true);
				msgDAO.saveMessage(m);
				break;
			case UNREAD:
				m.setHasRead(false);
				msgDAO.saveMessage(m);
				break;
			case FLAG:
				m.setFlag(true);
				msgDAO.saveMessage(m);
				break;
			case UNFLAG:
				m.setFlag(false);
				msgDAO.saveMessage(m);
				break;
			case MOVE_TYPE:
				m.setType(moveToType);
				m.setDeleteDate(null);
				msgDAO.saveMessage(m);
				break;
			}
		}
		}
	}
	
	
	public int getTotalSize() {
		if(msgs==null) return 0;
		return msgs.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	public void setRowsPerPage(int num){
		appState.setRowsPerPage(num);
	}
}
