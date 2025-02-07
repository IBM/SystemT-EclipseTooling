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
package com.ibm.biginsights.project.launch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaLaunchDelegate;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.Workbench;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.ibm.avatar.algebra.util.file.FileUtils;
import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.BigInsightsLibraryContainerInitializer;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.BigInsightsLocation.TestConnectionResult;
import com.ibm.biginsights.project.locations.IBigInsightsLocation;
import com.ibm.biginsights.project.locations.LocationRegistry;
import com.ibm.biginsights.project.util.BIConstants;
import com.ibm.biginsights.project.util.BIProjectPreferencesUtil;

@SuppressWarnings("restriction")
public class BigInsightsLaunchDelegate extends JavaLaunchDelegate
{
  @SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+          //$NON-NLS-1$
    "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

	protected boolean isLocalMode = false;
	protected boolean isLocallyInitiated = false;	
	protected String mainClass;
	protected String deleteDir;
	protected String hadoopProxyUser = null;
	protected String launchType;
	protected IBigInsightsLocation location = null;
	

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
	
		// only use JOB_EXECUTION_MODE if mode==run, otherwise it's implicit that we run locally
		isLocalMode = ILaunchManager.DEBUG_MODE.equals(mode) ||
				BIConstants.JOB_EXECUTION_MODE_LOCAL.equals(configuration.getAttribute(BIConstants.JOB_EXECUTION_MODE, "")); //$NON-NLS-1$		
		mainClass = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, ""); //$NON-NLS-1$
		deleteDir = configuration.getAttribute(BIConstants.JOB_DELETEDIR, ""); //$NON-NLS-1$
		launchType = configuration.getAttribute(BIConstants.JOB_LAUNCHTYPE, BIConstants.JOB_LAUNCHTYPE_RUN_FILE);
		isLocallyInitiated = configuration.getAttribute(BIConstants.JOB_EXECUTION_MODE_CLUSTER_IS_LOCALLY_INITIATED, false); //$NON-NLS-1$
		location = null;
		
		if (isLocalMode){
			/* in local mode:
			 * 	- delete the directory if specified
				- run the main class with parameters	
				- will also come here when JOB_LAUNCHTYPE is JOB_LAUNCHTYPE_EXPLAIN, but then deleteDir is null			
			 */			
			
			// delete local directory and all its subfolders if specified
			boolean deleteSuccess = true;
			if (deleteDir!=null && !deleteDir.isEmpty()) {
				File f = FileUtils.createValidatedFile(deleteDir);
				if (f.exists()) {
					try {
						deleteSuccess = deleteFile(f);
						if (!deleteSuccess) {
							final String message = Messages.bind(Messages.BIGINSIGHTS_DELETEDIR_ERROR, deleteDir);
							Display.getDefault().asyncExec(new Runnable() {								
								public void run() {
									MessageDialog.openError(Display.getCurrent().getActiveShell(), 
											Messages.BIGINSIGHTSLAUNCHDELEGATE_ERROR_TITLE, message);																	
								}});													
						}
					}
					catch (SecurityException ex) {
						final String message = Messages.bind(Messages.BIGINSIGHTS_DELETEDIR_ERROR, deleteDir )+"\n\n"+ex.getMessage(); //$NON-NLS-1$
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.BIGINSIGHTSLAUNCHDELEGATE_ERROR_TITLE, message);																	
							}});
					}
				}
			}

			if (deleteSuccess) {
				executeProgramInLocalMode(configuration, mode, launch, monitor);				
			}
		}
		else {								
			String locationKey = configuration.getAttribute(BIConstants.BIGINSIGHTS_LOCATION_KEY, ""); //$NON-NLS-1$		
	
			// need to make sure that the location still exists		
			location = (locationKey!=null && !locationKey.isEmpty()) ? 
											LocationRegistry.getInstance().getLocation(locationKey) : null;
			if (location==null) {
				// location that is referenced by the run config was deleted - open the run config dialog again
				final ILaunchConfiguration conf = configuration;
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.BIGINSIGHTSLAUNCHDELEGATE_ERROR_TITLE, Messages.BIGINSIGHTSLAUNCHDELEGATE_ERROR_DESC);
						DebugUITools.openLaunchConfigurationDialog(Display.getCurrent().getActiveShell(), conf, BIConstants.RUN_LAUNCH_GROUP_ID, null);
					}});				
				return;
			}	
			else if (isLocallyInitiated || BIConstants.JOB_LAUNCHTYPE_SHELL.equals(launchType)) { // all shell launches need to check for user
				// only do security checks when locally initiated; will be the case for pig, jaql, java, but not map-reduce
				if (location.getVersion().contains(BIConstants.BIGINSIGHTS_VERSION_V13)){
				// set the hadoop.job.ugi value only if the server is on 1.3.0.0 level, it's not supported anymore for 1.3FP1 and Hadoop 1.0
				// get the ugi first because it's not persisted
				String hadoopUgi = location.retrieveUgi();
				if (hadoopUgi!=null)
					updateConfigurationXMLFile(location, hadoopUgi);
				else
					return;
				}
				else {
					// since we run program from eclipse directly, need to do some security checks due to the new security features in Hadoop1.0
					// 0. if no security is set up or no linuxtaskcontroller, don't change anything (currently, linuxtaskcontroller is always set up with install options 1 and 2) 
					// 1. If the OS user is the same as the one we set up in BI server connection, allow connection.
					// 2. If the OS user is the user that can impersonate others as set up in the LinuxTaskController (check core-site.xml), and  
					//    the user is different from the one we set up to connect to the BI server, impersonate the bi user and connect
					// 3. If the OS user is different from BI server user and can't impersonate others, don't allow connection.
					String osUser = System.getProperty("user.name"); //$NON-NLS-1$
					if (location.getLinuxTaskControllerUsers()!=null && location.getLinuxTaskControllerUsers().length>0) {
						
						if (location.isLinuxTaskControllerUser(osUser) && location.getUserName()!=null && !location.getUserName().equals(osUser)) {
							// case 2
							hadoopProxyUser = location.getUserName();
						}
						else if(location.getUserName()!=null && !location.getUserName().equals(osUser)){
							// case 3							
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {		
									String locUserName = location.getUserName()==null ? System.getProperty("user.name") : location.getUserName(); //$NON-NLS-1$
									RunErrorDialog dlg = new RunErrorDialog(Display.getCurrent().getActiveShell(), 
											Messages.LAUNCHCONFIGURATIONDELEGATE_ERROR_TITLE, null, 
											Messages.bind(Messages.LAUNCHCONFIGURATIONDELEGATE_ERROR_USER, 
													System.getProperty("user.name"), locUserName),  //$NON-NLS-1$
													MessageDialog.ERROR, new String[]{IDialogConstants.OK_LABEL}, 0, 
													Messages.LAUNCHCONFIGURATIONDELEGATE_ERROR_USER_HELP_STMT, 
													BIConstants.HADOOP_EXEC_USER_ERROR);	
									dlg.open();
								}});				
							return;							
						}
					}
				}	
				// 54934: do a test connection here to force password validation, otherwise might be security issue
				final TestConnectionResult testConnectionResult = location.testConnection(false);
				if (!testConnectionResult.success) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {	
							MessageDialog.openError(Workbench.getInstance().getActiveWorkbenchWindow().getShell(), Messages.LOCATIONTESTCONNECTIONACTION_ERROR_TITLE, 
									Messages.LOCATIONTESTCONNECTIONACTION_ERROR_DESC+"\n"+ //$NON-NLS-1$
									(testConnectionResult.statusCode>-1 ? (testConnectionResult.statusCode+": "+testConnectionResult.statusMessage) : testConnectionResult.statusMessage)); //$NON-NLS-1$							
						}});
					return;					
				}
				
				// Thomas: comment out as part of defect 40564
				//if the user's folder does not exist on the server
//				try {
//					if(!location.doesUserFolderExistOnServer()){
//						Display.getDefault().asyncExec(new Runnable() {
//							public void run() {	
//								String user = location.getUserName();
//								if(user == null){
//									user = System.getProperty("user.name"); //os user
//								}
//								MessageBox messageBox = new MessageBox(Workbench.getInstance().getActiveWorkbenchWindow().getShell(), SWT.ERROR);
//								messageBox.setText(Messages.LAUNCHCONFIGURATIONDELEGATE_ERROR_TITLE);
//								messageBox.setMessage(Messages.bind(Messages.LAUNCHCONFIGURATIONDELEGATE_ERROR_USER_FOLDER_DOESNOTEXIST, user));
//								messageBox.open();											
//								
//							}});				
//						return;		
//					}
//				} catch (BIConnectionException e) {
//					location.handleBIConnectionExceptionFromThread(e);
//					// don't continue if connection error happened
//					return;
//				}
			}
			if (locationKey!=null && mainClass!=null) {	
				executeProgramInClusterMode(configuration, mode, launch, monitor, location);
			}
		}
	}
	
	protected void executeProgramInLocalMode(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		super.launch(configuration, mode, launch, monitor);
	}

	protected void executeProgramInClusterMode(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor, IBigInsightsLocation location) throws CoreException {
		super.launch(configuration, mode, launch, monitor);
	}

	public String getVMArguments(ILaunchConfiguration configuration) throws CoreException {
		ArrayList<String> newVMArguments = new ArrayList<String>();				
		String vmArguments = super.getVMArguments(configuration);	
		newVMArguments.add("-Dhadoop.tmp.dir=\""+System.getProperty("user.home")+"\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (vmArguments!=null && !vmArguments.isEmpty())
			newVMArguments.add(vmArguments); 				
		
		StringBuffer result = new StringBuffer();
		for (String s:newVMArguments) {
			result.append(s).append(" ");
		}
					
		return result.toString();
	}

	public String[] getClasspath(ILaunchConfiguration configuration) throws CoreException {
		String[] currentClasspath = super.getClasspath(configuration);		
		String[] result = new String[currentClasspath.length+3];
	
		if (location!=null) { // in local mode, nothing will be added to classpath
			for (int i=0; i<currentClasspath.length; i++) {
				result[i] = currentClasspath[i];
			}

			result[currentClasspath.length] = LocationRegistry.getJAQLConfLocation(location);
			result[currentClasspath.length+1] = LocationRegistry.getHadoopConfLocation(location);
			result[currentClasspath.length+2] = LocationRegistry.getHBaseConfLocation(location);

		}
		else 
			result = currentClasspath;
		
		return result;
	}
	
	/**
	 * Delete directory recursively.
	 * @param file: File or directory to delete
	 * @return true of no error; false if any error happened during delete
	 */
	private boolean deleteFile(File file) throws SecurityException {
		boolean result = true;
		if (file.isDirectory()) {
			for (File child:file.listFiles()) {
				result = deleteFile(child);
				if (!result)
					break;
			}
		}
		
		if (result)
			result = file.delete();
			
		return result;
	}

	public String[] getEnvironment(ILaunchConfiguration configuration) throws CoreException {
		// first get all the environment variables 
		// returns a list with all user-specified variables and all native env variables if append was set to true
		// if the user specified an env var name that is similar to the native env variable, the user's variable will override the native one 
		// and even the one we want to specify (this is done so the user can override our values in case they want to try something)	
		// if the user hasn't overriden our variables and a system var exists with the same name, except for PATH
		String[] env = super.getEnvironment(configuration);	
		// keys of user-defined variables
		Map<String, String> userDefinedEnvVariables = configuration.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, new HashMap<String, String>());
		
		// windows check is needed for additional parameters for symphony and system_root
		boolean isWindows = System.getProperty("os.name").toLowerCase().indexOf("win") > -1; //$NON-NLS-1$ //$NON-NLS-2$

		// newParameters: list of parameters that will be returned by this method
		HashMap<String, String>newParameters = new HashMap<String, String>();
		// add all environment variables to list first
		if (env!=null && env.length>0) {
			for (String entry:env) {
				int split = entry.indexOf("="); // split n the first = (there could be = in the value)
				newParameters.put(entry.substring(0, split), entry.substring(split+1));
			}
		}		
		
		// for Windows, always add the PATH (because of cygwin/bin in PATH and also TEMP (for temp directory), but only if not added by the System
		if (isWindows) {
			if (!iSystemDefinedVar("PATH", newParameters.keySet(), userDefinedEnvVariables.keySet())) {
				// if PATH was added by user (no matter if appended to system path or not), append the path again anyway, otherwise cygwin/bin may not be there if user has set it as system env var
				addEnvVariable(newParameters, "PATH", System.getenv("PATH"), false, false, true, userDefinedEnvVariables.keySet());
			}
			if (!iSystemDefinedVar("TEMP", newParameters.keySet(), userDefinedEnvVariables.keySet())) {
				// if TEMP is not there as env var, need to set it, otherwise jaql can't create temp dir (don't replace or append if user set TEMP)
				addEnvVariable(newParameters, "TEMP", System.getenv("TEMP"), false, false, false, userDefinedEnvVariables.keySet());
			}
		}
				
		// this method is called:
		// - in local/server mode for: Pig, Jaql, BigInsights Java app
		// - in shell mode: jaql, pig, hbase shell
		// - in local mode: Java map/reduce
		// this method is not called in Java map/reduce server mode		

		IProject project = null;
		String version = null;
		if (location==null) {
			project = ResourcesPlugin.getWorkspace().getRoot().getProject(configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "")); //$NON-NLS-1$
			version = BIProjectPreferencesUtil.getBigInsightsLibrariesVersion(project);
		}
		else 
			version = location.getVersionWithVendor();
		
		if (version.startsWith("v") || version.startsWith("V"))			 //$NON-NLS-1$ //$NON-NLS-2$
			version = version.substring(1);
		
		if (BIProjectPreferencesUtil.isAtLeast(version, BIConstants.BIGINSIGHTS_VERSION_V1301) || hadoopProxyUser!=null) {									
			// 		
			// add HADOOP_PROXY_USER if needed
			if (hadoopProxyUser!=null)
				addEnvVariable(newParameters, "HADOOP_PROXY_USER", hadoopProxyUser, true, false, false, userDefinedEnvVariables.keySet()); //$NON-NLS-1$		// override HADOOP_PROXY_USER if defined in the system		

			// add TEXTANALYTICS_HOME variable after V1.3GA: needs to point to root of biginsights-shared-libs
			// will be done right when I add the HADOOP_PROXY_USER to the env variables
			if (!version.contains(BIConstants.BIGINSIGHTS_VERSION_V13)) {
				if (LocationRegistry.getHadoopHomeDirectory(version)!=null)
					addEnvVariable(newParameters, "HADOOP_HOME", LocationRegistry.getHadoopHomeDirectory(version), true, false, false, userDefinedEnvVariables.keySet()); //$NON-NLS-1$ // override HADOOP_HOME if defined in the system				
				if (isWindows)
					addEnvVariable(newParameters, "SYSTEMROOT", System.getenv("SystemRoot"), false, false, false, userDefinedEnvVariables.keySet()); //$NON-NLS-1$ //$NON-NLS-2$ // if SYSTEMROOT is defined already, don't replace or append to it					
				if (LocationRegistry.getTextAnalyticsHomeDirectory(version)!=null)
					addEnvVariable(newParameters, "TEXTANALYTICS_HOME", LocationRegistry.getTextAnalyticsHomeDirectory(version), true, false, false, userDefinedEnvVariables.keySet()); //$NON-NLS-1$ // override TEXTANALYTICS_HOME if defined in the system
				if (LocationRegistry.getJAQLSystemSearchPath(version)!=null)
					addEnvVariable(newParameters, "JAQL_SEARCH_PATH", LocationRegistry.getJAQLSystemSearchPath(version), true, false, false, userDefinedEnvVariables.keySet()); //$NON-NLS-1$ // override JAQL_SEARCH_PATH if defined in the system
				if (LocationRegistry.getJAQLHomeDirectory(version)!=null) {					
					addEnvVariable(newParameters, "JAQL_HOME", LocationRegistry.getJAQLHomeDirectory(version), true, false, false, userDefinedEnvVariables.keySet()); //$NON-NLS-1$ // override JAQL_HOME if defined in the system
				}
				if (LocationRegistry.getHBasePath(version)!=null)
					addEnvVariable(newParameters, "HBASE_HOME", LocationRegistry.getHBasePath(version), true, false, false, userDefinedEnvVariables.keySet()); //$NON-NLS-1$  // override HBASE_HOME if defined in the system
				
				if (BIProjectPreferencesUtil.isAtLeast(version, BIConstants.BIGINSIGHTS_VERSION_V2)) {
					if (LocationRegistry.getHivePath(version)!=null)
						addEnvVariable(newParameters, "HIVE_HOME", LocationRegistry.getHivePath(version), true, false, false, userDefinedEnvVariables.keySet()); //$NON-NLS-1$ // override HIVE_HOME if defined in the system
					if (LocationRegistry.getHCatalogPath(version)!=null)
						addEnvVariable(newParameters, "HCATALOG_HOME", LocationRegistry.getHCatalogPath(version), true, false, false, userDefinedEnvVariables.keySet()); //$NON-NLS-1$ // override HCATALOG_HOME if defined in the system
				}
				
				// set LD_LIBRARY_PATH for GPFS; prepend LD_LIBRARY PATH to system (similar to LD_LIBRARY_PATH for PSMR)
				if (location!=null && location.isGPFSMounted()) { 
					String osArch = System.getProperty("os.arch");
					String path = LocationRegistry.getHadoopLocation(location)+((osArch!=null && osArch.startsWith("ppc")) ? BIConstants.HADOOP_NATIVE_LIB_PATH_64ppc : BIConstants.HADOOP_NATIVE_LIB_PATH_64);
					addEnvVariable(newParameters, "LD_LIBRARY_PATH", path, false, true, false, userDefinedEnvVariables.keySet());
				}
			}
			
			// set PSMR vm arguments in server and shell modes only		
			if (!isLocalMode && location.isSymphony()){	
				addEnvVariable(newParameters, BIConstants.PSMR_EGO_KD_PORT, Integer.toString(location.getEGO_KD_PORT()), true, false, false, userDefinedEnvVariables.keySet());
				addEnvVariable(newParameters, BIConstants.PSMR_EGO_MASTER_LIST, location.getEGO_MASTER_LIST(), true, false, false, userDefinedEnvVariables.keySet());
				addEnvVariable(newParameters, BIConstants.PSMR_EGO_SEC_PLUGIN, location.getEGO_SEC_PLUGIN(), true, false, false, userDefinedEnvVariables.keySet());
				addEnvVariable(newParameters, "PMR_HOME", BigInsightsLibraryContainerInitializer.getInstance().getSymphonyMapReduceHome(location.getVersion(), false), true, false, false, userDefinedEnvVariables.keySet());				
				addEnvVariable(newParameters, "SOAM_HOME", BigInsightsLibraryContainerInitializer.getInstance().getSymphonyHome(location.getVersion(), false), true, false, false, userDefinedEnvVariables.keySet());				// 
				if (isWindows) { // append PATH so that user's path takes precedence; no symphony server on Windows, so save to do so		
					addEnvVariable(newParameters, "PATH", BigInsightsLibraryContainerInitializer.getInstance().getSymphonyNativeLibraryPath(location.getVersion()), false, false, true, userDefinedEnvVariables.keySet());
				}
				else // prepend LD_LIBRARY PATH to system, in case eclipse runs on same machine as where BI with symphony is installed
					addEnvVariable(newParameters, "LD_LIBRARY_PATH", BigInsightsLibraryContainerInitializer.getInstance().getSymphonyNativeLibraryPath(location.getVersion()), false, true, false, userDefinedEnvVariables.keySet());				
			}			

		}			
		
		// convert the hashmap to array		
		ArrayList<String> elements = new ArrayList<String>(newParameters.size());
		for (Entry<String, String> entry:newParameters.entrySet()) {
			StringBuffer stringBuffer = new StringBuffer(entry.getKey());
			stringBuffer.append('=').append(entry.getValue());
			elements.add(stringBuffer.toString());
		}
		// System.out.println("newParameters:\n"+Arrays.toString(elements.toArray()));
		
		return (newParameters!=null && !newParameters.isEmpty()) ? elements.toArray(new String[elements.size()]) : env;
	}
	
	private void addEnvVariable(HashMap<String, String> map, String newValueKey, String newValueValue, boolean replaceSystemEnv, boolean prepend, boolean append, Set<String> userDefinedVars) {
		// override system env variables, but if the user specified the same variable in the run config, 
		// that takes precedence over ours (user might want to try out something)	
		boolean foundExistingValue = false;		
		for (Entry<String, String> entry:map.entrySet()) {        	
            String key = entry.getKey();
            if (isSameEnvName(key, newValueKey)) {	// variable is defined already, either by system or by user
            	if (isUserDefinedVar(key, userDefinedVars)) {
            		// user defined it, only prepend or append, but don't replace
            		if (prepend) // prepend to existing value (LD_LIBRARY_PATH)
                		entry.setValue(newValueValue+File.pathSeparator+entry.getValue());
                	else if (append) // append to existing value (PATH)
                		entry.setValue(entry.getValue()+File.pathSeparator+newValueValue);
            	}
            	else if (replaceSystemEnv)            
            		entry.setValue(newValueValue);
            	else if (prepend) // prepend to existing value (LD_LIBRARY_PATH)
            		entry.setValue(newValueValue+File.pathSeparator+entry.getValue());
            	else if (append) // append to existing value (PATH)
            		entry.setValue(entry.getValue()+File.pathSeparator+newValueValue);
            	foundExistingValue = true;
                break;
            }
        }
        
        // only add if no existing value found
        if (!foundExistingValue) {
        	map.put(newValueKey, newValueValue);
        }		
	}
	
	private boolean isUserDefinedVar(String varName, Set<String> userDefinedVars) {
		boolean result = false;
		for (String userDefinedVar:userDefinedVars) {
			if (isSameEnvName(varName, userDefinedVar))
			{
				result = true;
				break;
			}
		}
		
		return result;
	}

	private boolean iSystemDefinedVar(String varName, Set<String>allEnvVariables, Set<String> userDefinedVars) {
		boolean result = false;
		for (String systemVar:allEnvVariables) {
			if (isSameEnvName(varName, systemVar))
			{
				// env var defined, now check whether it was defined by user 
				result = isUserDefinedVar(varName, userDefinedVars);				
				break;
			}
		}		
		return result;
	}

	private boolean isSameEnvName(String varName1, String varName2) {
		// for windows ignore case, or linux compare exact case
		boolean isWindows = System.getProperty("os.name").toLowerCase().indexOf("win") > -1; //$NON-NLS-1$ //$NON-NLS-2$
		if ((isWindows && varName1.equalsIgnoreCase(varName2)) ||
			(!isWindows && varName1.equals(varName2))) 
		{
			return true;
		}
		else
			return false;
		
	}

	// method that updates core-site.xml for the location with the hadoop.job.ugi value 
	private void updateConfigurationXMLFile(IBigInsightsLocation location, String hadoopUgi) {		
		
		if (location!=null && hadoopUgi!=null) {
			// file is stored in .metadata/com.ibm.biginsights.project/locations/<locationName>/hadoop-conf/core-site.xml
			String hadoopConfLocation = LocationRegistry.getHadoopConfLocation(location);
			File coreSiteFile = FileUtils.createValidatedFile(hadoopConfLocation+"/core-site.xml"); ////$NON-NLS-1$			
			
			// make file writable
			coreSiteFile.setWritable(true);
			
			Document document = null;			
			DocumentBuilder builder;
			try {
				builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				document = builder.parse(coreSiteFile);
			} catch (Exception e) { 
				Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
				return;
			}
			
			FileOutputStream fileOutputStream = null;
			try {
				fileOutputStream = new FileOutputStream(coreSiteFile);
			} catch (FileNotFoundException e) {
				Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));						
			}
			/*
			<configuration>
			  <property>
			    <name>hadoop.job.ugi</name>
			    <value>value</value>
			  </property>
			</configuration>
			*/
			try {				
				Element root = document.getDocumentElement();
				if (BIConstants.USER_XML_CONFIGURATION.equals(root.getTagName()))
				{									
					// first remove existing node with hadoop.job.ugi name
					Node nodeToRemove = null;
					NodeList nodeList = root.getChildNodes();  // list of all <property> nodes 
					for (int i=0;i<nodeList.getLength();i++)
					{
						Node elementNode = nodeList.item(i);
						if (elementNode instanceof Element)
						{
							NodeList propertyNodes = elementNode.getChildNodes();  // returns name and value nodes
							for (int j=0;j<propertyNodes.getLength();j++)
							{
								Node propertyNode = propertyNodes.item(j);
								if (propertyNode instanceof Element)
								{
									Element prop = (Element)propertyNode;
									if (prop.getTagName().equals(BIConstants.USER_XML_NAME) && prop.hasChildNodes()) {							
										String name = ((Text)prop.getFirstChild()).getData();
										if (BIConstants.USER_XML_UGI.equals(name)) {
											nodeToRemove = elementNode;
											break;
										}
									}
								}
							}
						}
					}
					if (nodeToRemove!=null) {
						root.removeChild(nodeToRemove);
					}

					// then add a new node with the hadoop.job.ugi property					
					Element propertiesNode = document.createElement(BIConstants.USER_XML_PROPERTY);					
					root.appendChild(propertiesNode);
					propertiesNode.appendChild(document.createTextNode("\n")); //$NON-NLS-1$
					
					Element nameNode = document.createElement(BIConstants.USER_XML_NAME);					
					nameNode.appendChild(document.createTextNode(BIConstants.USER_XML_UGI));					
					propertiesNode.appendChild(nameNode);										
					propertiesNode.appendChild(document.createTextNode("\n")); //$NON-NLS-1$
					
					Element valueNode = document.createElement(BIConstants.USER_XML_VALUE);					
					valueNode.appendChild(document.createTextNode(hadoopUgi));					
					propertiesNode.appendChild(valueNode);
					propertiesNode.appendChild(document.createTextNode("\n")); //$NON-NLS-1$
					
					TransformerFactory transformerFactory = TransformerFactory.newInstance();
					Transformer transformer = transformerFactory.newTransformer();
					DOMSource domSource = new DOMSource(document);
					StreamResult streamResult = new StreamResult(fileOutputStream);
					transformer.transform(domSource, streamResult);			    				
				}
			}
			catch(Exception e) {
				Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));				
			}
			finally {
				try {
					fileOutputStream.close();
				} catch(Exception e) {}
			}

			// make file read-only again
			coreSiteFile.setWritable(false);
		}
	}
	
	public class RunErrorDialog extends MessageDialog {
		// message dialog with custom area that contains a link for more help
		private String linkText = null;
		private String linkURL = null;
		
	    public RunErrorDialog(Shell parentShell, String dialogTitle,
	            Image dialogTitleImage, String dialogMessage, int dialogImageType,
	            String[] dialogButtonLabels, int defaultIndex, String linkText, String linkURL) {
	    	
	    	super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels, defaultIndex);
	    	this.linkText = linkText;
	    	this.linkURL = linkURL;
	    }
	    
	    protected Control createCustomArea(Composite parent) {	
	    	if (linkText!=null && linkURL!=null) {	    		
		    	Link link = new Link(parent, SWT.BORDER);
		        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		        data.horizontalIndent = 60;
		        data.grabExcessHorizontalSpace = true;
		        link.setLayoutData(data);
		        
		    	link.setText("<a>"+ linkText +"</a>"); //$NON-NLS-1$ //$NON-NLS-2$
		    	link.addSelectionListener(new SelectionAdapter() {
	                public void widgetSelected(SelectionEvent e) {
	                	PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(linkURL);
	                }
	            });
	
	//	    	link.setText("<a>"+ (BIConstants.JOB_LAUNCHTYPE_SHELL.equals(launchType)?
	//	    			Messages.LAUNCHCONFIGURATIONDELEGATE_ERROR_USER_HELP : 
	//	    			Messages.LAUNCHCONFIGURATIONDELEGATE_ERROR_USER_HELP_STMT)+"</a>");
	//	    	link.addSelectionListener(new SelectionAdapter() {
	//                public void widgetSelected(SelectionEvent e) {
	//                	e.getSource();
	//                	PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(
	//                			(BIConstants.JOB_LAUNCHTYPE_SHELL.equals(launchType)?
	//                	    			BIConstants.JAQL_SHELL_HELP_ID : 
	//                	    			BIConstants.JAQL_SHELL_HELP_ID_STMT));
	//                }
	//            });
	    	}
	    	return parent;
	    }	   

	}

}
