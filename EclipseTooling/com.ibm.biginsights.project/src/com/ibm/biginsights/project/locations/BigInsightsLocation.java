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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.security.storage.EncodingUtils;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.BigInsightsLibraryContainerInitializer;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.apps.BigInsightsAppFolder;
import com.ibm.biginsights.project.locations.wizard.PasswordDialog;
import com.ibm.biginsights.project.util.Authenticator;
import com.ibm.biginsights.project.util.BIConnectionException;
import com.ibm.biginsights.project.util.BIConstants;
import com.ibm.json.java.JSON;
import com.ibm.json.java.JSONObject;

public class BigInsightsLocation implements IBigInsightsLocation {
  
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

	static final Pattern PROD_VER_BUILD_PTN = Pattern
		.compile("(.*?)?(v[\\d+\\.]+\\d+)(, build (linux[0-9]+_[a-z0-9_]+))?"); //$NON-NLS-1$
	
	public static final String HIVE_JDBC_DRIVER_071_TEMPLATE_ID = "com.ibm.biginsights.hivejdbc.hiveDriverTemplate.071"; //$NON-NLS-1$
	public static final String HIVE_JDBC_DRIVER__080_TEMPLATE_ID = "com.ibm.biginsights.hivejdbc.hiveDriverTemplate.080"; //$NON-NLS-1$
	public static final String HIVE_JDBC_DRIVER__080_OVERLAY_TEMPLATE_ID = "com.ibm.biginsights.hivejdbc.hiveDriverTemplate.080_overlay"; //$NON-NLS-1$
	public static final String HIVE_JDBC_DRIVER__090_TEMPLATE_ID = "com.ibm.biginsights.hivejdbc.hiveDriverTemplate.090"; //$NON-NLS-1$
	public static final String HIVE_JDBC_DRIVER__0120_TEMPLATE_ID = "com.ibm.biginsights.hivejdbc.hiveDriverTemplate.0120"; //$NON-NLS-1$
  public static final String HIVE_JDBC_DRIVER__0140_TEMPLATE_ID = "com.ibm.biginsights.hivejdbc.hiveDriverTemplate.0140"; //$NON-NLS-1$
	public static final String HIVE_JDBC_DRIVER__090_OVERLAY_TEMPLATE_ID = "com.ibm.biginsights.hivejdbc.hiveDriverTemplate.090_overlay"; //$NON-NLS-1$
	public static final String BIGSQL_JDBC_DRIVER__21_TEMPLATE_ID = "com.ibm.biginsights.bigsqljdbc.bigsqlDriverTemplate.21"; //$NON-NLS-1$
	public static final String BIGSQL_JDBC_DRIVER__2101_TEMPLATE_ID = "com.ibm.biginsights.bigsqljdbc.bigsqlDriverTemplate.2101"; //$NON-NLS-1$
	public static final String BIGSQL_JDBC_DRIVER__211_TEMPLATE_ID = "com.ibm.biginsights.bigsqljdbc.bigsqlDriverTemplate.211"; //$NON-NLS-1$
	public static final String BIGSQL_JDBC_DRIVER__212_TEMPLATE_ID = "com.ibm.biginsights.bigsqljdbc.bigsqlDriverTemplate.212"; //$NON-NLS-1$
	public static final String BIGSQL_JDBC_DRIVER__300_TEMPLATE_ID = "com.ibm.biginsights.bigsqljdbc.bigsqlDriverTemplate.300"; //$NON-NLS-1$
	public static final String BIGSQL3_JDBC_DRIVER__300_TEMPLATE_ID = "com.ibm.biginsights.bigsqljdbc.bigsql3DriverTemplate.300"; //$NON-NLS-1$
	public static enum FILESYSTEM {HDFS, GPFS}; 
	
	private String _contextRoot = null;
	private String _locationName;
	private String _hostName;
	private int _port;
	private String _userName;
	private String _password;
	private boolean _savePassword;
	private String _version;
	private String _vendor = BIConstants.LOCATION_XML_VENDOR_IBM; //default
	private boolean _useSSL;	
	private String _url;
	private HttpClient _client;
	private List<String> roles = new ArrayList<String>();
	private BigInsightsLocationSession httpSession = new BigInsightsLocationSession();
	private int viewBy = BIConstants.VIEWBY_NAME;
	private int hiveport = 10000; //default
	private String bigSQLNode = null;
	private int bigSQLPort = 7052; //default
	private String bigSQL2Node = null;
	private int bigSQL2Port = 51000; //default
	private FILESYSTEM _fileSystem;
	private String _mountPoint;
	private boolean _isGPFSMounted=false;	// don't save this value - if the user mounted the GPFS later on, need to be able to recognize this 
	private String[] _linuxTaskControllerUsers=null; // user id that can impersonate other uses as configured in core-site.xml
	private boolean clearChildren=false;
	private BILocationContentProvider provider=null;
	private Boolean userFolderExists = null;
	private String _egoMasterList = null;	// psmr_host
	private int _egoKDPort = -1;	// psmr_port
	private String _egoSecPlugin = null; // psmr_security_config
	private boolean _isYarn = false;

	// constructor to create a BigInsightsLocation from stored XML file
	public BigInsightsLocation(File file)
	{
		this.loadFromXML(file);
	}
	
	public BigInsightsLocation(String locationName, String url,  
			String userName, String password, boolean savePassword)
	{
		this._locationName = locationName;
		this.setURL(url);		
		this._userName = userName;
		this._password = password;
		this._savePassword = savePassword;						
	}
	
	@Override
	public String getLocationName() {
		return _locationName;
	}
	
	@Override
	public String getLocationDisplayString() {		
		return (_locationName==null ? "" : _locationName+" - ")+ //$NON-NLS-1$ //$NON-NLS-2$
	        	_hostName+":"+_port+                             //$NON-NLS-1$
            (_version!=null && !_version.isEmpty() ? " - "+ getVersionWithVendor() : "")+  //$NON-NLS-1$ //$NON-NLS-2$
            (isYarn() ? " yarn":""); //$NON-NLS-1$ //$NON-NLS-2$ 
	}

	@Override
	public String getUserName() {		
		return _userName;
	}

	@Override
	public String getHostName() {
		return _hostName;
	}

	@Override
	public int getPort() {		
		return _port;
	}
	
	public String getPassword() {
		return _password;
	}

	public boolean getSavePassword() {
		return _savePassword;
	}

	@Override
	public void save(File file) {
		FileOutputStream fileOutputStream = null;
		try
		{
			fileOutputStream = new FileOutputStream(file);
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element locationXML = document.createElement(BIConstants.LOCATION_XML_LOCATION);
			document.appendChild(locationXML);
			
			locationXML.appendChild(document.createTextNode("\n")); //$NON-NLS-1$
			Element propertiesNode = document.createElement(BIConstants.LOCATION_XML_PROPERTIES);
			locationXML.appendChild(propertiesNode);
			
			Element nameNode = document.createElement(BIConstants.LOCATION_XML_LOCATION_NAME);
			nameNode.appendChild(document.createTextNode(_locationName));
			propertiesNode.appendChild(nameNode);
			
			if (_fileSystem!=null) {
				Element filesystemNode = document.createElement(BIConstants.LOCATION_XML_LOCATION_FILESYSTEM);
				filesystemNode.appendChild(document.createTextNode(_fileSystem.toString()));
				propertiesNode.appendChild(filesystemNode);
			}

			if (_mountPoint!=null) {
				Element mountPointNode = document.createElement(BIConstants.LOCATION_XML_LOCATION_MOUNTPOINT);
				mountPointNode.appendChild(document.createTextNode(_mountPoint.toString()));
				propertiesNode.appendChild(mountPointNode);
			}
			
			Element hivePortNode = document.createElement(BIConstants.LOCATION_XML_LOCATION_HIVEPORT);
			hivePortNode.appendChild(document.createTextNode(String.valueOf(hiveport)));
			propertiesNode.appendChild(hivePortNode);

			if (isVersion2100orAbove()) {
				Element bigsqlPortNode = document.createElement(BIConstants.LOCATION_XML_LOCATION_BIGSQLPORT);
				bigsqlPortNode.appendChild(document.createTextNode(String.valueOf(bigSQLPort)));
				propertiesNode.appendChild(bigsqlPortNode);
			
				Element bigsqlServerNode = document.createElement(BIConstants.LOCATION_XML_LOCATION_BIGSQLNODE);
				bigsqlServerNode.appendChild(document.createTextNode(String.valueOf(bigSQLNode)));
				propertiesNode.appendChild(bigsqlServerNode);
			}

			if (isVersion3000orAbove()) {
				Element bigsql2PortNode = document.createElement(BIConstants.LOCATION_XML_LOCATION_BIGSQL2PORT);
				bigsql2PortNode.appendChild(document.createTextNode(String.valueOf(bigSQL2Port)));
				propertiesNode.appendChild(bigsql2PortNode);
				Element bigsql2ServerNode = document.createElement(BIConstants.LOCATION_XML_LOCATION_BIGSQL2NODE);
				bigsql2ServerNode.appendChild(document.createTextNode(String.valueOf(bigSQL2Node)));
				propertiesNode.appendChild(bigsql2ServerNode);
				}
				
			Element urlNode = document.createElement(BIConstants.LOCATION_XML_LOCATION_URL);
			urlNode.appendChild(document.createTextNode(_url));
			propertiesNode.appendChild(urlNode);

			if (_version!=null){
				Element versionNode = document.createElement(BIConstants.LOCATION_XML_LOCATION_VERSION);
				versionNode.appendChild(document.createTextNode(_version));
				propertiesNode.appendChild(versionNode);
			}
			
			if (_vendor!=null){
				Element vendorNode = document.createElement(BIConstants.LOCATION_XML_LOCATION_VENDOR);
				vendorNode.appendChild(document.createTextNode(_vendor));
				propertiesNode.appendChild(vendorNode);
			}
			
			Element rolesNode = document.createElement(BIConstants.LOCATION_XML_LOCATION_ROLES);
			propertiesNode.appendChild(rolesNode);	
			for(Iterator<String> itr=getRoles().iterator(); itr.hasNext();){
				String role = itr.next();
				if(role != null && role.length()>0){
					Element roleNode = document.createElement(BIConstants.LOCATION_XML_LOCATION_ROLE);
					roleNode.appendChild(document.createTextNode(role));
					rolesNode.appendChild(roleNode);					
				}
			}

			if (_userName!=null) {
				Element userNameNode = document.createElement(BIConstants.LOCATION_XML_LOCATION_USERNAME);
				userNameNode.appendChild(document.createTextNode(_userName));
				propertiesNode.appendChild(userNameNode);
			}
						
			Element savePasswordNode = document.createElement(BIConstants.LOCATION_XML_LOCATION_SAVE_PASSWORD);
			savePasswordNode.appendChild(document.createTextNode(Boolean.toString(_savePassword)));
			propertiesNode.appendChild(savePasswordNode);
			
			Element viewbyNode = document.createElement(BIConstants.LOCATION_XML_VIEWBY);
			viewbyNode.appendChild(document.createTextNode(Integer.toString(viewBy)));
			propertiesNode.appendChild(viewbyNode);

			if (this._linuxTaskControllerUsers!=null && this._linuxTaskControllerUsers.length>0) {
				Element ltsUserNode = document.createElement(BIConstants.LOCATION_XML_LINUXTASKCONTOLLER_USER);
				ltsUserNode.appendChild(document.createTextNode(StringUtils.join(this._linuxTaskControllerUsers, ","))); //$NON-NLS-1$
				propertiesNode.appendChild(ltsUserNode);
			}

      Element isYarnNode = document.createElement(BIConstants.LOCATION_XML_LOCATION_YARN);
      isYarnNode.appendChild(document.createTextNode(Boolean.toString(_isYarn)));
      propertiesNode.appendChild(isYarnNode);
      
			if (isSymphony()) {
				// save EGO_MASTER_LIST, EGO_KD_PORT and EGO_SEC_PLUGIN per location, since it's not read from conf file when restarting the workspace
				if (_egoMasterList!=null) {
					Element egoMasterListNode = document.createElement(BIConstants.LOCATION_XML_LOCATION_EGO_MASTER_LIST);
					egoMasterListNode.appendChild(document.createTextNode(_egoMasterList));
					propertiesNode.appendChild(egoMasterListNode);
				}

				if (_egoKDPort>-1) {
					Element egoKDPortNode = document.createElement(BIConstants.LOCATION_XML_LOCATION_EGO_KD_PORT);
					egoKDPortNode.appendChild(document.createTextNode(new Integer(_egoKDPort).toString()));
					propertiesNode.appendChild(egoKDPortNode);
				}
				
				if (_egoSecPlugin!=null) {
					Element egoSecPluginNode = document.createElement(BIConstants.LOCATION_XML_LOCATION_EGO_SEC_PLUGIN);
					egoSecPluginNode.appendChild(document.createTextNode(_egoSecPlugin));
					propertiesNode.appendChild(egoSecPluginNode);
				}
			}

			ISecurePreferences nodePreferences = BigInsightsLocation.getSecurePreferenceForLocation(this.getLocationName());

			// if savePassword is true, save the pw in the secure storage
		    if (nodePreferences!=null && _password!=null) {
				if (this.getSavePassword()) {
				    try {
						nodePreferences.put(BIConstants.LOCATION_XML_USER_ID_KEY, this.getUserName(), false);
						nodePreferences.put(BIConstants.LOCATION_XML_PASSWORD_KEY, this.getPassword(), true);
					} catch (StorageException e) {				
						Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
					}
				}
				else {
					// if savePassword is false, delete the node from the secureStore
					nodePreferences.clear();
					try {
						nodePreferences.flush();
					}
					catch (IOException e) {
						Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
					}
				}
		    }

			locationXML.appendChild(document.createTextNode("\n")); //$NON-NLS-1$
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource domSource = new DOMSource(document);
			StreamResult streamResult = new StreamResult(fileOutputStream);
			transformer.transform(domSource, streamResult);
		    
			
		}
		catch(Exception ex)
		{
			MessageDialog.openError(null, Messages.BIGINSIGHTSLOCATION_SAVE_ERROR, ex.toString());
		}
		finally {
			try {
				fileOutputStream.close();
			}
			catch(Exception e) {}
		}
	}

	public boolean loadFromXML(File file) {
		
		Document document = null;
		
		DocumentBuilder builder;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			document = builder.parse(file);
		} catch (Exception e) { 
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
			return false;
		}
		
		Element root = document.getDocumentElement();
		if (BIConstants.LOCATION_XML_LOCATION.equals(root.getTagName()))
		{
			NodeList nodeList = root.getChildNodes();
			for (int i=0;i<nodeList.getLength();i++)
			{
				Node elementNode = nodeList.item(i);
				if (elementNode instanceof Element)
				{
					NodeList propertyNodes = elementNode.getChildNodes();
					for (int j=0;j<propertyNodes.getLength();j++)
					{
						Node propertyNode = propertyNodes.item(j);
						if (propertyNode instanceof Element)
						{
							Element prop = (Element)propertyNode;
							if (prop.getTagName().equals(BIConstants.LOCATION_XML_LOCATION_NAME) && prop.hasChildNodes())								
								this._locationName = ((Text)prop.getFirstChild()).getData();
							else if (prop.getTagName().equals(BIConstants.LOCATION_XML_LOCATION_URL) && prop.hasChildNodes())								
								this.setURL(((Text)prop.getFirstChild()).getData());							
							else if (prop.getTagName().equals(BIConstants.LOCATION_XML_LOCATION_USERNAME) && prop.hasChildNodes())
								this._userName = ((Text)prop.getFirstChild()).getData();
							else if (prop.getTagName().equals(BIConstants.LOCATION_XML_LOCATION_CONTEXTROOT) && prop.hasChildNodes())
								this._contextRoot = ((Text)prop.getFirstChild()).getData();
							else if (prop.getTagName().equals(BIConstants.LOCATION_XML_LOCATION_VERSION) && prop.hasChildNodes())
								this._version = ((Text)prop.getFirstChild()).getData();
							else if (prop.getTagName().equals(BIConstants.LOCATION_XML_LOCATION_VENDOR) && prop.hasChildNodes())
								this._vendor = ((Text)prop.getFirstChild()).getData();
							else if (prop.getTagName().equals(BIConstants.LOCATION_XML_LOCATION_FILESYSTEM) && prop.hasChildNodes()) {
								String nodeValue = ((Text)prop.getFirstChild()).getData();
								if (nodeValue!=null)
									this._fileSystem = FILESYSTEM.valueOf(nodeValue);
							}
							else if (prop.getTagName().equals(BIConstants.LOCATION_XML_LOCATION_MOUNTPOINT) && prop.hasChildNodes()) {
								this._mountPoint = ((Text)prop.getFirstChild()).getData();
								// when we load the connections again, check if the GPFS is (still) mounted
								this._isGPFSMounted = determineGPFSMounted(this._mountPoint);
							}
							else if (prop.getTagName().equals(BIConstants.LOCATION_XML_LINUXTASKCONTOLLER_USER) && prop.hasChildNodes()) {
								String values = ((Text)prop.getFirstChild()).getData();								
								this._linuxTaskControllerUsers = values.split(","); //$NON-NLS-1$
							}
							else if (prop.getTagName().equals(BIConstants.LOCATION_XML_LOCATION_EGO_MASTER_LIST) && prop.hasChildNodes()) {														
								this._egoMasterList = ((Text)prop.getFirstChild()).getData();
							}
							else if (prop.getTagName().equals(BIConstants.LOCATION_XML_LOCATION_EGO_KD_PORT) && prop.hasChildNodes()) {
								String portString = ((Text)prop.getFirstChild()).getData();
								this._egoKDPort = Integer.valueOf(portString).intValue();
							}
							else if (prop.getTagName().equals(BIConstants.LOCATION_XML_LOCATION_EGO_SEC_PLUGIN) && prop.hasChildNodes()) {														
								this._egoSecPlugin = ((Text)prop.getFirstChild()).getData();
							}
							else if (prop.getTagName().equals(BIConstants.LOCATION_XML_LOCATION_HIVEPORT) && prop.hasChildNodes()){
								String portString = ((Text)prop.getFirstChild()).getData();
								this.hiveport = Integer.valueOf(portString).intValue();
							}
							else if (prop.getTagName().equals(BIConstants.LOCATION_XML_LOCATION_BIGSQLPORT) && prop.hasChildNodes()){
								String portString = ((Text)prop.getFirstChild()).getData();
								this.bigSQLPort = Integer.valueOf(portString).intValue();
							}
							else if (prop.getTagName().equals(BIConstants.LOCATION_XML_LOCATION_BIGSQLNODE) && prop.hasChildNodes()) {														
								this.bigSQLNode = ((Text)prop.getFirstChild()).getData();
							}
							else if (prop.getTagName().equals(BIConstants.LOCATION_XML_LOCATION_BIGSQL2PORT) && prop.hasChildNodes()){
								String portString = ((Text)prop.getFirstChild()).getData();
								this.bigSQL2Port = Integer.valueOf(portString).intValue();
							}
							else if (prop.getTagName().equals(BIConstants.LOCATION_XML_LOCATION_BIGSQL2NODE) && prop.hasChildNodes()) {
								this.bigSQL2Node = ((Text)prop.getFirstChild()).getData();
							}
							else if (prop.getTagName().equals(BIConstants.LOCATION_XML_LOCATION_SAVE_PASSWORD) && prop.hasChildNodes())
							{
								this._savePassword = new Boolean(((Text)prop.getFirstChild()).getData());
								// if password is saved, restore it from the secure storage now
								if (this._savePassword)
								{
									ISecurePreferences nodePreferences = BigInsightsLocation.getSecurePreferenceForLocation(this.getLocationName());
								    try {
								    	String userId = nodePreferences.get(BIConstants.LOCATION_XML_USER_ID_KEY, null);
								    	if (userId!=null && userId.equals(this._userName))
								    	{
								    		this._password = nodePreferences.get(BIConstants.LOCATION_XML_PASSWORD_KEY, null);	
								    	}										
									} catch (StorageException e) {				
										Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
									}
								}
							}else if (prop.getTagName().equals(BIConstants.LOCATION_XML_VIEWBY) && prop.hasChildNodes()){								
								this.viewBy = new Integer(((Text)prop.getFirstChild()).getData());									
							}else if(prop.getTagName().equals(BIConstants.LOCATION_XML_LOCATION_ROLES) && prop.hasChildNodes()){
								NodeList roleNodeList = prop.getChildNodes();
								for(int k=0; k < roleNodeList.getLength(); k++){
									Node roleNode = roleNodeList.item(k);
									String role = roleNode.getTextContent();
									if(role != null && role.length()>0){
										getRoles().add(role);
									}
								}
							}
              else if (prop.getTagName().equals(BIConstants.LOCATION_XML_LOCATION_YARN) && prop.hasChildNodes())
                this._isYarn = new Boolean(((Text)prop.getFirstChild()).getData());
						}
					}
				}
			}
		}

		return true;
	}
	
	@Override
	public void dispose() {
		// TODO add any clean-up operations
		
	}

	@Override
	public IBigInsightsLocationNode[] getChildren() { 
		IBigInsightsLocationNode[] folders = new IBigInsightsLocationNode[]{new BigInsightsAppFolder(this), new BigInsightsConfFolder(this)};
		
	    return folders;
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasChildren() {		
		return true;
	}

	public String toString()
	{
		return this.getLocationDisplayString();
//		return this.getLocationDisplayString() +" "+ (_connectionStatus!=null ? _connectionStatus : "");
	}

	@Override
	public void setLocationName(String value) {
		this._locationName = value;
	}

	@Override
	public void setUserName(String value) {
		this._userName = value;
	}

	@Override
	public void setPassword(String value) {
		this._password = value;		
	}

	@Override
	public void setSavePassword(boolean value) {
		this._savePassword = value;		
	}

	@Override
	public void initWithLocation(IBigInsightsLocation copyLocation) {
		this._locationName = copyLocation.getLocationName();
		this._hostName = copyLocation.getHostName();
		this._port = copyLocation.getPort();
		this._userName = copyLocation.getUserName();
		this._password = copyLocation.getPassword();		
		this._savePassword = copyLocation.getSavePassword();
		this._useSSL = copyLocation.getUseSSL();
		this._contextRoot = copyLocation.getContextRoot();
		this._version = copyLocation.getVersion();
		this._vendor = copyLocation.getVendor();
		this._url = copyLocation.getURL();
		this.hiveport = copyLocation.getHivePort();
		this.bigSQLPort = copyLocation.getBigSQLPort();
		this.bigSQLNode = copyLocation.getBigSQLNode();
		this.bigSQL2Port = copyLocation.getBigSQL2Port();
		this.bigSQL2Node = copyLocation.getBigSQL2Node();
		this.viewBy = copyLocation.getViewBy();
		this._fileSystem = copyLocation.getFileSystem();
		this._mountPoint = copyLocation.getMountpoint();
		this._isGPFSMounted = copyLocation.isGPFSMounted();
		this._linuxTaskControllerUsers = copyLocation.getLinuxTaskControllerUsers();
		this._egoMasterList = copyLocation.getEGO_MASTER_LIST();
		this._egoKDPort = copyLocation.getEGO_KD_PORT();
		this._egoSecPlugin = copyLocation.getEGO_SEC_PLUGIN();
    this._isYarn = copyLocation.isYarn();
	}
	
	public static ISecurePreferences getSecurePreferenceForLocation(String locationName) {
		ISecurePreferences securePreferences = SecurePreferencesFactory.getDefault();
	    String locationKey = EncodingUtils.encodeSlashes(locationName);
	    String path = Activator.PREFERENCES_NODE + locationKey;      
	    return securePreferences.node(path);
	}

	public static TestConnectionResult testConnection(String url, String userName, String password)
	{
		IBigInsightsLocation testLocation = new BigInsightsLocation(null, url, userName, password, false);
		return testLocation.testConnection(false);
	}
	
	@Override
	public TestConnectionResult testConnection(boolean retryConnection) {				
		TestConnectionResult testResult = null;
		
		int statusCode = -1;
		String returnMessage = null;
		boolean isAuthenticationNeeded = isAuthenticationNeeded();
		
		if(isAuthenticationNeeded && (this._userName == null || this._userName.length()==0 ||
		   this._password == null || this._password.length()==0)){
			Thread uithread = Display.getDefault().getThread();
			if(Thread.currentThread().getId() == uithread.getId()){
				showUserIdPasswordDialog();
			}else{
				Display.getDefault().syncExec(new Runnable(){
					@Override
					public void run() {
						showUserIdPasswordDialog();						
					}
				});
			}
			if( _userName == null || _userName.length()==0 ||
				_password == null || _password.length()==0){
				return new TestConnectionResult(false, -1, Messages.LOCATIONSERVERAUTHFAILED);	
			}
		}
		
		HttpClient httpClient=null;
		try {
			httpClient = this.getHttpClient();
		} catch (BIConnectionException e) {
			if(e.reason == BIConnectionException.REASON_INVALID_USERIDPW){
				return new TestConnectionResult(false, -1, Messages.LOCATIONSERVERAUTHFAILED);
			}else if(e.reason == BIConnectionException.REASON_INVALID_ROLE){
				return new TestConnectionResult(false, -1, Messages.LOCATIONWIZARDBASICDATAPAGE_ROLE_ERROR);
			}else if(e.reason == BIConnectionException.REASON_INVALID_HTTPERROR){
				return new TestConnectionResult(false, e.httpReturnCode, Messages.LOCATIONWIZARDBASICDATAPAGE_CONN_ERROR_DESC + " "+ e.httpReturnMessage); //$NON-NLS-1$
			}
		}
		if(httpClient == null){
			return new TestConnectionResult(false, -1, Messages.LOCATIONSERVERAUTHFAILED);
		}
		
		GetMethod getMethod = new GetMethod(this._url);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(1, false));
		Authenticator.setSessionCookie(getMethod, this);
		
		try {
			statusCode = httpClient.executeMethod(getMethod);
		}
		catch (IOException ex) {				
			returnMessage = ex.toString();
		}
		finally {
			getMethod.releaseConnection();
		}
		
		// return the testResult from the recursive calls or create a new result if only one test was run
		TestConnectionResult returnResult = testResult!=null ? testResult : 
						new TestConnectionResult(statusCode==HttpStatus.SC_OK, statusCode, returnMessage);
		returnResult.templocation = this;
//		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss MM-dd"); //$NON-NLS-1$
//		String connectionStatus = Messages.BIGINSIGHTSLOCATION_LAST_CONN+" ("+(statusCode==HttpStatus.SC_OK ? Messages.BIGINSIGHTSLOCATION_SUCCESS : Messages.BIGINSIGHTSLOCATION_ERROR)+
//					formatter.format(Calendar.getInstance().getTime())+")"; //$NON-NLS-1$
		
		return returnResult;
	}
	
	public void checkRoles() {	
		// TODO: handle user id to check if authentication works
		int statusCode = -1;	
		
		HttpClient httpClient = null;
		try {
			httpClient = this.getHttpClient();
		} catch (BIConnectionException e1) {
			return;
		}
		
		if(httpClient == null){
			return;
		}
		
		GetMethod method = new GetMethod(generateAbsoluteURL(BIConstants.URL_USERINFO));
		Authenticator.setSessionCookie(method, this);
		try {
			statusCode = httpClient.executeMethod(method);	
			InputStream response = method.getResponseBodyAsStream();
			
			if(statusCode==HttpStatus.SC_OK ){				
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				Document doc = null;
				try {			
					DocumentBuilder builder = factory.newDocumentBuilder();
					doc = builder.parse(response);
				} catch (ParserConfigurationException e) {
					 throw new RuntimeException(e);
				} catch (SAXParseException e) {
					 throw new RuntimeException(e);
				} catch (SAXException e) {					
					 throw new RuntimeException(e);
				} catch (IOException e) {
					 throw new RuntimeException(e);
				}
					
				roles.clear();
				if(doc != null){
					NodeList roleNodes = doc.getElementsByTagName("role"); //$NON-NLS-1$
					for(int i=0; i < roleNodes.getLength() ;i++){
						Node roleNode = roleNodes.item(i);
						String role = roleNode.getTextContent();
						if(role != null && role.length()>0){
							roles.add(role);
						}
					}
				}
				
			}
		}catch (IOException ex) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, ex.getMessage()));
		}
		
	}
	
	public int showUserIdPasswordDialog() {
		PasswordDialog dlg = new PasswordDialog(null, this);
		int result = dlg.open();
		if (result==Window.OK) {
			this.setUserName(dlg.getUserId());
			this.setPassword(dlg.getPassword());
			this.setSavePassword(dlg.getSavePassword());
		}	
		return result;
	}
	
	public class TestConnectionResult {
		public int statusCode;
		public boolean success;
		public String statusMessage;
		public BigInsightsLocation templocation;
		
		public TestConnectionResult(boolean success, int statusCode, String statusMessage) {
			this.success = success;
			this.statusCode = statusCode;
			this.statusMessage = statusMessage;
		}
	}
	
	public String getProtocol() {
		return _useSSL ? BIConstants.HTTPS : BIConstants.HTTP;
	}

	public String getVersion() {		
		return _version;
	}

	public String getVersionWithVendor() {
		if (this.isOverlay())
		return _version+" "+_vendor; //$NON-NLS-1$
		else
			return _version;
	}
	
	public String getVendor() {
		return _vendor;
	}
	
	public boolean isOverlay(){
		return _vendor!=null && !_vendor.isEmpty() && !_vendor.equalsIgnoreCase(BIConstants.LOCATION_XML_VENDOR_IBM) &&
		!_vendor.equalsIgnoreCase(BIConstants.LOCATION_XML_VENDOR_SYMPHONY);
	}
	
	@Override
	public boolean isSymphony() {
		return _vendor!=null && !_vendor.isEmpty() && _vendor.equalsIgnoreCase(BIConstants.LOCATION_XML_VENDOR_SYMPHONY);		
	}

	@Override
	public boolean getUseSSL() {		
		return _useSSL;
	}

	@Override
	public void setUseSSL(boolean value) {
		this._useSSL = value;		
	}

	@Override
	public void setURL(String value) {
		this._url = value;
		URL checkURL;
		try {
			checkURL = new URL(this._url);
			_hostName = checkURL.getHost();
			_port = checkURL.getPort();
			_useSSL = checkURL.getProtocol().equals(BIConstants.HTTPS);	
			
			String path = checkURL.getPath();
			if(path == null || path.isEmpty() || path.equals("/")){ //$NON-NLS-1$
				//http(s)://host[:port] 
				//add the default path to the url
				path = BIConstants.URL_CONTEXT_ROOT + BIConstants.URL_DIRECT_ROOT + BIConstants.URL_INDEX_HTML;					
				_url = new URL(checkURL.getProtocol(), checkURL.getHost(), checkURL.getPort(), path).toString();
			}
			
			_contextRoot = (path.indexOf(BIConstants.URL_CONTEXT_ROOT)>-1) ? BIConstants.URL_CONTEXT_ROOT : ""; // set empty context root for dev env //$NON-NLS-1$
		} catch (MalformedURLException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
		}				
	}

	public boolean retrieveConfigurationFiles() {	
		boolean result = false;
		int statusCode = -1;
		String returnMessage = null;		
		
		
		HttpClient httpClient=null;
		try {
			httpClient = this.getHttpClient();
		} catch (BIConnectionException e) {
			handleBIConnectionExceptionFromThread(e);
			return result;
		}
		if(httpClient == null){
			return result;
		}
		
		String uri = generateAbsoluteURL(BIConstants.URL_CONFIGURATIONS);
		GetMethod getMethod = new GetMethod(uri);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(1, false));
		Authenticator.setSessionCookie(getMethod, this);
		try {
			statusCode = httpClient.executeMethod(getMethod);			
			result = statusCode==HttpStatus.SC_OK;
			if (!result)
			{				
				returnMessage = getMethod.getStatusText();				
			}
			else {
				// get the zip file and put the extracted content in the metadata folder of the plugin
				InputStream inputStream = getMethod.getResponseBodyAsStream();				
				File zipFileFromServer = new File(LocationRegistry.getLocationTargetDirectory(this), BIConstants.CONF_ZIP); 
				FileOutputStream out = new FileOutputStream(zipFileFromServer);
				byte buffer[] = new byte[1024];
				int l;
				while((l=inputStream.read(buffer))>0) {
					out.write(buffer, 0, l);					
				}
				out.close();
				inputStream.close();
				
				// extract the temporary zip file 
				returnMessage = extractConfigurationFiles(zipFileFromServer);
				result = returnMessage==null; // result is false if error message is returned from extractConfigurationFiles 
					
				// delete the temporary zip file
				zipFileFromServer.delete();		
				
				if (result) {
					// reset previously set values 
					_mountPoint = null;
					_isGPFSMounted=false;	 
					_linuxTaskControllerUsers=null;

					// if files were retrieved properly, set the flag for the filesystem of the location					
					File coreSiteFile = new File(LocationRegistry.getHadoopConfLocation(this)+"/core-site.xml"); ////$NON-NLS-1$			
					DocumentBuilder builder;
          Document coreDocument = null;
          try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            coreDocument = builder.parse(coreSiteFile);
            
            if (coreDocument!=null) {
              NodeList list = coreDocument.getElementsByTagName("property"); //$NON-NLS-1$
							for (int i=0; i<list.getLength(); i++) {
								Node node = list.item(i);
								if (node instanceof Element)
								{
									String nodeValue = getXMLNodeValue((Element)node, "name"); //$NON-NLS-1$
									if (nodeValue!=null) {
                    // fs.default.name is being deprecated, the newer keyword is fs.defaultFS
                    if ("fs.defaultFS".equals(nodeValue)) { //$NON-NLS-1$
                      String value = getXMLNodeValue((Element)node, "value"); //$NON-NLS-1$
                      this._fileSystem = FILESYSTEM.valueOf(value.substring(0, 4).toUpperCase());                   
                    }
                    else if ("fs.default.name".equals(nodeValue)) { //$NON-NLS-1$
											String value = getXMLNodeValue((Element)node, "value"); //$NON-NLS-1$
											this._fileSystem = FILESYSTEM.valueOf(value.substring(0, 4).toUpperCase());										
										}
										else if ("gpfs.mount.dir".equals(nodeValue)) { //$NON-NLS-1$
											String value = getXMLNodeValue((Element)node, "value"); //$NON-NLS-1$
											this._mountPoint = value;
											this._isGPFSMounted = determineGPFSMounted(this._mountPoint);
										}
										else if (nodeValue.startsWith("hadoop.proxyuser.") && nodeValue.endsWith("groups")) { //$NON-NLS-1$ //$NON-NLS-2$
											String value = nodeValue.substring("hadoop.proxyuser.".length()); //$NON-NLS-1$
											addLinuxTaskControllerUser(value.substring(0, value.indexOf(".")));											 //$NON-NLS-1$
										}
									}
								}
							}
						}
					} catch (Exception e) { 
						Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));						
					}

	        // Set the "yarn" flag for the location
          File mapredSiteFile = new File(LocationRegistry.getHadoopConfLocation(this) + "/mapred-site.xml"); ////$NON-NLS-1$      
          Document mapredDocument = null;
          try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            mapredDocument = builder.parse(mapredSiteFile);
            
            if (mapredDocument!=null) {
              NodeList list = mapredDocument.getElementsByTagName("property"); //$NON-NLS-1$
              for (int i=0; i<list.getLength(); i++) {
                Node node = list.item(i);
                if (node instanceof Element)
                {
                  String nodeValue = getXMLNodeValue((Element)node, "name"); //$NON-NLS-1$
                  if (nodeValue!=null) {
                    if ("mapreduce.framework.name".equals(nodeValue)) { //$NON-NLS-1$
                      String value = getXMLNodeValue((Element)node, "value"); //$NON-NLS-1$
                      this._isYarn = BIConstants.LOCATION_XML_LOCATION_YARN.equalsIgnoreCase(value); //$NON-NLS-1$
                      break;
                    }
                  }
                }
              }
            }
          } catch (Exception e) { 
            Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));            
          }
          

					// retrieve information for psmr from ego.conf file
					if (isSymphony()) {
						File psmrConfFile = new File(LocationRegistry.getPSMRConfLocation(this)+"/"+BIConstants.PSMR_CONF_FILE); ////$NON-NLS-1$
						if (psmrConfFile.exists()) {							
							InputStream inputStreamConfFile = new FileInputStream(psmrConfFile);
							try {
								Properties properties = new Properties(); 
								properties.load(inputStreamConfFile);								
								
								// set psmr values from ego.conf value when retrieving conf files
								// EGO_MASTER_LIST
								String egoMasterList = (String)properties.getProperty(BIConstants.PSMR_EGO_MASTER_LIST);
								// have to remove the double-quotes around the node
								if (egoMasterList!=null && egoMasterList.startsWith("\"") && egoMasterList.endsWith("\""))
									egoMasterList = egoMasterList.substring(1, egoMasterList.length()-1);
								this._egoMasterList = (egoMasterList!=null && !egoMasterList.isEmpty()) ?
																egoMasterList : getHostName();
								// EGO_KD_PORT
								String egoKDPort = (String)properties.getProperty(BIConstants.PSMR_EGO_KD_PORT);
								try {
									this._egoKDPort = new Integer(egoKDPort);
								}
								catch(NumberFormatException ex) {
									// if not able to read from conf file, read it from file under com.ibm.psmr.client for that particular version		
									// for now, the EGO_KD_PORT can't be changed during the BI install
									_egoKDPort = BigInsightsLibraryContainerInitializer.getInstance().getSymphonyDefaultEgoKDPort(getVersionWithVendor());
								}
								// EGO_SEC_PLUGIN
								String egoSecPlugin = (String)properties.getProperty(BIConstants.PSMR_EGO_SEC_PLUGIN);
								// if not able to read from conf file, read it from file under com.ibm.psmr.client for that particular version
								// for now, the security plugin is static for BI install
								this._egoSecPlugin = (egoSecPlugin!=null && !egoSecPlugin.isEmpty()) ?
															egoSecPlugin : BigInsightsLibraryContainerInitializer.getInstance().getSymphonyDefaultSecurityPlugin(getVersionWithVendor());
							}
							catch(Exception ex) {
								Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, ex.getMessage()));
							}
							finally {
								try {
									if (inputStreamConfFile!=null)
										inputStreamConfFile.close();
								}
								catch(Exception e) {}
							}
														
							
						}
					}
				}
			}
		}
		catch (IOException ex) {	
			result = false;
			returnMessage = ex.toString();
		}
		finally {
			getMethod.releaseConnection();
		}

		if (!result) {
			MessageDialog.openError(Activator.getActiveWorkbenchShell(), Messages.BIGINSIGHTSLOCATION_CONF_ERROR_TITLE, Messages.BIGINSIGHTSLOCATION_CONF_ERROR_DESC+"\n"+returnMessage); //$NON-NLS-1$
		}
		return result;
	}
	
	private void addLinuxTaskControllerUser(String value) {
		if (value!=null) {
			if (this._linuxTaskControllerUsers==null) {
				this._linuxTaskControllerUsers = new String[]{value};
			}
			else {
				String[] newArray = new String[this._linuxTaskControllerUsers.length+1];
				System.arraycopy(this._linuxTaskControllerUsers, 0, newArray, 0, this._linuxTaskControllerUsers.length);
				newArray[newArray.length-1] = value;
				this._linuxTaskControllerUsers = newArray;
			}
		}
	}
	
	private String getXMLNodeValue(Element element, String tagName) {
		NodeList list = element.getElementsByTagName(tagName);
		if (list.getLength()>0) {
			NodeList valueNodeList = list.item(0).getChildNodes();
			return ((Node)valueNodeList.item(0)).getNodeValue();
		}
		return null;
	}
	
	private String extractConfigurationFiles(File zipFile) throws IOException {
		String result = null;
		// after that extract the file and remove the zip
		ZipInputStream zipInputStream = null;
		FileOutputStream fileOutputStream = null;
		try {
			zipInputStream = new ZipInputStream(new FileInputStream(zipFile));
			ZipEntry zipEntry = null;
			byte[] buffer = new byte[1024];
			while ((zipEntry=zipInputStream.getNextEntry())!=null) {
				String entry  = zipEntry.getName();
				File newFile = new File(entry);
				
				// first make the file readable in case it exists
				File f = new File(LocationRegistry.getLocationTargetDirectory(this)+"/"+entry); //$NON-NLS-1$
				if (f.exists())
					f.setWritable(true);
				
				String directory = newFile.getParent();
				if (directory==null) {
					if (newFile.isDirectory()) {
						File dir = new File(LocationRegistry.getLocationTargetDirectory(this)+"/"+newFile.getName()); //$NON-NLS-1$
						if (!dir.exists())
							dir.mkdirs();
						break;
					}
				}
				else {
					File dir = new File(LocationRegistry.getLocationTargetDirectory(this)+"/"+directory); //$NON-NLS-1$
					if (!dir.exists())
						dir.mkdirs();
				}
				
				fileOutputStream = new FileOutputStream(LocationRegistry.getLocationTargetDirectory(this)+"/"+entry); //$NON-NLS-1$
				int length;
				while ((length=zipInputStream.read(buffer, 0, 1024))>-1)
					fileOutputStream.write(buffer, 0, length);
								
				fileOutputStream.close();
				zipInputStream.closeEntry();
				// make the file read-only				
				if (f.exists())
					f.setWritable(false);
			}
		}
		catch (Exception e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
			result = e.getMessage();
		}
		finally {
			if (zipInputStream!=null)
				zipInputStream.close();
			if (fileOutputStream!=null)
				fileOutputStream.close();
		}
		return result;
	}

	@Override
	public void retrieveVersion() {

		int statusCode = -1;
		String returnMessage = null;		
		boolean result = false;
		
		HttpClient httpClient=null;
		try {
			httpClient = this.getHttpClient();
		} catch (BIConnectionException e) {
			handleBIConnectionExceptionFromThread(e);
			return;
		}
		if(httpClient == null){
			return;
		}
		
		
		String uri = generateAbsoluteURL(BIConstants.URL_CONFIGURATIONS_VERSION);
		GetMethod getMethod = new GetMethod(uri);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(1, false));
		Authenticator.setSessionCookie(getMethod, this);
		try {
			statusCode = httpClient.executeMethod(getMethod);			
			result = statusCode==HttpStatus.SC_OK;
			if (!result)
			{				
				returnMessage = getMethod.getStatusText();				
			}
			else {
				// get the version number and set it in the location object
				String version = getMethod.getResponseBodyAsString();
				if (version!=null && !version.isEmpty()) {
					// for v1.3: v1.3.0.0
					// for FP1: IBM InfoSphere BigInsights Enterprise Edition v1.3.0.1
					Matcher m = PROD_VER_BUILD_PTN.matcher(version);
					if (m.matches()) 
						this._version = m.group(2);
				}
			}
		}
		catch (IOException ex) {				
			returnMessage = ex.toString();
		}
		finally {
			getMethod.releaseConnection();
		}
		
		if (!result) {
			MessageDialog.openError(Activator.getActiveWorkbenchShell(), Messages.BIGINSIGHTSLOCATION_VERSION_ERROR_TITLE, Messages.BIGINSIGHTSLOCATION_VERSION_ERROR_DESC+"\n"+returnMessage); //$NON-NLS-1$
		}
	}

	@Override
	public String retrieveUgi() {

		String ugi = null;
		int statusCode = -1;
		String returnMessage = null;		
		boolean result = false;
		
		HttpClient httpClient=null;
		try {
			httpClient = this.getHttpClient();
		} catch (BIConnectionException e) {
			handleBIConnectionExceptionFromThread(e);
			return ugi;
		}
		if(httpClient == null){
			return ugi;
		}
		
		
		String uri = generateAbsoluteURL(BIConstants.URL_HADOOP_UGI);
		GetMethod getMethod = new GetMethod(uri);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(1, false));
		Authenticator.setSessionCookie(getMethod, this);
		try {
			statusCode = httpClient.executeMethod(getMethod);			
			result = statusCode==HttpStatus.SC_OK;
			if (!result)
			{				
				returnMessage = getMethod.getStatusText();				
			}
			else {
				// get the ugi 
				ugi = getMethod.getResponseBodyAsString();
			}
		}
		catch (IOException ex) {				
			returnMessage = ex.toString();
		}
		finally {
			getMethod.releaseConnection();
		}
		
		if (!result) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, returnMessage));
		}
		return ugi;
	}

	@Override
	public void retrieveVendor() {
		int statusCode = -1;
		String returnMessage = null;		
		boolean result = false;
		
		HttpClient httpClient=null;
		try {
			httpClient = this.getHttpClient();
		} catch (BIConnectionException e) {
			handleBIConnectionExceptionFromThread(e);
			return;
		}
		if(httpClient == null){
			return;
		}
		
		
		String uri = generateAbsoluteURL(BIConstants.URL_CONFIGURATIONS_VENDOR);
		GetMethod getMethod = new GetMethod(uri);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(1, false));
		Authenticator.setSessionCookie(getMethod, this);
		try {
			statusCode = httpClient.executeMethod(getMethod);			
			result = statusCode==HttpStatus.SC_OK;
			if (!result)
			{				
				returnMessage = getMethod.getStatusText();				
			}
			else {
				// get the vendor string and set it in the location object
				this._vendor = getMethod.getResponseBodyAsString();
				if (this._vendor==null || this._vendor.isEmpty())
					this._vendor = BIConstants.LOCATION_XML_VENDOR_IBM;
			}
		}
		catch (IOException ex) {				
			returnMessage = ex.toString();
		}
		finally {
			getMethod.releaseConnection();
		}
		
		if (!result) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, returnMessage));
		}
	}
		
	@Override
	public BILocationContentProvider getContentProvider() {		
		return provider;
	}

	@Override
	public void setContentProvider(BILocationContentProvider biProvider) {
		provider = biProvider;		
	}
	
	public String generateAbsoluteURL(String relativeURL) {
		
		String uri;
		try {
			URL checkURL = new URL(this._url);
			uri = new URL(checkURL.getProtocol(), checkURL.getHost(), checkURL.getPort(), this.getContextRoot() + relativeURL).toString();		
		} catch (MalformedURLException e) {
			uri = this.getProtocol() +"://"+ this.getHostName()+":"+this.getPort() + this.getContextRoot() + relativeURL;//$NON-NLS-1$ //$NON-NLS-2$
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));		
		}
		return uri;
	}

	@Override
	public String getContextRoot() {		
		return _contextRoot;
	}

	@Override
	public String getURL() {
		return _url;
	}
	/**
	 * admin can do everything 
	 */
	public boolean isAdmin(){		
		if(roles.contains(BIConstants.ROLE_BIGINSIGHTS_APPLICATION_ADMINISTRATOR)){
			return true;
		}
		return false;
	}
	
	public boolean isAdminOrUser(){		
		if(isAdmin() || isUser()){
			return true;
		}
		return false;
	}
	
	/**
	 * Users are allowed to publish apps, replace apps, 
	 * get list of apps will only return apps for the groups they are member of
	 * @return
	 */
	public boolean isUser(){		
		if(roles.contains(BIConstants.ROLE_BIGINSIGHTS_USER)){
			return true;
		}
		return false;
	}
	
	public List<String> getRoles() {
		return roles;
	}

	public  void resetHttpClient(){
		getHttpSession().setCookies(null);		
		getHttpSession().setLastServerRequest(null);
		_client = null;
	}
	
	public HttpClient cancelableGetHttpClient(IProgressMonitor monitor) throws BIConnectionException{
		
		
		class ConnectJob extends Job{
			HttpClient hc=null;
			boolean done=false;
			BIConnectionException error = null;
			
			ConnectJob (){
				super("connect"); //$NON-NLS-1$
			}
			@Override
			protected IStatus run(IProgressMonitor monitor) {
						try {
							hc = getHttpClient();
						} catch (BIConnectionException e) {
							error = e;
						}
						done=true;	
				return Status.OK_STATUS;
			}
			public HttpClient getClient(){
				return hc;
			}
			public boolean isDone(){
				return done;
			}
			public BIConnectionException getError(){
				return error;
			}
		};
		ConnectJob job = new ConnectJob();

		// Start the Job
		job.schedule();
		
		while(!job.isDone()){
			if(monitor.isCanceled()){
				throw new RuntimeException(new InterruptedException(Messages.SERVER_CALL_CANCELED));
			}
		}
		if(job.getError()!=null){
			throw job.getError();
		}
		return job.getClient();
		
	}
	
	
	/**
	 * create the HttpClient with credentials 
	 * @return HttpClient
	 */
	public HttpClient getHttpClient() throws BIConnectionException{
		
		if (this._client==null) 
			_client = new HttpClient();
		
		//is authentication needed?
		if(httpSession.isAuthenticationNeeded(this)){
			httpSession.authenticate(_client, this );
			
			if(!isAdminOrUser() || this.httpSession.getCookies() == null){
				//userid was not an admin/user or bad userid/pw
				this._password = null;
				this.httpSession.setCookies(null);				
				_client = null;
				if(!isAdminOrUser() && !this.roles.isEmpty()){
					BIConnectionException e = new BIConnectionException(BIConnectionException.REASON_INVALID_ROLE);
					throw e;
				}else{
					BIConnectionException e = new BIConnectionException(BIConnectionException.REASON_INVALID_USERIDPW);
					throw e;
				}
			}
		}
		return _client;
	}

	public BigInsightsLocationSession getHttpSession() {
		return httpSession;
	}
	
	public boolean isAuthenticationNeeded() {
		return httpSession.isAuthenticationNeeded(this);
	}

	public void handleBIConnectionExceptionFromThread(BIConnectionException e){
		Display.getDefault().syncExec(new ShowError(e));
	}
	
	public class ShowError implements Runnable{
		private Exception exp;
		public ShowError (Exception _exp){
			exp = _exp;
		}
		@Override
		public void run() {
			String msg = ""; //$NON-NLS-1$
			if(exp instanceof BIConnectionException){
				BIConnectionException biError = (BIConnectionException)exp;
				msg = biError.getMessage();
				if( biError.reason == BIConnectionException.REASON_INVALID_USERIDPW){					
					_password = null;
				}
			}else{
				msg = Messages.LOCATIONWIZARDBASICDATAPAGE_CONN_ERROR_DESC + " " + exp.getMessage(); //$NON-NLS-1$
			}
			MessageDialog.openError(Activator.getActiveWorkbenchShell(), Messages.BIGINSIGHTSLOCATION_CONF_ERROR_TITLE, msg ); //$NON-NLS-1$
		}
		
	}

	public int getViewBy() {
		return viewBy;
	}

	/**
	 * Valid values are:
	 * <br> BIConstants.VIEWBY_NAME
	 * <br> BIConstants.VIEWBY_CATEGORY
	 * @param viewBy
	 */
	public void setViewBy(int viewBy) {
		if(viewBy < BIConstants.VIEWBY_NAME || viewBy > BIConstants.VIEWBY_TYPE){
			return; //invalid
		}
		this.viewBy = viewBy;
	}
	
	public boolean isVersion1300(){
		if(_version.equals("v"+BIConstants.BIGINSIGHTS_VERSION_V13) || //$NON-NLS-1$
		   _version.equals("v1.3.0")){ //$NON-NLS-1$
			return true;
		}
		return false;
	}
	
	public boolean isVersion1301(){
		return (_version.equals("v"+BIConstants.BIGINSIGHTS_VERSION_V1301) ); //$NON-NLS-1$
	}

	public boolean isVersion2101(){
		return (_version.equals("v"+BIConstants.BIGINSIGHTS_VERSION_V2101) ); //$NON-NLS-1$
	}

	public boolean isVersion2110(){
		return (_version.equals("v"+BIConstants.BIGINSIGHTS_VERSION_V2110) ); //$NON-NLS-1$
	}

        public boolean isVersion2120(){
                return (_version.equals("v"+BIConstants.BIGINSIGHTS_VERSION_V2120) ); //$NON-NLS-1$
        }

	public boolean isVersion1301orAbove(){
		if(isVersion1301())	return true;
		if(isVersion1300())	return false;
		
		Version v = new Version(getVersion().startsWith("v") ? getVersion().substring(1) : getVersion()); //$NON-NLS-1$
		Version v1301 = new Version(BIConstants.BIGINSIGHTS_VERSION_V1301);
		
		return v.compareTo(v1301)>=0;
	}

	public boolean isVersion2orAbove(){	
		Version v = new Version(getVersion().startsWith("v") ? getVersion().substring(1) : getVersion()); //$NON-NLS-1$
		Version v2 = new Version(BIConstants.BIGINSIGHTS_VERSION_V2);
		
		return v.compareTo(v2)>=0;
	}
	
	public boolean isVersion2100orAbove(){		
		Version v = new Version(getVersion().startsWith("v") ? getVersion().substring(1) : getVersion()); //$NON-NLS-1$
		Version v2100 = new Version(BIConstants.BIGINSIGHTS_VERSION_V21);
		
		return v.compareTo(v2100)>=0;
	}

	public boolean isVersion211orAbove(){		
		Version v = new Version(getVersion().startsWith("v") ? getVersion().substring(1) : getVersion()); //$NON-NLS-1$
		Version v2110 = new Version(BIConstants.BIGINSIGHTS_VERSION_V2110);
		
		return v.compareTo(v2110)>=0;
	}

	public boolean isVersion212orAbove(){		
		Version v = new Version(getVersion().startsWith("v") ? getVersion().substring(1) : getVersion()); //$NON-NLS-1$
		Version v2120 = new Version(BIConstants.BIGINSIGHTS_VERSION_V2120);
		
		return v.compareTo(v2120)>=0;
	}

        public boolean isVersion3000orAbove(){   
                Version v = new Version(getVersion().startsWith("v") ? getVersion().substring(1) : getVersion()); //$NON-NLS-1$
                Version v3000 = new Version(BIConstants.BIGINSIGHTS_VERSION_V3000);
    
                return v.compareTo(v3000)>=0;
        }

	@Override
	public String[] getLinuxTaskControllerUsers() {
		return this._linuxTaskControllerUsers;
	}
	
	public boolean isLinuxTaskControllerUser(String userId) {
		boolean result = false;
		if (userId!=null && this._linuxTaskControllerUsers!=null) {
			for (String user:this._linuxTaskControllerUsers) {
				if (userId.equals(user)) {
					result = true;
					break;
				}
			}			
		}
		return result;
	}

	public void retrieveHivePort(){
		HttpClient httpClient=null;
		try {
			httpClient = this.getHttpClient();
		} catch (BIConnectionException e) {
			handleBIConnectionExceptionFromThread(e);
			return;
		}
		if(httpClient == null){
			return;
		}
		
		String uri = generateAbsoluteURL(BIConstants.URL_HIVEINFO);
		GetMethod getMethod = new GetMethod(uri);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(1, false));
		Authenticator.setSessionCookie(getMethod, this);
		try {
			int statusCode = httpClient.executeMethod(getMethod);	
			if (statusCode==HttpStatus.SC_OK)
			{				
				String jsonText = getMethod.getResponseBodyAsString();				
				JSONObject obj = (JSONObject) JSON.parse(jsonText);
				String jsonValue = (String)obj.get("nodeUrl");				
				//json returned from this api call; url value is "nodeUrl":"svltest143.svl.ibm.com:10000"				
				String[] values = jsonValue.split(":");
				if (values.length==2) {					
					// host is always the console node, can't be changed during install
					Integer port = Integer.valueOf(values[1]);					
					if(port != null){
						hiveport = port.intValue();
					}
				}
			}
		} catch (IOException ex) {
			//error
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, ex.getMessage()));
	    } finally {
			getMethod.releaseConnection();
		}
	}

	public void retrieveBigSQLNodeAndPort(){
		HttpClient httpClient=null;
		try {
			httpClient = this.getHttpClient();
		} catch (BIConnectionException e) {
			handleBIConnectionExceptionFromThread(e);
			return;
		}
		if(httpClient == null){
			return;
		}
		
		String uri = generateAbsoluteURL(BIConstants.URL_BIGSQLINFO);
		GetMethod getMethod = new GetMethod(uri);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(1, false));
		Authenticator.setSessionCookie(getMethod, this);
		try {
			int statusCode = httpClient.executeMethod(getMethod);	
			if (statusCode==HttpStatus.SC_OK)
			{				
				String jsonText = getMethod.getResponseBodyAsString();
				JSONObject obj = (JSONObject) JSON.parse(jsonText);
				String jsonValue = (String)obj.get("url");				
				//json returned from this api call; url value is bdvm335.svl.ibm.com:7052				
				String[] values = jsonValue.split(":");
				if (values.length==2) {
					bigSQLNode = values[0];
					Integer port = Integer.valueOf(values[1]);					
					if(port != null){
						bigSQLPort = port.intValue();
					}
				}
				if (this.isVersion3000orAbove()) {
					// for 3.0 and later also retrieve the BigSQL2 host and server
					String jsonValue2 = (String)obj.get("jdbcUrl2");
					//json returned from this api call; url value is jdbc:db2://hdtest085.svl.ibm.com:51000/bigsql
					if (jsonValue2!=null && !jsonValue2.isEmpty()) {
						jsonValue2 = jsonValue2.substring(jsonValue2.indexOf("//")+2, jsonValue2.lastIndexOf("/"));
						String[] values2 = jsonValue2.split(":");
						if (values2.length==2) {
							bigSQL2Node = values2[0];
							Integer port = Integer.valueOf(values2[1]);
							if(port != null){
								bigSQL2Port = port.intValue();
							}
						}
					}
				}
			}
		} catch (IOException ex) {
			//error
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, ex.getMessage()));
	    } finally {
			getMethod.releaseConnection();
		}
	}

	public boolean isGPFSMounted() {
		return _isGPFSMounted;
	}
	/**
	 * Method to read the /proc/mounts table and check whether the gpfsMountPoint is part of the file.
	 * This value will be compared with the gpfs.mount.dir value of the BI server to determine whether 
	 * running JAQL can be supported on this client (GPFS needs to be mounted on the client with eclipse).
	 * @return true if if GPFS is mounted; false for any windows platform or if GPFS is not mounted
	 */
	private boolean determineGPFSMounted(String gpfsMountPoint) {
		boolean result = false;
		if (gpfsMountPoint!=null && System.getProperty("os.name").toLowerCase().indexOf("win") == -1) { //$NON-NLS-1$ //$NON-NLS-2$
			FileInputStream fstream = null;
			DataInputStream istream = null;
			BufferedReader reader = null;
			try {			
				fstream = new FileInputStream("/proc/mounts"); 	 //$NON-NLS-1$
				istream = new DataInputStream(fstream);
				reader = new BufferedReader(new InputStreamReader(istream));
				String line = null;			
				while (!result && (line=reader.readLine())!=null) {
					// parse the line for the mount point					
					// split the line on the space
					String[]values = line.split(" "); //$NON-NLS-1$
					for (String s:values) {
						// look for exact entry of mount point
						if (gpfsMountPoint.equals(s)) {
							result = true;
							break;
						}
					}
				}
			} catch (FileNotFoundException e) {
				Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));			
			} catch (IOException e) {
				Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));			
			}
			finally {
				try {
					fstream.close();
					istream.close();
					reader.close();
				} catch (IOException e) {				
				}
			}		
		}		
		return result;
	}

	public int getHivePort(){
		return hiveport;
	}
	
	public int getBigSQLPort(){
		return bigSQLPort;
	}
	public int getBigSQL2Port(){
		return bigSQL2Port;
	}

	public String getBigSQL2Node(){
		return bigSQL2Node;
	}

	public String getBigSQLNode(){
		return bigSQLNode;
	}

	public FILESYSTEM getFileSystem() {
		return _fileSystem;
	}
	
	public String getMountpoint() {
		return _mountPoint;
	}

	public boolean isConnected(){
		if(_client == null){
			return false;
		}
		return true;
	}
	
	public boolean isConnectionStale(){
		if(!isConnected()){
			return true;
		}
		
		//if no auth needed
		if(httpSession.getAuthEnabled()!=null && !httpSession.getAuthEnabled().booleanValue()){
			return false;
		}
		
		//if previously connected but expired
		if(httpSession.hasSessionExpired() && httpSession.getCookies()!=null){
			return true;
		}
		
		return false;
	}
	
	public boolean isClearChildren(){
		return clearChildren;
	}
	
	public void setClearChildren(boolean setting){
		clearChildren = setting;
	}
	
	public String getHiveTemplateId() {
		String result = HIVE_JDBC_DRIVER_071_TEMPLATE_ID;
		
		if (isVersion211orAbove()) {
			result = HIVE_JDBC_DRIVER__0140_TEMPLATE_ID;
		}
		else if (isVersion2orAbove()) {
			if (isOverlay())				
			    result = HIVE_JDBC_DRIVER__090_OVERLAY_TEMPLATE_ID;
			else 
				result = HIVE_JDBC_DRIVER__090_TEMPLATE_ID;			
		}
		else if (isVersion1301orAbove())
		{
			if (isOverlay())
				result = HIVE_JDBC_DRIVER__080_OVERLAY_TEMPLATE_ID;
			else
				result = HIVE_JDBC_DRIVER__080_TEMPLATE_ID;
		}

		return result;
	}
	
	public String getBigSQLTemplateId() {
		String result = BIGSQL_JDBC_DRIVER__21_TEMPLATE_ID;
		
		if (isVersion2101())
			result = BIGSQL_JDBC_DRIVER__2101_TEMPLATE_ID;
		else if (isVersion2110())
			result = BIGSQL_JDBC_DRIVER__211_TEMPLATE_ID;	
		else if (isVersion2120())
			result = BIGSQL_JDBC_DRIVER__212_TEMPLATE_ID;	
		else if (isVersion3000orAbove())
			result = BIGSQL_JDBC_DRIVER__300_TEMPLATE_ID; 

		return result;	
	}
	public String getBigSQL2TemplateId() {
		String result = BIGSQL3_JDBC_DRIVER__300_TEMPLATE_ID;

		return result;
	}

	public boolean doesUserFolderExistOnServer() throws BIConnectionException{
		if(userFolderExists == null || !userFolderExists.booleanValue()){ //try again if folder was created
			checkUserFolderExists();
		}
		return userFolderExists.booleanValue();
	}

	private void checkUserFolderExists() throws BIConnectionException{
		int statusCode = -1;	
		
		HttpClient httpClient = null;
		try {
			httpClient = this.getHttpClient();
		} catch (BIConnectionException e1) {
			throw e1;
		}
		
		if(httpClient == null){
			return;
		}
		String user = getUserName();
		if(user == null){
			user = System.getProperty("user.name"); //os user
		}
		
		GetMethod method = new GetMethod(generateAbsoluteURL(BIConstants.URL_FOLDER_INFO)+user);
		Authenticator.setSessionCookie(method, this);
		try {
			statusCode = httpClient.executeMethod(method);	
			if(statusCode==HttpStatus.SC_OK ){	
				//exists
				userFolderExists = new Boolean(true);
			}else{
				//error
				userFolderExists = new Boolean(false);
			}
		}catch (IOException ex) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, ex.getMessage()));
		}
		
	}

	@Override
	public int getEGO_KD_PORT() {	
		return _egoKDPort;
	}

	@Override
	public String getEGO_SEC_PLUGIN() {
		return _egoSecPlugin;
	}

	@Override
	public String getEGO_MASTER_LIST() {		
		return _egoMasterList;
	}

  @Override
  public boolean isYarn() {
    return _isYarn;
  }
}
