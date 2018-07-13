package ntu.celt.eUreka2.pages.user;

import java.util.ArrayList;
import java.util.List;

import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.EvenOdd;
import ntu.celt.eUreka2.data.FilterType;
import ntu.celt.eUreka2.data.UserSearchableField;

import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.Grid;
import org.apache.tapestry5.grid.GridDataSource;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class UserList extends AbstractPageUser {
	@SuppressWarnings("unused")
	@Property
	private User user;
	@Property
	private GridDataSource users;
	@Property
	private String character;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private String prefix;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private String searchText;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private UserSearchableField searchIn;
	@SuppressWarnings("unused")
	@Property
	private EvenOdd evenOdd ;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private FilterType filterType;
	
	@Inject
	private UserDAO userDAO;
	@Inject
	private Messages messages;
	
	@SessionState
	private AppState appState;

	@InjectComponent
	private Grid grid;
	private int firstResult;
	private int maxResult ;

	
	void setupRender() {
		evenOdd = new EvenOdd();
		
		maxResult = appState.getRowsPerPage();
		firstResult = (grid.getCurrentPage()-1)*grid.getRowsPerPage();
		
		if(filterType==null)
			filterType = FilterType.CONTAIN;
		
		
		users = userDAO.searchUsersAsDataSource(filterType, searchText, searchIn
				,true, null, null
				, firstResult, maxResult, grid.getSortModel().getSortConstraints());
	}
	
	public String[] getCharList(){
		return new String[]{"ALL", "A", "B", "C", "D", "E"
				, "F", "G", "H", "I", "J", "K", "L", "M", "N"
				, "O", "P", "Q", "R", "S", "T", "U", "V", "W"
				, "X", "Y", "Z"};
	}
	public String getClassForChar(){
		if(character.equalsIgnoreCase(prefix))
			return "highlight";
		else
			return null;
	}
	
	void onActionFromStartWith(String ch){
		prefix = ch;
		searchText = (prefix.equalsIgnoreCase("ALL"))? "": prefix;
		filterType = FilterType.START_WITH;
		
	}
	
	void onSuccessFromSearchForm(){
		prefix = null;
	}
	
	
	public SelectModel getFilterTypeModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		
		for (FilterType ft : FilterType.values()) {
			OptionModel optModel = new OptionModelImpl(messages.get(ft.name()), ft);
			optModelList.add(optModel);
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	
	
	public int getTotalSize() {
		return users.getAvailableRows();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
}
