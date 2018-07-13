package ntu.celt.eUreka2.modules.message;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import ntu.celt.eUreka2.data.LastUpdateItem;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.pages.modules.message.MessageIndex;

public class MessageDAOImpl implements MessageDAO{

	@Inject
    private Session session;
	private PageRenderLinkSource linkSource;
	
	
	public MessageDAOImpl(Session session, PageRenderLinkSource linkSource) {
		super();
		this.session = session;
		this.linkSource = linkSource;
	}

	@Override
	public String getModuleName() {
		return PredefinedNames.MODULE_MESSAGE;
	}
	
	@Override
	public void deleteMessage(Message msg) {
		msg.getRecipients().clear();
		session.delete(msg);
		
	}

	@Override
	public Message getMessageById(Long id) {
		if(id==null) return null;
		return (Message) session.get(Message.class, id);
	}
	
	@Override
	public void saveMessage(Message msg) {
		session.persist(msg);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Message> getMessages(User owner, Project proj, MessageType msgType) {
		
		Query q = session.createQuery("SELECT m FROM Message AS m " 
					+ " WHERE m.type = :rMtype" 
					+ " AND m.owner = :rUser "
					+ (  proj!=null?  " AND m.proj.id=:rProjId " : "")
					+ " ORDER BY m.sendDate DESC ")
					.setParameter("rMtype", msgType)
					.setParameter("rUser", owner)
					;
		if(proj!=null)
			q = q.setParameter("rProjId", proj.getId());
			
		
		return q.list();
	}

	/**
	 * Count the number of unread msgs, given User and MessageType
	 */
	@Override
	public Long getCountUnreadMessage(User owner, Project proj, MessageType msgType) {
		Query q = session.createQuery("SELECT COUNT(m) " 
				+ " FROM Message AS m " 
				+ " WHERE m.hasRead = false " 
				+ " AND m.type = :rMtype"
				+ (  proj!=null?  " AND m.proj.id=:rProjId " : "")
				+ " AND m.owner = :rUser ")
				.setParameter("rMtype", msgType)
				.setParameter("rUser", owner)
				;
		if(proj!=null)
			q = q.setParameter("rProjId", proj.getId());
			
		
		return (Long) q.uniqueResult();
	}

	/**
	 * Search for msgs that belong to the user, and matched the searchText 
	 * (match to subject, content, sender's name, sender's username, recipients' name
	 * recipients' username, and externalEmails), it return all msgs that belong to 
	 * the user if searchText is NULL
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Message> searchMessages(User owner, Project proj, String searchText) {
		if(searchText == null){  //list all msgs that belong to the user
			 Criteria c = session.createCriteria(Message.class).add(Restrictions.eq("owner", owner));
			 if(proj!=null)
				 c.add(Restrictions.eq("proj.id", proj.getId()));
			 return c.list();
		}
		StringBuffer qStr = new StringBuffer();
		qStr.append(
				"FROM Message a1 WHERE a1.id IN ("+
					"SELECT DISTINCT m.id FROM Message AS m " 
					+ " JOIN m.sender AS s "
					+ " LEFT JOIN m.recipients AS r "
					+ " LEFT JOIN m.externalEmails AS e "
					+ " WHERE m.owner = :rUser " 
					+ (  proj!=null?  " AND m.proj.id=:rProjId " : "")
					)
				;
		String[] words = searchText.split(" ");
		for(int i = 0; i<words.length; i++){
			qStr.append( " AND ( m.subject LIKE :rWord"+i+" " 
					+ " OR m.content LIKE :rWord"+i+" "
					+ " OR s.firstName LIKE :rWord"+i+" "
					+ " OR s.lastName LIKE :rWord"+i+" "
					+ " OR s.username LIKE :rWord"+i+" "
					+ " OR r.firstName LIKE :rWord"+i+" "
					+ " OR r.lastName LIKE :rWord"+i+" "
					+ " OR r.username LIKE :rWord"+i+" "
					+ " OR e LIKE :rWord"+i+" "
					+ " )"
			);	
		}
		qStr.append(
				")"+
				" ORDER BY a1.sendDate DESC ");
		
		Query q = session.createQuery(qStr.toString());
		q.setParameter("rUser", owner);
		if(proj!=null)
			q.setParameter("rProjId", proj.getId());
		for(int i = 0; i<words.length; i++){
			q.setString("rWord"+i, "%"+words[i]+"%");
		}
		
		return q.list();
	}

	/**
	 * Delete all msgs which have been in Trash folder for more than 30 days
	 * @return number of rows deleted
	 */
	@SuppressWarnings("unchecked")
	@Override
	public int deleteTrashMessages() {
		Query q = session.createQuery("SELECT m FROM Message as m " 
				+ " WHERE m.type = :rType "
				+ " AND m.sendDate < current_date() - 30)  ")
				.setParameter("rType", MessageType.TRASH)
				;
		List<Message> mList = q.list();
		
		int count = 0;
		for(Message m : mList){
			this.deleteMessage(m);
			count++;
		}
		
		return count;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<LastUpdateItem> getLastUpdates(Project project, User curUser, int numPastDay) {
		Calendar pastDay = Calendar.getInstance();
		pastDay.add(Calendar.DAY_OF_YEAR, -1 * numPastDay);
		
		Query q = session.createQuery("SELECT m FROM Message AS m " +
				" WHERE m.proj.id=:rPid " +
				"   AND m.hasRead = false " +
				"   AND m.type = :rType " +
				"   AND m.owner = :rUser " +
				"   AND m.sendDate>=:rPastDay " +
				"   ORDER BY sendDate DESC" +
				" 	 ")
				.setString("rPid", project.getId())
				.setParameter("rType", MessageType.INBOX)
				.setParameter("rUser", curUser)
				.setDate("rPastDay", pastDay.getTime())
				;
		
		List<Message> mList = q.list();
		List<LastUpdateItem> lList = new ArrayList<LastUpdateItem>();
		for(Message m : mList){
			LastUpdateItem l = new LastUpdateItem();
			l.setTime(m.getSendDate());
			l.setTitle(m.getSubject());
			l.setUrl(linkSource.createPageRenderLinkWithContext(MessageIndex.class
					, m.getProj().getId()
					, m.getType())
					.toAbsoluteURI());
			
			lList.add(l);
		}
		
		return lList;
	}

	

}
