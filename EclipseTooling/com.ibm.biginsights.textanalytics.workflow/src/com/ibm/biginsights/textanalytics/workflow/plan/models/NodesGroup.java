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
package com.ibm.biginsights.textanalytics.workflow.plan.models;

import java.util.ArrayList;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.workflow.editors.SelectionInfo;
import com.ibm.biginsights.textanalytics.workflow.editors.TaggingEditor;
import com.ibm.biginsights.textanalytics.workflow.util.Enumerations.AqlGroupType;
import com.ibm.biginsights.textanalytics.workflow.util.Enumerations.GroupType;
import com.ibm.biginsights.textanalytics.workflow.util.Icons;

/**
 * defines a group of nodes
 * 
 * 
 * 
 */
public class NodesGroup extends TreeParent {

	@SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+//$NON-NLS-1$
		"US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$
	
	protected GroupType    groupType;
  protected AqlGroupType aqlGroupType;

	public NodesGroup(String name, GroupType groupType, TreeParent parent) {
		super(name);

		this.groupType = groupType;

		if (parent != null) {
		  parent.addChild (this);
		}

		initIconFromType();
	}

	public void initIconFromType() {
	  if (groupType != null) {
		switch (groupType) {
		case TAG_FOLDER:
			icon = Icons.LABELS_FOLDER_ICON;
			break;
		case EXAMPLES:
			icon = Icons.EXAMPLES_FOLDER_ICON;
			break;
		case AQL_FOLDER:
			icon = Icons.AQL_COMPONENTS_FOLDER_ICON;
			break;
		case BASIC_FEATURES:
			icon = Icons.BASIC_FEATURES_ICON;
			break;
		case CONCEPTS:
			icon = Icons.CONCEPTS_ICON;
			break;
		case REFINEMENT:
			icon = Icons.REFINEMENT_ICON;
			break;
    case FINALS:
      icon = Icons.FINALS_ICON;
      break;
		default:
			if (this instanceof LabelNode) {
				if (((LabelNode) this).isDone()) {    // TODO move this feature to label provider
					icon = Icons.DONE_ICON;
				} else {
					icon = Icons.LABEL_ICON;
				}
				break;
			}
		}
	  }
	}

	public void doclick() {
		switch (groupType) {
		case EXAMPLES:
			ArrayList<SelectionInfo> list = new ArrayList<SelectionInfo>();

			IEditorPart ieditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

			if (ieditor instanceof TaggingEditor) {
				TaggingEditor editor = (TaggingEditor) ieditor;

				for (TreeObject ichild : children) {
					if (ichild instanceof ExampleNode) {
						ExampleNode child = (ExampleNode) ichild;
						if (editor.isFileOpened(child.getFilePath(), child.getFileLabel()) != null) {
							list.add(child.getPair());
						}
					}
				}
				
				editor.highlight(list);
			}

			break;
		}
	}

	public GroupType getGroupType() {
		return groupType;
	}

  public AqlGroupType getAqlType ()
  {
    return aqlGroupType;
  }

  public void setAqlType (AqlGroupType aqlGroupType)
  {
    this.aqlGroupType = aqlGroupType;
  }
}
