/****************************************************************
 * Copyright (C) 2014 LAMS Foundation (http://lamsfoundation.org)
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 * USA
 *
 * http://www.gnu.org/licenses/gpl.txt
 * ****************************************************************
 */ 

package org.lamsfoundation.lams.pages;

import org.lamsfoundation.lams.pages.author.FLAPage;
import org.lamsfoundation.lams.pages.monitor.addlesson.AddLessonPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * IndexPage 
 * 
 * @author Ernie Ghiglione (ernieg@lamsfoundation.org)
 *
 */
public class IndexPage extends AbstractPage {
	
	@FindBy(id = "openFla")
	private WebElement flaLink;	
	
	@FindBy(id = "My Profile")
	private WebElement myProfileTab; 
	
	@FindBy(className = "add-lesson-button")
	private WebElement addLessonButton; 

	
	public IndexPage(WebDriver driver) {
		super(driver);
	}
	
	public FLAPage openFla() {
		flaLink.click();
		driver.switchTo().window("FlashlessAuthoring");
		return PageFactory.initElements(driver, FLAPage.class);		
	}
	
	public AddLessonPage addLesson() {
		
		addLessonButton.click();
		WebDriverWait wait = new WebDriverWait(driver, 2);
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("dialogFrame")));
		
		return PageFactory.initElements(driver, AddLessonPage.class);		
	}
	
	public Boolean isLessonPresent(String lessonName) {
		
		return ((driver.findElements(By.linkText(lessonName)).size() > 0) ? true : false);
		
	
	}
	
}
