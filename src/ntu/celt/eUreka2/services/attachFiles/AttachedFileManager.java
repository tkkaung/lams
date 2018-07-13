package ntu.celt.eUreka2.services.attachFiles;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.tapestry5.upload.services.UploadedFile;

public interface AttachedFileManager {

	/**
	 * Get output stream of the attachFile. it is used for user to download the file 
	 * @param attFile
	 * @return
	 * @throws FileNotFoundException
	 */
	AttachedFileStreamResponse getAttachedFileAsStream(AttachedFile attFile)
			throws FileNotFoundException;
	
	AttachedFileStreamResponse getAttachedFileAsStream(String filename,
			String path, String contentType) throws FileNotFoundException;
	
	
	/**
	 * Get the File object of AttachedFile 
	 * @param attFile
	 * @return
	 * @throws FileNotFoundException
	 */
	File getAttachedFile(AttachedFile attFile) ;

	/**
	 * Remove the actual file
	 * 
	 * @param attFile
	 * @return
	 */
	boolean removeAttachedFile(AttachedFile attFile);

	/**
	 * write the UploadedFile to local disk, then return path to the file.
	 * This path is formatted as:  /<project ID>/<module name>/<file ID>
	 * 
	 * @param upFile
	 * @param attachedFileId
	 * @param projId
	 * @return relative path to the file (path to be saved by
	 *         attachedFile.setPath()
	 */
	String saveAttachedFile(UploadedFile upFile, String newFileName,
			String moduleName, String projId);
	String saveAttachedFile(File file, String newFileName,
			String moduleName, String projId);
	String saveAttachedFile(InputStream inputStream, String fileName, String newFileName,
			String moduleName, String projId);
	String generateFileLocation(String orgFilename, String newFileName,
			String moduleName, String projId);
	String getRelativePath(String absolutePath);
	String getRootFolder();
	
	/**
	 * copy file from a zip file into the repository (the virtual drive)
	 * @param zipFile
	 * @param zipEntryName
	 * @param attachedFileId
	 * @param moduleName
	 * @param projId
	 * @return the relative path to the file (made up from projId, moduleName, and attachedFileId)
	 * @throws IOException
	 */
	String copyFileFromZipToRepo(File zipFile, String zipEntryName, String attachedFileId,
			String moduleName, String projId ) throws IOException;
	String copyFileFromZipToRepo(File zipFile, String zipEntryName, String attachedFileId,
			String moduleName, String projId, String extraFolder ) throws IOException;

	
}