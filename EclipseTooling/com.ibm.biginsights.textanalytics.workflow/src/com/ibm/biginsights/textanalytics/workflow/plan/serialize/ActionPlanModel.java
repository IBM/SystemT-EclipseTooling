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
package com.ibm.biginsights.textanalytics.workflow.plan.serialize;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;

import com.ibm.biginsights.textanalytics.util.common.StringUtils;

@XmlRootElement(namespace = "com.ibm.biginsights.textanalytics.workflow.plan.model")
public class ActionPlanModel {


	
  public static final String MODULAR_VERSION = "1.5";

	String name;
	String version;
	ArrayList<LabelModel> roots;
	private CollectionModel collection;

	public ActionPlanModel(){
		name = "";
		roots = new ArrayList<LabelModel>();
		setCollection(new CollectionModel());
	}

  public ActionPlanModel(String name, ArrayList<LabelModel> roots, CollectionModel collection) {
		super();
		this.name = name;
		this.roots = roots;
		this.setCollection(collection);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
  
  public String getVersion ()
  {
    return version;
  }

  public void setVersion (String version)
  {
    this.version = version;
  }

  public boolean isModularVersion()
  {
    return (version != null && version.equals (MODULAR_VERSION));
  }

  public ArrayList<LabelModel> getRoots() {
		return roots;
	}

	public void setRoots(ArrayList<LabelModel> roots) {
		this.roots = roots;
	}

	public void setCollection(CollectionModel collection) {
		this.collection = collection;
	}

	public CollectionModel getCollection() {
		return collection;
	}

  public void convertToModularVersion ()
  {
    version = MODULAR_VERSION;
    
    if (getRoots () != null && !getRoots ().isEmpty ()) {
      for (LabelModel lm : getRoots ()) {

        if (lm == null)
          continue;

        setFilePathToAqlNodes(lm.getBasicfilepath (), lm.getBasics ());
        setFilePathToAqlNodes(lm.getConceptfilepath (), lm.getConcepts ());
        setFilePathToAqlNodes(lm.getRefinementfilepath (), lm.getRefinements ());
        setFilePathToAqlNodes(lm.getFinalsfilepath (), lm.getFinals ());

      }
    }
  }

  private void setFilePathToAqlNodes (String filePath, ArrayList<AQLNodeModel> aqlNodes)
  {
    if ( !StringUtils.isEmpty (filePath) && !StringUtils.isEmpty (aqlNodes)) {
      for (AQLNodeModel aqlmodel : aqlNodes) {
        aqlmodel.setAqlfilepath (filePath);
      }
    }
  }
}
