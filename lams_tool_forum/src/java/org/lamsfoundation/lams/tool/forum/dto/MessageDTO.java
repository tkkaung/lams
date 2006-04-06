/****************************************************************
 * Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)
 * =============================================================
 * License Information: http://lamsfoundation.org/licensing/lams/2.0/
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2.0 
 * as published by the Free Software Foundation.
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
 * ****************************************************************
 */

/* $$Id$$ */	

package org.lamsfoundation.lams.tool.forum.dto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lamsfoundation.lams.tool.forum.persistence.ForumReport;
import org.lamsfoundation.lams.tool.forum.persistence.Message;


public class MessageDTO {

	private Message message;
	private String author;
	private boolean hasAttachment;
	private short level;
	private int threadNum;
	private boolean isAuthor;
	private Float mark;

	/**
	 * Get a <code>MessageDTO</code> instance from a given <code>Message</code>.
	 * 
	 * @param msg
	 * @return
	 */
	public static MessageDTO getMessageDTO(Message msg){
		if(msg == null)
			return null;
		
		MessageDTO dto = new MessageDTO();
		dto.setMessage(msg);
		dto.setAuthor(msg.getCreatedBy().getFirstName()+" "+msg.getCreatedBy().getLastName());
		if(msg.getAttachments() == null || msg.getAttachments().isEmpty())
			dto.setHasAttachment(false);
		else
			dto.setHasAttachment(true);
		
		ForumReport report = msg.getReport();
		if(report != null && report.getMark() != null)
			dto.mark = report.getMark();
		
		return dto;
	}

	/**
	 * Get a list of <code>MessageDTO</code> according to given list of <code>Message</code>.
	 * @param msgList
	 * @return
	 */
	public static List getMessageDTO(List msgList){
		List retSet = new ArrayList();
		if(msgList == null || msgList.isEmpty())
			return retSet;
		
		Iterator iter = msgList.iterator();
		while(iter.hasNext()){
			Message msg = (Message) iter.next();
			MessageDTO msgDto = new MessageDTO();
			msgDto.setMessage(msg);
			msgDto.setAuthor(msg.getCreatedBy().getFirstName()+" "+msg.getCreatedBy().getLastName());
			
			if(msg.getAttachments() == null || msg.getAttachments().isEmpty())
				msgDto.setHasAttachment(false);
			else
				msgDto.setHasAttachment(true);
			ForumReport report = msg.getReport();
			if(report != null && report.getMark() != null)
				msgDto.mark = report.getMark();

			retSet.add(msgDto);
		}
		return retSet;
	}
	//-------------------------------DTO get/set method------------------------------
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public boolean getHasAttachment() {
		return hasAttachment;
	}
	public void setHasAttachment(boolean isAttachment) {
		this.hasAttachment = isAttachment;
	}
	public Message getMessage() {
		return message;
	}
	public void setMessage(Message message) {
		this.message = message;
	}
	public short getLevel() {
		return level;
	}
	public void setLevel(short level) {
		this.level = level;
	}
	public int getThreadNum() {
		return threadNum;
	}
	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}

	public boolean getIsAuthor() {
		return isAuthor;
	}

	public void setAuthor(boolean isAuthor) {
		this.isAuthor = isAuthor;
	}
	
	public Float getMark() {
		return mark;
	}

	public void setMark(Float mark) {
		this.mark = mark;
	}	
}
