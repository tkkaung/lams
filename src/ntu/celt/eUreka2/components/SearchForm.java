package ntu.celt.eUreka2.components;

import java.util.ArrayList;
import java.util.List;

import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.search.SearchType;
import ntu.celt.eUreka2.pages.Search;

import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.internal.services.StringValueEncoder;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;


@Import(library="SearchForm.js")
public class SearchForm {

	@Inject
    private PageRenderLinkSource linkSource;
    @Inject
    private Messages messages;
    
	@Property
    private String srchTxt = messages.get("search-default-text");
    @Property
    private String srchField ;
    @SuppressWarnings("unused")
	@Property
	private StringValueEncoder stringEncoder = new StringValueEncoder();
	Object onSubmitFromQuickSrchForm(){
    	//just forward to the Search page
		if(messages.get("search-default-text").equals(srchTxt))
			srchTxt = null;
    	return linkSource.createPageRenderLinkWithContext(Search.class, srchTxt, srchField, true);
    }
	void onSuccessFromQuickSrchForm(){
		
	}
    public SelectModel getSrchModel(){
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		for (SearchType t : SearchType.values()) {
			OptionModel optModel = new OptionModelImpl(Util.capitalize(t.name()), t.name());
			optModelList.add(optModel);
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
}
