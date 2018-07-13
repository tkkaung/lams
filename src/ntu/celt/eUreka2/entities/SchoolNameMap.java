package ntu.celt.eUreka2.entities;


import java.io.Serializable;

import org.hibernate.annotations.Entity;

@Entity
public class SchoolNameMap implements Serializable{
	private static final long serialVersionUID = 5344929423556247697L;
	private int id;
	private String nameFrom; 
	private String nameTo;
	
	public SchoolNameMap() {
		super();
	}
	public SchoolNameMap(String nameFrom, String nameTo) {
		super();
		this.nameFrom = nameFrom;
		this.nameTo = nameTo;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	public int getId() {
		return id;
	}
	public String getNameFrom() {
		return nameFrom;
	}
	public void setNameFrom(String nameFrom) {
		this.nameFrom = nameFrom;
	}
	public String getNameTo() {
		return nameTo;
	}
	public void setNameTo(String nameTo) {
		this.nameTo = nameTo;
	}
	
	
}
