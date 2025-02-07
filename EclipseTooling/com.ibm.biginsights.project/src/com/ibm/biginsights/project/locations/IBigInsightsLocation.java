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
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.biginsights.project.locations.BigInsightsLocation.FILESYSTEM;
import com.ibm.biginsights.project.locations.BigInsightsLocation.TestConnectionResult;
import com.ibm.biginsights.project.util.BIConnectionException;

public interface IBigInsightsLocation extends IBigInsightsLocationNode {

	public String getLocationName();
	
	public String getLocationDisplayString();
	
	public String getUserName();
	
	public String getPassword();
	
	public String getHostName();	
	
	public boolean getSavePassword();
	
	public boolean getUseSSL();
	
	public HttpClient getHttpClient() throws BIConnectionException;
	
	public int getPort();
	
	public String getURL();
	
	public void initWithLocation(IBigInsightsLocation copyLocation);
		
	public TestConnectionResult testConnection(boolean retryConnection);	
	
	public void save(File file);
	
	public void dispose();
	
	public void setLocationName(String value);
	
	public void setUserName(String value);
	
	public void setPassword(String value);
	
	public void setSavePassword(boolean value);
	
	public void setUseSSL(boolean value);
	
	public void setURL(String value);

	public String getProtocol();
	
	public String getVersion();
	
	public String getVersionWithVendor();
	
	public String getVendor();
	
	public boolean isOverlay();
	
	public boolean isSymphony();
	
	public String getContextRoot();
	
	public boolean retrieveConfigurationFiles();
	
	public void retrieveVersion();
	
	public void retrieveVendor();
	
	public String retrieveUgi();
	
	public String generateAbsoluteURL(String relativeURL);
	
	public boolean isAdmin();
	
	public boolean isUser();

	public boolean isAdminOrUser();
	
	public List<String> getRoles();
	
	public void checkRoles();
	
	public BigInsightsLocationSession getHttpSession();
	
	public boolean isAuthenticationNeeded() throws BIConnectionException;
	
	public void handleBIConnectionExceptionFromThread(BIConnectionException e);
	
	public  void resetHttpClient();
	
	public void retrieveHivePort();
	
	public int getHivePort();
	
	public void retrieveBigSQLNodeAndPort();
	
	public int getBigSQLPort();
	
	public String getBigSQLNode();
	
	public int getBigSQL2Port();
	
	public String getBigSQL2Node();
	
	public int getViewBy();
	
	public FILESYSTEM getFileSystem();
	
	public String getMountpoint();

	public boolean isGPFSMounted();
	
	public String[] getLinuxTaskControllerUsers();
	
	public boolean isLinuxTaskControllerUser(String userId);
	
	public HttpClient cancelableGetHttpClient(IProgressMonitor progMonitor) throws BIConnectionException;
	
	public boolean isConnected();
	
	public boolean isConnectionStale();
	
	public void setClearChildren(boolean setting);
	public boolean isClearChildren();
	
	public boolean isVersion1301orAbove();
	
	public boolean isVersion2orAbove();
	
	public boolean isVersion2100orAbove();
	
	public boolean isVersion3000orAbove();
        
        public boolean isVersion211orAbove();
	
	public String getHiveTemplateId();
	
	public String getBigSQLTemplateId();
	
	public String getBigSQL2TemplateId();
	
	public boolean doesUserFolderExistOnServer() throws BIConnectionException;
	
	// symphony-related properties
	public String getEGO_MASTER_LIST();
	
	public int getEGO_KD_PORT();
	
	public String getEGO_SEC_PLUGIN();

	public boolean isYarn();  

}
