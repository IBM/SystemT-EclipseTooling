/*******************************************************************************
* Copyright IBM
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
 *******************************************************************************/
package com.ibm.biginsights.project.util;

import org.eclipse.jface.menus.IMenuStateIds;

/**
 * Container for various constants used in this plugin, and which should NOT be
 * externalized.
 * 
 * 
 * 
 */
public class BIConstants {
  
  @SuppressWarnings("unused")
	private static final String _COPYRIGHT = "Copyright IBM\n"+
 "Licensed under the Apache License, Version 2.0 (the \"License\");\n"+
 "you may not use this file except in compliance with the License.\n"+
 "You may obtain a copy of the License at\n\n"+
 "    http://www.apache.org/licenses/LICENSE-2.0\n\n"+
 "Unless required by applicable law or agreed to in writing, software\n"+
 "distributed under the License is distributed on an \"AS IS\" BASIS,\n"+
 "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"+
 "See the License for the specific language governing permissions and\n"+
 "limitations under the License.";

	// Project preferences
	// IMPORTANT note: If you add or remove properties to .biginsights file,
	// do remember to update setDefaultValuesInPreferenceStore() method
	
	// valid versions for BI plugins - need to match version number in biginsights.version file
	public static final String BIGINSIGHTS_VERSION_V12 = "1.2.0.0"; //$NON-NLS-1$
	public static final String BIGINSIGHTS_VERSION_V13 = "1.3.0.0"; //$NON-NLS-1$
	public static final String BIGINSIGHTS_VERSION_V1301 = "1.3.0.1"; //$NON-NLS-1$
	public static final String BIGINSIGHTS_VERSION_V1302 = "1.3.0.2"; //$NON-NLS-1$
	public static final String BIGINSIGHTS_VERSION_V1400 = "1.4.0.0"; //$NON-NLS-1$
	public static final String BIGINSIGHTS_VERSION_V1401 = "1.4.0.1"; //$NON-NLS-1$
	public static final String BIGINSIGHTS_VERSION_V2 = "2.0.0.0"; //$NON-NLS-1$
	public static final String BIGINSIGHTS_VERSION_V21 = "2.1.0.0"; //$NON-NLS-1$
	public static final String BIGINSIGHTS_VERSION_V2101 = "2.1.0.1"; //$NON-NLS-1$
	public static final String BIGINSIGHTS_VERSION_V2110 = "2.1.1.0"; //$NON-NLS-1$
	public static final String BIGINSIGHTS_VERSION_V2120 = "2.1.2.0"; //$NON-NLS-1$
	public static final String BIGINSIGHTS_VERSION_V3000 = "3.0.0.0"; //$NON-NLS-1$
	public static final String BIGINSIGHTS_VERSION_QUALIFIER_YARN = "yarn";
	
	// key of property in biginsights.version file
	public static final String BIGINSIGHTS_VERSION_FILE = "biginsights.version"; //$NON-NLS-1$
	public static final String BIGINSIGHTS_VERSION_ID = "product.version"; //$NON-NLS-1$
	
	// constants for General tab
	public static final String BI_LIBRARIES_VERSION = "bigInsights.librariesVersion"; //$NON-NLS-1$
	public static final String UTF8 = "UTF-8";	//$NON-NLS-1$
	
	// constants for Hadoop
	public static final String HADOOP_EXEC = "org.apache.hadoop.util.RunJar";	//$NON-NLS-1$
	public static final String HADOOP_EXEC_USER_ERROR = "/com.ibm.biginsights.project.help/html/run_cluster_help.html"; //$NON-NLS-1$
	
	// constants for HBase
	public static final String HBASE_SHELL_EXEC = "org.jruby.Main";	//$NON-NLS-1$
	
	// constants for JAQLSearch path 
	public static final String SEARCHPATH_JAQLPATH = "jaql.searchPath";	//$NON-NLS-1$	
	public static final String JAQLFILEPATH_SEPARATOR = ";";	//$NON-NLS-1$
	public static final String JAQL_SCRIPT_NAME = "JAQL";	//$NON-NLS-1$
	public static final String JAQL_FILE_EXTENSION = "jaql";	//$NON-NLS-1$
	public static final String JAQL_SHELL_EXEC = "com.ibm.jaql.util.shell.JaqlShell";	//$NON-NLS-1$
	public static final String HADOOP_NATIVE_LIB_PATH_32 = "/lib/native/Linux-i386-32/"; //$NON-NLS-1$
	public static final String HADOOP_NATIVE_LIB_PATH_64 = "/lib/native/Linux-amd64-64/"; //$NON-NLS-1$
	public static final String HADOOP_NATIVE_LIB_PATH_64ppc = "/lib/native/Linux-ppc64-64/"; //$NON-NLS-1$
	public static final String JAQL_PATH_BIGINSIGHTS_MODULES = "${BIGINSIGHTS_MODULES}"; //$NON-NLS-1$
	public static final String JAQL_PATH_PROJECT_LOC ="${project_loc}";//$NON-NLS-1$
	
	public static final String LOCATION_PARM = "com.ibm.biginsights.shell.serverParm";	//$NON-NLS-1$
	public static final String RUN_LAUNCH_GROUP_ID = "org.eclipse.debug.ui.launchGroup.run";	//$NON-NLS-1$
	
	// constants for JAQL Shell
	public static final String USER_XML_CONFIGURATION = "configuration";	//$NON-NLS-1$
	public static final String USER_XML_PROPERTY = "property";	//$NON-NLS-1$
	public static final String USER_XML_NAME = "name";	//$NON-NLS-1$
	public static final String USER_XML_VALUE = "value";	//$NON-NLS-1$
	public static final String USER_XML_UGI = "hadoop.job.ugi"; ////$NON-NLS-1$
	public static final String JAQL_SHELL_HELP_ID = "/com.ibm.biginsights.jaql.help/html/jaql_launch_shell_help.html"; //$NON-NLS-1$
	public static final String JAQL_SHELL_HELP_ID_STMT = "/com.ibm.biginsights.jaql.help/html/jaql_run_statements_help.html"; //$NON-NLS-1$
	
	// constants for Pig
	public static final String PIG_SCRIPT_NAME = "Pig";	//$NON-NLS-1$
	public static final String PIG_FILE_EXTENSION = "pig";	//$NON-NLS-1$
	public static final String PIG_SHELL_EXEC = "org.apache.pig.Main";	//$NON-NLS-1$
	public static final String PIG_BRIEF_PARAMETER = "pig_brief_parameter"; //$NON-NLS-1$
	public static final String PIG_DOT_PARAMETER = "pig_dot_parameter"; //$NON-NLS-1$
	public static final String PIG_OUT_PARAMETER = "pig_out_parameter"; //$NON-NLS-1$
	public static final String PIG_PARAM_STRING = "pig_param_string"; //$NON-NLS-1$
	public static final String PIG_OUTPUT_DIRECTORY = "pig_output_directory"; //$NON-NLS-1$
	public static final String PIG_PARAM_NAME_VALUE_PAIRS = "pig_param_name_value_pairs"; //$NON-NLS-1$
	
	// constants for BigSheets plugins
	public static final String READER_ABSTRACT_READER = "com.ibm.bigsheets.reader.AbstractReader";			 //$NON-NLS-1$
	public static final String READER_ABSTRACT_TEXT_READER = "com.ibm.bigsheets.reader.AbstractTextReader";	 //$NON-NLS-1$
	
	// library container definition and XML
	public final static String LIB_CONTAINER_DEFINITIONS_PATH = "libcontainerDefinitions";	//$NON-NLS-1$
	public final static String CONTAINER_ID = "com.ibm.biginsights.project.BIGINSIGHTS_LIBS_CONTAINER";	//$NON-NLS-1$
	public final static String CONTAINER_ID_DEFAULT = "DEFAULT";	//$NON-NLS-1$
	public final static String CONTAINER_NAME = "BigInsights";	//$NON-NLS-1$
	public final static String XML_CONTAINER = "container";		//$NON-NLS-1$
	public final static String XML_CONTAINER_VERSION = "version";	//$NON-NLS-1$
	public final static String XML_CONTAINER_ISDEFAULT = "isdefault";	//$NON-NLS-1$
	public final static String XML_BUNDLE = "bundle";	//$NON-NLS-1$
	public final static String XML_BUNDLE_ID = "id";	//$NON-NLS-1$
	public final static String XML_BUNDLE_VERSION = "version";	//$NON-NLS-1$
	public final static String XML_FILE_EXTENSION = ".xml";	//$NON-NLS-1$
	public final static String XML_BUNDLE_ISRUNTIME = "isRuntime"; //$NON-NLS-1$
	public final static String XML_BUNDLE_RUNTIME_VENDOR = "runtimeVendor"; //$NON-NLS-1$

	//BI perspective
	public final static String BI_PERSPECTIVE_ID = "com.ibm.biginsights.project.perspective";	//$NON-NLS-1$
	public final static String LOCATIONS_VIEW_ID = "com.ibm.biginsights.view.locations";	//$NON-NLS-1$
	
	// BI preferences
	public static final String BIGINSIGHTS_PREF_FILE = ".biginsights";  //$NON-NLS-1$	
	public static final String BIGINSIGHTS_FEATURE_VERSION = "com.ibm.biginsights.version"; //$NON-NLS-1$
	
	// BI run configuration values
	public static final String BIGINSIGHTS_LOCATION_KEY = "BIGINSIGHTS_LOCATION_KEY";	//$NON-NLS-1$
	public static final String BIGINSIGHTS_JAQL_PATH = "BIGINSIGHTS_JAQL_PATH";	//$NON-NLS-1$
	// job run configuration submission constants	
	public static final String JOB_PROJECT = "project";	//$NON-NLS-1$
	public static final String JAR_FILE_EXTENSION = ".jar";	//$NON-NLS-1$
	public static final String ZIP_FILE_EXTENSION = ".zip";	//$NON-NLS-1$
	public static final String JAR_LIB_FOLDER = "lib";	//$NON-NLS-1$
	public static final String JOB_JAR = "job_jar"; 	//$NON-NLS-1$
	public static final String JOB_NAME = "job_name";	//$NON-NLS-1$
	public static final String JOB_ARGUMENTS = "job_arguments";	//$NON-NLS-1$
	public static final String JOB_DELETEDIR = "job_deletedir";	//$NON-NLS-1$
	public static final String JOB_CREATE_JAR = "job_createjar";	//$NON-NLS-1$
	public static final String JOB_LAUNCHTYPE = "job_launchtype";	//$NON-NLS-1$
	public static final String JOB_LAUNCHTYPE_RUN_FILE = "job_launchtype_runfile";	//$NON-NLS-1$
	public static final String JOB_LAUNCHTYPE_RUN_STMT = "job_launchtype_runstmt";	//$NON-NLS-1$
	public static final String JOB_LAUNCHTYPE_SHELL = "job_launchtype_shell";	//$NON-NLS-1$
	public static final String JOB_LAUNCHTYPE_EXPLAIN = "job_launchtype_explain";	//$NON-NLS-1$
	public static final String JOB_LAUNCHTYPE_ILLUSTRATE = "job_launchtype_illustrate";	//$NON-NLS-1$	
	public static final String JOB_JAR_CONTENT_FILES = "job_jarcontentfiles";	//$NON-NLS-1$
	public static final String JOB_JAR_CONTENT_JARS = "job_jarcontentjars";	//$NON-NLS-1$
	public static final String JOB_LIST_SEPARATOR = ",";	//$NON-NLS-1$
	public static final String LAUNCH_STATEMENT = "launch_statement"; //$NON-NLS-1$
	public static final String JOB_MESSAGE = "message";	//$NON-NLS-1$
	public static final String JOB_EXECUTION_MODE = "job_mode";	//$NON-NLS-1$
	public static final String JOB_EXECUTION_MODE_LOCAL = "job_mode_local";	//$NON-NLS-1$
	public static final String JOB_EXECUTION_MODE_CLUSTER = "job_mode_cluster";	//$NON-NLS-1$
	public static final String JOB_EXECUTION_MODE_CLUSTER_IS_LOCALLY_INITIATED = "job_mode_cluster_is_loc_init";	//$NON-NLS-1$
	
	// BI location 
	public static final String LOCATION_XML = "location.xml";	//$NON-NLS-1$
	public static final String CONF_ZIP = "conf.zip";	//$NON-NLS-1$
	
	// BI application
	public static final String APP_STATUS_UNDEPLOYED = "NOT DEPLOYED";	//$NON-NLS-1$
	public static final String APP_STATUS_DEPLOYED = "DEPLOYED";	//$NON-NLS-1$
	public static final String APP_FOLDER = "BIApp";	//$NON-NLS-1$
	public static final String APP_ZIP_NAME = "BIApp.zip";	//$NON-NLS-1$
	public static final String APP_DEFAULT_ICON = "defaultApp_L.png";	//$NON-NLS-1$
	
	// BI location XML
	public static final String LOCATION_XML_PROPERTIES = "properties";	//$NON-NLS-1$
	public static final String LOCATION_XML_LOCATION = "location";	//$NON-NLS-1$
	public static final String LOCATION_XML_LOCATION_NAME = "locationName";	//$NON-NLS-1$
	public static final String LOCATION_XML_LOCATION_HIVEPORT = "hivePort";	//$NON-NLS-1$
	public static final String LOCATION_XML_LOCATION_BIGSQLPORT = "bigsqlPort";	//$NON-NLS-1$
	public static final String LOCATION_XML_LOCATION_BIGSQLNODE = "bigsqlNode";	//$NON-NLS-1$
	public static final String LOCATION_XML_LOCATION_BIGSQL2PORT = "bigsql2Port"; //$NON-NLS-1$
	public static final String LOCATION_XML_LOCATION_BIGSQL2NODE = "bigsql2Node"; //$NON-NLS-1$
	public static final String LOCATION_XML_LOCATION_URL = "url";	//$NON-NLS-1$
	public static final String LOCATION_XML_LOCATION_USERNAME = "userName";	//$NON-NLS-1$
	public static final String LOCATION_XML_LOCATION_CONTEXTROOT = "contextRoot";	//$NON-NLS-1$
	public static final String LOCATION_XML_LOCATION_VERSION = "version";		//$NON-NLS-1$
	public static final String LOCATION_XML_LOCATION_VENDOR = "vendor";		//$NON-NLS-1$
	public static final String LOCATION_XML_LOCATION_SAVE_PASSWORD = "savePassword";	//$NON-NLS-1$
	public static final String LOCATION_XML_LOCATION_EGO_MASTER_LIST = "ego_master_list";	//$NON-NLS-1$
	public static final String LOCATION_XML_LOCATION_EGO_KD_PORT = "ego_kd_port";	//$NON-NLS-1$
	public static final String LOCATION_XML_LOCATION_EGO_SEC_PLUGIN = "ego_sec_plugin";	//$NON-NLS-1$
	public static final String LOCATION_XML_LOCATION_ROLES = "roles";	//$NON-NLS-1$
	public static final String LOCATION_XML_LOCATION_ROLE = "role";	//$NON-NLS-1$
	public static final String LOCATION_XML_USER_ID_KEY = "userid"; //$NON-NLS-1$
	public static final String LOCATION_XML_PASSWORD_KEY = "password"; //$NON-NLS-1$
	public static final String LOCATION_XML_VIEWBY = "viewby"; //$NON-NLS-1$
	public static final String LOCATION_XML_LOCATION_FILESYSTEM = "filesystem"; //$NON-NLS-1$
	public static final String LOCATION_XML_LOCATION_MOUNTPOINT = "mountpoint"; //$NON-NLS-1$
	public static final String LOCATION_XML_LINUXTASKCONTOLLER_USER = "ltcuser"; //$NON-NLS-1$
	public static final String LOCATION_XML_VENDOR_IBM = "ibm";  //$NON-NLS-1$
	public static final String LOCATION_XML_VENDOR_SYMPHONY = "psmr";  //$NON-NLS-1$
	public static final String LOCATION_XML_LOCATION_YARN = "yarn";
	
	//server location view - View By menu choices
	public static int VIEWBY_NAME = 1;
	public static int VIEWBY_CATEGORY = 2;
	public static int VIEWBY_TYPE = 3;
	public static String VIEWBY_TYPE_WORKFLOW = "WORKFLOW"; //$NON-NLS-1$
	public static String VIEWBY_TYPE_BIGSHEETSPLUGIN = "BIGSHEETSPLUGIN"; //$NON-NLS-1$
	public static String VIEWBY_TYPE_JAQLMODULE = "JAQLMODULE"; //$NON-NLS-1$
	public static String VIEWBY_TYPE_WORKFLOW_BIGSHEETSPLUGIN = "WORKFLOW,BIGSHEETSPLUGIN"; //$NON-NLS-1$
	public static String VIEWBY_SUBTYPE_TEXTANALYTICS = "TEXTANALYTICS"; //$NON-NLS-1$
	public static String VIEWBY_SUBTYPE_MACHINELEARNING = "MACHINELEARNING"; //$NON-NLS-1$
	public static final String LOCATIONS_NEWBUTTONID = "com.ibm.biginsights.project.locations.newbutton"; //$NON-NLS-1$
	public static final String LOCATIONS_NEWMENUID = "com.ibm.biginsights.project.locations.newmenu"; //$NON-NLS-1$
		
	// BI web server
	public static final String URL_CONTEXT_ROOT = "/data";	//$NON-NLS-1$
	public static final String URL_DIRECT_ROOT = "/html";	//$NON-NLS-1$
	public static final String HTTP = "http";	//$NON-NLS-1$
	public static final String HTTPS = "https";		//$NON-NLS-1$
	public static final String CONTENT_TYPE = "Content-type";	//$NON-NLS-1$
	public static final String CONTENT_TYPE_JAVA_ARCHIVE = "application/java-archive"; 	//$NON-NLS-1$
	public static final String URL_APPLICATIONS = "/controller/catalog/applications";	//$NON-NLS-1$
	public static final String URL_APPLICATIONS_CATEGORIES = "/controller/catalog/applications/categories";	//$NON-NLS-1$
	public static final String URL_APPLICATIONS_BYCATEGORY = "/controller/catalog/applications/bycategory";	//$NON-NLS-1$
	public static final String URL_APPLICATIONS_BYTYPE = "/controller/catalog/applications/byassettype";	//$NON-NLS-1$
	public static final String URL_APPLICATIONS_NAMES = "/controller/catalog/applications/names";	//$NON-NLS-1$
	public static final String URL_JAQLMODULES_NAMES = "/controller/catalog/applications/jaqlmodules";	//$NON-NLS-1$
	public static final String URL_JOBTRACKER = "/controller/JobTracker";	//$NON-NLS-1$
	public static final String URL_DELETEDIR = "/controller/dfs";	//$NON-NLS-1$
	public static final String URL_FOLDER_INFO = "/controller/dfs/user/";	//$NON-NLS-1$
	public static final String URL_INDEX_HTML = "/index.html";	//$NON-NLS-1$
	public static final String URL_CONFIGURATIONS = "/controller/configuration";	//$NON-NLS-1$
	public static final String URL_CONFIGURATIONS_VERSION = "/controller/configuration/getVersion";	//$NON-NLS-1$
	public static final String URL_CONFIGURATIONS_VENDOR = "/controller/configuration/getVendor";	//$NON-NLS-1$
	public static final String URL_HADOOP_VERSION = "/controller/configuration/getHadoopVersion";	//$NON-NLS-1$
	public static final String URL_HADOOP_UGI = "/controller/configuration/getUgi";	//$NON-NLS-1$
	public static final String URL_JOBTRACKER_ACTION_TYPE = "actiontype";	//$NON-NLS-1$
	public static final String URL_JOBTRACKER_JAR_FILE = "jarFile";	//$NON-NLS-1$
	public static final String URL_JOBTRACKER_MAIN_CLASS = "mainClass";	//$NON-NLS-1$
	public static final String URL_JOBTRACKER_JOB_NAME = "jobName";	//$NON-NLS-1$
	public static final String URL_JOBTRACKER_CREATE_JAR_JOB = "createJarJob";	//$NON-NLS-1$
	public static final String URL_JOBTRACKER_UPLOAD_JAR = "uploadJar";	//$NON-NLS-1$
	public static final String URL_JOBTRACKER_JAR_ARGUMENTS = "jarArguments";	//$NON-NLS-1$
	public static final String URL_JOBTRACKER_JOB_ID = "jobid";	//$NON-NLS-1$
	public static final String URL_USERINFO = "/controller/AuthenticationAction?actiontype=getUserInfo"; //$NON-NLS-1$
	public static final String URL_HIVEINFO = "/controller/ClusterStatus/hive.json"; //$NON-NLS-1$
	public static final String URL_BIGSQLINFO = "/controller/ClusterStatus/bigSQL.json"; //$NON-NLS-1$
	public static final String URL_ECLIPSE_PROJECTS = "/controller/DataManagement?action=getsampleapps"; //$NON-NLS-1$
	public static final String URL_FILEDOWNLOAD = "/controller/FileDownload"; //$NON-NLS-1$	
	
	public static final String ROLE_VALUE = "value";	//$NON-NLS-1$
	// BI roles must be kept in synch with /InstallConfiguration/src/com/ibm/biginsights/install/config/InstallConfigurationConstants.java
	public static final String ROLE_BIGINSIGHTS_SYSTEM_ADMINISTRATOR = "BigInsightsSystemAdministrator";	//$NON-NLS-1$
	public static final String ROLE_BIGINSIGHTS_DATA_ADMINISTRATOR = "BigInsightsDataAdministrator";	//$NON-NLS-1$
	public static final String ROLE_BIGINSIGHTS_APPLICATION_ADMINISTRATOR = "BigInsightsApplicationAdministrator";	//$NON-NLS-1$
	public static final String ROLE_BIGINSIGHTS_SECURITY_ADMINISTRATOR = "BigInsightsSecurityAdministrator";	//$NON-NLS-1$
	public static final String ROLE_BIGINSIGHTS_DEVELOPER = "BigInsightsDeveloper";	//$NON-NLS-1$
	public static final String ROLE_BIGINSIGHTS_USER = "BigInsightsUser";	//$NON-NLS-1$
	
	// Hive pre-req check
	public static final String ORG_ECLIPSE_DATATOOLS_SQLDEVTOOLS_FEATURE_ID = "org.eclipse.datatools.sqldevtools.feature"; //$NON-NLS-1$
	public static final String ORG_ECLIPSE_DATATOOLS_SQLDEVTOOLS_FEATURE_MIN_VERSION = "1.7.0"; //$NON-NLS-1$
	
	//JAQL Editor
    static public String MARKER_TYPE_JAQL = "com.ibm.biginsights.jaql.editor.jaqlerror";//$NON-NLS-1$
    static public String STORE_MARKER_KEY = "org.eclipse.ui.commands/state/com.ibm.biginsights.jaql.editor.command.togglemarkers/STYLE"; //$NON-NLS-1$
    static public String PREVIOUSLY_SET_STORE_MARKER_KEY = "com.ibm.biginsights.jaql.editor.command.togglemarkers/KEY_PREVIOUSLY_SET"; //$NON-NLS-1$
    static public String MARKER_TOGGLE_STATE = IMenuStateIds.STYLE;
    static public String MARKER_TOGGLE_COMMANDID = "com.ibm.biginsights.jaql.editor.command.togglemarkers"; //$NON-NLS-1$
    
    // sample projects
    public static String SAMPLE_PROJECT_SUFFIX = "_eclipse.zip"; //$NON-NLS-1$
    public static String MDA_PROJECT_SUFFIX = "-mach_eclipse.zip"; //$NON-NLS-1$
    public static String SDA_PROJECT_SUFFIX = "-soc_eclipse.zip"; //$NON-NLS-1$
    public static String DATA_ACQUISITION_APP_NAME = "Data Download"; //$NON-NLS-1$  
    
    //psmr-related
    public static String PSMR_EGO_MASTER_LIST = "EGO_MASTER_LIST";
    public static String PSMR_EGO_KD_PORT = "EGO_KD_PORT";
    public static String PSMR_EGO_SEC_PLUGIN = "EGO_SEC_PLUGIN";
	public static int PSMR_DEFAULT_EGO_KD_PORT = 7870;
	public static String PSMR_DEFAULT_EGO_SEC_PLUGIN = "sec_ego_default";
	public static String PSMR_CONF_FILE = "ego.conf";

}
