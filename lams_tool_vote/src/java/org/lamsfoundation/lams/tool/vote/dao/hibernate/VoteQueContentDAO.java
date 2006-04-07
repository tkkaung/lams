/***************************************************************************
 * Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)
 * =============================================================
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 * 
 * http://www.gnu.org/licenses/gpl.txt
 * ***********************************************************************/
package org.lamsfoundation.lams.tool.vote.dao.hibernate;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.lamsfoundation.lams.tool.vote.dao.IVoteQueContentDAO;
import org.lamsfoundation.lams.tool.vote.pojos.VoteQueContent;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;


/**
 * @author Ozgur Demirtas
 * 
 * <p>Hibernate implementation for database access to McQueContent for the mc tool.</p>
 */
public class VoteQueContentDAO extends HibernateDaoSupport implements IVoteQueContentDAO {
	 	static Logger logger = Logger.getLogger(VoteQueContentDAO.class.getName());
	 	
	 	private static final String LOAD_QUESTION_CONTENT_BY_CONTENT_ID = "from mcQueContent in class VoteQueContent where mcQueContent.mcContentId=:mcContentId order by mcQueContent.displayOrder";
	 	
	 	private static final String CLEAN_QUESTION_CONTENT_BY_CONTENT_ID_SIMPLE = "from mcQueContent in class VoteQueContent where mcQueContent.mcContentId=:mcContentId";
	 	
	 	private static final String CLEAN_QUESTION_CONTENT_BY_CONTENT_ID = "from mcQueContent in class VoteQueContent where mcQueContent.mcContentId=:mcContentId and mcQueContent.disabled=true";
	 	
	 	private static final String REFRESH_QUESTION_CONTENT 			= "from mcQueContent in class VoteQueContent where mcQueContent.mcContentId=:mcContentId and mcQueContent.disabled=false order by mcQueContent.displayOrder";
	 	
	 	private static final String LOAD_QUESTION_CONTENT_BY_QUESTION_TEXT = "from mcQueContent in class VoteQueContent where mcQueContent.question=:question and mcQueContent.mcContentId=:mcContentUid";
	 	
	 	private static final String LOAD_QUESTION_CONTENT_BY_DISPLAY_ORDER = "from mcQueContent in class VoteQueContent where mcQueContent.displayOrder=:displayOrder and mcQueContent.mcContentId=:mcContentUid";
	 	
	 	private static final String GET_NEXT_AVAILABLE_DISPLAY_ORDER = "from mcQueContent in class VoteQueContent where mcQueContent.mcContentId=:mcContentId";
	 		 	
	 	
	 	public VoteQueContent getMcQueContentByUID(Long uid)
		{
			 return (VoteQueContent) this.getHibernateTemplate()
	         .get(VoteQueContent.class, uid);
		}
		
		
	 	public VoteQueContent getToolDefaultQuestionContent(final long mcContentId)
	    {
	        HibernateTemplate templ = this.getHibernateTemplate();
			List list = getSession().createQuery(LOAD_QUESTION_CONTENT_BY_CONTENT_ID)
				.setLong("mcContentId", mcContentId)
				.list();
			
			if(list != null && list.size() > 0){
				VoteQueContent mcq = (VoteQueContent) list.get(0);
				return mcq;
			}
			return null;
	    }
	 	
	 	
	 	public List getAllQuestionEntries(final long mcContentId)
	    {
	        HibernateTemplate templ = this.getHibernateTemplate();
			List list = getSession().createQuery(LOAD_QUESTION_CONTENT_BY_CONTENT_ID)
				.setLong("mcContentId", mcContentId)
				.list();

			return list;
	    }
	 	
	 	public List refreshQuestionContent(final Long mcContentId)
	    {
	        HibernateTemplate templ = this.getHibernateTemplate();
			List list = getSession().createQuery(REFRESH_QUESTION_CONTENT)
				.setLong("mcContentId", mcContentId.longValue())
				.list();
			
			return list;
	    }
	 	
	 	
	 	public VoteQueContent getQuestionContentByQuestionText(final String question, final Long mcContentUid)
	    {
	        HibernateTemplate templ = this.getHibernateTemplate();
			List list = getSession().createQuery(LOAD_QUESTION_CONTENT_BY_QUESTION_TEXT)
				.setString("question", question)
				.setLong("mcContentUid", mcContentUid.longValue())				
				.list();
			
			if(list != null && list.size() > 0){
				VoteQueContent mcq = (VoteQueContent) list.get(0);
				return mcq;
			}
			return null;
	    }
	 	
	 	
	 	public VoteQueContent getQuestionContentByDisplayOrder(final Long displayOrder, final Long mcContentUid)
	    {
	        HibernateTemplate templ = this.getHibernateTemplate();
			List list = getSession().createQuery(LOAD_QUESTION_CONTENT_BY_DISPLAY_ORDER)
				.setLong("displayOrder", displayOrder.longValue())
				.setLong("mcContentUid", mcContentUid.longValue())				
				.list();
			
			if(list != null && list.size() > 0){
				VoteQueContent mcq = (VoteQueContent) list.get(0);
				return mcq;
			}
			return null;
	    }
	 	
	 	
	 	public void removeQuestionContentByMcUid(final Long mcContentUid)
	    {
			HibernateTemplate templ = this.getHibernateTemplate();
			List list = getSession().createQuery(LOAD_QUESTION_CONTENT_BY_CONTENT_ID)
				.setLong("mcContentId", mcContentUid.longValue())
				.list();

			if(list != null && list.size() > 0){
				Iterator listIterator=list.iterator();
		    	while (listIterator.hasNext())
		    	{
		    		VoteQueContent mcQueContent=(VoteQueContent)listIterator.next();
					this.getSession().setFlushMode(FlushMode.AUTO);
		    		templ.delete(mcQueContent);
		    		templ.flush();
		    	}
			}
	    }
	 	

	 	public void resetAllQuestions(final Long mcContentUid)
	    {
			HibernateTemplate templ = this.getHibernateTemplate();
			List list = getSession().createQuery(LOAD_QUESTION_CONTENT_BY_CONTENT_ID)
				.setLong("mcContentId", mcContentUid.longValue())
				.list();

			if(list != null && list.size() > 0){
				Iterator listIterator=list.iterator();
		    	while (listIterator.hasNext())
		    	{
		    		VoteQueContent mcQueContent=(VoteQueContent)listIterator.next();
					this.getSession().setFlushMode(FlushMode.AUTO);
		    		templ.update(mcQueContent);
		    	}
			}
	    }
	 	

	 	public void cleanAllQuestions(final Long mcContentUid)
	    {
			HibernateTemplate templ = this.getHibernateTemplate();
			List list = getSession().createQuery(CLEAN_QUESTION_CONTENT_BY_CONTENT_ID)
				.setLong("mcContentId", mcContentUid.longValue())
				.list();

			if(list != null && list.size() > 0){
				Iterator listIterator=list.iterator();
		    	while (listIterator.hasNext())
		    	{
		    		VoteQueContent mcQueContent=(VoteQueContent)listIterator.next();
	    			this.getSession().setFlushMode(FlushMode.AUTO);
	    			logger.debug("deleting mcQueContent: " + mcQueContent);
		    		templ.delete(mcQueContent);	
		    	}
			}
	    }

	 	
	 	public void cleanAllQuestionsSimple(final Long mcContentUid)
	    {
			HibernateTemplate templ = this.getHibernateTemplate();
			List list = getSession().createQuery(CLEAN_QUESTION_CONTENT_BY_CONTENT_ID_SIMPLE)
				.setLong("mcContentId", mcContentUid.longValue())
				.list();

			if(list != null && list.size() > 0){
				Iterator listIterator=list.iterator();
		    	while (listIterator.hasNext())
		    	{
		    		VoteQueContent mcQueContent=(VoteQueContent)listIterator.next();
	    			this.getSession().setFlushMode(FlushMode.AUTO);
	    			logger.debug("deleting mcQueContent: " + mcQueContent);
		    		templ.delete(mcQueContent);	
		    	}
			}
	    }

	 	
	 	public List getNextAvailableDisplayOrder(final long mcContentId)
	    {
	        HibernateTemplate templ = this.getHibernateTemplate();
			List list = getSession().createQuery(GET_NEXT_AVAILABLE_DISPLAY_ORDER)
				.setLong("mcContentId", mcContentId)
				.list();
			
			return list;
	    }

	 	
	 	public void saveMcQueContent(VoteQueContent mcQueContent)
	    {
	    	this.getHibernateTemplate().save(mcQueContent);
	    }
	    
		public void updateMcQueContent(VoteQueContent mcQueContent)
	    {
	    	this.getHibernateTemplate().update(mcQueContent);
	    }
		
		public void saveOrUpdateMcQueContent(VoteQueContent mcQueContent)
	    {
	    	this.getHibernateTemplate().saveOrUpdate(mcQueContent);
	    }
		
		public void removeMcQueContentByUID(Long uid)
	    {
			VoteQueContent mcq = (VoteQueContent)getHibernateTemplate().get(VoteQueContent.class, uid);
			this.getSession().setFlushMode(FlushMode.AUTO);
	    	this.getHibernateTemplate().delete(mcq);
	    }
		
		
		public void removeMcQueContent(VoteQueContent mcQueContent)
	    {
			this.getSession().setFlushMode(FlushMode.AUTO);
	        this.getHibernateTemplate().delete(mcQueContent);
	    }
		
		 public void flush()
	    {
	        this.getHibernateTemplate().flush();
	    }
	} 