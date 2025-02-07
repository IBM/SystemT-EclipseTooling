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
package com.ibm.biginsights.project.locations;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.BigInsightsLibraryContainerInitializer;
import com.ibm.biginsights.project.locations.IBigInsightsLocationChangeListener.LocationChangeEventType;
import com.ibm.biginsights.project.util.BIConstants;

public class LocationRegistry {

	private static final String LOCATIONS_FOLDER = "locations";	 //$NON-NLS-1$
	private static final String HADOOP_CONF_FOLDER = "hadoop-conf"; //$NON-NLS-1$
	private static final String JAQL_CONF_FOLDER = "jaql/conf";	 //$NON-NLS-1$
	private static final String HBASE_CONF_FOLDER = "hbase/conf";	 //$NON-NLS-1$
	private static final String HIVE_CONF_FOLDER = "hive/conf";	 //$NON-NLS-1$
	private static final String BIGSQL_CONF_FOLDER="bigsql/conf"; //$NON-NLS-1$
	private static final String PSMR_CONF_FOLDER = "HAManager/data/kernel/conf"; //$NON-NLS-1$
	
	/*
	 BI locations are stored under <workspace>/.metadata/.plugins/com.ibm.biginsights.project/
	 storing structure:
	 	com.ibm.biginsights.project
			/locations
			 	/svlhdev18
			 	   location.xml
			 	   /conf
			 	   		/hadoop-conf
			 	   		/jaql-conf
	*/
	
	private static final LocationRegistry _instance = new LocationRegistry();	

	private Set<IBigInsightsLocationChangeListener> listeners = new HashSet<IBigInsightsLocationChangeListener>();

	public static String getLocationTargetDirectory(IBigInsightsLocation location) {
		String result = LocationRegistry.getBaseDir() + "/" + LOCATIONS_FOLDER;		 //$NON-NLS-1$
		if (location!=null)
			result += "/"+location.getLocationName(); //$NON-NLS-1$
		return result;
	}
	
	public static String getHadoopConfLocation(IBigInsightsLocation location) {
		return getLocationTargetDirectory(location)+"/"+HADOOP_CONF_FOLDER; //$NON-NLS-1$
	}

	public static String getJAQLConfLocation(IBigInsightsLocation location) {
		return getLocationTargetDirectory(location)+"/"+JAQL_CONF_FOLDER; //$NON-NLS-1$
	}

	public static String getHBaseConfLocation(IBigInsightsLocation location) {
		return getLocationTargetDirectory(location)+"/"+HBASE_CONF_FOLDER; //$NON-NLS-1$
	}

	public static String getHiveConfLocation(IBigInsightsLocation location) {
		return getLocationTargetDirectory(location)+"/"+HIVE_CONF_FOLDER; //$NON-NLS-1$
	}
	
	public static String getBigSQLConfLocation(IBigInsightsLocation location) {
		return getLocationTargetDirectory(location)+"/"+BIGSQL_CONF_FOLDER; //$NON-NLS-1$
	}

	public static String getPSMRConfLocation(IBigInsightsLocation location) {
		return getLocationTargetDirectory(location)+"/"+PSMR_CONF_FOLDER; //$NON-NLS-1$
	}

	private static IClasspathContainer getLibContainer(String containerVersion) {
		IClasspathContainer result = null;
		String mappedVersion = BigInsightsLibraryContainerInitializer.getInstance().mapVersionToContainerVersion(containerVersion);
		result = mappedVersion!=null ? BigInsightsLibraryContainerInitializer.getInstance().getClasspathContainerByVersion(mappedVersion, false, BIConstants.LOCATION_XML_VENDOR_IBM) : null;
		return result;
	}
	
	public static String getHadoopLocation(IBigInsightsLocation location) {
		// returns the physical location of the hadoop libraries - needed to retrieve the location of the native libs when running with GPFS
		String result = null;
		IClasspathContainer libContainer = getLibContainer(location.getVersionWithVendor());
		if (libContainer!=null) {
			for (IClasspathEntry entry:libContainer.getClasspathEntries()) {
				IPath path = entry.getPath();
				if (path.toOSString().matches(".*hadoop.*core.*jar")) { //$NON-NLS-1$
					path = path.removeLastSegments(1);
					result = path.toOSString();					
					break;
				}
			}
		}			
		return result;
	}

	public static String getHadoopHomeDirectory(String containerVersion) {
		// returns the folder that needs to be set as HADOOP_HOME env variable 
		String result = null;
		IClasspathContainer libContainer = getLibContainer(containerVersion);
		if (libContainer!=null) {
			for (IClasspathEntry entry:libContainer.getClasspathEntries()) {
				IPath path = entry.getPath();
				if (path.toOSString().matches(".*hadoop-core.*.jar")) { //$NON-NLS-1$
					result = path.toOSString().substring(0, path.toOSString().indexOf("hadoop-core")); //$NON-NLS-1$
					break;
				}
			}
		}			
		return result;
	}

	public static String getTextAnalyticsHomeDirectory(String containerVersion) {
		// returns the folder that needs to be set as TEXTANALYTICS_HOME env variable when running TA from jaql
		String result = null;
		IClasspathContainer libContainer = getLibContainer(containerVersion);
		if (libContainer!=null) {
			for (IClasspathEntry entry:libContainer.getClasspathEntries()) {
				IPath path = entry.getPath();
				if (path.toOSString().matches(".*systemT.jar")) { //$NON-NLS-1$
					result = path.toOSString().substring(0, path.toOSString().indexOf(File.separator+"lib"+File.separator+"text-analytics")); //$NON-NLS-1$ //$NON-NLS-2$
					break;
				}
			}
		}			
		return result;
	}

	public static String getJAQLHomeDirectory(String containerVersion) {
		// returns the folder that needs to be set as JAQL_HOME env variable when running jaql modules from Eclipse
		// returns the location for the com.ibm.biginsights.jaql.lib/lib folder		
		String result = null;
		IClasspathContainer libContainer = getLibContainer(containerVersion);
		if (libContainer!=null) {
			for (IClasspathEntry entry:libContainer.getClasspathEntries()) {
				IPath path = entry.getPath();
				if (path.toOSString().matches(".*jaql.jar")) { //$NON-NLS-1$
					result = path.toOSString().substring(0, path.toOSString().indexOf("jaql.jar")); //$NON-NLS-1$
					break;
				}
			}
		}			
		return result;
	}

	public static String getJAQLSystemSearchPath(String containerVersion) {
		// returns the folder that needs to be set as JAQL_SEARCH_PATH env variable when running jaql modules from Eclipse
		// returns the location for the com.ibm.biginsights.thirdparty/lib folder
		// for now use the derby.jar as file to find the location 
		String result = null;
		IClasspathContainer libContainer = getLibContainer(containerVersion);
		if (libContainer!=null) {
			for (IClasspathEntry entry:libContainer.getClasspathEntries()) {
				IPath path = entry.getPath();
				if (path.toOSString().matches(".*derby.jar")) { //$NON-NLS-1$
					result = path.toOSString().substring(0, path.toOSString().indexOf("derby.jar")); //$NON-NLS-1$
					break;
				}
			}
		}			
		return result;
	}

	public static String getHBasePath(String containerVersion) {
		// returns the folder that needs to be set as HBASE_HOME when running jaql modules from Eclipse
		// returns the location for the com.ibm.hbase.core.bin folder
		String result = null;
		IClasspathContainer libContainer = getLibContainer(containerVersion);
		if (libContainer!=null) {
			for (IClasspathEntry entry:libContainer.getClasspathEntries()) {
				IPath path = entry.getPath();
				if (path.toOSString().matches(".*hbase.*hbase.*.jar")) { //$NON-NLS-1$
					path = path.removeLastSegments(1);
					result = path.toOSString();					
					break;
				}
			}
		}			
		return result;
	}

	public static String getHCatalogPath(String containerVersion) {
		// returns the folder that needs to be set as HCATALOG_HOME when running the Jaql hcatalog module from Eclipse
		// returns the location for the biginsights-shared-libraries\lib\hcatalog-0.4 folder		
		String result = getJAQLSystemSearchPath(containerVersion)!=null ? getJAQLSystemSearchPath(containerVersion)+"hcatalog-0.4" : null;
		return result;
	}

	public static String getHivePath(String containerVersion) {
		// returns the folder that needs to be set as HIVE_HOME when running the Jaql hcatalog module from Eclipse
		// returns the location for the com.ibm.biginsights.hive.lib folder		
		String result = null;
		IClasspathContainer libContainer = getLibContainer(containerVersion);
		if (libContainer!=null) {
			for (IClasspathEntry entry:libContainer.getClasspathEntries()) {
				IPath path = entry.getPath();
				if (path.toOSString().matches(".*hive-exec.*.jar")) { //$NON-NLS-1$
					path = path.removeLastSegments(2); // remove file and lib folder
					result = path.toOSString();					
					break;
				}
			}
		}			

		return result;
	}

	private static String getBaseDir() {
		return Activator.getDefault().getStateLocation().toFile().getAbsolutePath(); 
	}
	
	private LocationRegistry() {		
		File targetDir = new File(getLocationTargetDirectory(null));
		if (targetDir.exists() && !targetDir.isDirectory())
			targetDir.delete();
		if (!targetDir.exists())
			targetDir.mkdirs();
		load();
	}

	private Map<String, IBigInsightsLocation> locations;
	
	public static LocationRegistry getInstance() {
	  return _instance;
	}

	// get a list of all existing BigInsights locations
	public synchronized Collection<IBigInsightsLocation> getLocations() {
	  return Collections.unmodifiableCollection(locations.values());
	}
	
	// get a list of all existing BigInsights locations
	public synchronized Collection<IBigInsightsLocation> getAdminLocations() {
		List<IBigInsightsLocation> adminLoc = new ArrayList<IBigInsightsLocation>();
		for(Iterator<IBigInsightsLocation> itr = locations.values().iterator(); itr.hasNext();){
			IBigInsightsLocation loc = itr.next();
			if(loc.isAdmin()){
				adminLoc.add(loc);
			}
		}
		
		return Collections.unmodifiableCollection(adminLoc);
	}

	// load saved locations from workspace
	private synchronized void load() {
		Map<String, IBigInsightsLocation> locationMap = new TreeMap<String, IBigInsightsLocation>();
		File targetDir = new File(getLocationTargetDirectory(null));
		
		for (File file:targetDir.listFiles()) {
			if (file.isDirectory()) {	// directory is name of location
				for (File locationFile:file.listFiles()) {
					// find the location.xml file and create a location object
					if (locationFile.isFile() && locationFile.getName().endsWith(BIConstants.LOCATION_XML)) {
						try {
							IBigInsightsLocation server = new BigInsightsLocation(locationFile);
							if (server!=null && server.getLocationName()!=null) 
								locationMap.put(server.getLocationName(), server);
						} catch (Exception e) {
							Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
						}				
						break;
					}
				}
			}
		}
		this.locations = locationMap;
	}

	public void save (IBigInsightsLocation location){
		saveLocation(location);
	}
	
	private synchronized void saveLocation(IBigInsightsLocation location) {
		
		File dir = new File(getLocationTargetDirectory(location));
		if (!dir.exists())
			dir.mkdirs();
			
		location.save(new File(dir, BIConstants.LOCATION_XML));			
		
	}

	public void dispose() {
		for (IBigInsightsLocation location : getLocations()) {
			location.dispose();
		}
	}

	// get a location with a specific location name
	public synchronized IBigInsightsLocation getLocation(String location) {
		return locations.get(location);
	}

	public synchronized IBigInsightsLocation getLocationByDisplayName(String locationDisplayName) {
		IBigInsightsLocation result = null;
		for (IBigInsightsLocation loc:locations.values()) {
			if (loc.getLocationDisplayString().equals(locationDisplayName)) {
				result = loc;
				break;
			}
		}
		return result;
	}
	
	public synchronized IBigInsightsLocation getLocationByHostName(String HostName, String userid) {
		IBigInsightsLocation result = null;
		for (IBigInsightsLocation loc:locations.values()) {
			if (loc.getHostName().equals(HostName) && loc.getUserName() != null && loc.getUserName().equals(userid)) {
				result = loc;
				break;
			}
		}
		return result;
	}

	public synchronized void removeLocation(IBigInsightsLocation location) {
		this.locations.remove(location.getLocationName());
		// remove stored artifacts
		File storedLocation = new File(getLocationTargetDirectory(location));		
		if (storedLocation.exists() && storedLocation.isDirectory()) {
			deleteDirectory(storedLocation);
		}
		
		Collection<IBigInsightsLocation>locArray = new ArrayList<IBigInsightsLocation>();
		locArray.add(location);
		notifyListeners(LocationChangeEventType.LOCATION_DELETED, locArray);
	}

	private static boolean deleteDirectory(File file) {
		if (file.isDirectory()) {
			File[]children = file.listFiles();
			for (int i=0; i<children.length; i++) {
				boolean result = deleteDirectory(children[i]);
				if (!result)
					return false;
			}
		}
		
		return file.delete();	// we are in a file or the folder is emtpy now -> remove it
	}
	
	public synchronized void removeLocations(Collection<IBigInsightsLocation> locations) {
		for (IBigInsightsLocation loc:locations) 
			removeLocation(loc);	     
		notifyListeners(LocationChangeEventType.LOCATION_DELETED, locations);
	}
  
	// add a new BigInsights location
	public synchronized boolean addLocation(IBigInsightsLocation location) {
		this.locations.put(location.getLocationName(), location);
		
		location.retrieveVersion();
		location.retrieveVendor();
		
		location.checkRoles();
		
		if(location.isAdminOrUser()){
			
			//get the hive port
			location.retrieveHivePort();

			//get the bigsql port and hostname
			if (location.isVersion2100orAbove())
				location.retrieveBigSQLNodeAndPort();

			// save location first because the location files need to exist before retrieving config files
			saveLocation(location);    
	    
			// when adding a location, retrieve the latest config files
			location.retrieveConfigurationFiles();
			
			// need to save location again, because we get the filesystem flag
			saveLocation(location);

			Collection<IBigInsightsLocation>locArray = new ArrayList<IBigInsightsLocation>();
			locArray.add(location);
			notifyListeners(LocationChangeEventType.LOCATION_ADDED, locArray);
			return true;
		}
		return false;
	}

	// update an existing location with the name 'oldName' with the new values in location  
	public synchronized boolean updateLocation(String oldName, IBigInsightsLocation location) {

		if (!location.getLocationName().equals(oldName)) {
			locations.remove(oldName);
			// when updating the name, rename the old location folder into the new one (don't delete folder first because conf files are not stored in the location object)
			File oldFileLocation = new File(getLocationTargetDirectory(null)+"/"+oldName); //$NON-NLS-1$
			File newFileLocation = new File(getLocationTargetDirectory(location));
			oldFileLocation.renameTo(newFileLocation);
		  
			// also remove old entry in the secure store
			ISecurePreferences nodePreferences = BigInsightsLocation.getSecurePreferenceForLocation(oldName);
			if (nodePreferences!=null) {
				nodePreferences.removeNode();
			}

			locations.put(location.getLocationName(), location);
		}
		else {    	
			IBigInsightsLocation existingLoc = locations.get(oldName);
			existingLoc.initWithLocation(location);
			
			//reset connection in case userid changed
			existingLoc.resetHttpClient();
			
			location = existingLoc;  // all other calls should be done on existing connection
		}

		// even when updating a location, retrieve the version
		location.retrieveVersion();
		location.retrieveVendor();
		if (location.isVersion2100orAbove())
			location.retrieveBigSQLNodeAndPort();
		location.retrieveHivePort();
		location.checkRoles();
		location.retrieveConfigurationFiles(); // when updating retrieve the latest config files as the user might have switched to a different server or updated the server
		
		if(location.isAdminOrUser()){
			saveLocation(location);    
			Collection<IBigInsightsLocation>locArray = new ArrayList<IBigInsightsLocation>();
			locArray.add(location);
			notifyListeners(LocationChangeEventType.LOCATION_UPDATED, locArray);
			return true;
		}
		return false;
	}
  
	public void addListener(IBigInsightsLocationChangeListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	public void removeListener(IBigInsightsLocationChangeListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	private void notifyListeners(LocationChangeEventType eventType, Collection<IBigInsightsLocation> locations) {
		synchronized (listeners) {
			for (IBigInsightsLocationChangeListener listener : listeners) {
				listener.change(eventType, locations);
			}	
		}
	}

}
