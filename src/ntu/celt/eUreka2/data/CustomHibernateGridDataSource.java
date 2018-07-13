package ntu.celt.eUreka2.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.apache.tapestry5.hibernate.HibernateGridDataSource;


/*
 * A simple sub-class of org.apache.tapestry5.hibernate.HibernateGridDataSource
 * with AdditionalConstraints
 * 
 * This class is NOT thread-safe; it maintains internal state
 **/
public class CustomHibernateGridDataSource extends HibernateGridDataSource{
	private List<Criterion> criterions = new ArrayList<Criterion>();
	private Map<String, String> aliasMap = new TreeMap<String, String>();
	
	
	@SuppressWarnings("unchecked")
	public CustomHibernateGridDataSource(Session session, Class entityType) {
		super(session, entityType);
	}
	
	
	public void applyAdditionalConstraints(Criteria crit) {
		
		
		for(String key : aliasMap.keySet()){
			crit.createAlias(key, aliasMap.get(key));
		}
		for(Criterion cr : criterions){
			crit.add(cr);
		}
	}

	public void setCriterions(List<Criterion> criterions) {
		this.criterions = criterions;
	}
	public List<Criterion> getCriterions() {
		return criterions;
	}


	public Map<String, String> getAlias() {
		return aliasMap;
	}
	public void setAlias(Map<String, String> aliasMap) {
		this.aliasMap = aliasMap;
	}
	
	
	
}
