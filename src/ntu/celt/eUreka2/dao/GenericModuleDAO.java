package ntu.celt.eUreka2.dao;

import java.util.List;

import ntu.celt.eUreka2.data.LastUpdateItem;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;

public interface GenericModuleDAO {
	List<LastUpdateItem> getLastUpdates(Project project, User curUser, int numPastDay);
	String getModuleName();
}
