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

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.progress.IProgressConstants;

import com.ibm.biginsights.project.util.BIConstants;

public class PerspectiveFactory implements IPerspectiveFactory {
  
  @Override
  public void createInitialLayout(IPageLayout layout) {
    
    String ID_PROJECT_EXPLORER = "org.eclipse.ui.navigator.ProjectExplorer"; //$NON-NLS-1$
    
    String editorArea = layout.getEditorArea();

    IFolderLayout folder= layout.createFolder("leftFolder", IPageLayout.LEFT, (float)0.25, editorArea); //$NON-NLS-1$
    folder.addView(ID_PROJECT_EXPLORER);

    // No more BigInsights server view for now but we may need one in the future
//    IFolderLayout locationsFolder = layout.createFolder("locationsFolder", IPageLayout.BOTTOM, (float)0.6, "leftFolder"); //$NON-NLS-1$ //$NON-NLS-2$
//    locationsFolder.addView(BIConstants.LOCATIONS_VIEW_ID);
    
    IFolderLayout outputfolder= layout.createFolder("bottom", IPageLayout.BOTTOM, (float)0.7, editorArea); //$NON-NLS-1$
    outputfolder.addView(IPageLayout.ID_PROBLEM_VIEW);    
    outputfolder.addPlaceholder(NewSearchUI.SEARCH_VIEW_ID);
    outputfolder.addView(IConsoleConstants.ID_CONSOLE_VIEW);

    layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
    layout.addActionSet(JavaUI.ID_ACTION_SET);
    layout.addActionSet(JavaUI.ID_ELEMENT_CREATION_ACTION_SET);
    layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);

    // views - java
    layout.addShowViewShortcut(JavaUI.ID_PACKAGES);
    layout.addShowViewShortcut(JavaUI.ID_SOURCE_VIEW);

    // views - search
    layout.addShowViewShortcut(NewSearchUI.SEARCH_VIEW_ID);

    // views - debugging
    layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);

    // views - standard workbench
    layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
    layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);    
    layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
    layout.addShowViewShortcut(IProgressConstants.PROGRESS_VIEW_ID);
    layout.addShowViewShortcut(ID_PROJECT_EXPLORER);
    layout.addShowViewShortcut("org.eclipse.pde.runtime.LogView"); //$NON-NLS-1$

    // 'Window' > 'Open Perspective' contributions
    layout.addPerspectiveShortcut(BIConstants.BI_PERSPECTIVE_ID);   
  }

}
