package ntu.celt.eUreka2.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;

import ntu.celt.eUreka2.entities.AnalyticBean;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.ProjRole;
import ntu.celt.eUreka2.entities.ProjStatus;
import ntu.celt.eUreka2.entities.ProjType;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;

public class AnalyticDAOImpl implements AnalyticDAO {
	private Session session;
	private Logger log;

	

	public AnalyticDAOImpl(Session session, Logger log) {
		super();
		this.session = session;
		this.log = log;
	}



	@Override
	public List<Object> statInstructor(Date startDate, Date endDate, String name,
			School school, ProjStatus status, ProjType type, String term,
			String moduleName) {
		
		
	
		String sqlNative = 

"		SELECT t2.* "
+"		, (SELECT count(*) from tbl_projuser pu WHERE pu.userid=t2.userid ) as numProject"
+"		, (SELECT count(*) from tbl_assessment a JOIN tbl_projuser pu ON a.project = pu.projid WHERE pu.userid=t2.userid ) as numAssessment"
+"		, (SELECT count(*) from tbl_evaluation e JOIN tbl_projuser pu ON e.project = pu.projid WHERE pu.userid=t2.userid ) as numEvaluation"
+"		FROM"
+"		("
+"		SELECT t1.username, u.id as userid, u.lastname,  s.name as userSchool, module, sum(numPageLoad) as numPageLoad , count(t1.username) as numVisit"
+"		 FROM"
+"		("
+"		SELECT  username, SUBSTRING_index(url, '/', 3) as module , count(*) as numPageLoad"
+"		FROM eureka2.tbl_sessionvisit t"
+"		where"
+"		 pageName not in ( 'Exception' , 'Search' )"
+"		group by SessionID, username, module"
+"		) as t1"
+"		LEFT JOIN tbl_user u ON t1.username=u.username"
+"		LEFT JOIN tbl_school s ON u.school=s.id"
+"		group by username, module"
+"		) as t2";

Query qNative = session.createQuery(sqlNative);
return qNative.list();

	/*	
		String sql1 = "SELECT username  FROM SessionVisitStatistic s " +
		""
	//			"WHERE "
		//	+ startDate!=null ? " AND accessTime >= :startDate " : ""
		//	+ endDate!=null ? "  s.accessTime <= :endDate " : ""
			//+ 	SUBSTRING(url,  CHAR_LENGTH(pageName)+3) as ProjID
	
				
				;

		Query q1 = session.createQuery(sql1)
		;
		q1.setMaxResults(10);
*/
//		if(startDate!=null)
//			q1.setParameter("startDate", Util.getDate0000(startDate));
//		if(endDate!=null)
//			q1.setParameter("endDate", Util.getDate2359(endDate));

				
		//		q.setString("rName", name);

		//q.setParameter("rSchl", searchSchool);
		
//		return q1.list();
	}



	@Override
	public List<Object> querySQLDB(String sql, String className) {
		List result = new ArrayList<Object>();
		Class cls;
		try {
			cls = Class.forName(className);
			Query query = session.createSQLQuery(sql)
			.addEntity(cls);
			result = query.list();
		} catch (ClassNotFoundException e) {
			
			e.printStackTrace();
		}
		
		
			
		return result;
	}
	@Override
	public List<Object> querySQLDB(String sql) {
		Query query = session.createSQLQuery(sql);
		List result = query.list();
			
		return result;
	}



	@Override
	public List<String> getDistinctModules() {
		Query q = session.createQuery("SELECT DISTINCT v.moduleName FROM SessionVisitStatistic AS v");
		return q.list();
	}



	@Override
	public List<AnalyticBean> statByProjID(Date startDate, Date endDate,
			String projID, School school, ProjStatus status, ProjType type,
			String term, String moduleName) {
		List<AnalyticBean> results = new ArrayList<AnalyticBean>();
		
		String filterSQL =  
				"   AND (v.accessTime BETWEEN :rStartDate AND :rEndDate ) " 
				+ ((projID!=null && !projID.isEmpty())? " AND v.proj.id=:rPID" : "")
				+ ((school!=null)? " AND v.proj.school=:rSchl" : "")
				+ ((status!=null)? " AND v.proj.status=:rStatus" : "")
				+ ((type!=null)? " AND v.proj.type=:rType" : "")
				+ ((term!=null)? " AND v.proj.term=:rTerm" : "")
				+ ((moduleName!=null)? " AND v.moduleName=:rModuleName" : "")
				;
		
		Query q = session.createQuery("SELECT v.proj, count(v.sessionID), count(distinct v.sessionID), v.projRole, v.moduleName  " +
				" FROM SessionVisitStatistic AS v WHERE 1=1 "
				+ filterSQL
				+ " GROUP BY v.proj, v.projRole, v.moduleName "
	    		+ " ORDER BY v.proj, v.moduleName, v.projRole ASC "
	    		);
		
	    Query q2 = session.createQuery("SELECT   count(v.sessionID), count(distinct v.sessionID)  " +
	    		" FROM SessionVisitStatistic AS v WHERE v.proj IS NULL "
	    		+ filterSQL	
	    		);
	    

		q.setDate("rStartDate", Util.getDate0000(startDate));
		q2.setDate("rStartDate", Util.getDate0000(startDate));
		q.setDate("rEndDate", Util.getDatePlus1(endDate));
		q2.setDate("rEndDate", Util.getDatePlus1(endDate));
		
		boolean hasFilter = false;
	    if (projID!=null && !projID.isEmpty()){
			q.setString("rPID", projID);
			q2.setString("rPID", projID);
			hasFilter = true;
		}
	    if (school!=null){
			q.setParameter("rSchl", school);
			q2.setParameter("rSchl", school);
			hasFilter = true;
		}
	    if (status!=null){
			q.setParameter("rStatus", status);
			q2.setParameter("rStatus", status);
			hasFilter = true;
		}
	    if (type!=null){
			q.setParameter("rType", type);
			q2.setParameter("rType", type);
			hasFilter = true;
		}
		if (term!=null){
			q.setParameter("rTerm", term);
			q2.setParameter("rTerm", term);
			hasFilter = true;
		}
		if (moduleName!=null){
			q.setString("rModuleName", moduleName);
			q2.setString("rModuleName", moduleName);
			hasFilter = true;
		}
		if (!hasFilter){
			List<Object[]> ans2 = q2.list();
			for(Object[] a : ans2) {
			    AnalyticBean result = new AnalyticBean(null,  (Long) a[0], (Long) a[1]);
			    results.add(result);
			}
		}
		
		List<Object[]> ans = q.list();
		for(Object[] a : ans) {
		    AnalyticBean result = new AnalyticBean((Project)a[0],  (Long) a[1], (Long) a[2]);
		    result.setProjRole((ProjRole) a[3]);
		    result.setModuleName((String) a[4]);
		    
		    results.add(result);
		}
		
	    
		return results;
		
	}
	


}
