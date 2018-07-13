package ntu.celt.eUreka2.pages.modules.message;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.message.Message;
import ntu.celt.eUreka2.modules.message.MessageDAO;
import ntu.celt.eUreka2.modules.message.MessageType;
import ntu.celt.eUreka2.modules.message.PrivilegeMessage;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;

public class MessageView extends AbstractPageMessage {
	@Property
	private Project curProj;
	@Property
	private MessageType type;
	@Property
	private MessageType tempType;
	private Long unreadCount;
	@SuppressWarnings("unused")
	@Property
	private User tempReciepent;
	@Property
	private String searchText;
	
	
	private Long msgId;
	@Property
	private Message msg;
	@Property
	private MessageType moveToType;
	
	@Inject
	private MessageDAO msgDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject 
	private Messages _messages;
	
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
		msg = msgDAO.getMessageById(msgId);
		curProj = msg.getProj();
		return linkSource.createPageRenderLinkWithContext(MessageSearch.class, curProj.getId(), searchText);
	}
	
	
	//----------------------------------
	
	void onActivate(EventContext ec) {
		msgId = ec.get(Long.class, 0);
	}
	Object[] onPassivate() {
		return new Object[] { msgId};
	}
	void setupRender(){
		msg = msgDAO.getMessageById(msgId);
		if(msg==null)
			throw new RecordNotFoundException(_messages.format("entity-not-exists", "MessageID", msgId));
		curProj = msg.getProj();
		type = msg.getType();
		
		if(!canViewMessage(curProj)){
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		//check if user owner
		if(!msg.getOwner().equals(getCurUser())){
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		
		updateMailStatus(msg);
	}
	
	@CommitAfter
	private void updateMailStatus(Message mail){
		mail.setHasRead(true);
		msgDAO.saveMessage(mail);
	}
	
	public boolean isTrash(MessageType type){
		if(MessageType.TRASH.equals(type))
			return true;
		return false;
	}
	
	@Inject
	private Block defaultActionToolBar;
	@Inject
	private Block trashActionToolBar;
	public Block getActionToolBar(MessageType type){
		switch(type){
		//	case INBOX: return defaultBlock;
		//	case DRAFT: return draftBlock;
		//	case SENT: return sentBlock;
			case TRASH: return trashActionToolBar;
		}
		return defaultActionToolBar;
	}
	
	public Object[] getMsgIdAndReply(){
		return new Object[]{ msg.getId(), MessageReply.ReplyType.ONE};
	}
	public Object[] getMsgIdAndReplyAll(){
		return new Object[]{msg.getId(), MessageReply.ReplyType.ALL};
	}
	public Object[] getMsgIdAndForward(){
		return new Object[]{msg.getId(), MessageReply.ReplyType.FORWARD};
	}

	@CommitAfter
	Object onActionFromDelete(String mId){
		msg = msgDAO.getMessageById(msgId);
		msg.setType(MessageType.TRASH);
		msg.setDeleteDate(new Date());
		msgDAO.saveMessage(msg);
		return linkSource.createPageRenderLinkWithContext(MessageIndex.class, msg.getProj().getId(), msg.getType());
	}
	@CommitAfter
	void onActionFromFlag(String mId){
		msg = msgDAO.getMessageById(msgId);
		msg.setFlag(true);
		msgDAO.saveMessage(msg);
	}
	@CommitAfter
	void onActionFromUnflag(String mId){
		msg = msgDAO.getMessageById(msgId);
		msg.setFlag(false);
		msgDAO.saveMessage(msg);
	}
	@CommitAfter
	Object onActionFromDeleteForever(String mId){
		msg = msgDAO.getMessageById(msgId);
		curProj = msg.getProj();
		type = msg.getType();
		msgDAO.deleteMessage(msg);
		return linkSource.createPageRenderLinkWithContext(MessageIndex.class, curProj.getId(), type);
	}
	
	void onPrepareFromMoveToForm(){
		msg = msgDAO.getMessageById(msgId);
		curProj = msg.getProj();
		type = msg.getType();
	}
	@CommitAfter
	Object onSuccessFromMoveToForm(){
		msg.setType(moveToType);
		msg.setDeleteDate(null);
		msgDAO.saveMessage(msg);
		
		return linkSource.createPageRenderLinkWithContext(MessageIndex.class, curProj.getId(), moveToType);
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
}
