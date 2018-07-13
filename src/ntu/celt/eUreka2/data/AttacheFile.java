package ntu.celt.eUreka2.data;

import org.apache.tapestry5.upload.services.UploadedFile;

public class AttacheFile {
	private UploadedFile afile;
	private Integer id;
	public UploadedFile getAfile() {
		return afile;
	}
	public void setAfile(UploadedFile afile) {
		this.afile = afile;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
}
