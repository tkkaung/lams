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
 * ************************************************************************
 */
package org.lamsfoundation.lams.util;

import java.io.Serializable;

/**
 * @author Manpreet Minhas
 * This class represents the message sent by the server to the 
 * Flash client.
 * 
 */
public class FlashMessage implements Serializable {
	
	/** Message type indicating that operation was
	 * unsuccessful due to some error on FLASH side. 
	 * For example the WDDX packet contains a null value 
	 */
	public static final int ERROR = 1;

	/**
	 *  Message type indicating that operation failed 
	 * due to some system eror. For example, the client
	 * was unable to serilaize the WDDX packet 
	 */
	public static final int CRITICAL_ERROR = 2;
	
	/**
	 *  Message type indicating that operation 
	 * was executed successfully
	 */
	public static final int OBJECT_MESSAGE = 3;

	/** Usually the name of the method that was called by the flash */
	private String messageKey;
	
	/**
	 *  The response to the flash's request. Normally a string either 
	 * stating the error message or the WDDX packet
	 */
	private Object messageValue;
	
	/**
	 * Represents the type of message being sent to Flash. Can be one of 
	 * the following values
	 * <ul><b>
	 * 		<li>ERROR</li>
	 * 		<li>CRITICAL_ERROR</li>
	 * 		<li>OBJECT_MESSAGE</li>
	 * </b></ul> 
	 */
	private int messageType;
	
	/** Minimal Constructor */
	public FlashMessage(String messageKey, Object messageValue) {
		this.messageKey = messageKey;
		this.messageValue = messageValue;
		this.messageType = OBJECT_MESSAGE;
	}
	/** Full Constructor*/
	public FlashMessage(String messageKey, Object messageValue, int messageType) {
		this.messageKey = messageKey;
		this.messageValue = messageValue;
		this.messageType = messageType;
	}
	public String getMessageKey() {
		return messageKey;
	}	
	public int getMessageType() {
		return messageType;
	}
	public Object getMessageValue() {
		return messageValue;
	}	
}
