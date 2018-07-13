package ntu.celt.eUreka2.components;

import java.util.List;

import ntu.celt.eUreka2.dao.SysAnnouncementDAO;
import ntu.celt.eUreka2.entities.SysAnnouncement;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;

public class SystemAnnouncement {
	
	@SuppressWarnings("unused")
	@Property
	private List<SysAnnouncement> _sysAnnmts ;
	@Property
	private SysAnnouncement _sysAnnmt ;
	
	@Inject
	private SysAnnouncementDAO _sysAnnmtDAO;
	
	void setupRender() {
		_sysAnnmts = _sysAnnmtDAO.getActiveSysAnnouncements();
	}
	
	public String getUrgentClass(){
		if(_sysAnnmt.isUrgent())
			return "urgentAnnmt";
		return null;
	}
	
/*	
	void beginRender(MarkupWriter writer) {
		if(sysAnnmts.length!=0){
			writer.element("div", "class", "sysAnnmt");
			writer.element("div");
			writer.element("div");
			writer.element("div");
			
			
			for(int i=0; i<sysAnnmts.length; i++){
				if(i!=0){
					writer.element("hr"); writer.end();
				}	
				writer.writeRaw(sysAnnmts[i]);
			}
			writer.end();
			writer.end();
			writer.end();
			writer.end();
		}
		else{
			writer.writeRaw("");
		}
	}
*/	
	
}
