package ntu.celt.eUreka2.services.attachFiles;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ntu.celt.eUreka2.internal.Config;

import org.apache.commons.io.FilenameUtils;
import org.apache.tapestry5.upload.services.UploadedFile;

public class AttachedFileManagerImpl implements AttachedFileManager {
	
	
	@Override
	public AttachedFileStreamResponse getAttachedFileAsStream(
			AttachedFile attFile) throws FileNotFoundException {
		File f = new File(getRootFolder() + attFile.getPath());
		InputStream is = new BufferedInputStream(new FileInputStream(f));
		return new AttachedFileStreamResponse(is, attFile.getFileName(),
				attFile.getContentType(), f.length());
	}
	@Override
	public AttachedFileStreamResponse getAttachedFileAsStream(
			String filename, String path, String contentType) throws FileNotFoundException {
		File f = new File(getRootFolder() + path);
		InputStream is = new BufferedInputStream(new FileInputStream(f));
		return new AttachedFileStreamResponse(is, filename, contentType, f.length());
	}
	
	@Override
	public boolean removeAttachedFile(AttachedFile attFile) {
		File f = new File(getRootFolder() + attFile.getPath());
		return f.delete();
	}

	@Override
	public String saveAttachedFile(UploadedFile upFile, String newFileName,
			String moduleName, String projId) {
		String location = generateFileLocation(upFile.getFileName(), newFileName, moduleName, projId);
		File saveFile = new File(location);
		
		upFile.write(saveFile);

		return getRelativePath(location);
	}
	@Override
	public String saveAttachedFile(File file, String newFileName,
			String moduleName, String projId)  {
		String location = generateFileLocation(file.getName(), newFileName, moduleName, projId);
		File saveFile = new File(location);
		
//System.out.println("........file="+file.getAbsolutePath()+" ...saveToFile="+saveFile.getAbsolutePath());		
		try{
			InputStream in = new FileInputStream(file);
			OutputStream out = new FileOutputStream(saveFile);
			byte[] buf = new byte[1024];
			int len;
			while((len=in.read(buf))>0){
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
//System.out.println("........done..savefile="+saveFile.getAbsolutePath());		
		} catch (IOException e) {
			e.printStackTrace();
		}
		return getRelativePath(location);
	}
	@Override
	public String saveAttachedFile(InputStream inputStream, String fileName, String newFileName,
			String moduleName, String projId)  {
		String location = generateFileLocation(fileName, newFileName, moduleName, projId);
		File saveFile = new File(location);
		
//System.out.println("........file="+file.getAbsolutePath()+" ...saveToFile="+saveFile.getAbsolutePath());		
		try{
			OutputStream out = new FileOutputStream(saveFile);
			byte[] buf = new byte[1024];
			int len;
			while((len=inputStream.read(buf))>0){
				out.write(buf, 0, len);
			}
			inputStream.close();
			out.close();
//System.out.println("........done..savefile="+saveFile.getAbsolutePath());		
		} catch (IOException e) {
			e.printStackTrace();
		}
		return getRelativePath(location);
	}
	
	@Override
	public String generateFileLocation(String orgFilename, String newFileName,
			String moduleName, String projId){
		String folderPath = "/" + projId + "/" + moduleName.replace(" ", "_");
		String extension = FilenameUtils.getExtension(orgFilename);
		String abFolderPath = getRootFolder() + folderPath;
		File saveFolder = new File(abFolderPath);
		if (!saveFolder.exists())
			saveFolder.mkdirs();
		return abFolderPath + "/" + newFileName + "." +extension;
	}
	@Override
	public String getRelativePath(String absolutePath){
		if(absolutePath==null) 
			return absolutePath;
		return absolutePath.replace("\\", "/").replace(getRootFolder().replace("\\", "/"), "");
	}
	
	@Override
	public File getAttachedFile(AttachedFile attFile){
		return new File(getRootFolder() + attFile.getPath());
	}

	@Override
	public String copyFileFromZipToRepo(File zipFile, String zipEntryName,
			String attachedFileId, String moduleName, String projId) throws IOException {
		return copyFileFromZipToRepo(zipFile, zipEntryName,	attachedFileId, moduleName,
				projId, null);
	}

	@Override
	public String copyFileFromZipToRepo(File zipFile, String zipEntryName,
			String attachedFileId, String moduleName,
			String projId, String extraFolder) throws IOException {
		String folderPath = "/" + projId + "/" + moduleName.replace(" ", "_");
		String extension = FilenameUtils.getExtension(zipEntryName);
		
		File saveFolder = new File(getRootFolder() + folderPath);
		if (!saveFolder.exists())
			saveFolder.mkdirs();
		if(extraFolder!=null){
			saveFolder = new File(saveFolder.getAbsolutePath() + "/" + extraFolder);
			folderPath = folderPath + "/" + extraFolder;
			if (!saveFolder.exists())
				saveFolder.mkdirs();
		}
		File saveFile = new File(saveFolder.getAbsolutePath() + "/" + attachedFileId+ "." +extension);
		
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
		BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(saveFile));
		ZipEntry entry;
		try {
			while((entry = zis.getNextEntry()) != null) {
				if(!entry.isDirectory() && entry.getName().equals(zipEntryName)){
					int BUFFER = 1024;
					int count;
					byte data[] = new byte[BUFFER];
					while ((count = zis.read(data, 0, BUFFER))  != -1) {
						bout.write(data, 0, count);
			        }
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			zis.close();
			bout.close();
		}
		return folderPath + "/" + attachedFileId+ "." +extension;
	}

	@Override
	public String getRootFolder(){
		return Config.getString(Config.VIRTUAL_DRIVE);
	}
	
	
}
