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
package com.ibm.biginsights.textanalytics.goldstandard.menu;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

import com.ibm.biginsights.textanalytics.goldstandard.GoldStandardPlugin;
import com.ibm.biginsights.textanalytics.goldstandard.Messages;
import com.ibm.biginsights.textanalytics.goldstandard.model.AnnotationType;
import com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil;
import com.ibm.biginsights.textanalytics.resultviewer.util.EditorInput;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * This class contributes dynamic menu items to AnnotateAs popup menu. 
 * The input is picked from gold standard configuration.
 * 
 *  Krishnamurthy
 *
 */
public class DynamicAnnotateAsContributor extends CompoundContributionItem {


	
	@Override
	protected IContributionItem[] getContributionItems() {
		
		EditorInput editorInput = GoldStandardUtil.getActiveEditorInput();
		IFile gsFile = (IFile)editorInput.getUserData();
		AnnotationType[] annTypes = GoldStandardUtil.getAnnotationTypes(GoldStandardUtil.getGoldStandardFolder(gsFile));
		if(annTypes == null || annTypes.length==0){
			LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowError(Messages.AnnotateAsHandler_NO_ANNOTATION_TYPE_DEFINED);
			return new IContributionItem[0];
		}
		ArrayList<IContributionItem> menuItems = new ArrayList<IContributionItem>();
		
		int i = 0;
		for (AnnotationType annType : annTypes) {
			if(annType.isEnabled()){
				final CommandContributionItemParameter contributionParameter = 
				      new CommandContributionItemParameter(
				        PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
				        "com.ibm.biginsights.textanalytics.goldstandard.annType"+i, "com.ibm.biginsights.textanalytics.goldstandard.command.annotateAs",
				        SWT.NONE);
				contributionParameter.label = annType.getViewName()+"."+annType.getFieldName();
				
				//add shortcut key in menu item only when annType.shortcutKey is not empty
				if(!StringUtils.isEmpty(annType.getShortcutKey())){
					contributionParameter.label += "\tCtrl+"+annType.getShortcutKey();
				}
				CommandContributionItem cmdContribItem = new CommandContributionItem(contributionParameter);
				menuItems.add(cmdContribItem);
				i++;
			}
		}
		return menuItems.toArray(new IContributionItem[menuItems.size()]);
	}


	

}
