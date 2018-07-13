package ntu.celt.eUreka2.pages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.annotation.PublicPage;
import ntu.celt.eUreka2.dao.AnalyticDAO;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.assessment.Assessment;
import ntu.celt.eUreka2.pages.modules.assessment.View;

import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.apache.tapestry5.services.Response;
import org.hibernate.Query;
import org.slf4j.Logger;

@PublicPage
public class TestQuery {
	
	@Inject 
	private Response response;
	@Persist("flash")
	@Property
	private String msg;
	@Persist("flash")
	@Property
	private String logMsg;
	
	

	@Inject
	private Logger logger;
	@Inject
	private AnalyticDAO anaDAO;
	@Inject
	private Messages messages;
	@Inject
	private BeanModelSource beanModelSource;
	@Inject
	private PropertyConduitSource propertyConduitSource;

	@Persist("flash")
	@Property
	private String className;
	@Persist("flash")
	@Property
	private String sql;
	@Persist("flash")
	private Class cls;
	
	@SuppressWarnings("unused")
	@Property
	private int rowIndex;
	
	@Persist("flash")
	@Property
	private List<Object> results;
	@Property
	private Object result;

	void setupRender(){
		if(className==null){
			className = "ntu.celt.eUreka2.entities.Project";
		}
		if(sql==null){
			sql = "SELECT * FROM tbl_project LIMIT 50";
		}
	}

	
	void onSuccessFromQueryForm(){
		try {
			className = className.trim();
			cls = Class.forName(className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		results = anaDAO.querySQLDB(sql, className);
		
		
	}
	
	public BeanModel<Object> getModel() {
		BeanModel<Object> model = beanModelSource.createEditModel(cls, messages);
		
		return model;
	}


}
