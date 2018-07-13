package ntu.celt.eUreka2.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;

import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.entities.ProjCAOExtraInfo;
import ntu.celt.eUreka2.entities.ProjFYPExtraInfo;
import ntu.celt.eUreka2.entities.Project;

public class ProjExtraInfoDAOImpl implements ProjExtraInfoDAO {

	private Session session;
	@SuppressWarnings("unused")
	private Logger logger ;
	

	public ProjExtraInfoDAOImpl(Session session, Logger logger) {
		super();
		this.session = session;
		this.logger = logger;
	}

	@Override
	public ProjCAOExtraInfo getProjExtraInfoById(int id) {
		return (ProjCAOExtraInfo) session.get(ProjCAOExtraInfo.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ProjCAOExtraInfo getProjExtraInfoByProject(Project project) {
		Criteria crit = session.createCriteria(ProjCAOExtraInfo.class)
				.add(Restrictions.eq("project.id", project.getId()))
				;
		List<ProjCAOExtraInfo> pList = crit.list();
		if(pList.isEmpty())
			return null;
		else
			return pList.get(0);
	}
	
	@Override
	public ProjCAOExtraInfo getProjExtraInfoByCAOId(String caoId) {
		return (ProjCAOExtraInfo) session.createQuery("SELECT pe FROM ProjCAOExtraInfo " 
				+ " WHERE caoID=:caoId "
				+ " AND project.status.name = :statusDelete  "
				)
				.setString("caoId", caoId)
				.setString("statusDelete", PredefinedNames.PROJSTATUS_DELETED)
			.uniqueResult();
	}
	
	@Override
	public void immediateSaveProjExtraInfo(ProjCAOExtraInfo info) {
		session.save(info);
		session.flush();
		session.clear();
	}
	@Override
	public void updateProjExtraInfo(ProjCAOExtraInfo info) {
		session.persist(info);
	}
	@Override
	public void deleteProjExtraInfo(ProjCAOExtraInfo info) {
		session.delete(info);
	}
	
	
	
	
	@Override
	public ProjFYPExtraInfo getProjFYPExtraInfoById(int id) {
		return (ProjFYPExtraInfo) session.get(ProjFYPExtraInfo.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ProjFYPExtraInfo getProjFYPExtraInfoByProject(Project project) {
		Criteria crit = session.createCriteria(ProjFYPExtraInfo.class)
				.add(Restrictions.eq("project.id", project.getId()))
				;
		List<ProjFYPExtraInfo> pList = crit.list();
		if(pList.isEmpty())
			return null;
		else
			return pList.get(0);
	}
	
	@Override
	public ProjFYPExtraInfo getProjFYPExtraInfoByFYPId(String fypId) {
		return (ProjFYPExtraInfo) session.createQuery("SELECT pe FROM ProjFYPExtraInfo AS pe " 
				+ " WHERE fypID=:fypId "
				+ " AND project.status.name = :statusDelete  "
				)
				.setString("fypId", fypId)
				.setString("statusDelete", PredefinedNames.PROJSTATUS_DELETED)
			.uniqueResult();
	}
	
	@Override
	public void immediateSaveProjExtraInfo(ProjFYPExtraInfo info) {
		session.save(info);

		session.flush();
		session.clear();
	}
	@Override
	public void updateProjExtraInfo(ProjFYPExtraInfo info) {
		session.persist(info);
	}
	@Override
	public void deleteProjExtraInfo(ProjFYPExtraInfo info) {
		session.delete(info);
	}

	@Override
	public void saveOrUpdateBatchProjExtraInfo(List<ProjCAOExtraInfo> pList) {
		for(ProjCAOExtraInfo p : pList){
			session.saveOrUpdate(p.getProject());
			session.saveOrUpdate(p);
		}
		session.flush();
		session.clear();
	}

	@Override
	public void saveOrUpdateBatchProjExtraInfoFYP(List<ProjFYPExtraInfo> pList) {
	/*	Transaction tx = session.beginTransaction();
		try{
			for(ProjFYPExtraInfo p : pList){
				session.saveOrUpdate(p.getProject());
				session.saveOrUpdate(p);
			}
			session.flush();
			session.clear();
			tx.commit();
		}catch(Exception e){
			//tx.rollback();
			logger.error(e.getMessage());
		}
		*/
		for(ProjFYPExtraInfo p : pList){
			session.saveOrUpdate(p.getProject());
			session.saveOrUpdate(p);
		}
		session.flush();
		session.clear();
		
	}
}
