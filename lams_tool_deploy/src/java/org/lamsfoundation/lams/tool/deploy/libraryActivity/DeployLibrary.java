/* 
Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
USA

http://www.gnu.org/licenses/gpl.txt 
*/
/* $$Id$$ */
package org.lamsfoundation.lams.tool.deploy.libraryActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lamsfoundation.lams.tool.deploy.DeployLanguageFilesTask;

/**
 * @author mtruong
 *
 * Deployer for library activities that contain more than one tool activity.
 * Currently, is only supported for the complex activity types: parallel,
 * options, sequence
 * 
 * The main logic is adopted from Deploy.java
 * See org.lamsfoundation.lams.tool.deploy.Deploy
 */
public class DeployLibrary {

    public DeployLibrary() {
        
    }
 
    
    public static void main (String[] args)
    {
        if ((args.length < 1) || (args[0] == null))
        {
            throw new IllegalArgumentException("Usage: Deployer <properties_file_path> <language file directory>." );
        }
        
        System.out.println("Starting Library Deploy");
        
       try
       {
            DeployLibraryConfig config= new DeployLibraryConfig(args[0]);
            LibraryDBDeployTask dbDeployTask = new LibraryDBDeployTask(config);
            dbDeployTask.execute();
            System.out.println("Inserted activities into database");

        	List libraries = dbDeployTask.getLearningLibraries();
        	Iterator iter = libraries.iterator();
        	while ( iter.hasNext() ) {

        		LearningLibrary lib = (LearningLibrary) iter.next();
        		String languageFileDirectory = lib.getLanguageFileDirectory();
                if ( languageFileDirectory != null ) {

                	// generate package names in which the language files are placed.
                	String packagePath = "org.lamsfoundation.lams.library.llid"+lib.getLearningLibraryId();
            		
            		// copy the files to the lams-dictionary.jar
		            DeployLanguageFilesTask deployLanguageFilesTask = new DeployLanguageFilesTask();
		            deployLanguageFilesTask.setLamsEarPath(config.getLamsEarPath());
		            deployLanguageFilesTask.setDictionaryPacket(packagePath);
		            deployLanguageFilesTask.setDeployFiles(createLanguageFilesList(languageFileDirectory));
		            deployLanguageFilesTask.execute();

	        		System.out.println("Updating activity with the language file path");
	        		ActivityDBLanguageUpdateTask activityDBTask = new ActivityDBLanguageUpdateTask();
	        		activityDBTask.setDbUsername(config.getDbUsername());
	        		activityDBTask.setDbPassword(config.getDbPassword());
	        		activityDBTask.setDbDriverClass(config.getDbDriverClass());
	        		activityDBTask.setDbDriverUrl(config.getDbDriverUrl());
	        		activityDBTask.setActivityId(lib.getParentActivityId());
	        		activityDBTask.setLanguageFilename(packagePath+'.'+lib.getLanguageFileRoot());
	        		activityDBTask.execute();
                }

                
        		System.out.println("Activating learning library "+lib.getLearningLibraryId());
                LibraryDBActivateTask libActivateTask = new LibraryDBActivateTask();
                libActivateTask.setDbUsername(config.getDbUsername());
                libActivateTask.setDbPassword(config.getDbPassword());
                libActivateTask.setDbDriverClass(config.getDbDriverClass());
                libActivateTask.setDbDriverUrl(config.getDbDriverUrl());
                libActivateTask.setLearningLibraryId(lib.getLearningLibraryId());
                libActivateTask.execute();
        	
        	}
            
            
            System.out.println("Library Deployed");
            
       }
       catch (Exception ex)
       {
           System.out.println("TOOL DEPLOY FAILED");
           ex.printStackTrace();
       }
       
        
        
    }
    
    /**
     * Sets the list of file paths to operate on.
     * @param deployFiles New value of property deployFiles.
     */
    private static List<String> createLanguageFilesList(String path)
    {
    	List<String> deployFiles = new ArrayList<String>();
    	if ( path != null ) {
    		File dir = new File(path);
    		if ( ! dir.exists() || ! dir.isDirectory() || ! dir.canRead() ) {
    			System.err.println("Unable to copy language files - path is invalid: "+path);
    		} else {
    			for ( String filename: dir.list() ) {
    				deployFiles.add(path+File.separator+filename);
    			}
    		}
    	}
		return deployFiles;
    }



}
