/****************************************************************
 * Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)
 * =============================================================
 * License Information: http://lamsfoundation.org/licensing/lams/2.0/
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2.0 
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 * 
 * http://www.gnu.org/licenses/gpl.txt
 * ****************************************************************
 */

/* $$Id$$ */
package org.lamsfoundation.lams.tool.deploy;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * @author mtruong
 *
 * Subclass of DeployConfig.
 * 
 * Encapsulates the configuration data for the Tool Deployer.
 * 
 * The XML outputted by XStream will contain the absolute classname if 
 * an alias is not specified. To make the XML output more concise, the XML elememnt
 * "Deploy" is mapped to this class (org.lamsfoundation.lams.tool.deploy.DeployToolConfig class)
 * 
 * See templateDeployTool.xml for the example XML format/structure
 */
public class DeployToolConfig extends DeployConfig {
    
    // private static Log log = LogFactory.getLog(DeployToolConfig.class);
    
    private static final String TOOL_WEB_URI = "toolWebUri";
    private static final String TOOL_CONTEXT = "toolContext";
    private static final String TOOL_INSERT_SCRIPT_PATH = "toolInsertScriptPath";
    private static final String TOOL_LIBRARY_INSERT_SCRIPT_PATH = "toolLibraryInsertScriptPath";
    private static final String TOOL_TABLES_SCRIPT_PATH = "toolTablesScriptPath";
    private static final String TOOL_TABLES_DELETE_SCRIPT_PATH = "toolTablesDeleteScriptPath";
    private static final String DEPLOY_FILES= "deployFiles";
    protected static final String LANGUAGE_FILES= "languageFiles";
  
    /**
     * Holds value of property toolSignature.
     */
    private String toolSignature;

    /**
     * Holds value of property toolWebUri.
     */
    private String toolWebUri;
    
    /**
     * Holds value of property toolContextRoot.
     */
    private String toolContext;
    
    /**
     * Holds value of property toolInsertScriptPath.
     */
    private String toolInsertScriptPath;
    
    /**
     * Holds value of property toolLibraryInsertScriptPath.
     */
    private String toolLibraryInsertScriptPath;
    
    /**
     * Holds value of property toolActivityInsertScriptPath.
     */
    private String toolActivityInsertScriptPath;
    
    /**
     * Holds value of property toolTablesScriptPath.
     */
    private String toolTablesScriptPath;
    
    /**
     * Holds value of property toolTablesDeleteScriptPath.
     */
    private String toolTablesDeleteScriptPath;

    /**
     * Holds value of property deployFiles.
     */
    private ArrayList<String> deployFiles;
    
    /**
     * Holds value of property languageFiles.
     */
    private ArrayList<String> languageFiles;


    /**
     * Creates an instance of DeployToolConfig object.
     */
    public DeployToolConfig()
    {
        super();
        xstream.alias(ROOT_ELEMENT, DeployToolConfig.class);
    }
    
    /**
     * Creates an instance of DeployToolConfig object, with the values
     * of its properties, set to that specified by the Xml configuration file
     * @param configurationFilePath
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public DeployToolConfig(String configurationFilePath) throws ParserConfigurationException, IOException, SAXException
    {
        super();
        xstream.alias(ROOT_ELEMENT, DeployToolConfig.class);
        updateConfigurationProperties(configurationFilePath);
    }
    
    /** @see org.lamsfoundation.lams.tool.deploy.DeployConfig#updateConfigurationProperties(String) */
    public void updateConfigurationProperties(String configFilePath) throws ParserConfigurationException, IOException, SAXException
    {
        String xml = readFile(configFilePath);
        DeployToolConfig config = (DeployToolConfig)deserialiseXML(xml);
        copyProperties(config);
        //printObjectProperties(); //for testing purposes
    }
     
  
   /**
    * Set an arbitrary property. Note: these will probably have come through
    * ant, and that removes the case of the key, so ignore case.
    */
   protected void setProperty(String key, String value) throws DeployException {
       if ( key == null )
           throw new DeployException("Invalid parameter: Key is null. ");
       
       super.setProperty(key, value);
       //System.out.println("ToolConfig " + key + " is: " + value);
     
       if ( key.equalsIgnoreCase(TOOL_SIGNATURE) ) {
          
           toolSignature = value;
       }

       if ( key.equalsIgnoreCase(TOOL_WEB_URI) ) {
           toolWebUri = value;
       }

       if ( key.equalsIgnoreCase(TOOL_CONTEXT) ) {
           toolContext = value;
       }

       if ( key.equalsIgnoreCase(TOOL_INSERT_SCRIPT_PATH) ) {
           toolInsertScriptPath  = value;
       }

       if ( key.equalsIgnoreCase(TOOL_LIBRARY_INSERT_SCRIPT_PATH) ) {
           toolLibraryInsertScriptPath  = value;
       }

       if ( key.equalsIgnoreCase(TOOL_ACTIVITY_INSERT_SCRIPT_PATH) ) {
           toolActivityInsertScriptPath  = value;
       }

       if ( key.equalsIgnoreCase(TOOL_TABLES_SCRIPT_PATH) ) {
           toolTablesScriptPath  = value;
       }

       if ( key.equalsIgnoreCase(TOOL_TABLES_DELETE_SCRIPT_PATH) ) {
           toolTablesDeleteScriptPath  = value;
       }

       if ( key.equalsIgnoreCase(DEPLOY_FILES) ) {
           deployFiles = convertList(value);
       }

   }
   
   /**
    * Pass through some filename lists to be assigned to a parameter
    */
   protected void setFilenames(String key, ArrayList<String> filenames) throws DeployException {
       if ( key == null )
           throw new DeployException("Invalid parameter: Key is null. ");

       super.setFilenames(key, filenames);

       if ( key.equalsIgnoreCase(LANGUAGE_FILES) ) {
           setLanguageFiles(filenames);
       }
   }

   /**
    * Converts a String to a List. Entries should be comma separated.
    * @param Input string containing entries.
    * @return List of (String) properties, null if not found.
    */
   protected ArrayList<String> convertList(String input) throws DeployException
   {
       String[] strings = input.split(",");
       ArrayList<String> list = new ArrayList<String>(strings.length);
       for ( String value : strings) {
           list.add(value);
       }
       return list;
   }
   
   /** Check that all the correct properties exist - tool delete path is optional. */
   public void validateProperties() throws DeployException {
       boolean valid;
       validationError = ""; // object attribute - will be updated by validateProperty() if something is missing.

       valid = validateStringProperty(toolWebUri, TOOL_WEB_URI);
       valid = valid && validateStringProperty(toolContext, TOOL_CONTEXT);
       valid = valid && validateStringProperty(getLamsEarPath(), LAMS_EAR_PATH);
       valid = valid && validateListProperty(getLanguageFiles(),LANGUAGE_FILES);
       valid = valid && validateStringProperty(toolInsertScriptPath, TOOL_INSERT_SCRIPT_PATH);
       valid = valid && validateStringProperty(toolLibraryInsertScriptPath, TOOL_LIBRARY_INSERT_SCRIPT_PATH);
       valid = valid && validateStringProperty(toolActivityInsertScriptPath, TOOL_ACTIVITY_INSERT_SCRIPT_PATH);
       valid = valid && validateStringProperty(toolWebUri, TOOL_TABLES_SCRIPT_PATH);
       valid = valid && validateStringProperty(getDbUsername(), DB_USERNAME);
       valid = valid && validateStringProperty(getDbPassword(), DB_PASSWORD);
       valid = valid && validateStringProperty(getDbDriverClass(), DB_PASSWORD);
       valid = valid && validateStringProperty(getDbDriverUrl(), DB_DRIVER_URL);
       valid = valid && validateListProperty(deployFiles,DEPLOY_FILES);
       
       if (!valid )
           throw new DeployException("Invalid deployment properties: "+validationError);
   }
   
   /**
    * Upon deserialisation of the xml string, a new object will be created. 
    * The properties of this object will be copied to the calling object.
    * Only copy properties if the properties are not null
    * @param config
    */
   protected void copyProperties(DeployToolConfig config)
   {
	   super.copyProperties(config);
       if (config.getToolSignature() != null)
           this.toolSignature = config.getToolSignature();
       if (config.getToolWebUri() != null)
           this.toolWebUri = config.getToolWebUri();
       if (config.getToolContext() != null)
           this.toolContext = config.getToolContext();
       if (config.getToolInsertScriptPath() != null)
           this.toolInsertScriptPath = config.getToolInsertScriptPath();
       if (config.getToolLibraryInsertScriptPath() != null)
	       this.toolLibraryInsertScriptPath = config.getToolLibraryInsertScriptPath();
       if (config.getToolActivityInsertScriptPath() != null)
	       this.toolActivityInsertScriptPath = config.getToolActivityInsertScriptPath();
       if (config.getToolTablesScriptPath() != null)
	       this.toolTablesScriptPath = config.getToolTablesScriptPath();
       if (config.getToolTablesDeleteScriptPath() != null)
	       this.toolTablesDeleteScriptPath = config.getToolTablesDeleteScriptPath();
       if (config.getDeployFiles() != null)
	       this.deployFiles = config.getDeployFiles();
       if (config.getLanguageFiles() != null)
	       this.setLanguageFiles(config.getLanguageFiles());

   }
   
   /** Used for testing purposes only */
   public void printObjectProperties()
   {
	   super.printObjectProperties();
       System.out.println("Tool Signature: " + this.toolSignature);
       System.out.println("ToolWebUri: " + this.toolWebUri);
       System.out.println("ToolContext: " + this.toolContext);
       System.out.println("ToolInsertScriptPath: " + this.toolInsertScriptPath);
       System.out.println("ToolLibraryInsertScriptPath: " + this.toolLibraryInsertScriptPath);
       System.out.println("ToolActivityInsertScriptPath: " + this.toolActivityInsertScriptPath);
       System.out.println("ToolTableScriptPath: " + this.toolTablesScriptPath);
       System.out.println("ToolTableDeleteScriptPath: " + this.toolTablesDeleteScriptPath);
       ArrayList list = this.deployFiles;
       for(int i=0; i<list.size(); i++)
       {
           System.out.println("DeployFiles: " + list.get(i));
       }
	   list = getLanguageFiles();
	   for(int i=0; i<list.size(); i++)
	   {
	       System.out.println("LanguageFiles: " + list.get(i));
       }
}
   //inherit from parents writePropertiesToFile
 /* public void writePropertiesToFile(Writer writer)
   {
       xstream.toXML(this, writer);       
   } */
  
   /**
     * @return Returns the deployFiles.
     */
    public ArrayList<String> getDeployFiles() {
        return deployFiles;
    }
    /**
     * @param deployFiles The deployFiles to set.
     */
    public void setDeployFiles(ArrayList<String> deployFiles) {
        this.deployFiles = deployFiles;
    }
    /**
     * @return Returns the toolActivityInsertScriptPath.
     */
    public String getToolActivityInsertScriptPath() {
        return toolActivityInsertScriptPath;
    }
    /**
     * @param toolActivityInsertScriptPath The toolActivityInsertScriptPath to set.
     */
    public void setToolActivityInsertScriptPath(
            String toolActivityInsertScriptPath) {
        this.toolActivityInsertScriptPath = toolActivityInsertScriptPath;
    }
    /**
     * @return Returns the toolContextRoot.
     */
    public String getToolContext() {
        return toolContext;
    }
    /**
     * @param toolContextRoot The toolContextRoot to set.
     */
    public void setToolContext(String toolContextRoot) {
        this.toolContext = toolContextRoot;
    }
    /**
     * @return Returns the toolInsertScriptPath.
     */
    public String getToolInsertScriptPath() {
        return toolInsertScriptPath;
    }
    /**
     * @param toolInsertScriptPath The toolInsertScriptPath to set.
     */
    public void setToolInsertScriptPath(String toolInsertScriptPath) {
        this.toolInsertScriptPath = toolInsertScriptPath;
    }
    /**
     * @return Returns the toolLibraryInsertScriptPath.
     */
    public String getToolLibraryInsertScriptPath() {
        return toolLibraryInsertScriptPath;
    }
    /**
     * @param toolLibraryInsertScriptPath The toolLibraryInsertScriptPath to set.
     */
    public void setToolLibraryInsertScriptPath(
            String toolLibraryInsertScriptPath) {
        this.toolLibraryInsertScriptPath = toolLibraryInsertScriptPath;
    }
    /**
     * @return Returns the toolSignature.
     */
    public String getToolSignature() {
        return toolSignature;
    }
    /**
     * @param toolSignature The toolSignature to set.
     */
    public void setToolSignature(String toolSignature) {
        this.toolSignature = toolSignature;
    }
    /**
     * @return Returns the toolTablesDeleteScriptPath.
     */
    public String getToolTablesDeleteScriptPath() {
        return toolTablesDeleteScriptPath;
    }
    /**
     * @param toolTablesDeleteScriptPath The toolTablesDeleteScriptPath to set.
     */
    public void setToolTablesDeleteScriptPath(String toolTablesDeleteScriptPath) {
        this.toolTablesDeleteScriptPath = toolTablesDeleteScriptPath;
    }
    /**
     * @return Returns the toolTablesScriptPath.
     */
    public String getToolTablesScriptPath() {
        return toolTablesScriptPath;
    }
    /**
     * @param toolTablesScriptPath The toolTablesScriptPath to set.
     */
    public void setToolTablesScriptPath(String toolTablesScriptPath) {
        this.toolTablesScriptPath = toolTablesScriptPath;
    }
    /**
     * @return Returns the toolWebUri.
     */
    public String getToolWebUri() {
        return toolWebUri;
    }
    /**
     * @param toolWebUri The toolWebUri to set.
     */
    public void setToolWebUri(String toolWebUri) {
        this.toolWebUri = toolWebUri;
    }

	public ArrayList<String> getLanguageFiles() {
		return languageFiles;
	}

	public void setLanguageFiles(ArrayList<String> languageFiles) {
		this.languageFiles = languageFiles;
	}

 
}
