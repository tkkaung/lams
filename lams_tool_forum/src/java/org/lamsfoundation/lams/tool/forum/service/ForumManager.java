package org.lamsfoundation.lams.tool.forum.service;

import org.lamsfoundation.lams.tool.forum.persistence.Forum;
import org.lamsfoundation.lams.tool.forum.persistence.Message;
import org.lamsfoundation.lams.tool.forum.core.PersistenceException;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: conradb
 * Date: 8/06/2005
 * Time: 14:49:59
 * To change this template use File | Settings | File Templates.
 */
public interface ForumManager {

    public Forum createForum(Forum forum, Map attachments, Map topics) throws PersistenceException;
    public Forum editForum(Forum forum, Map attachments, Map topics) throws PersistenceException;
    public Forum getForum(Long forumId) throws PersistenceException;
    public void deleteForum(Long forumId) throws PersistenceException;
    public List getTopics(Long forumId) throws PersistenceException;
    public void deleteForumAttachment(Long attachmentId) throws PersistenceException;
    public Message createMessage(Long forumId, Message message) throws PersistenceException ;
    public Message editMessage(Message message) throws PersistenceException;
    public Message getMessage(Long messageId) throws PersistenceException;
    public void deleteMessage(Long messageId) throws PersistenceException;
    public Message replyToMessage(Long parentId, Message message) throws PersistenceException;

}
