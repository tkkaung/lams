package ntu.celt.eUreka2.entities;

import java.io.Serializable;

import ntu.celt.eUreka2.data.ThemeColor;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.hibernate.annotations.Entity;
@Entity
public class Preference implements Serializable{
	
	private static final long serialVersionUID = -5119255476056080127L;
	@NonVisual
	private int id;
	private User user;
	private ThemeColor themeColor;
	
	public String toString() {
		final String DIVIDER = ", ";
		
		StringBuffer buf = new StringBuffer();
		buf.append(this.getClass().getSimpleName() + ": ");
		buf.append("[");
		buf.append("id=" + id + DIVIDER);
		buf.append("user=" + user + DIVIDER);
		buf.append("themeColor=" + themeColor );
		buf.append("]");
		return buf.toString();
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public ThemeColor getThemeColor() {
		return themeColor;
	}
	public void setThemeColor(ThemeColor themeColor) {
		this.themeColor = themeColor;
	}
}
