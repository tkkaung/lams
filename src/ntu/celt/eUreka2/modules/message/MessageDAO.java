package ntu.celt.eUreka2.modules.message;

import java.util.List;

import org.apache.tapestry5.hibernate.annotations.CommitAfter;

import ntu.celt.eUreka2.dao.GenericModuleDAO;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;

public interface MessageDAO extends GenericModuleDAO {
	Message getMessageById(Long id);
  	void saveMessage(Message msg) ;
  	void deleteMessage(Message msg) ;
 	List<Message> getMessages(User owner, Project proj, MessageType msgType); 
 	Long getCountUnreadMessage(User owner, Project proj,MessageType msgType); 
 	
 	List<Message> searchMessages(User owner, Project proj, String searchText);
 	@CommitAfter
 	int deleteTrashMessages();
} 
