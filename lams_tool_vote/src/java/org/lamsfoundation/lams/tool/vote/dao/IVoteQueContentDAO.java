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

package org.lamsfoundation.lams.tool.vote.dao;

import java.util.List;

import org.lamsfoundation.lams.tool.vote.pojos.VoteQueContent;


/**
 * 
 * @author Ozgur Demirtas
 * <p></p>
 *
 */
public interface IVoteQueContentDAO
{
 	public VoteQueContent getVoteQueContentByUID(Long uid);

 	public VoteQueContent getToolDefaultQuestionContent(final long voteContentId);

 	public List getAllQuestionEntries(final long voteContentId);
 	
 	public VoteQueContent getQuestionContentByQuestionText(final String question, final Long voteContentUid);
 	
 	public void removeQuestionContentByVoteUid(final Long voteContentUid);
 	
 	public void resetAllQuestions(final Long voteContentUid);

 	public void cleanAllQuestions(final Long voteContentUid);
 	
 	public void cleanAllQuestionsSimple(final Long voteContentUid);
 	
 	public void saveVoteQueContent(VoteQueContent voteQueContent);
    
	public void updateVoteQueContent(VoteQueContent voteQueContent);
	
	public void saveOrUpdateVoteQueContent(VoteQueContent voteQueContent);
	
	public void removeVoteQueContentByUID(Long uid);
	
	public void removeVoteQueContent(VoteQueContent voteQueContent);
	
 	public void flush();
}
