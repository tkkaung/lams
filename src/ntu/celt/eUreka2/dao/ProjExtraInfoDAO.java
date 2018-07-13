package ntu.celt.eUreka2.dao;

import java.util.List;

import org.apache.tapestry5.hibernate.annotations.CommitAfter;

import ntu.celt.eUreka2.entities.ProjCAOExtraInfo;
import ntu.celt.eUreka2.entities.ProjFYPExtraInfo;
import ntu.celt.eUreka2.entities.Project;

public interface ProjExtraInfoDAO {
	
	ProjCAOExtraInfo getProjExtraInfoById(int id);
	ProjCAOExtraInfo getProjExtraInfoByProject(Project project);
	ProjCAOExtraInfo getProjExtraInfoByCAOId(String caoId) ;
	@CommitAfter
	void immediateSaveProjExtraInfo(ProjCAOExtraInfo info);
	void updateProjExtraInfo(ProjCAOExtraInfo info);
	void deleteProjExtraInfo(ProjCAOExtraInfo info);
	void saveOrUpdateBatchProjExtraInfo(List<ProjCAOExtraInfo> pList);
	
	
	ProjFYPExtraInfo getProjFYPExtraInfoById(int id);
	ProjFYPExtraInfo getProjFYPExtraInfoByProject(Project project);
	ProjFYPExtraInfo getProjFYPExtraInfoByFYPId(String fypId) ;
	@CommitAfter
	void immediateSaveProjExtraInfo(ProjFYPExtraInfo info);
	void updateProjExtraInfo(ProjFYPExtraInfo info);
	void deleteProjExtraInfo(ProjFYPExtraInfo info);
	void saveOrUpdateBatchProjExtraInfoFYP(List<ProjFYPExtraInfo> pList);
	
}
