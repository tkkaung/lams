package ntu.celt.eUreka2.pages.modules.message;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.message.Message;
import ntu.celt.eUreka2.modules.message.MessageDAO;
import ntu.celt.eUreka2.modules.message.MessageType;
import ntu.celt.eUreka2.services.email.EmailManager;
import ntu.celt.eUreka2.services.email.EmailTemplateConstants;
import ntu.celt.eUreka2.services.email.EmailTemplateVariables;
import ntu.celt.eUreka2.services.email.SendEmailFailException;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.internal.services.StringValueEncoder;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.slf4j.Logger;

public class MessageReply extends AbstractPageMessage {
	@SessionState
	private AppState appState;
	@Property
	private Project curProj;
	@Property
	private MessageType type;
	@Property
	private MessageType tempType;
	private Long unreadCount;
	@Property
	private String searchText;
	
	@Property
	private Message msg;
	@Property
	private List<String> toUsersId;
	@SuppressWarnings("unused")
	@Property
	private StringValueEncoder stringEncoder = new StringValueEncoder();
	enum SubmitType {SEND, SAVE, AUTO};
	private SubmitType submitType;
	@Property
	private String extEmails;
	private final String  SEPARATE_STRING = ";";
	private Long toMsgId;
	@Property
	private Message toMsg;
	private String replyType;  //one, all, forward
	public enum ReplyType {ONE, ALL, FORWARD};
	
	
	
	@Inject
	private MessageDAO msgDAO;
	@Inject
	private UserDAO userDAO;
	@Inject
	private Logger logger;
	@Inject
	private EmailManager emailManager;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Request request;
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
		toMsg = msgDAO.getMessageById(toMsgId);
		curProj = toMsg.getProj();
		return linkSource.createPageRenderLinkWithContext(MessageSearch.class, curProj.getId(), searchText);
	}
	//----------------------------------
	
	
	void onActivate(EventContext ec) {
		if(ec.getCount()==1){
			toMsgId = ec.get(Long.class, 0);
		}
		else if(ec.getCount()==2){
			toMsgId = ec.get(Long.class, 0);
			replyType = ec.get(String.class, 1);
		}
	}
	Object[] onPassivate() {
		if(replyType==null)
			return new Object[] {toMsgId};
		return new Object[] {toMsgId, replyType};
	}
	void setupRender(){
		toMsg = msgDAO.getMessageById(toMsgId);
		if(toMsg==null)
			throw new RecordNotFoundException(_messages.format("entity-not-exists", "MessageID", toMsgId));
		curProj = toMsg.getProj();
		if(replyType == null)
			replyType = ReplyType.ONE.name();
		
		
		if(!canReplyMessage(curProj)){
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		//check if user owner
		if(!toMsg.getOwner().equals(getCurUser())){
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		
		type = toMsg.getType();
	}
	
	void onPrepareFromGridForm(){
		toMsg = msgDAO.getMessageById(toMsgId);
		curProj = toMsg.getProj();
		
		msg = new Message();
		msg.setSender(getCurUser());
		msg.setCreateDate(new Date());
		msg.setSendDate(msg.getCreateDate());
		msg.setProj(curProj);
		
		if(replyType.equalsIgnoreCase(ReplyType.ONE.name())){
			msg.addRecipients(toMsg.getSender());
			
			if((toMsg.getSubject()!=null && !toMsg.getSubject().startsWith("RE:"))){
				msg.setSubject("RE: " + toMsg.getSubject()); 
			}
		}
		else if(replyType.equalsIgnoreCase(ReplyType.ALL.name())){
			msg.addRecipients(toMsg.getSender());
			for(User u : toMsg.getRecipients()){
				if(!u.equals(getCurUser())){
					msg.addRecipients(u);
				}
			}
			for(String email : toMsg.getExternalEmails()){
				msg.addExternalEmail(email);
			}
			
			if((toMsg.getSubject()!=null && !toMsg.getSubject().startsWith("RE:"))){
				msg.setSubject("RE: " + toMsg.getSubject()); 
			}
		}
		else if(replyType.equalsIgnoreCase(ReplyType.FORWARD.name())){
			msg.setSubject("FWD: " + toMsg.getSubject()); 
		}
		
		msg.setContent("<p>&nbsp;</p><blockquote style=\"padding: 3px 3px 9px 6px; border-left: 3px solid #999; margin: 6px 6px 3px;\">" +
					"<strong>Subject:</strong> "+toMsg.getSubject()+"<br />" +
					"<strong>Sent:</strong> "+Util.formatDateTime(toMsg.getSendDate(), Config.getString(Config.FORMAT_TIME_PATTERN))+"<br />" +
					"<strong>From:</strong> "+toMsg.getSender().getDisplayName()+"<br />" +
					"<strong>To:</strong> "+toMsg.getRecipientsDisplay()+"<br />" +
					"<hr /> "+toMsg.getContent()+
					"</blockquote>");
		if(toUsersId == null)
			toUsersId = msg.getToOtherUsersAsStringIds();
		if(extEmails == null)
			extEmails = msg.getToExternalEmailAsString();
	}
	
	void onSelectedFromSend1(){
		submitType = SubmitType.SEND;
	}
	void onSelectedFromSend(){
		submitType = SubmitType.SEND;
	}
	void onSelectedFromSave(){
		submitType = SubmitType.SAVE;
	}
	void onValidateFormFromGridForm(){
		
	}
	
	@CommitAfter
	void saveMail(Message mail){
		msgDAO.saveMessage(mail);
	}
	Object onSuccessFromGridForm(){
		for (String uId : toUsersId) {
			User user = userDAO.getUserById(Integer.parseInt(uId));
			msg.addRecipients(user);
		}
		if(extEmails!=null){
			String[] emails = extEmails.split(SEPARATE_STRING);
			for(String email : emails){
				if(!email.trim().isEmpty()){
					msg.addExternalEmail(email.trim());
				}
			}
		}
		msg.setSendDate(new Date());
		msg.setOwner(getCurUser());
		msg.setContent(Util.filterOutRestrictedHtmlTags(Util.nvl(msg.getContent())));
		msg.setSubject(Util.nvl(msg.getSubject()));
		if("auto".equalsIgnoreCase(request.getParameter("mode")))
			submitType = SubmitType.AUTO;
		
		if(SubmitType.SAVE.equals(submitType)
			|| SubmitType.AUTO.equals(submitType)){
			msg.setType(MessageType.DRAFT);
			saveMail(msg);
			return linkSource.createPageRenderLinkWithContext(MessageEdit.class, msg.getId());
		}
		else if(SubmitType.SEND.equals(submitType)){
			msg.setType(MessageType.SENT);
			saveMail(msg);
			
			
			for(User u : msg.getRecipients()){
				Message tempMail = (Message) msg.clone();
				tempMail.setOwner(u);
				tempMail.setType(MessageType.INBOX);
				saveMail(tempMail);
			}
			
			EmailTemplateVariables var = new EmailTemplateVariables(msg.getCreateDate().toString(), 
					msg.getSendDate().toString(), 
					msg.getSubject(), 
					Util.truncateString(Util.stripTags(msg.getContent()), Config.getInt("max_content_lenght_in_email")), 
					msg.getSender().getDisplayName(), msg.getProj().getDisplayName(), 
					emailManager.createLinkBackURL(request, MessageView.class, msg.getProj().getId(), msg.getId()), 
					null, null);
			
			try{
				emailManager.sendHTMLMail(msg.getRecipients(), msg.getExternalEmails(), 
						 EmailTemplateConstants.MESSAGE_SEND_MESSAGE, var);
				appState.recordInfoMsg(_messages.get("message-successfully-sent"));
			}
			catch(SendEmailFailException e){
				String errMsg =  _messages.get("sendEmailFailedException-message") + e.getMessage(); 
				logger.warn(errMsg, e);
				appState.recordWarningMsg(errMsg);
			}
			return linkSource.createPageRenderLinkWithContext(MessageIndex.class, curProj.getId());
		}
		return null;
	}
	
	
	public SelectModel getAvailableUsersModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		for (User u : curProj.getUsers()) {
			OptionModel optModel = new OptionModelImpl(u.getDisplayName(), Integer.toString(u.getId()));
			optModelList.add(optModel);
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	
}
