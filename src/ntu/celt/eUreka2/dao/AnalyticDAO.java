package ntu.celt.eUreka2.dao;

import java.util.Date;
import java.util.List;


import ntu.celt.eUreka2.entities.AnalyticBean;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.ProjStatus;
import ntu.celt.eUreka2.entities.ProjType;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.User;

public interface AnalyticDAO {
	List<Object> statInstructor(Date startDate, Date endDate, String name, School school, ProjStatus status, ProjType type, String term, String moduleName );
	
	List<Object> querySQLDB(String sql, String className );
	List<Object> querySQLDB(String sql );
	
	List<String> getDistinctModules();
	
	List<AnalyticBean> statByProjID(Date startDate, Date endDate, String projID, 
			School school, ProjStatus status, ProjType type, String term, String moduleName );
	
}
