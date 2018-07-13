package ntu.celt.eUreka2.modules.scheduling;
import java.io.IOException;
import java.io.InputStream;


import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.services.Response;


public class ExportFileStreamResponse implements StreamResponse {
	private InputStream is = null;
	protected String contentType = "application/vnd.ms-excel"; // for xlsx file: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    protected String filename = "default.xls";

    public ExportFileStreamResponse(InputStream is, String filename, String contentType) {
        this.is = is;
        if (filename != null) {
        	this.filename = filename;
        }
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public InputStream getStream() throws IOException {
        return is;
    }

    public void prepareResponse(Response arg0) {
        arg0.setHeader("Content-Disposition", "attachment; filename=\"" + filename+"\"");
        arg0.setHeader("Expires", "0");
        arg0.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        arg0.setHeader("Pragma", "public");
 }

}