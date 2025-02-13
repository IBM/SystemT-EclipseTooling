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

public class ProjectPersistence
{
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
	
	Map<Integer, String> projectMaster;
	Map<String, Integer> projectMasterReverseLookup;
	File xmlFile;
	
	public ProjectPersistence(File xmlFile) {
		super();
		this.xmlFile = xmlFile;
	}

	public void writeProjectMaster() throws Exception{
		
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		
		// Creating a root element
		Element rootElement = doc.createElement("projectConfiguration");
		// Add it to the Document
		doc.appendChild(rootElement );
		
		if(projectMaster != null){
			for (Integer key : projectMaster.keySet()) {
				// Creating student elements 
				Element ChildElement = doc.createElement("projectMaster");
				//adding attribute to student element
				ChildElement .setAttribute("projectId", key.toString());
				ChildElement .setAttribute("project", projectMaster.get(key));
				rootElement.appendChild(ChildElement);
			}
		}else if(projectMasterReverseLookup != null){
			for (String key : projectMasterReverseLookup.keySet()) {
				// Creating student elements 
				Element ChildElement = doc.createElement("projectMaster");
				//adding attribute to student element
				ChildElement .setAttribute("projectId", projectMasterReverseLookup.get(key).toString());
				ChildElement .setAttribute("project", key);
				rootElement.appendChild(ChildElement);
			}
		}

		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(doc);
		StreamResult streamResult = new StreamResult(xmlFile);
 
		transformer.transform(source, streamResult);
	}
	
  // Comment out this function because (1) it's not being used and (2) appscan reports Path Traversal issue at the line
  //     Document doc = docBuilder.parse(xmlFile);
  // ------------------------------------------------------------------------------------------------------------------
//	public void readProjectMaster()throws Exception {
//		projectMaster = new HashMap<Integer, String> ();
//		projectMasterReverseLookup = new HashMap<String, Integer> ();
//
//		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
//		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
//		Document doc = docBuilder.parse(xmlFile);
//		doc.getDocumentElement ().normalize ();
//		NodeList listProjectMaster = doc.getElementsByTagName("projectMaster");
//		for (int i = 0; i < listProjectMaster.getLength(); i++) {
//			Element projMaster = (Element)listProjectMaster.item(i);
//			Integer projectId = Integer.getInteger(projMaster.getAttribute("projectId"));
//			String project = projMaster.getAttribute("project");
//			projectMaster.put(projectId, project);
//			projectMasterReverseLookup.put(project, projectId);
//		}
//	}
	
	
	public Map<Integer, String> getProjectMaster() {
		return projectMaster;
	}

	public Map<String, Integer> getProjectMasterReverseLookup() {
		return projectMasterReverseLookup;
	}

	public void setProjectMaster(Map<Integer, String> projectMaster) {
		this.projectMaster = projectMaster;
	}

	public void setProjectMasterReverseLookup(
			Map<String, Integer> projectMasterReverseLookup) {
		this.projectMasterReverseLookup = projectMasterReverseLookup;
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
