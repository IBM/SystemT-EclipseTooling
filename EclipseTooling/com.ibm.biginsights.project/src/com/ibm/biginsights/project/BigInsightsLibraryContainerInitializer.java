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
package com.ibm.biginsights.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ibm.biginsights.project.util.BIConstants;

public class BigInsightsLibraryContainerInitializer extends ClasspathContainerInitializer {
	
	private ArrayList<BigInsightsContainerDefinition>_containerDefinitions;	
	private static final IStatus READ_ONLY= new Status(IStatus.ERROR, Activator.PLUGIN_ID, ClasspathContainerInitializer.ATTRIBUTE_READ_ONLY, new String(), null);
	
	public BigInsightsLibraryContainerInitializer(){
		super();
		// read the XML files for all supported containers		
		_containerDefinitions = new ArrayList<BigInsightsContainerDefinition>();
		
		URL containerDefinitionsURL = FileLocator.find(Platform.getBundle(Activator.PLUGIN_ID), new Path(BIConstants.LIB_CONTAINER_DEFINITIONS_PATH),null);	    	
    	String containerDefinitionsString = null;
    	try {
    		containerDefinitionsString = FileLocator.toFileURL(containerDefinitionsURL).getPath();
    		containerDefinitionsString = URLDecoder.decode(containerDefinitionsString, BIConstants.UTF8); 
    		
    		Path containerDefinitionsPath = new Path(containerDefinitionsString);
    		File containerDefinitionsFile = new File(containerDefinitionsPath.toOSString());
    		
    		File[] containerDefinitions = containerDefinitionsFile.listFiles();
    		for (File containerDefinition:containerDefinitions) {
    			if (containerDefinition.getName().endsWith(BIConstants.XML_FILE_EXTENSION)) { // simple test for now, should be xml file 
    				try {
    					BigInsightsContainerDefinition def = readContainerDefinitionXML(containerDefinition);
    					if (def!=null)
    						_containerDefinitions.add(def);
					} catch (Exception e) {
						// just log the error and go on to the next file
						Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));						
					}
    			}
    		}
    		
    	} catch (IOException e)
    	{
    		Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
    	}
    	
	}
	
	// returns a list of supported containers that are defined through xml files
	public String[] getSupportedContainers() {
		ArrayList<String> result = new ArrayList<String>();
		for (BigInsightsContainerDefinition def:_containerDefinitions) {
			result.add(this.composeContainerName(def));
		}
		return result.toArray(new String[]{});
	}
	
	public static BigInsightsLibraryContainerInitializer getInstance() {
		return (BigInsightsLibraryContainerInitializer)JavaCore.getClasspathContainerInitializer(BIConstants.CONTAINER_ID);
	}
	
	public IPath getContainerPathByName(String containerName) {
		// first extract version from container
		String versionString = containerName.substring(containerName.lastIndexOf("V")); //$NON-NLS-1$
		return new Path(BIConstants.CONTAINER_ID).append(versionString);
	}
	
	public IClasspathEntry getClasspathEntryByName(String containerName) {
		return JavaCore.newContainerEntry(getContainerPathByName(containerName));
	}
	
	public IClasspathEntry getClasspathEntryByVersion(String version) {		
		return JavaCore.newContainerEntry(new Path(BIConstants.CONTAINER_ID).append(mapVersionToContainerVersion(version)));
	}
	
	public String getDefaultJAQLHomePath(String version) {
		String result = null;
		// first get the container definition for the particular version
		String jaqlLibVersion = null;
		BigInsightsContainerDefinition def = getContainerDefinition(version);
		if (def!=null) {
			for (BundleDefinition bundleDef:def.bundles) {
				if (bundleDef.bundleId.equals("com.ibm.biginsights.jaql.lib")) { //$NON-NLS-1$
					jaqlLibVersion = bundleDef.version;
					break;
				}				
			}
		}
		if (jaqlLibVersion!=null) {
			// then retrieve the jaql module with the particular version
			Bundle jarBundle = BigInsightsLibraryContainerInitializer.getBundleWithIdAndVersion("com.ibm.biginsights.jaql.lib", jaqlLibVersion); //$NON-NLS-1$
			if (jarBundle!=null) {
				URL jaqlModulePathURL = FileLocator.find(jarBundle, new Path("lib"),null); //$NON-NLS-1$
				if (jaqlModulePathURL!=null) {			
		    		try {
		    			String jaqlModulePathString = FileLocator.toFileURL(jaqlModulePathURL).getPath();
						jaqlModulePathString = URLDecoder.decode(jaqlModulePathString, BIConstants.UTF8); 
			    		Path jaqlModulePath = new Path(jaqlModulePathString);
			    		result =  jaqlModulePath.toOSString();
					} catch (Exception e) {				
						Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
					} 
				}				
			}			
		}
		return result;
	}
	
	public String getDefaultJAQLSearchPath(String version) {
		String result = null;
		// first get the container definition for the particular version
		String jaqlLibVersion = null;
		BigInsightsContainerDefinition def = getContainerDefinition(version);
		if (def!=null) {
			for (BundleDefinition bundleDef:def.bundles) {
				if (bundleDef.bundleId.equals("com.ibm.biginsights.jaql.lib")) { //$NON-NLS-1$
					jaqlLibVersion = bundleDef.version;
					break;
				}				
			}
		}
		if (jaqlLibVersion!=null) {
			// then retrieve the jaql module with the particular version
			Bundle jarBundle = BigInsightsLibraryContainerInitializer.getBundleWithIdAndVersion("com.ibm.biginsights.jaql.lib", jaqlLibVersion); //$NON-NLS-1$
			if (jarBundle!=null) {
				URL jaqlModulePathURL = FileLocator.find(jarBundle, new Path("lib/modules"),null); //$NON-NLS-1$
				if (jaqlModulePathURL!=null) {			
		    		try {
		    			String jaqlModulePathString = FileLocator.toFileURL(jaqlModulePathURL).getPath();
						jaqlModulePathString = URLDecoder.decode(jaqlModulePathString, BIConstants.UTF8); 
			    		Path jaqlModulePath = new Path(jaqlModulePathString);
			    		result =  jaqlModulePath.toOSString();
					} catch (Exception e) {				
						Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
					} 
				}				
			}			
		}
		return result;
	}
	
	public String getSymphonyHome(String containerVersion, boolean isNative) {
		String result = null;
		String psmrBasePlugin = "com.ibm.psmrclient";
		// returns the folder of the com.ibm.psmrclient plugin for the current platform		
		// needs to be set as result SOAM_HOME
		String psmrVersion = null;
		// get the psmr version from the containerDefinition which includes a reference to the psmrBasePlugin
		BigInsightsContainerDefinition def = getContainerDefinition(containerVersion);
		if (def!=null) {
			for (BundleDefinition bundleDef:def.bundles) {
				if (bundleDef.bundleId.equals(psmrBasePlugin)) { //$NON-NLS-1$
					psmrVersion = bundleDef.version;
					break;
				}				
			}
		}

		boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows"); //$NON-NLS-1$ //$NON-NLS-2$
		boolean isPPC = false;
		if (!isWindows) {
			String osArch = System.getProperty("os.arch"); //$NON-NLS-1$
			isPPC = osArch!=null && osArch.startsWith("ppc");
		}
		String pluginName = isNative ? (isWindows ? "com.ibm.psmrclient.windows" : (isPPC ? "com.ibm.psmrclient.linux.ppc" : "com.ibm.psmrclient.linux")) : psmrBasePlugin;			

		// retrieve the baseplugin or the native plugin 
		if (psmrVersion!=null) {			
			Bundle psmrBundle = BigInsightsLibraryContainerInitializer.getBundleWithIdAndVersion(pluginName, psmrVersion); //$NON-NLS-1$
			if (psmrBundle!=null) {
				URL psmrModulePathURL = FileLocator.find(psmrBundle, new Path("/"),null); //$NON-NLS-1$
				if (psmrModulePathURL!=null) {			
		    		try {
		    			String psmrModulePathString = FileLocator.toFileURL(psmrModulePathURL).getPath();
						psmrModulePathString = URLDecoder.decode(psmrModulePathString, BIConstants.UTF8); 
			    		Path psmrModulePath = new Path(psmrModulePathString);
			    		result =  psmrModulePath.toOSString();
					} catch (Exception e) {				
						Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
					} 
				}				
			}			
		}
		return result;
	}
	
	public String getSymphonyMapReduceHome(String containerVersion, boolean isNative) {
		// needs to be set as result PMR_HOME
		StringBuffer result = new StringBuffer();
		result.append(getSymphonyHome(containerVersion, isNative));		
		result.append("mapreduce");
		result.append(File.separator);
		return result.toString();
	}

	public String getSymphonyNativeLibraryPath(String containerVersion) {
		// needs to be set as result LD_LIBRARY_PATH		
		boolean is64bit = System.getProperty("sun.arch.data.model").equals("64");
		
		StringBuffer result = new StringBuffer();		
		if (is64bit) {
			result.append(getSymphonyMapReduceHome(containerVersion, true));			
			result.append("lib64");	// psmr61/mapreduce/lib
			result.append(File.pathSeparator);			
			result.append(getSymphonyHome(containerVersion, true));			
			result.append("lib64");	// psmr61/lib
			result.append(File.pathSeparator);
		}
		result.append(getSymphonyMapReduceHome(containerVersion, true));		
		result.append("lib");	// psmr61/mapreduce/lib
		result.append(File.pathSeparator);
		result.append(getSymphonyHome(containerVersion, true));		
		result.append("lib");	// psmr61/lib
		result.append(File.pathSeparator);
		return result.toString();
	}
	
	public String getSymphonyDefaultSecurityPlugin(String containerVersion) {
		String result = BIConstants.PSMR_DEFAULT_EGO_SEC_PLUGIN;
		String fileName = getSymphonyHome(containerVersion, false)+"conf"+File.separator+BIConstants.PSMR_CONF_FILE;		
		if (new File(fileName).exists()) {
			Properties properties = new Properties();
	    	try {
	    		properties.load(new FileInputStream(fileName));		
	    		result = properties.getProperty(BIConstants.PSMR_EGO_SEC_PLUGIN);	 
	    	} catch (IOException ex) {
				// don't do anything, use default value
			}
		}		
		return result;
	}

	public int getSymphonyDefaultEgoKDPort(String containerVersion) {
		int result = BIConstants.PSMR_DEFAULT_EGO_KD_PORT;
		String fileName = getSymphonyHome(containerVersion, false)+"conf"+File.separator+BIConstants.PSMR_CONF_FILE;		
		if (new File(fileName).exists()) {
			Properties properties = new Properties();
	    	try {
	    		properties.load(new FileInputStream(fileName));   		
	    		result = new Integer(properties.getProperty(BIConstants.PSMR_EGO_KD_PORT));	 
	    	} catch (IOException ex) {
				// don't do anything, use default value
			}
		}		
		return result;
	}
	
	public String mapVersionToContainerVersion(String serverVersion) {
		// needs to be exact match, otherwise the hadoop libs might be different
		String result = null;
		if (serverVersion.startsWith("v") || serverVersion.startsWith("V"))			 //$NON-NLS-1$ //$NON-NLS-2$
			serverVersion = serverVersion.substring(1);
		// first look for exact match
		for (BigInsightsContainerDefinition def:this._containerDefinitions) {
			if (serverVersion.equals(def.version)) {				
				result = def.version; 				
				break;
			}
		}	

		return result;
	}
	
	public BigInsightsContainerDefinition getContainerDefinition(String serverVersion) {
		BigInsightsContainerDefinition result = null;
		String version = mapVersionToContainerVersion(serverVersion);
		if (version!=null) {
			for (BigInsightsContainerDefinition def:this._containerDefinitions) {
				if (version.equals(def.version)) {				
					result = def; 				
					break;
				}
			}
		}
		return result;
	}

	public String getFullContainerNameByVersion(String serverVersion) {
		BigInsightsContainerDefinition def = getContainerDefinition(serverVersion);
		return def!=null ? composeContainerName(def) : ""; //$NON-NLS-1$
	}

	public IClasspathContainer getClasspathContainerByVersion(String version, boolean isRuntime, String vendor) {
		// 
		IClasspathContainer result = null;	
		if (BIConstants.CONTAINER_ID_DEFAULT.equals(version.toUpperCase())) {
			// get the default container definition and return entries from there
			for (BigInsightsContainerDefinition def:_containerDefinitions) {
	    		if (def.isDefault) {
	    			result = buildContainerEntries(new Path(BIConstants.CONTAINER_ID).append(def.version), def, isRuntime, vendor);
	    			break;
	    		}
			}
		}
		else {
			if (version.startsWith("v") || version.startsWith("V")) //$NON-NLS-1$ //$NON-NLS-2$
				version = version.substring(1);
			for (BigInsightsContainerDefinition def:this._containerDefinitions) {
				if (def.version.equals(version)) {
					result = buildContainerEntries(new Path(BIConstants.CONTAINER_ID).append(version), def, isRuntime, vendor);
					break;
				}
			}
		}
		return result;	
	}
	
	private String getAttributeByName(Node node, String attribute) {
		NamedNodeMap map = node.getAttributes();
		return map.getNamedItem(attribute)==null ? null : map.getNamedItem(attribute).getNodeValue();		
	}
	
	private BigInsightsContainerDefinition readContainerDefinitionXML(File containerDefinitionFile) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(containerDefinitionFile);
		
		// create a new container definition file
		BigInsightsContainerDefinition containerDefinition = new BigInsightsContainerDefinition();
		containerDefinition.version = getAttributeByName(doc.getElementsByTagName(BIConstants.XML_CONTAINER).item(0), BIConstants.XML_CONTAINER_VERSION);
		containerDefinition.isDefault = getAttributeByName(doc.getElementsByTagName(BIConstants.XML_CONTAINER).item(0), BIConstants.XML_CONTAINER_ISDEFAULT)==null ? false : 
				new Boolean(getAttributeByName(doc.getElementsByTagName(BIConstants.XML_CONTAINER).item(0), BIConstants.XML_CONTAINER_ISDEFAULT));
				
		if(doc != null){
			//collect all nodes with tag bundle and get all file elements
			NodeList bundleNodes = doc.getElementsByTagName(BIConstants.XML_BUNDLE);
			 
			for(int i=0; i<bundleNodes.getLength();i++) {
				Node bundleNode = bundleNodes.item(i);					
				
				BundleDefinition bundleDefinition = new BundleDefinition(
							getAttributeByName(bundleNode, BIConstants.XML_BUNDLE_ID), 
							getAttributeByName(bundleNode, BIConstants.XML_BUNDLE_VERSION));
				
				// check if bundle even exists
				Bundle bundle = getBundleWithIdAndVersion(bundleDefinition.bundleId, bundleDefinition.version);				
				if (bundle!=null) {			
					containerDefinition.bundles.add(bundleDefinition);
					NodeList fileNodes = bundleNode.getChildNodes();
					if (fileNodes.getLength()>0) {					
						for (int j=0; j<fileNodes.getLength(); j++) {
							Node fileNode = fileNodes.item(j);					
							if (fileNode.getNodeType()!=Node.COMMENT_NODE && !fileNode.getTextContent().trim().isEmpty()) {
								String isRuntimeValue = getAttributeByName(fileNode, BIConstants.XML_BUNDLE_ISRUNTIME);
								String runtimeVendors = getAttributeByName(fileNode, BIConstants.XML_BUNDLE_RUNTIME_VENDOR);
								BundleDefinitionFile bundleDefFile = null;																
								if (runtimeVendors!=null) {
									// runtimeVendor supplied (list of comma-separated values
									String[] runtimeVendorArray = runtimeVendors.split(",");									
									bundleDefFile = new BundleDefinitionFile(
											fileNode.getTextContent(), 
											isRuntimeValue!=null && "true".equals(isRuntimeValue), //$NON-NLS-1$
											runtimeVendorArray); 	
								}
								else {
									bundleDefFile = new BundleDefinitionFile(
											fileNode.getTextContent(), 
											isRuntimeValue!=null && "true".equals(isRuntimeValue)); //$NON-NLS-1$
								}
								bundleDefinition.addBundleDefinitionFile(bundleDefFile);
							}
						}
					}
				}
				else {
					// don't return container definition if not all the plugins are there to support it
					containerDefinition = null;
					break;					
				}
			 }
		}
		return containerDefinition;		
	}
	
	@Override
	public void initialize(IPath containerPath, IJavaProject project)throws CoreException {
		
		if (isValidBigInsighsContainerPath(containerPath)) {		
			BigInsightsLibraryContainer container = null;
			String version= containerPath.segment(1);
			if (version.startsWith("v") || version.startsWith("V")) //$NON-NLS-1$ //$NON-NLS-2$
				version = version.substring(1); // remove V from version string
			
			// look in list of supported containers for the right version
			for (BigInsightsContainerDefinition def:this._containerDefinitions) {
				if (("DEFAULT".equals(version) && def.isDefault) || def.version.equals(version)) { //$NON-NLS-1$
					container = buildContainerEntries(containerPath, def, false, BIConstants.LOCATION_XML_VENDOR_IBM);// for compile purposes, pass in IBM always, not symphony				
					break;
				}
			}
			
			if (container!=null) {
				JavaCore.setClasspathContainer(containerPath, new IJavaProject[] {project}, new IClasspathContainer[] {container}, null);
			}
		} else {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "The specified container path is invalid: "+containerPath)); //$NON-NLS-1$        	
        }
	}
	
	/**
	 * 
	 * @param containerPath
	 * @param definition
	 * @param isRuntime: whether the classpath should contain also the runtime-only libs (true) or not (false)
	 * @param runtimeVendor: vendor for which to build the classpath (ibm, symphony)
	 * @return
	 */
	private BigInsightsLibraryContainer buildContainerEntries(IPath containerPath, BigInsightsContainerDefinition definition,
																boolean isRuntime, String runtimeVendor) {
		ArrayList<IClasspathEntry> entryList = new ArrayList<IClasspathEntry>();
		
		for (BundleDefinition bundleDef:definition.bundles) {						
			Bundle bundle = getBundleWithIdAndVersion(bundleDef.bundleId, bundleDef.version);
				
			if (bundle!=null) {
				IPath baseBundlePath = null;
				URL bundleURL= null;
				try {
					bundleURL= FileLocator.toFileURL(bundle.getEntry("/")); //$NON-NLS-1$
				} catch (IOException e) {
					// just log error
					Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
				}
				if (bundleURL!=null) {
					String bundleAbsolutePath= new File(bundleURL.getPath()).getAbsolutePath();					
					baseBundlePath = Path.fromOSString(bundleAbsolutePath);		
				}
				
				if (baseBundlePath!=null) {		
					if (bundleDef.files.isEmpty()) {
						// if no files specified, add the actual file location of the bundle
						try {
							URL selfContainedJarURL = FileLocator.find(bundle, new Path("/"), null);	 //$NON-NLS-1$
							if (selfContainedJarURL!=null) {								
								URL u = FileLocator.resolve(selfContainedJarURL);
								String selfContainedJarPath = u.getFile();
								// strip of leading file: and trailing characters in case it's a jar bundle
								if (selfContainedJarPath.startsWith("file:")) //$NON-NLS-1$
									selfContainedJarPath = selfContainedJarPath.substring("file:".length()); //$NON-NLS-1$
								if (selfContainedJarPath.endsWith("!/")) // will be the case if URI is file //$NON-NLS-1$
									selfContainedJarPath = selfContainedJarPath.substring(0, selfContainedJarPath.length()-2);
								File bundleFile = null;
								bundleFile = new File(selfContainedJarPath);
								// in dev env, the BICredentials project is a folder - need to add /bin
								// in runtime env, it will be a jar file
								addPathToEntryList(new Path(bundleFile.isDirectory() ? 
										bundleFile.getAbsolutePath()+"/bin" : bundleFile.getPath()), entryList); //$NON-NLS-1$
							}						
						} catch (Exception e) {
							// don't add the entry
							Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
						}
					}
					else {
						for (BundleDefinitionFile bundleDefFile: bundleDef.files) {
							if ((isRuntime && bundleDefFile.isRuntimeOnly && bundleDefFile.supportsRuntimeVendor(runtimeVendor)) ||
								(isRuntime && bundleDefFile.supportsRuntimeVendor(runtimeVendor)) ||
								(!isRuntime && !bundleDefFile.isRuntimeOnly)) 
							{ 
//								System.out.println("isRuntime="+isRuntime+" adding "+bundleDefFile.file);
								// add the file if:
								// - isRuntime is true and the file is runtime only and file supports the passed-in vendor
								// - isRuntime is true and the file is not only for runtime and file supports the passed-in vendor
								// - or if it's not for runtime and the file is not just a runtime parameter  
								// first parm is path, second parm is file name
								Path p = new Path(bundleDefFile.file);										
								Enumeration<URL> entries = bundle.findEntries(bundleDefFile.file.substring(0,bundleDefFile.file.indexOf(p.lastSegment())), p.lastSegment(), false);
								if (entries!=null) {
									while (entries.hasMoreElements()) {
										URL u = entries.nextElement();	
										try {
											URL url = FileLocator.resolve(u);
											String selfContainedJarPath = url.getFile();
											File bundleFile = new File(selfContainedJarPath);
											addPathToEntryList(new Path(bundleFile.getPath()), entryList);											
										} catch (IOException e) {
											// don't add the entry
											Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
										}
									}
								}
							}
//							else
//								System.out.println("isRuntime="+isRuntime+" skipping "+bundleDefFile.file);
							
						}
					}
				}
			}
		}
		
        IClasspathEntry[] entries = new IClasspathEntry[entryList.size()];
        entries = (IClasspathEntry[])entryList.toArray(entries);

		BigInsightsLibraryContainer result = new BigInsightsLibraryContainer(containerPath, entries, composeContainerName(definition));		
		return result;
	}
	
	private void addPathToEntryList(IPath path, List<IClasspathEntry>list) {
        IAccessRule[] accessRules={};				
		IClasspathAttribute[] attributes;
		attributes = new IClasspathAttribute[0];

		// new classpath entry for each file
		list.add( JavaCore.newLibraryEntry(path , null, null, accessRules, attributes, false));                		
	}
	
	private String composeContainerName(BigInsightsContainerDefinition definition) {
		return BIConstants.CONTAINER_NAME+" V"+definition.version; //$NON-NLS-1$
	}
	
	private static boolean isValidBigInsighsContainerPath(IPath path) {
		return path != null && path.segmentCount() == 2 && BIConstants.CONTAINER_ID.equals(path.segment(0));
	}

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.ClasspathContainerInitializer#canUpdateClasspathContainer(org.eclipse.core.runtime.IPath, org.eclipse.jdt.core.IJavaProject)
     */
    @Override
    public boolean canUpdateClasspathContainer(IPath containerPath, IJavaProject project) {
        return true; // need to be true to be able to modify source code attachment for libraries
    }

	public IStatus getAccessRulesStatus(IPath containerPath, IJavaProject project) {
		return READ_ONLY; // don't allow changing access rules for BI libraries - similar to JRE lib container
	}
	
	public static Bundle getBundleWithIdAndVersion(String bundleId, String version) {
		Bundle result = null;
		Bundle[] bundles= Platform.getBundles(bundleId, version);
		if (bundles != null && bundles.length>0) {
			for (Bundle bundle:bundles) {
				Dictionary dict = bundle.getHeaders();
				String versionBundle = (String)dict.get("Bundle-Version"); //$NON-NLS-1$
				
				if (versionBundle.startsWith(version)) {
					result = bundle;
					break;
				}
			}			
		}
		return result;
	}
	
    public IClasspathEntry getDefaultBigInsightsContainerEntry() {
    	for (BigInsightsContainerDefinition def:_containerDefinitions) {
    		if (def.isDefault) {
    			return JavaCore.newContainerEntry(new Path(BIConstants.CONTAINER_ID).append("V"+def.version)); //$NON-NLS-1$
    		}
    	}
    	return null;
    }
    
    public String getDefaultBigInsightsContainerEntryName() {
    	for (BigInsightsContainerDefinition def:_containerDefinitions) {
    		if (def.isDefault) {
    			return this.composeContainerName(def);
    		}
    	}
    	return null;
    }

    public String getDefaultBigInsightsContainerVersion() {
    	for (BigInsightsContainerDefinition def:_containerDefinitions) {
    		if (def.isDefault) {
    			return def.version;
    		}
    	}
    	return null;
    }

	private class BigInsightsContainerDefinition {
		
		public String version;
		public boolean isDefault;
		public List<BundleDefinition>bundles;
		
		public BigInsightsContainerDefinition() {
			bundles = new ArrayList<BundleDefinition>();
		}
	}
	
	public static List<String> searchJars(String bundleName, String version, String path, Set<String> filter) {

		List<String> result = new ArrayList<String>();
		
		Bundle bundle = BigInsightsLibraryContainerInitializer.getBundleWithIdAndVersion(bundleName, version);
		if(bundle==null) return result;

		URL url = FileLocator.find(bundle, new Path(path), null);
		if(url==null) return result;
		
		try {
			String dir = FileLocator.toFileURL(url).getPath();
			dir = URLDecoder.decode(dir, BIConstants.UTF8);

			Path dirPath = new Path(dir);
			File dirFile = new File(dirPath.toOSString());

			File[] files = dirFile.listFiles();
			for (File file : files) {
				if (file.isFile()
						&& file.getName().endsWith(BIConstants.JAR_FILE_EXTENSION)) {
					for (String prefix : filter) {
						if (file.getName().startsWith(prefix)) {
							result.add(file.getName());
						}
					}

				}
			}

		} catch (IOException e) {
			Activator
					.getDefault()
					.getLog()
					.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e
							.getMessage()));
		}

		return result;

	}

}
