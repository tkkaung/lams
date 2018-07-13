package ntu.celt.eUreka2.modules.search;

import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.entities.User;

public interface SearchDAO {
	List<SearchResult> searchProjects(String searchText, User curUser);
	List<SearchResult> searchAnnouncements(String searchText, User curUser);
	List<SearchResult> searchForums(String searchText, User curUser);
	List<SearchResult> searchResources(String searchText, User curUser);
	List<SearchResult> searchWebBlogs(String searchText, User curUser);
	
	List<SearchResult> advancedSearchProjects(String allWord,
			String exactPhrase, String anyWord, String withoutWord,
			List<String> selSchools, Date fromDate, Date toDate, User curUser);
	List<SearchResult> advancedSearchAnnouncements(String allWord,
			String exactPhrase, String anyWord, String withoutWord,
			List<String> selSchools, Date fromDate, Date toDate, User curUser);
	List<SearchResult> advancedSearchForums(String allWord,
			String exactPhrase, String anyWord, String withoutWord,
			List<String> selSchools, Date fromDate, Date toDate, User curUser);
	List<SearchResult> advancedSearchResources(String allWord,
			String exactPhrase, String anyWord, String withoutWord,
			List<String> selSchools, Date fromDate, Date toDate, 
			boolean only, String includeFileFormats,  User curUser) ;
	List<SearchResult> advancedSearchWebBlogs(String allWord,
			String exactPhrase, String anyWord, String withoutWord,
			List<String> selSchools, Date fromDate, Date toDate, User curUser);
}
