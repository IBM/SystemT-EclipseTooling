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
package com.ibm.biginsights.textanalytics.indexer.persist;

import java.io.File;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ModulePersistence
{
	@SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+          //$NON-NLS-1$
		"US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

	Map<Integer, String> moduleMaster;
	Map<String, Integer> moduleMasterReverseLookup;
	
	File xmlFile;
	
	public ModulePersistence(File xmlFile) {
		super();
		this.xmlFile = xmlFile;
	}

	public void writeModuleMaster() throws Exception{
		
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		
		// Creating a root element
		Element rootElement = doc.createElement("moduleConfiguration");
		// Add it to the Document
		doc.appendChild(rootElement );
		
		writeModuleMaster(doc, rootElement);

		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(doc);
		StreamResult streamResult = new StreamResult(xmlFile);
 
		transformer.transform(source, streamResult);
	}

	
	private void writeModuleMaster(Document doc, Element rootElement) {
		if(moduleMaster != null){
			for (Integer key : moduleMaster.keySet()) {
				// Creating student elements 
				Element ChildElement = doc.createElement("moduleMaster");
				//adding attribute to student element
				ChildElement .setAttribute("fileID", key.toString());
				ChildElement .setAttribute("module", moduleMaster.get(key));
				rootElement.appendChild(ChildElement);
			}
		}
	}
	
	// Comment out this function because (1) it's not being used and (2) appscan reports Path Traversal issue at the line
	//     Document doc = docBuilder.parse(xmlFile);
	// ------------------------------------------------------------------------------------------------------------------
//	public void writeProjectMaster(File xmlFile)throws Exception {
//		moduleMaster = new HashMap<Integer, String> ();
//		moduleMasterReverseLookup = new HashMap<String, Integer> ();
//
//		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
//		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
//		Document doc = docBuilder.parse(xmlFile);
//		doc.getDocumentElement ().normalize ();
//		NodeList listProjectMaster = doc.getElementsByTagName("moduleMaster");
//		for (int i = 0; i < listProjectMaster.getLength(); i++) {
//			Element projMaster = (Element)listProjectMaster.item(i);
//			Integer projectId = Integer.getInteger(projMaster.getAttribute("fileID"));
//			String project = projMaster.getAttribute("module");
//			moduleMaster.put(projectId, project);
//			moduleMasterReverseLookup.put(project, projectId);
//		}
//	}

	public Map<Integer, String> getModuleMaster() {
		return moduleMaster;
	}

	public void setModuleMaster(Map<Integer, String> moduleMaster) {
		this.moduleMaster = moduleMaster;
	}

	public Map<String, Integer> getModuleMasterReverseLookup() {
		return moduleMasterReverseLookup;
	}


	/*public static void main(String a[]) throws Exception{
		ProjectMasterPersistence p = new ProjectMasterPersistence();
		Map<Integer, String> projectMaster = new HashMap<Integer, String> ();
		projectMaster.put(1, "proj1");
		projectMaster.put(2, "proj2");
		projectMaster.put(3, "proj3");
		Map<String, Integer> projectMasterReverseLookup = new HashMap<String, Integer> ();
		projectMasterReverseLookup.put("proj1", 1);
		projectMasterReverseLookup.put("proj2", 2);
		projectMasterReverseLookup.put("proj3", 3);
		File xml = new File("C:\\file.xml");
		p.writeProjectMaster(xml, projectMaster, projectMasterReverseLookup);
	}
*/
}
