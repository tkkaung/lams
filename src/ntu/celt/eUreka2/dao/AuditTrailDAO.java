package ntu.celt.eUreka2.dao;

import java.util.List;

import ntu.celt.eUreka2.entities.AuditTrail;

public interface AuditTrailDAO {
	List<AuditTrail> getAuditTrailsByModuleNObjID(String moduleName, String ObjID);
	
	void saveAuditTrail(AuditTrail auditTrail);
	void deleteAuditTrail(AuditTrail auditTrail);
}
