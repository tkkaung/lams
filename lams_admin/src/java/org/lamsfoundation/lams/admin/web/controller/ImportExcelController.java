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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 * USA
 *
 * http://www.gnu.org/licenses/gpl.txt
 * ****************************************************************
 */

package org.lamsfoundation.lams.admin.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.lamsfoundation.lams.admin.web.form.ImportExcelForm;
import org.lamsfoundation.lams.util.WebUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author jliew
 *
 *
 *
 *
 *
 *
 *
 */
@Controller
public class ImportExcelController {

    @RequestMapping("/importexcel")
    public String execute(@ModelAttribute ImportExcelForm importExcelForm, HttpServletRequest request)
	    throws Exception {

	Integer orgId = WebUtil.readIntParam(request, "orgId", true);
	//if (orgId==null) orgId = (Integer)request.getAttribute("orgId");

	importExcelForm.setOrgId(orgId);

	return "import/importexcel";
    }

}
