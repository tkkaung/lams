USE lams;

SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS lams_cr_node_version_property;
DROP TABLE IF EXISTS lams_cr_node_version;
DROP TABLE IF EXISTS lams_cr_node;
DROP TABLE IF EXISTS lams_cr_workspace_tools;
DROP TABLE IF EXISTS lams_cr_tool;
DROP TABLE IF EXISTS lams_cr_workspace;

---
--- Table structure for table 'lams_cr_node'
---

CREATE TABLE lams_cr_node (
  node_id bigint(20) unsigned NOT NULL auto_increment,
  path varchar(255) NOT NULL default '0',
  PRIMARY KEY  (node_id),
  UNIQUE KEY node_id (node_id),
  KEY path (path)
) TYPE=InnoDB COMMENT='The main table containing the node definition';

--
-- Table structure for table 'lams_cr_node_version'
--

CREATE TABLE lams_cr_node_version (
  id bigint(20) unsigned NOT NULL auto_increment,
  node_id bigint(20) unsigned NOT NULL default '0',
  version_id bigint(20) unsigned NOT NULL default '0',
  created_date_time timestamp(14) NOT NULL,
  parent_node_id bigint(20) unsigned default '0',
  parent_version_id bigint(20) unsigned default '0',
  PRIMARY KEY  (id),
  INDEX (node_id, version_id),
  INDEX (parent_node_id),
  CONSTRAINT FK_lams_cr_node_version_1 
  		FOREIGN KEY (parent_node_id) 
  		REFERENCES lams_cr_node (node_id) 
  		ON DELETE NO ACTION ON UPDATE NO ACTION
) TYPE=InnoDB COMMENT='Represents a version of a node';

--
-- Table structure for table 'lams_cr_node_property'
--

CREATE TABLE lams_cr_node_version_property (
  id bigint(20) unsigned NOT NULL auto_increment,
  node_id bigint(20) unsigned NOT NULL default '0',
  version_id bigint(20) unsigned NOT NULL default '0',
  name varchar(255) NOT NULL default '',
  value varchar(255) NOT NULL default '',
  value_type tinyint(3) unsigned NOT NULL default '1',
  PRIMARY KEY  (id),
  UNIQUE KEY id (id),
  INDEX (node_id, version_id),
  CONSTRAINT FK_lams_version_property_1 
  		FOREIGN KEY (node_id, version_id) 
  		REFERENCES lams_cr_node_version (node_id, version_id) 
  		ON DELETE NO ACTION ON UPDATE NO ACTION
) TYPE=InnoDB COMMENT='Records the property for a node';

--
-- Table structure for table 'lams_cr_workspace'
--

CREATE TABLE lams_cr_workspace (
  workspace_id bigint(20) unsigned NOT NULL auto_increment,
  name varchar(255) NOT NULL default '0',
  root_node_id bigint(20) unsigned default '0',
  PRIMARY KEY  (workspace_id),
  UNIQUE KEY workspace_id (workspace_id,name),
  KEY name (name)
) TYPE=InnoDB COMMENT='Content repository workspace';
--
-- Table structure for table 'lams_cr_tool'
--

CREATE TABLE lams_cr_tool (
  tool_id bigint(20) unsigned NOT NULL auto_increment,
  name varchar(255) NOT NULL default '0',
  password varchar(255) NOT NULL default '0',
  PRIMARY KEY  (tool_id),
  UNIQUE KEY id (tool_id,name),
  KEY name (name)
) TYPE=InnoDB COMMENT='Records the identification properties for a tool.';

--
-- Table structure for table 'lams_cr_workspace_tools'
--

CREATE TABLE lams_cr_workspace_tools (
  workspace_id bigint(20) unsigned NOT NULL default '0',
  tool_id bigint(20) unsigned NOT NULL default '0',
  PRIMARY KEY  (tool_id,workspace_id),
  INDEX (tool_id),
  INDEX (workspace_id),
  CONSTRAINT FK_lams_cr_workspace_tools_1 
  		FOREIGN KEY (tool_id) 
  		REFERENCES lams_cr_tool(tool_id) 
  		ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT FK_lams_cr_workspace_tools_2 
  		FOREIGN KEY (workspace_id) 
  		REFERENCES lams_cr_workspace (workspace_id) 
  		ON DELETE NO ACTION ON UPDATE NO ACTION
) TYPE=InnoDB COMMENT='Maps which tools access which workspaces';

SET FOREIGN_KEY_CHECKS=1;
