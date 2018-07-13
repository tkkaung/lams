package ntu.celt.eUreka2.pages.modules.message;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
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
import org.apache.tapestry5.Link;
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

public class MessageNew extends AbstractPageMessage {
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
	
	@Inject
	private ProjectDAO projDAO;
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
		curProj = projDAO.getProjectById(projId);
		return linkSource.createPageRenderLinkWithContext(MessageSearch.class, curProj.getId(), searchText);
	}
	//----------------------------------
	
	
	void onActivate(EventContext ec) {
		projId = ec.get(String.class, 0);
	}
	Object[] onPassivate() {
		return new Object[] {projId};
	}
	void setupRender(){
		curProj = projDAO.getProjectById(projId);
		if(curProj==null){
			throw new RecordNotFoundException(_messages.format("entity-not-exists", "ProjectID", projId));
		}
		if(!canCreateMessage(curProj)){
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		
		
	}
	
	void onPrepareFromGridForm(){
		curProj = projDAO.getProjectById(projId);
		msg = new Message();
		msg.setSender(getCurUser());
		msg.setCreateDate(new Date());
		msg.setSendDate(msg.getCreateDate());
		msg.setSubject("[eUreka] "+ curProj.getDisplayName()); //set default subject
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
	void saveMail(Message msg){
		msgDAO.saveMessage(msg);
	}
	Link onSuccessFromGridForm(){
		Link resultLink = null;
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
		msg.setProj(curProj);
		msg.setContent(Util.filterOutRestrictedHtmlTags(Util.nvl(msg.getContent())));
		msg.setSubject(Util.nvl(msg.getSubject()));
		if("auto".equalsIgnoreCase(request.getParameter("mode")))
			submitType = SubmitType.AUTO;
		
		if(SubmitType.SAVE.equals(submitType)
			|| SubmitType.AUTO.equals(submitType)){
			
			msg.setType(MessageType.DRAFT);
			saveMail(msg);
			resultLink = linkSource.createPageRenderLinkWithContext(MessageEdit.class, msg.getId());
		}
		else if(SubmitType.SEND.equals(submitType)){
			msg.setType(MessageType.SENT);
			saveMail(msg);
			
			
			for(User u : msg.getRecipients()){
				Message tempMsg = (Message) msg.clone();
				tempMsg.setOwner(u);
				tempMsg.setType(MessageType.INBOX);
				saveMail(tempMsg);
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
		return resultLink;
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
