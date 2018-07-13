package ntu.celt.eUreka2.services.attachFiles;
import java.io.IOException;
import java.io.InputStream;


import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.services.Response;


public class AttachedFileStreamResponse implements StreamResponse {
	private InputStream is = null;
	protected String contentType = "text/plain";
    protected String filename = "default.txt";
    protected long fileSize = 0;

    public AttachedFileStreamResponse(InputStream is, String filename, String contentType, long fileSize) {
        this.is = is;
        if (filename != null) {
        	this.filename = filename;
        }
        this.contentType = contentType;
        this.fileSize = fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public InputStream getStream() throws IOException {
        return is;
    }

    public void prepareResponse(Response arg0) {
        arg0.setHeader("Content-Disposition", "attachment; filename=\"" + filename+"\"");
        //arg0.setHeader("Expires", "0");
        arg0.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        arg0.setHeader("Pragma", "public");
        arg0.setHeader("Content-Length", Long.toString(fileSize));
 }

}