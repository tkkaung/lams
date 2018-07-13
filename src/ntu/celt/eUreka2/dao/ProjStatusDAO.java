package ntu.celt.eUreka2.dao;

import java.util.List;


import ntu.celt.eUreka2.entities.ProjStatus;

public interface ProjStatusDAO {
	List<ProjStatus> getAllStatus();
	ProjStatus getStatusById(int id);
	ProjStatus getStatusByName(String name);
	boolean isStatusNameExist(String name);
	void updateStatus(ProjStatus status);
	void deleteStatus(ProjStatus status);
}
