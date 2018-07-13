package ntu.celt.eUreka2.dao;

import java.util.List;

import ntu.celt.eUreka2.entities.ProjType;

public interface ProjTypeDAO {
	List<ProjType> getAllTypes();
	ProjType getTypeById(int id);
	ProjType getTypeByName(String name);
	boolean isTypeNameExist(String name);
	void addType(ProjType type);
	void updateType(ProjType type);
	void deleteType(ProjType type);
}
