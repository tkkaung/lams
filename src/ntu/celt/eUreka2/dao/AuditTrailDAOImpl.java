package ntu.celt.eUreka2.dao;

import java.util.List;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.Session;

import ntu.celt.eUreka2.entities.AuditTrail;

public class AuditTrailDAOImpl implements AuditTrailDAO {
	@Inject
	private Session session;

	
	@SuppressWarnings("unchecked")
	@Override
	public List<AuditTrail> getAuditTrailsByModuleNObjID(String moduleName,
			String ObjID) {
		return session.createQuery("SELECT a FROM AuditTrail AS a " 
				+ " WHERE a.moduleName = :rModuleName "
				+ " AND a.objID = :robjID "
				+ " ORDER BY actionTime DESC "
				)
				.setString("rModuleName", moduleName)
				.setString("robjID", ObjID)
			.list();
	}

	@Override
	public void saveAuditTrail(AuditTrail auditTrail) {
		session.persist(auditTrail);
	}
	@Override
	public void deleteAuditTrail(AuditTrail auditTrail) {
		session.delete(auditTrail);
	}

}
