/****************************************************************
 * Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)
 * =============================================================
 * License Information: http://lamsfoundation.org/licensing/lams/2.0/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
 * USA
 *
 * http://www.gnu.org/licenses/gpl.txt
 * ****************************************************************
 */

package org.lamsfoundation.lams.tool.dokumaran.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;

/**
 * Dokumaran
 *
 * @author Andrey Balan
 */
@Entity
@Table(name = "tl_ladoku11_dokumaran")
public class Dokumaran implements Cloneable {
    private static final Logger log = Logger.getLogger(Dokumaran.class);

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uid;

    @Column(name = "content_id")
    private Long contentId;

    @Column
    private String title;

    @Column
    private String description;

    @Column
    private String instructions;

    // advance

    @Column(name = "use_select_leader_tool_ouput")
    private boolean useSelectLeaderToolOuput;

    @Column(name = "allow_multiple_leaders")
    private boolean allowMultipleLeaders;

    @Column(name = "time_limit")
    private int timeLimit;

    //date when teacher has started time counter (pressed start button)
    @Column(name = "time_limit_launched_date")
    private Date timeLimitLaunchedDate;

    @Column(name = "show_chat")
    private boolean showChat;

    @Column(name = "show_line_numbers")
    private boolean showLineNumbers;

    @Column(name = "shared_pad_id")
    private String sharedPadId;

    @Column(name = "lock_on_finished")
    private boolean lockWhenFinished;

    @Column(name = "define_later")
    private boolean defineLater;

    @Column(name = "content_in_use")
    private boolean contentInUse;

    @Column(name = "reflect_on_activity")
    private boolean reflectOnActivity;

    @Column(name = "reflect_instructions")
    private String reflectInstructions;

    // general information

    @Column(name = "create_date")
    private Date created;

    @Column(name = "update_date")
    private Date updated;

    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    @JoinColumn(name = "create_by")
    private DokumaranUser createdBy;

    // **********************************************************
    // Function method for Dokumaran
    // **********************************************************
    public static Dokumaran newInstance(Dokumaran defaultContent, Long contentId) {
	Dokumaran toContent = new Dokumaran();
	toContent = (Dokumaran) defaultContent.clone();
	toContent.setContentId(contentId);

	// reset user info as well
	if (toContent.getCreatedBy() != null) {
	    toContent.getCreatedBy().setDokumaran(toContent);
	}
	return toContent;
    }

    @Override
    public Object clone() {
	Dokumaran dokumaran = null;
	try {
	    dokumaran = (Dokumaran) super.clone();
	    dokumaran.setUid(null);
	    // clone ReourceUser as well
	    if (createdBy != null) {
		dokumaran.setCreatedBy((DokumaranUser) createdBy.clone());
	    }
	} catch (CloneNotSupportedException e) {
	    Dokumaran.log.error("When clone " + Dokumaran.class + " failed");
	}

	return dokumaran;
    }

    @Override
    public boolean equals(Object o) {
	if (this == o) {
	    return true;
	}
	if (!(o instanceof Dokumaran)) {
	    return false;
	}

	final Dokumaran genericEntity = (Dokumaran) o;

	return new EqualsBuilder().append(uid, genericEntity.uid).append(title, genericEntity.title)
		.append(description, genericEntity.description).append(instructions, genericEntity.instructions)
		.append(created, genericEntity.created).append(updated, genericEntity.updated)
		.append(createdBy, genericEntity.createdBy).isEquals();
    }

    @Override
    public int hashCode() {
	return new HashCodeBuilder().append(uid).append(title).append(description).append(instructions).append(created)
		.append(updated).append(createdBy).toHashCode();
    }

    /**
     * Updates the modification data for this entity.
     */
    public void updateModificationData() {

	long now = System.currentTimeMillis();
	if (created == null) {
	    this.setCreated(new Date(now));
	}
	this.setUpdated(new Date(now));
    }

    // **********************************************************
    // get/set methods
    // **********************************************************
    /**
     * Returns the object's creation date
     *
     * @return date
     *
     */
    public Date getCreated() {
	return created;
    }

    /**
     * Sets the object's creation date
     *
     * @param created
     */
    public void setCreated(Date created) {
	this.created = created;
    }

    /**
     * Returns the object's date of last update
     *
     * @return date updated
     */
    public Date getUpdated() {
	return updated;
    }

    /**
     * Sets the object's date of last update
     *
     * @param updated
     */
    public void setUpdated(Date updated) {
	this.updated = updated;
    }

    /**
     * @return Returns the userid of the user who created the Share dokumaran.
     */
    public DokumaranUser getCreatedBy() {
	return createdBy;
    }

    /**
     * @param createdBy
     *            The userid of the user who created this Share dokumaran.
     */
    public void setCreatedBy(DokumaranUser createdBy) {
	this.createdBy = createdBy;
    }

    public Long getUid() {
	return uid;
    }

    public void setUid(Long uid) {
	this.uid = uid;
    }

    /**
     * @return Returns the title.
     */
    public String getTitle() {
	return title;
    }

    /**
     * @param title
     *            The title to set.
     */
    public void setTitle(String title) {
	this.title = title;
    }

    public String getDescription() {
	return description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    /**
     * @return Returns the lockWhenFinish.
     */
    public boolean getLockWhenFinished() {
	return lockWhenFinished;
    }

    /**
     * @param lockWhenFinished
     *            Set to true to lock the dokumaran for finished users.
     */
    public void setLockWhenFinished(boolean lockWhenFinished) {
	this.lockWhenFinished = lockWhenFinished;
    }

    /**
     * @return Returns the instructions set by the teacher.
     */
    public String getInstructions() {
	return instructions;
    }

    public void setInstructions(String instructions) {
	this.instructions = instructions;
    }

    public boolean isContentInUse() {
	return contentInUse;
    }

    public void setContentInUse(boolean contentInUse) {
	this.contentInUse = contentInUse;
    }

    public boolean isDefineLater() {
	return defineLater;
    }

    public void setDefineLater(boolean defineLater) {
	this.defineLater = defineLater;
    }

    public Long getContentId() {
	return contentId;
    }

    public void setContentId(Long contentId) {
	this.contentId = contentId;
    }

    /**
     * @return Returns the time limitation, that students have to complete an attempt.
     */
    public int getTimeLimit() {
	return timeLimit;
    }

    /**
     * @param timeLimit
     *            the time limitation, that students have to complete an attempt.
     */
    public void setTimeLimit(int timeLimit) {
	this.timeLimit = timeLimit;
    }

    /**
     * @return date when teacher has started time counter (pressed start button)
     */
    public Date getTimeLimitLaunchedDate() {
	return timeLimitLaunchedDate;
    }

    public void setTimeLimitLaunchedDate(Date timeLimitLaunchedDate) {
	this.timeLimitLaunchedDate = timeLimitLaunchedDate;
    }

    public boolean isShowChat() {
	return showChat;
    }

    public void setShowChat(boolean showChat) {
	this.showChat = showChat;
    }

    public boolean isShowLineNumbers() {
	return showLineNumbers;
    }

    public void setShowLineNumbers(boolean showLineNumbers) {
	this.showLineNumbers = showLineNumbers;
    }

    public String getSharedPadId() {
	return sharedPadId;
    }

    public void setSharedPadId(String sharedPadId) {
	this.sharedPadId = sharedPadId;
    }

    public boolean isSharedPadEnabled() {
	return StringUtils.isNotEmpty(sharedPadId);
    }

    public boolean isUseSelectLeaderToolOuput() {
	return useSelectLeaderToolOuput;
    }

    public void setUseSelectLeaderToolOuput(boolean useSelectLeaderToolOuput) {
	this.useSelectLeaderToolOuput = useSelectLeaderToolOuput;
    }

    public boolean isAllowMultipleLeaders() {
	return allowMultipleLeaders;
    }

    public void setAllowMultipleLeaders(boolean allowMultipleLeaders) {
	this.allowMultipleLeaders = allowMultipleLeaders;
    }

    public String getReflectInstructions() {
	return reflectInstructions;
    }

    public void setReflectInstructions(String reflectInstructions) {
	this.reflectInstructions = reflectInstructions;
    }

    public boolean isReflectOnActivity() {
	return reflectOnActivity;
    }

    public void setReflectOnActivity(boolean reflectOnActivity) {
	this.reflectOnActivity = reflectOnActivity;
    }
}
