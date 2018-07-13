package ntu.celt.eUreka2.dao;

import java.util.List;

import ntu.celt.eUreka2.entities.Module;

public interface ModuleDAO {
	List<Module> getAllModules();
	
	Module getModuleById(int id);
	Module getModuleByName(String name);
	boolean isModuleNameExists(String name);
	void updateModule(Module module);
	void deleteModule(Module module);
}
